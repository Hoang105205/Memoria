package com.example.memoria.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.memoria.data.model.Card;

import java.util.List;
import java.util.UUID;

@Dao
public interface CardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCard(Card card);

    @Update
    void updateCard(Card card);

    @Delete
    void deleteCard(Card card);

    // Lấy 1 thẻ cụ thể theo ID
    @Query("SELECT * FROM cards WHERE card_id = :cardId")
    Card getCardById(UUID cardId);

    // Lấy toàn bộ thẻ của 1 bộ thẻ
    @Query("SELECT * FROM cards WHERE deck_id = :deckId")
    List<Card> getCardsByDeckId(UUID deckId);

    // Đếm số thẻ có trong bộ
    @Query("SELECT COUNT(*) FROM cards WHERE deck_id = :deckId")
    int countCardsInDeck(UUID deckId);

    // Lấy ra các thẻ cần ôn tập
    // Công thức: Nếu (Lần ôn cuối + (Số ngày * 24h * 60p * 60s * 1000ms)) <= Thời gian hiện tại -> Thì cần ôn tập
    // 86400000 là số mili-giây trong 1 ngày
    @Query("SELECT * FROM cards " +
            "WHERE last_review_at IS NOT NULL " +
            "AND (last_review_at + (interval_days * 86400000)) <= :currentTime")
    List<Card> getDueCards(long currentTime);

    // Lấy các thẻ mới chưa học bao giờ (last_review_at là null)
    @Query("SELECT * FROM cards WHERE last_review_at IS NULL AND deck_id = :deckId LIMIT :limit")
    List<Card> getNewCards(UUID deckId, int limit);
}