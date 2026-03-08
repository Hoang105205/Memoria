package com.example.memoria.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.memoria.data.model.SearchHistory;

import java.util.List;

@Dao
public interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSearch(SearchHistory searchHistory);

    @Delete
    void deleteSearch(SearchHistory searchHistory);

    // Lấy 10 từ khóa tìm kiếm gần nhất
    @Query("SELECT * FROM search_histories ORDER BY searched_at DESC LIMIT 10")
    List<SearchHistory> getRecentSearches();

    // Lấy toàn bộ lịch sử tìm kiếm
    @Query("SELECT * FROM search_histories ORDER BY searched_at")
    List<SearchHistory> getAllSearches();

    // Xóa toàn bộ lịch sử tìm kiếm
    @Query("DELETE FROM search_histories")
    void clearAllSearchHistory();

    @Query("SELECT * FROM search_histories WHERE word_text = :word LIMIT 1")
    SearchHistory findByWordText(String word);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SearchHistory item);
}