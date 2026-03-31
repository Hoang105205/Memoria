package com.example.memoria.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.memoria.data.model.entity.QuizHistory;
import com.example.memoria.data.model.entity.QuizStat;

import java.util.List;
import java.util.UUID;

@Dao
public interface QuizDao {
    // --- Phần Thống Kê Tổng Quiz (QuizStat) ---
    // Lấy bảng thống kê (Luôn chỉ có 1 dòng với ID = 1)
    @Query("SELECT * FROM quiz_stat WHERE stat_id = 1")
    QuizStat getQuizStats();

    // Cập nhật hoặc tạo mới thống kê
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateQuizStats(QuizStat stat);

    // --- Phần Lịch Sử Quiz (QuizHistory) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuizHistory(QuizHistory history);

    @Update
    void updateQuizHistory(QuizHistory history);

    // Lấy lịch sử Quiz của 1 bộ thẻ cụ thể, mới nhất xếp trước
    @Query("SELECT * FROM quiz_his WHERE deck_id = :deckId ORDER BY taken_at DESC")
    List<QuizHistory> getHistoryByDeck(UUID deckId);

    // Lấy toàn bộ lịch sử Quiz của người dùng
    @Query("SELECT * FROM quiz_his ORDER BY taken_at DESC")
    List<QuizHistory> getAllHistory();



    @Query("SELECT DISTINCT (taken_at / 86400000) * 86400000 FROM quiz_his " +
            "WHERE taken_at >= :startOfMonth AND taken_at <= :endOfMonth")
    List<Long> getStudyDaysInMonth(long startOfMonth, long endOfMonth);

    @Query("SELECT (taken_at / 86400000) * 86400000 AS date, " +
            "SUM(correct_count) AS total_correct, " +
            "SUM(total_questions - correct_count) AS total_incorrect " +
            "FROM quiz_his " +
            "WHERE taken_at >= :start AND taken_at <= :end " +
            "GROUP BY date ORDER BY date ASC")
    List<WeeklyChartData> getWeeklyReport(long start, long end);

    // Class hứng dữ liệu tạm thời
    class WeeklyChartData {
        public long date;
        public int total_correct;
        public int total_incorrect;
    }
}