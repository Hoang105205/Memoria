package com.example.memoria.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.memoria.data.model.entity.SearchHistory;

import java.util.List;

@Dao
public interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSearch(SearchHistory searchHistory);

    @Delete
    void deleteSearch(SearchHistory searchHistory);

    // Lấy 20 từ khóa tìm kiếm gần nhất
    @Query("SELECT * FROM search_histories ORDER BY searched_at DESC LIMIT 20")
    List<SearchHistory> getRecentSearches();

    // Lấy toàn bộ lịch sử tìm kiếm
    @Query("SELECT * FROM search_histories ORDER BY searched_at")
    List<SearchHistory> getAllSearches();

    // Xóa toàn bộ lịch sử tìm kiếm
    @Query("DELETE FROM search_histories")
    void clearAllSearchHistory();

    @Query("SELECT * FROM search_histories WHERE word_text = :word LIMIT 1")
    SearchHistory findByWordText(String word);

    // Xóa các bản ghi cũ, chỉ giữ 20 bản ghi mới nhất
    @Query("DELETE FROM search_histories WHERE history_id NOT IN (SELECT history_id FROM search_histories ORDER BY searched_at DESC LIMIT 20)")
    void trimTo20MostRecent();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SearchHistory item);

    @Transaction
    default void upsertAndTrim(SearchHistory item) {
        upsert(item);
        trimTo20MostRecent();
    }
}