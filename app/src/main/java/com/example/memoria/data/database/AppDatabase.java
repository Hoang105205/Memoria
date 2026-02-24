package com.example.memoria.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

// 1. Khai báo tất cả các Entity
import com.example.memoria.data.model.*;
import com.example.memoria.utils.DateConverter;
import com.example.memoria.utils.UUIDConverter;

@Database(
        entities = {
                Deck.class,
                Card.class,
                FavFolder.class,
                FavWord.class,
                SearchHistory.class,
                QuizStat.class,
                QuizHistory.class
        },
        version = 1,
        exportSchema = false
)
// Đăng ký Converter để hiểu kiểu Date, UUID
@TypeConverters({DateConverter.class, UUIDConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    // Phần khai báo DAO sẽ viết ở commit sau
    // public abstract DeckDao deckDao();
    // public abstract CardDao cardDao();
    // ...
}
