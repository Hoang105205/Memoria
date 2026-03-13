package com.example.memoria.data.repository;

import com.example.memoria.data.database.dao.DeckDao;
import com.example.memoria.data.model.Deck;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DeckRepository {
    private final DeckDao deckDao;
    private final ExecutorService executor; // run on background

    @Inject
    public DeckRepository(DeckDao deckDao) {
        this.deckDao = deckDao;
        executor = Executors.newSingleThreadExecutor();
    }

    // Lấy danh sách (Chạy trên main thread vì Room cho phép query trả về List trực tiếp.
    // Nếu không dùng LiveData thì phải xử lý thread, trả về trực tiếp nhưng chạy trong background.
    // Để đơn giản cho ViewModel, dùng Callback hoặc trả về LiveData.
    // Chạy background và trả kết quả về qua Callback. Có thể thay đổi sau khi có dữ liệu thật.

    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }

    public void getAllDecks(DataCallback<List<Deck>> callback) {
        executor.execute(() -> {
            List<Deck> data = deckDao.getAllDecks();
            callback.onDataLoaded(data);
        });
    }

    public void insertDeck(Deck deck) {
        executor.execute(() -> deckDao.insertDeck(deck));
    }

    public void getDeckById(UUID deckId, DataCallback<Deck> callback) {
        executor.execute(() -> {
            Deck data = deckDao.getDeckById(deckId);
            callback.onDataLoaded(data);
        });
    }

    public void updateDeck(Deck deck) {
        executor.execute(() -> deckDao.updateDeck(deck));
    }

    public void deleteDeck(Deck deck) {
        executor.execute(() -> deckDao.deleteDeck(deck));
    }
}