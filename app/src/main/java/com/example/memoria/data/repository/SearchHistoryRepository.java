package com.example.memoria.data.repository;

import com.example.memoria.data.database.dao.SearchHistoryDao;

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
}
