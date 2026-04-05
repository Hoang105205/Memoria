package com.example.memoria.data.repository;

import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.memoria.data.database.dao.CardDao;
import com.example.memoria.data.model.entity.Card;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CardRepository {
    private final CardDao cardDao;
    private final ExecutorService executor;

    @Inject
    public CardRepository(CardDao cardDao) {
        this.cardDao = cardDao;
        executor = Executors.newSingleThreadExecutor();
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }

    // Lấy danh sách thẻ theo Deck ID
    public LiveData<List<Card>> getCardsByDeckId(UUID deckId) {
        return cardDao.getCardsByDeckId(deckId);
    }

    // Cập nhật thẻ sau khi học xong (Nhớ/Quên)
    public void updateCard(Card card, Runnable onUIComplete, Runnable onCloudComplete) {
        executor.execute(() -> {
            card.setSyncStatus(0);
            cardDao.updateCard(card);
            if (onUIComplete != null) onUIComplete.run();

            if (card.getCardType() == 1 && card.getFrontImage() != null && !card.getFrontImage().startsWith("http")) {
                uploadFlashcardImageToCloudinary(card, onCloudComplete);
            } else {
                if (onCloudComplete != null) {
                    onCloudComplete.run();
                }
            }
        });
    }

    public void insertCard(Card card, Runnable onUIComplete, Runnable onCloudComplete) {
        executor.execute(() -> {
            card.setSyncStatus(0);
            cardDao.insertCard(card);
            if (onUIComplete != null) onUIComplete.run();

            if (card.getCardType() == 1 && card.getFrontImage() != null && !card.getFrontImage().startsWith("http")) {
                uploadFlashcardImageToCloudinary(card, onCloudComplete);
            } else {
                if (onCloudComplete != null) {
                    onCloudComplete.run();
                }
            }
        });
    }

    private void uploadFlashcardImageToCloudinary(Card card, Runnable onCloudComplete) {
        // KỊCH BẢN 1: Bỏ qua nếu không phải thẻ Ảnh, hoặc không có ảnh, hoặc ảnh đã là link Cloud (http)
        if (card.getCardType() != 1 || card.getFrontImage() == null || card.getFrontImage().startsWith("http")) {
            if (onCloudComplete != null) onCloudComplete.run();
            return;
        }

        // KỊCH BẢN 2: Có ảnh Local -> Bắt đầu đẩy lên Cloudinary
        Uri localUri = Uri.parse(card.getFrontImage());
        String cardIdString = card.getCardId().toString();

        MediaManager.get().upload(localUri)
                .option("folder", "flashcards")    // Tạo folder riêng biệt cho Flashcard cho sạch sẽ
                .option("public_id", cardIdString)       // Đặt tên file bằng đúng UUID của thẻ
                .option("overwrite", true)         // Cho phép ghi đè
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // Đang up ngầm, không cần làm phiền UI
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Bỏ qua
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String secureUrl = (String) resultData.get("secure_url");

                        card.setFrontImage(secureUrl);

                        executor.execute(() -> {
                            cardDao.updateCard(card);

                            if (onCloudComplete != null) {
                                onCloudComplete.run();
                            }
                        });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        android.util.Log.e("Cloudinary", "Lỗi up ảnh thẻ: " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Bỏ qua
                    }
                }).dispatch();
    }

    public void deleteCard(Card card, Runnable onComplete) {
        executor.execute(() -> {
            card.setSyncStatus(2); // Đánh dấu chờ xóa
            cardDao.updateCard(card); // Update để trigger lấy list unsynced
            if (onComplete != null) onComplete.run();
        });
    }

    public void markCardsForDeleted(UUID deckId, Runnable onComplete) {
        executor.execute(() -> {
            cardDao.markCardsForDeleted(deckId);
            if (onComplete != null) onComplete.run();
        });
    }

    // Lấy số từ đã học hôm nay

    public LiveData<Integer> getWordsLearnedTodayLiveData(long startOfToday) {
        return cardDao.countCardsReviewedToday(startOfToday);
    }

    public LiveData<List<Long>> getAllReviewDaysLiveData() {
        return cardDao.getDistinctStudyDays();
    }
    public int getDueCardsCountSync(long currentTime) {
        return cardDao.countDueCards(currentTime);
    }

    // Thêm Card nếu chưa tồn tại
    public void insertCardIfNotExists(Card card, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            // Kiểm tra số lượng thẻ trùng lặp trong Deck
            int count = cardDao.checkCardExist(card.getDeckId(), card.getFrontText());

            if (count == 0) {
                card.setSyncStatus(0);
                cardDao.insertCard(card); // Chưa có thì thêm vào
                if (callback != null) callback.onDataLoaded(true); // Trả về true (Thành công)
            } else {
                if (callback != null) callback.onDataLoaded(false); // Trả về false (Đã tồn tại)
            }
        });
    }

    public void getCardsByDeckIdList(UUID deckId, DataCallback<List<Card>> callback) {
        executor.execute(() -> {
            List<Card> cards = cardDao.getCardsByDeckIdSync(deckId);
            callback.onDataLoaded(cards);
        });
    }

    public void searchCards(UUID deckId, String keyword, DataCallback<List<Card>> callback) {
        executor.execute(() -> {
            List<Card> cards = cardDao.searchCardsInDeck(deckId, keyword);
            callback.onDataLoaded(cards);
        });
    }
}
