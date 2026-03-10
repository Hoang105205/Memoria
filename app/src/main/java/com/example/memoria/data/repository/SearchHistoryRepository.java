package com.example.memoria.data.repository;

import android.app.Application;

import com.example.memoria.data.database.AppDatabase;
import com.example.memoria.data.database.dao.SearchHistoryDao;
import com.example.memoria.data.model.SearchHistory;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SearchHistoryRepository {
    private final SearchHistoryDao searchHistoryDao;
    private final ExecutorService executor;

    @Inject
    public SearchHistoryRepository(SearchHistoryDao searchHistoryDao) {
        this.searchHistoryDao = searchHistoryDao;
        executor = Executors.newSingleThreadExecutor();
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }

    public void getAllSearchHistories(DataCallback<List<SearchHistory>> callback) {
        executor.execute(() -> {
            List<SearchHistory> data = searchHistoryDao.getAllSearches();
            callback.onDataLoaded(data);
        });
    }

    public void getRecentSearchHistories(DataCallback<List<SearchHistory>> callback) {
        executor.execute(() -> {
            List<SearchHistory> data = searchHistoryDao.getRecentSearches();
            callback.onDataLoaded(data);
        });
    }

    public void insertSearchHistory(SearchHistory item) {
        executor.execute(() -> searchHistoryDao.insertSearch(item));
    }

    public void deleteSearchHistory(SearchHistory item) {
        executor.execute(() -> searchHistoryDao.deleteSearch(item));
    }

    public void saveOrMoveToTop(String word) {
        executor.execute(() -> {
            SearchHistory existing = searchHistoryDao.findByWordText(word);

            SearchHistory h = new SearchHistory(existing != null ? existing.getHistoryId() : UUID.randomUUID());
            h.setWordText(word);
            h.setSearchedAt(new Date());
            searchHistoryDao.upsertAndTrim(h);
        });
    }
}