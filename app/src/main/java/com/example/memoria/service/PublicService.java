package com.example.memoria.service;

import com.example.memoria.data.database.dao.CardDao;
import com.example.memoria.data.database.dao.DeckDao;
import com.example.memoria.data.model.dto.DeckWithCard;
import com.example.memoria.data.model.entity.Card;
import com.example.memoria.data.model.entity.Deck;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PublicService {
    private final DeckDao deckDao;
    private final CardDao cardDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor;

    public interface Callback<T> {
        void onResult(boolean success, T data, String message);
    }

    @Inject
    public PublicService(FirebaseFirestore firestore, DeckDao deckDao, CardDao cardDao) {
        this.firestore = firestore;
        this.deckDao = deckDao;
        this.cardDao = cardDao;
        this.executor = Executors.newSingleThreadExecutor();
    }

    // Hàm phụ trợ tạo mảng từ khóa (Ví dụ: "JLPT N3" -> ["jlpt", "n3"])
    // ham nay de phuc vu viec lach luat vi firestore free KHÔNG HỖ TRỢ truy vấn chứa chuỗi (substring/contains)
    private List<String> generateSearchKeywords(String deckName) {
        List<String> keywords = new ArrayList<>();
        if (deckName != null && !deckName.isEmpty()) {
            String[] words = deckName.toLowerCase().trim().split("\\s+");
            Collections.addAll(keywords, words);
        }
        return keywords;
    }

    // Ban nay la ban dung N-gram, tuy nhien neu ten deck qua dai thi document se phai luu rat nhieu
//    private List<String> generateSearchKeywords(String deckName) {
//        List<String> keywords = new ArrayList<>();
//        if (deckName != null && !deckName.isEmpty()) {
//            String name = deckName.toLowerCase().trim();
//            // Cắt theo từ (để bao luôn trường hợp search theo từ)
//            String[] words = name.split("\\s+");
//            for (String word : words) {
//                // Tạo tất cả các chuỗi con của từng từ (N-Grams)
//                for (int i = 0; i < word.length(); i++) {
//                    // Thay vì j = i + 1, ta bắt đầu từ j = i + 2
//                    // Để đảm bảo chuỗi con (substring) sinh ra luôn có ít nhất 2 ký tự, 1 thì quá nhiều giá trị rác
//                    for (int j = i + 2; j <= word.length(); j++) {
//                        String sub = word.substring(i, j);
//                        if (!keywords.contains(sub)) {
//                            keywords.add(sub);
//                        }
//                    }
//                }
//            }
//        }
//        return keywords;
//    }

    /**
     * PUBLISH DECK: Đồng bộ Snapshot (Thêm mới, Cập nhật, và Xóa các thẻ không còn tồn tại).
     * Ta sẽ dùng chính "{userId}_{localDeckId}" làm publicDocId.
     * Nếu user ấn public lại, nó sẽ tự động ghi đè document cũ.
     * Giữ nguyên lượt tải (downloadCount) nếu đã từng public trước đó.
     */
    public void publishDeck(String userId, String authorName, String localDeckId, Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                UUID deckUuid = UUID.fromString(localDeckId);
                Deck localDeck = deckDao.getDeckById(deckUuid);
                List<Card> localCards = cardDao.getCardsByDeckIdSync(deckUuid);

                if (localDeck == null || localCards.isEmpty()) {
                    if (callback != null)
                        callback.onResult(false, null, "Deck rỗng hoặc không tồn tại!");
                    return;
                }

                // Tạo định danh cố định để tự động overwrite snapshot cũ
                String publicDocId = userId + "_" + localDeckId;

                // Chuẩn bị 2 Task song song:
                // 1. Lấy thông tin meta của Deck cũ (để giữ lại count)
                Task<DocumentSnapshot> metaTask = firestore.collection("public_decks").document(publicDocId).get();
                // 2. Lấy danh sách Cards cũ trên Public (để tìm các thẻ mồ côi cần xóa)
                Task<QuerySnapshot> cardsTask = firestore.collection("public_decks").document(publicDocId).collection("cards").get();

                // Cho 2 task tren kia chay xong
                Tasks.whenAllSuccess(metaTask, cardsTask).addOnCompleteListener(tasks -> {
                    try {
                        long currentCount = 0;
                        List<String> existingPublicCardIds = new ArrayList<>();

                        // Trích xuất downloadCount cũ (nếu có)
                        DocumentSnapshot metaSnapshot = (DocumentSnapshot) tasks.getResult().get(0);
                        if (metaSnapshot.exists() && metaSnapshot.contains("downloadCount")) {
                            Long fetchedCount = metaSnapshot.getLong("downloadCount");
                            if (fetchedCount != null) {
                                currentCount = fetchedCount;
                            }
                        }

                        // Trích xuất danh sách Card ID đang có trên Public
                        QuerySnapshot existingCardsSnapshot = (QuerySnapshot) tasks.getResult().get(1);
                        if (existingCardsSnapshot != null) {
                            for (QueryDocumentSnapshot doc : existingCardsSnapshot) {
                                existingPublicCardIds.add(doc.getId());
                            }
                        }

                        // bat dau ghi de du lieu
                        WriteBatch batch = firestore.batch();

                        // Cập nhật Meta Data cho Public Deck
                        Map<String, Object> publicDeckData = new HashMap<>();
                        publicDeckData.put("publicDocId", publicDocId);
                        publicDeckData.put("originalDeckId", localDeckId);
                        publicDeckData.put("authorId", userId);
                        publicDeckData.put("authorName", authorName);
                        publicDeckData.put("deckName", localDeck.getDeckName());
                        publicDeckData.put("coverColor", localDeck.getCoverColor());
                        publicDeckData.put("searchKeywords", generateSearchKeywords(localDeck.getDeckName()));
                        publicDeckData.put("downloadCount", currentCount); // Giữ count cũ
                        publicDeckData.put("publishedAt", new Date());

                        batch.set(firestore.collection("public_decks").document(publicDocId), publicDeckData);

                        // XU LY CARD LIST
                        // Lấy danh sách ID của bộ thẻ hiện tại ở dưới máy để tiện so sánh
                        List<String> localCardIds = new ArrayList<>();
                        for (Card c : localCards) {
                            localCardIds.add(c.getCardId().toString());
                        }

                        // Xóa các thẻ cũ không còn nằm trong máy (Thẻ bị user xóa)
                        for (String publicCardId : existingPublicCardIds) {
                            if (!localCardIds.contains(publicCardId)) {
                                // Nếu trên Public có, mà máy không có -> Đưa vào danh sách xóa
                                batch.delete(firestore.collection("public_decks").document(publicDocId)
                                        .collection("cards").document(publicCardId));
                            }
                        }

                        // Cập nhật hoặc Thêm mới các thẻ từ máy lên Public
                        for (Card card : localCards) {
                            // TẠO BẢN SAO ẢO VÀ RESET DATA TRƯỚC KHI PUSH LÊN FIRESTORE
                            Card publicCard = new Card();
                            publicCard.setCardId(card.getCardId());
                            publicCard.setDeckId(card.getDeckId());
                            publicCard.setFrontText(card.getFrontText());
                            publicCard.setFrontImage(card.getFrontImage());
                            publicCard.setCardType(card.getCardType());

                            if (card.getBackTypes() != null) {
                                publicCard.setBackTypes(new ArrayList<>(card.getBackTypes()));
                            }
                            if (card.getBackMeanings() != null) {
                                publicCard.setBackMeanings(new ArrayList<>(card.getBackMeanings()));
                            }

                            publicCard.setCreatedAt(card.getCreatedAt() != null ? card.getCreatedAt() : new Date());
                            publicCard.setUpdatedAt(new Date());

                            // TIẾN HÀNH RESET TIẾN ĐỘ HỌC
                            publicCard.setEaseFactor(2.5);
                            publicCard.setIntervalDays(0);
                            publicCard.setLastResult(0);
                            publicCard.setLastReviewAt(null);
                            publicCard.setNextReviewDate(null);
                            publicCard.setReviewCount(0);

                            // Không cần quản lý Sync đối với public deck
                            publicCard.setFirestoreId(null);
                            publicCard.setSyncStatus(0);

                            // Dùng publicCard thay vì card cũ
                            batch.set(
                                    firestore.collection("public_decks").document(publicDocId)
                                            .collection("cards").document(card.getCardId().toString()),
                                    publicCard
                            );
                        }

                        // Thực thi toàn bộ Batch
                        batch.commit().addOnCompleteListener(commitTask -> {
                            if (callback != null) {
                                if (commitTask.isSuccessful()) {
                                    callback.onResult(true, true, "Publish thành công!");
                                } else {
                                    callback.onResult(false, null, "Lỗi Firebase: " + commitTask.getException().getMessage());
                                }
                            }
                        });

                    }
                    catch (Exception e) {
                        if (callback != null) callback.onResult(false, null, "Lỗi xử lý dữ liệu: " + e.getMessage());
                    }
                });
            }
            catch (Exception e) {
                if (callback != null) callback.onResult(false, null, "Lỗi xử lý: " + e.getMessage());
            }
        });
    }

    /**
     * Lấy danh sách Public Decks (Có phân trang)
     * @param searchQuery Chuỗi tìm kiếm do user nhập (Nhập null hoặc rỗng nếu chỉ muốn lấy list bình thường)
     */
    public void getPublicDecks(int limit, DocumentSnapshot lastVisible, String searchQuery, Callback<List<DocumentSnapshot>> callback) {
        Query query = firestore.collection("public_decks");

        // neu co dung search thi thuc hien
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            // Chuyển từ khóa nhập vào thành chữ thường để khớp với mảng trên DB
            String keyword = searchQuery.trim().toLowerCase();

            // Tìm những document có mảng searchKeywords chứa từ khóa này
            query = query.whereArrayContains("searchKeywords", keyword);
        }

        // phan trang va gioi han so luong
        query = query.orderBy("publishedAt", Query.Direction.DESCENDING)
                .limit(limit);

        // neo phan trang
        if (lastVisible != null){
            query = query.startAfter(lastVisible);
        }

        // batch thuc thi
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                if (callback != null) callback.onResult(true, docs, "Lấy danh sách thành công!");
            } else {
                if (callback != null) callback.onResult(false, null, "Lỗi kéo dữ liệu: " + task.getException().getMessage());
            }
        });
    }

    /**
     * Lấy Preview Card (Vài từ đầu tiên để user xem thử)
     */
    public void getPreviewDeck(String publicDocId, int limit, Callback<List<Card>> callback) {
        firestore.collection("public_decks").document(publicDocId)
                .collection("cards")
                .limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Card> previewCards = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            previewCards.add(doc.toObject(Card.class));
                        }
                        if (callback != null) callback.onResult(true, previewCards, "Thành công");
                    } else {
                        if (callback != null) callback.onResult(false, null, "Không lấy được preview");
                    }
                });
    }

    /**
     * Download Deck: Tải toàn bộ và trả về DTO để chuyển qua CloneService
     */
    public void downloadDeck(String publicDocId, Callback<DeckWithCard> callback) {
        // Tăng lượt tải lên 1
        firestore.collection("public_decks").document(publicDocId)
                .update("downloadCount", FieldValue.increment(1));

        // Tải Deck Meta
        Task<DocumentSnapshot> deckTask = firestore.collection("public_decks").document(publicDocId).get();

        // Tải toàn bộ Cards
        Task<QuerySnapshot> cardsTask = firestore.collection("public_decks").document(publicDocId).collection("cards").get();

        Tasks.whenAllSuccess(deckTask, cardsTask).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                try {
                    DocumentSnapshot deckDoc = (DocumentSnapshot) task.getResult().get(0);
                    QuerySnapshot cardsSnapshot = (QuerySnapshot) task.getResult().get(1);

                    // Phục dựng lại đối tượng Deck (Bỏ qua các trường như authorName, chỉ lấy những gì Deck Class cần)
                    Deck originalDeck = new Deck();
                    originalDeck.setDeckName(deckDoc.getString("deckName"));
                    originalDeck.setCoverColor(deckDoc.getString("coverColor"));

                    List<Card> downloadedCards = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : cardsSnapshot) {
                        downloadedCards.add(doc.toObject(Card.class));
                    }

                    // Đóng gói vào DTO
                    DeckWithCard resultDto = new DeckWithCard(originalDeck, downloadedCards);
                    if (callback != null) callback.onResult(true, resultDto, "Tải xuống hoàn tất!");

                } catch (Exception e) {
                    if (callback != null) callback.onResult(false, null, "Lỗi parse dữ liệu");
                }
            } else {
                if (callback != null) callback.onResult(false, null, "Lỗi đường truyền");
            }
        });
    }
}
