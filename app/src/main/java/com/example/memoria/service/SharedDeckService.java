package com.example.memoria.service;

import com.example.memoria.data.database.dao.CardDao;
import com.example.memoria.data.database.dao.DeckDao;
import com.example.memoria.data.model.dto.DeckWithCard;
import com.example.memoria.data.model.entity.Card;
import com.example.memoria.data.model.entity.Deck;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
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
public class SharedDeckService {
    private final FirebaseFirestore firestore;
    private final DeckDao deckDao;
    private final CardDao cardDao;
    private final ExecutorService executor;

    public interface Callback<T> {
        void onResult(boolean success, T data, String message);
    }

    @Inject
    public SharedDeckService(FirebaseFirestore firestore, DeckDao deckDao, CardDao cardDao) {
        this.firestore = firestore;
        this.deckDao = deckDao;
        this.cardDao = cardDao;
        this.executor = Executors.newSingleThreadExecutor();
    }

    private String generateShareCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void exportDeck(String userId, String authorName, String localDeckId, Callback<String> callback) {
        executor.execute(() -> {
            try {
                UUID deckUuid = UUID.fromString(localDeckId);
                Deck localDeck = deckDao.getDeckById(deckUuid);
                List<Card> localCards = cardDao.getCardsByDeckIdSync(deckUuid);

                if (localDeck == null || localCards.isEmpty()) {
                    if (callback != null) callback.onResult(false, null, "Bộ bài rỗng hoặc không tồn tại!");
                    return;
                }

                String shareCode = generateShareCode();
                WriteBatch batch = firestore.batch();

                // Luu thong tin
                Map<String, Object> sharedDeckData = new HashMap<>();
                sharedDeckData.put("shareCode", shareCode);
                sharedDeckData.put("originalDeckId", localDeckId);
                sharedDeckData.put("authorId", userId);
                sharedDeckData.put("authorName", authorName);
                sharedDeckData.put("deckName", localDeck.getDeckName());
                sharedDeckData.put("coverColor", localDeck.getCoverColor());
                sharedDeckData.put("totalCards", localCards.size());
                sharedDeckData.put("sharedAt", new Date());

                batch.set(firestore.collection("shared_decks").document(shareCode), sharedDeckData);

                for (Card card : localCards) {
                    Card sharedCard = new Card();
                    sharedCard.setCardId(card.getCardId());
                    sharedCard.setDeckId(card.getDeckId());
                    sharedCard.setFrontText(card.getFrontText());
                    sharedCard.setFrontImage(card.getFrontImage());
                    sharedCard.setCardType(card.getCardType());

                    if (card.getBackTypes() != null) {
                        sharedCard.setBackTypes(new ArrayList<>(card.getBackTypes()));
                    }
                    if (card.getBackMeanings() != null) {
                        sharedCard.setBackMeanings(new ArrayList<>(card.getBackMeanings()));
                    }

                    // reset flashcard learn mode card
                    sharedCard.setCreatedAt(new Date());
                    sharedCard.setUpdatedAt(new Date());

                    sharedCard.setEaseFactor(2.5);
                    sharedCard.setIntervalDays(0);
                    sharedCard.setLastResult(0);
                    sharedCard.setLastReviewAt(null);
                    sharedCard.setNextReviewDate(null);
                    sharedCard.setReviewCount(0);

                    sharedCard.setFirestoreId(null);
                    sharedCard.setSyncStatus(0);

                    batch.set(
                            firestore.collection("shared_decks").document(shareCode)
                                    .collection("cards").document(card.getCardId().toString()),
                            sharedCard
                    );
                }

                batch.commit().addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onResult(true, shareCode, "Tạo mã chia sẻ thành công!");
                        } else {
                            callback.onResult(false, null, "Lỗi kết nối Firebase: " + task.getException().getMessage());
                        }
                    }
                });

            } catch (Exception e) {
                if (callback != null) callback.onResult(false, null, "Lỗi xử lý hệ thống: " + e.getMessage());
            }
        });
    }

    public void downloadSharedDeck(String shareCode, Callback<DeckWithCard> callback) {
        String formattedCode = shareCode.trim().toUpperCase();

        Task<DocumentSnapshot> deckTask = firestore.collection("shared_decks").document(formattedCode).get();
        Task<QuerySnapshot> cardsTask = firestore.collection("shared_decks").document(formattedCode).collection("cards").get();

        Tasks.whenAllSuccess(deckTask, cardsTask).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                try {
                    DocumentSnapshot deckDoc = (DocumentSnapshot) task.getResult().get(0);
                    QuerySnapshot cardsSnapshot = (QuerySnapshot) task.getResult().get(1);

                    if (!deckDoc.exists()) {
                        if (callback != null) callback.onResult(false, null, "Bộ bài không tồn tại hoặc đã bị xóa!");
                        return;
                    }

                    Deck originalDeck = new Deck();
                    originalDeck.setDeckName(deckDoc.getString("deckName"));
                    originalDeck.setCoverColor(deckDoc.getString("coverColor"));

                    List<Card> downloadedCards = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : cardsSnapshot) {
                        downloadedCards.add(doc.toObject(Card.class));
                    }

                    DeckWithCard resultDto = new DeckWithCard(originalDeck, downloadedCards);
                    if (callback != null) callback.onResult(true, resultDto, "Tải dữ liệu hoàn tất!");

                } catch (Exception e) {
                    if (callback != null) callback.onResult(false, null, "Lỗi phân tích dữ liệu: " + e.getMessage());
                }
            } else {
                if (callback != null) callback.onResult(false, null, "Lỗi đường truyền Firebase!");
            }
        });
    }
}