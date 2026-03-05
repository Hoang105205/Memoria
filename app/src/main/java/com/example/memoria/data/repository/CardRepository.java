package com.example.memoria.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memoria.data.database.AppDatabase;
import com.example.memoria.data.database.dao.CardDao;
import com.example.memoria.data.model.Card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CardRepository {
    private static volatile CardRepository INSTANCE;
    private final CardDao cardDao;
    private final ExecutorService executor;

    private CardRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        cardDao = db.cardDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public static CardRepository getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (CardRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CardRepository(application);
                }
            }
        }
        return INSTANCE;
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }

    // Lấy danh sách thẻ theo Deck ID
    public LiveData<List<Card>> getCardsByDeckId(UUID deckId) {
        MutableLiveData<List<Card>> liveData = new MutableLiveData<>();
        // List<Card> data = cardDao.getCardsByDeckId(deckId);

        // Mockup-data
        List<Card> data = new ArrayList<>();

        Card c1 = new Card(UUID.randomUUID());
        c1.setFrontText("Scrumptious");
        c1.setBackTypes(new ArrayList<>(Arrays.asList("adj", "adj", "adj", "adj", "adj", "adj", "adj", "adj", "adj")));
        c1.setBackMeanings(new ArrayList<>(Arrays.asList("(Of food) Very delicious", "(Of a person) Very attractive", "Who know?", "Who care?", "I know!", "And I CARE!", "ADJ", "WOWOw")));
        data.add(c1);

        Card c2 = new Card(UUID.randomUUID());
        c2.setFrontText("Serendipity");
        c2.setBackTypes(new ArrayList<>(Arrays.asList("noun", "noun")));
        c2.setBackMeanings(new ArrayList<>(Arrays.asList("The occurrence of events by chance in a happy or beneficial way", "Good luck in finding valuable things one was not looking for")));
        data.add(c2);

        Card c3 = new Card(UUID.randomUUID());
        c3.setFrontText("Petrichor");
        c3.setBackTypes(new ArrayList<>(Arrays.asList("noun", "noun")));
        c3.setBackMeanings(new ArrayList<>(Arrays.asList("A pleasant smell that frequently accompanies the first rain after a long period of warm, dry weather", "(Context) The distinct scent of rain on dry earth")));
        data.add(c3);

        Card c4 = new Card(UUID.randomUUID());
        c4.setFrontText("Ephemeral");
        c4.setBackTypes(new ArrayList<>(Arrays.asList("adj", "adj")));
        c4.setBackMeanings(new ArrayList<>(Arrays.asList("Lasting for a very short time", "(Of plants) Having a very short life cycle")));
        data.add(c4);

        Card c5 = new Card(UUID.randomUUID());
        c5.setFrontText("Ineffable");
        c5.setBackTypes(new ArrayList<>(Arrays.asList("adj", "adj")));
        c5.setBackMeanings(new ArrayList<>(Arrays.asList("Too great or extreme to be expressed in words", "(Of a name) Too sacred to be spoken")));
        data.add(c5);

        liveData.setValue(data);

        return liveData;
    }

    // Cập nhật thẻ sau khi học xong (Nhớ/Quên)
    public void updateCard(Card card) {
        executor.execute(() -> cardDao.updateCard(card));
    }
}
