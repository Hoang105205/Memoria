package com.example.memoria.data.repository;

import com.example.memoria.data.database.dao.QuizDao;

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
}
