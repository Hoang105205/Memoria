package com.example.memoria.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

// Import Entities
import com.example.memoria.data.model.*;
// Import DAOs
import com.example.memoria.data.database.dao.*;
// Import Converters
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

    // Khai báo các DAO (Interface)
    public abstract DeckDao deckDao();
    public abstract CardDao cardDao();
    public abstract FavDao favDao();
    public abstract QuizDao quizDao();
    public abstract SearchDao searchDao();

    // Singleton Pattern (Chỉ tạo 1 instance duy nhất)
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "memoria_database" // Tên file DB lưu trong máy
                            )
                            // .allowMainThreadQueries() // Chỉ dùng dòng này khi test nhanh, cấm dùng khi release!
                            .fallbackToDestructiveMigration() // Nếu đổi version DB thì reset lại từ đầu (Dev mode)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Hàm xóa toàn bộ dữ liệu cũ, dùng khi logout
    public void clearAllTablesAsync() {
        new Thread(() -> {
            clearAllTables(); // Hàm có sẵn của Room, xóa sạch dữ liệu
        }).start();
    }
}
