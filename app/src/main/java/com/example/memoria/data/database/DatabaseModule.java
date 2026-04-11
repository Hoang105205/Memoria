package com.example.memoria.data.database;

import android.content.Context;

import androidx.room.Room;

import com.example.memoria.data.database.dao.CardDao;
import com.example.memoria.data.database.dao.DeckDao;
import com.example.memoria.data.database.dao.FavDao;
import com.example.memoria.data.database.dao.QuizDao;
import com.example.memoria.data.database.dao.SearchHistoryDao;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public static AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                        context,
                        AppDatabase.class,
                        "memoria_database"
                )
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public static CardDao provideCardDao(AppDatabase db) {
        return db.cardDao();
    }

    @Provides
    public static DeckDao provideDeckDao(AppDatabase db) {
        return db.deckDao();
    }

    @Provides
    public static FavDao provideFavDao(AppDatabase db) {
        return db.favDao();
    }

    @Provides
    public static QuizDao provideQuizDao(AppDatabase db) {
        return db.quizDao();
    }

    @Provides
    public static SearchHistoryDao provideSearchHistoryDao(AppDatabase db) {
        return db.searchDao();
    }

    @Provides
    @Singleton
    public static com.google.firebase.firestore.FirebaseFirestore provideFirestore() {
        return com.google.firebase.firestore.FirebaseFirestore.getInstance();
    }
}