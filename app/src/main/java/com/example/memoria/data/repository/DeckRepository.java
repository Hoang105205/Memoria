package com.example.memoria.data.repository;

import android.app.Application;
import com.example.memoria.data.database.AppDatabase;
import com.example.memoria.data.database.dao.DeckDao;
import com.example.memoria.data.model.Deck;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeckRepository {
    private static volatile DeckRepository INSTANCE;
    private final DeckDao deckDao;
    private final ExecutorService executor; // run on background

    private DeckRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        deckDao = db.deckDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public static DeckRepository getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (DeckRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DeckRepository(application);
                }
            }
        }
        return INSTANCE;
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
}