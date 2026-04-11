package com.example.memoria.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

// Import Entities
// Import DAOs
import com.example.memoria.data.database.dao.*;
// Import Converters
import com.example.memoria.data.model.entity.Card;
import com.example.memoria.data.model.entity.Deck;
import com.example.memoria.data.model.entity.FavFolder;
import com.example.memoria.data.model.entity.FavWord;
import com.example.memoria.data.model.entity.QuizHistory;
import com.example.memoria.data.model.entity.QuizStat;
import com.example.memoria.data.model.entity.SearchHistory;
import com.example.memoria.utils.DateConverter;
import com.example.memoria.utils.JSONStringConverter;
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
        version = 2,
        exportSchema = false
)
// Đăng ký Converter để hiểu kiểu Date, UUID
@TypeConverters({DateConverter.class, UUIDConverter.class, JSONStringConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    // Khai báo các DAO (Interface)
    public abstract DeckDao deckDao();
    public abstract CardDao cardDao();
    public abstract FavDao favDao();
    public abstract QuizDao quizDao();
    public abstract SearchHistoryDao searchDao();

    // Hàm xóa toàn bộ dữ liệu cũ, dùng khi logout
    public void clearAllTablesAsync() {
        new Thread(this::clearAllTables).start();
    }
}
