package com.example.memoria.data.repository;

import com.example.memoria.data.database.dao.QuizDao;
import com.example.memoria.data.model.QuizHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class QuizRepository {
    private final QuizDao quizDao;
    private final ExecutorService executor;

    @Inject
    public QuizRepository(QuizDao quizDao) {
        this.quizDao = quizDao;
        executor = Executors.newSingleThreadExecutor();
    }


    public void getStudyDaysInMonth(long startOfMonth, long endOfMonth, CardRepository.DataCallback<List<Long>> callback) {
        executor.execute(() -> {
            List<Long> days = quizDao.getStudyDaysInMonth(startOfMonth, endOfMonth);
            callback.onDataLoaded(days);
        });

        /*List<Long> mockDays = new ArrayList<>();
        java.util.Calendar cal = java.util.Calendar.getInstance();

        // Giả sử tháng này học vào ngày 1, ngày 5, ngày 10 và hôm nay
        cal.setTimeInMillis(startOfMonth); // Bắt đầu từ ngày đầu tháng

        // Ngày 1
        mockDays.add(cal.getTimeInMillis());

        // Ngày 5
        cal.set(java.util.Calendar.DAY_OF_MONTH, 5);
        mockDays.add(cal.getTimeInMillis());

        // Ngày 10
        cal.set(java.util.Calendar.DAY_OF_MONTH, 19);
        mockDays.add(cal.getTimeInMillis());

        // Ngày hôm nay
        mockDays.add(System.currentTimeMillis());

        // Trả kết quả về ngay lập tức
        callback.onDataLoaded(mockDays);*/
    }

    public void getWeeklyReport(long start, long end, CardRepository.DataCallback<List<QuizDao.WeeklyChartData>> callback) {
        executor.execute(() -> {
            List<QuizDao.WeeklyChartData> data = quizDao.getWeeklyReport(start, end);
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                    callback.onDataLoaded(data));
        });

        /*List<QuizDao.WeeklyChartData> mockData = new ArrayList<>();

        // Giả sử Thứ 2 (start) có học
        QuizDao.WeeklyChartData day1 = new QuizDao.WeeklyChartData();
        day1.date = start;
        day1.total_correct = 10;
        day1.total_incorrect = 2;
        mockData.add(day1);

        // Giả sử Thứ 4 (start + 2 ngày) có học
        QuizDao.WeeklyChartData day3 = new QuizDao.WeeklyChartData();
        day3.date = start + (2 * 86400000L);
        day3.total_correct = 15;
        day3.total_incorrect = 5;
        mockData.add(day3);

        // Trả về dữ liệu mock ngay lập tức
        callback.onDataLoaded(mockData);*/
    }

    public void addQuizResult(QuizHistory history, CardRepository.DataCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                history.setSyncStatus(0);
                quizDao.insertQuizHistory(history);

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                        callback.onDataLoaded(true)
                );
            } catch (Exception e) {
                e.printStackTrace();
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                        callback.onDataLoaded(false)
                );
            }
        });
    }

    public void updateQuizResult(QuizHistory history, CardRepository.DataCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                history.setSyncStatus(0);
                quizDao.updateQuizHistory(history);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                        callback.onDataLoaded(true)
                );
            } catch (Exception e) {
                e.printStackTrace();
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                        callback.onDataLoaded(false)
                );
            }
        });
    }
}
