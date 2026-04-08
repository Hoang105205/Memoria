package com.example.memoria.data.repository;
import com.example.memoria.data.database.dao.CardDao;
import com.example.memoria.data.database.dao.DeckDao;
import com.example.memoria.data.model.entity.Card;
import com.example.memoria.data.model.entity.Deck;
import com.example.memoria.data.model.dto.CardAudioItem;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
@Singleton
public class VocabularyRepository {
    private final CardDao cardDao;
    private final DeckDao deckDao;
    private final ExecutorService executor;
    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }
    @Inject
    public VocabularyRepository(CardDao cardDao, DeckDao deckDao) {
        this.cardDao = cardDao;
        this.deckDao = deckDao;
        executor = Executors.newSingleThreadExecutor();
    }
    public void getAllCardsOfAllDecksFiltered(
            @Nullable List<String> deckIdStrings,
            boolean selectAll,
            DataCallback<List<CardAudioItem>> callback
    ) {
        executor.execute(() -> {
            List<Deck> decks = deckDao.getAllDecks();
            List<CardAudioItem> result = new ArrayList<>();

            for (Deck d : decks) {
                UUID deckId = d.getDeckId();

                boolean include = selectAll;
                if (!include && deckIdStrings != null) {
                    include = deckIdStrings.contains(deckId.toString());
                }
                if (!include) continue;

                List<Card> cards = cardDao.getCardsByDeckIdSync(deckId);
                for (Card c : cards) {
                    result.add(new CardAudioItem(c.getFrontText(), c.getBackMeanings()));
                }
            }

            callback.onDataLoaded(result);
        });
    }
}
