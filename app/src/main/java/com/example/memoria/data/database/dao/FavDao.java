package com.example.memoria.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.memoria.data.model.FavFolder;
import com.example.memoria.data.model.FavFolderWithCount;
import com.example.memoria.data.model.FavWord;

import java.util.List;
import java.util.UUID;

@Dao
public interface FavDao {
    // --- Phần Folder ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFolder(FavFolder folder);

    @Update
    void updateFolder(FavFolder folder);

    @Delete
    void deleteFolder(FavFolder folder);

    // Lấy toàn bộ danh sách Favorite folder, sắp xếp theo ngày tạo mới nhất
    @Query("SELECT * FROM fav_folders ORDER BY created_at DESC")
    List<FavFolder> getAllFolders();

    // Lấy chi tiết 1 favorite folder theo ID
    @Query("SELECT * FROM fav_folders WHERE folder_id = :folderId")
    FavFolder getFavFolderById(UUID folderId);

    // --- Phần Word ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWord(FavWord word);

    @Update
    void updateWord(FavWord word);

    @Delete
    void deleteWord(FavWord word);

    // Lấy danh sách từ trong 1 folder, đưa các từ được ghim (pin) lên đầu
    @Query("SELECT * FROM fav_words WHERE folder_id = :folderId ORDER BY pin_status DESC, added_at DESC")
    List<FavWord> getWordsByFolder(UUID folderId);

    // Xóa nhanh 1 từ theo ID
    @Query("DELETE FROM fav_words WHERE fav_id = :wordId")
    void deleteWordById(UUID wordId);

    // Truy vấn danh sách thư mục kèm theo số lượng từ bên trong
    @Query("SELECT f.*, (SELECT COUNT(fav_id) FROM fav_words WHERE folder_id = f.folder_id) AS word_count " +
            "FROM fav_folders f ORDER BY f.created_at DESC")
    List<FavFolderWithCount> getFoldersWithWordCount();

    // Kiểm tra xem từ vựng đã tồn tại trong thư mục chưa (Trả về số lượng)
    @Query("SELECT COUNT(fav_id) FROM fav_words WHERE folder_id = :folderId AND word_text = :wordText")
    int checkWordExist(UUID folderId, String wordText);
}
