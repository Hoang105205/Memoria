package com.example.memoria.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.memoria.data.model.entity.Deck;
import com.example.memoria.data.model.entity.DeckWithCount;
import com.example.memoria.data.model.entity.FavFolderWithCount;

import java.util.List;
import java.util.UUID;

@Dao
public interface DeckDao {
    // Thêm bộ thẻ mới (Nếu trùng ID thì thay thế)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDeck(Deck deck);

    // Cập nhật thông tin bộ thẻ
    @Update
    void updateDeck(Deck deck);

    // Xóa bộ thẻ
    @Delete
    void deleteDeck(Deck deck);

    // Lấy toàn bộ danh sách bộ thẻ, sắp xếp theo ngày tạo mới nhất
    @Query("SELECT * FROM decks ORDER BY created_at DESC")
    List<Deck> getAllDecks();

    // Lấy chi tiết 1 bộ thẻ theo ID
    @Query("SELECT * FROM decks WHERE deck_id = :deckId")
    Deck getDeckById(UUID deckId);

    // Tìm kiếm bộ thẻ theo tên
    @Query("SELECT * FROM decks WHERE deck_name LIKE '%' || :keyword || '%'")
    List<Deck> searchDecks(String keyword);

    @Query("SELECT d.*, COUNT(DISTINCT c.card_id) AS total_cards, MAX(q.correct_count) AS highest_score " +
            "FROM decks d " +
            "LEFT JOIN cards c ON d.deck_id = c.deck_id AND c.sync_status IN (0, 1) " +
            "LEFT JOIN quiz_his q ON d.deck_id = q.deck_id AND q.sync_status IN (0, 1) " +
            "WHERE d.sync_status IN (0, 1) " +
            "GROUP BY d.deck_id " +
            "ORDER BY d.created_at DESC")
    List<DeckWithCount> getAllDecksWithCount();

    // Lấy danh sách Deck chưa đồng bộ
    @Query("SELECT * FROM decks WHERE sync_status NOT IN (1)")
    List<Deck> getUnsyncedDecks();

    // Truy vấn danh sách bộ thẻ kèm số lượng từ, lọc theo tên (Search)
    @Query("SELECT d.*, (SELECT COUNT(card_id) FROM cards WHERE deck_id = d.deck_id AND sync_status IN (0, 1)) AS total_cards " +
            "FROM decks d WHERE d.sync_status IN (0, 1) AND d.deck_name LIKE '%' || :keyword || '%' " +
            "ORDER BY d.created_at DESC")
    List<DeckWithCount> searchDecksWithWordCount(String keyword);
}