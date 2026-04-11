package com.example.memoria.service;

import com.example.memoria.data.database.dao.CardDao;
import com.example.memoria.data.database.dao.DeckDao;
import com.example.memoria.data.model.dto.DeckWithCard;
import com.example.memoria.data.model.entity.Card;
import com.example.memoria.data.model.entity.Deck;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CloneService {
    private final DeckDao deckDao;
    private final CardDao cardDao;
    private final ExecutorService executor;

    public interface CloneCallBack {
        void onCloneResult(boolean success, UUID newDeckId);
    }

    @Inject
    public CloneService(DeckDao deckDao, CardDao cardDao) {
        this.deckDao = deckDao;
        this.cardDao = cardDao;
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Hàm thực hiện Deep Copy Deck và danh sách Card từ Firebase về Local thông qua DTO.
     * @param data DTO chứa Deck gốc và danh sách Card
     * @param callback Trả về kết quả cho UI
     */
    public void cloneDeckToLocal(DeckWithCard data, CloneCallBack callback) {
        executor.execute(() -> {
            try {
                if (data == null || data.getDeck() == null) {
                    if (callback != null) callback.onCloneResult(false, null);
                    return;
                }

                // Tách dữ liệu từ DTO
                Deck originalDeck = data.getDeck();
                List<Card> originalCards = data.getCards();

                // Tao UUID moi cho Deck
                UUID newDeckId = UUID.randomUUID();
                Date currentTime = new Date();

                // Clone Deck
                Deck clonedDeck = new Deck();
                clonedDeck.setDeckId(newDeckId);
                // Do du lieu tu original deck vao
                clonedDeck.setDeckName(originalDeck.getDeckName());
                clonedDeck.setCoverColor(originalDeck.getCoverColor());
                clonedDeck.setCreatedAt(currentTime);
                clonedDeck.setUpdatedAt(currentTime);
                clonedDeck.setFirestoreId(null); // null de Firebase tu tao document
                clonedDeck.setSyncStatus(0); // de syncworker day len cloud

                // Luu Deck xuong Room DB truoc (de card co cho tham chieu foreignkey)
                deckDao.insertDeck(clonedDeck);

                // looping de clone card
                if (originalCards != null && !originalCards.isEmpty()) {
                    for (Card card : originalCards) {
                        Card clonedCard = new Card();

                        // tao UUID moi va gan deckId
                        clonedCard.setCardId(UUID.randomUUID());
                        clonedCard.setDeckId(newDeckId);

                        // copy du lieu tu front card
                        clonedCard.setFrontText(card.getFrontText());
                        clonedCard.setFrontImage(card.getFrontImage());

                        // copy kieu card
                        clonedCard.setCardType(card.getCardType());

                        // copy du lieu tu back card
                        if (card.getBackTypes() != null) {
                            clonedCard.setBackTypes(new ArrayList<>(card.getBackTypes()));
                        }
                        if (card.getBackMeanings() != null) {
                            clonedCard.setBackMeanings(new ArrayList<>(card.getBackMeanings()));
                        }

                        // tao thoi gian dong bo moi
                        clonedCard.setCreatedAt(currentTime);
                        clonedCard.setUpdatedAt(currentTime);

                        // Reset lại toàn bộ tiến độ học tập (vì đây là bộ thẻ mới đối với người dùng này)
                        clonedCard.setEaseFactor(2.5);
                        clonedCard.setIntervalDays(0);
                        clonedCard.setLastResult(0);
                        clonedCard.setLastReviewAt(null);
                        clonedCard.setNextReviewDate(null);
                        clonedCard.setReviewCount(0);

                        // Reset ID của Firestore và gán Sync Status
                        clonedCard.setFirestoreId(null);
                        clonedCard.setSyncStatus(0); // 0 = Chưa đồng bộ

                        // Lưu Card xuống Room DB
                        cardDao.insertCard(clonedCard);
                    }
                }

                // bao UI thanh cong
                if (callback != null) {
                    callback.onCloneResult(true, newDeckId);
                }

            }
            catch (Exception e) {
                e.printStackTrace();
                // Bao loi ve UI neu co Exception
                if (callback != null) callback.onCloneResult(false, null);
            }
        });
    }

}
