package com.example.memoria.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memoria.data.database.dao.CardDao;
import com.example.memoria.data.model.Card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public void updateCard(Card card, Runnable onComplete) {
        executor.execute(() -> {
            card.setSyncStatus(0);
            cardDao.updateCard(card);
            if (onComplete != null) onComplete.run();
        });
    }

    public void insertCard(Card card, Runnable onComplete) {
        executor.execute(() -> {
            card.setSyncStatus(0);
            cardDao.insertCard(card);
            if (onComplete != null) onComplete.run();
        });
    }

    public void deleteCard(Card card, Runnable onComplete) {
        executor.execute(() -> {
            card.setSyncStatus(2); // Đánh dấu chờ xóa
            cardDao.updateCard(card); // Update để trigger lấy list unsynced
            if (onComplete != null) onComplete.run();
        });
    }

    // Lấy số từ đã học hôm nay
    public void getWordsLearnedToday(DataCallback<Integer> callback) {
        executor.execute(() -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            calendar.set(java.util.Calendar.SECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);

            long startOfToday = calendar.getTimeInMillis();

            int count = cardDao.countCardsReviewedToday(startOfToday);

            callback.onDataLoaded(count);
        });
    }
}
