package com.example.memoria.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.memoria.data.model.entity.Card;

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
    @Query("SELECT * FROM cards WHERE deck_id = :deckId ORDER BY created_at DESC, card_id DESC")
    androidx.lifecycle.LiveData<List<Card>> getCardsByDeckId(UUID deckId);

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

    //Tổng số từ đã học trong hôm nay
    //Thời gian review cuối cùng lớn hơn 0h ngày hôm nay
    @Query("SELECT COUNT(*) FROM cards WHERE last_review_at >= :startOfDay")
    LiveData<Integer> countCardsReviewedToday(long startOfDay);

    @Query("SELECT DISTINCT (last_review_at / 86400000) * 86400000 AS study_date " +
            "FROM cards " +
            "WHERE last_review_at IS NOT NULL " +
            "ORDER BY study_date DESC")
    LiveData<List<Long>> getDistinctStudyDays();


    //Card den han
    @Query("SELECT COUNT(*) FROM cards WHERE next_review_date <= :currentTime")
    int countDueCards(long currentTime);

    @Query("SELECT COUNT(*) FROM cards WHERE last_review_at >= :startOfDay")
    int countCardsReviewedTodaySync(long startOfDay); // Bản không có LiveData

    @Query("SELECT DISTINCT (last_review_at / 86400000) * 86400000 AS study_date FROM cards WHERE last_review_at IS NOT NULL ORDER BY study_date DESC")
    List<Long> getDistinctStudyDaysSync(); // Bản không có LiveData
    // Lấy danh sách Card chưa đồng bộ
    @Query("SELECT * FROM cards WHERE sync_status NOT IN (1)")
    List<Card> getUnsyncedCards();

    // Kiểm tra xem thẻ đã tồn tại trong bộ chưa (Trả về số lượng)
    @Query("SELECT COUNT(card_id) FROM cards WHERE deck_id = :deckId AND front_text = :frontText AND sync_status IN (0, 1)")
    int checkCardExist(UUID deckId, String frontText);

    // Lấy danh sách thẻ theo deckId dạng List tĩnh (để phục vụ cho tính năng Search)
    @Query("SELECT * FROM cards WHERE deck_id = :deckId AND sync_status IN (0, 1) ORDER BY created_at DESC")
    List<Card> getCardsByDeckIdSync(UUID deckId);

    // Tìm kiếm thẻ trong bộ thẻ theo từ khóa (Tìm theo mặt trước của thẻ)
    @Query("SELECT * FROM cards WHERE deck_id = :deckId AND sync_status IN (0, 1) AND front_text LIKE '%' || :keyword || '%' ORDER BY created_at DESC")
    List<Card> searchCardsInDeck(UUID deckId, String keyword);
}