package com.example.memoria.data.repository;

import com.example.memoria.data.database.dao.FavDao;
import com.example.memoria.data.model.FavFolder;
import com.example.memoria.data.model.FavFolderWithCount;
import com.example.memoria.data.model.FavWord;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FavRepository {
    private final FavDao favDao;
    private final ExecutorService executor;

    @Inject
    public FavRepository(FavDao favDao) {
        this.favDao = favDao;
        executor = Executors.newSingleThreadExecutor();
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }

    public void getAllFolders(DataCallback<List<FavFolder>> callback) {
        executor.execute(() -> {
            List<FavFolder> data = favDao.getAllFolders();
            callback.onDataLoaded(data);
        });
    }

    public void insertFolder(FavFolder folder) {
        executor.execute(() -> favDao.insertFolder(folder));
    }

    public void getFolderById(UUID folderId, DataCallback<FavFolder> callback) {
        executor.execute(() -> {
            FavFolder folder = favDao.getFavFolderById(folderId);
            callback.onDataLoaded(folder);
        });
    }

    public void updateFolder(FavFolder folder) {
        executor.execute(() -> favDao.updateFolder(folder)); // name only
    }

    public void deleteFolder(FavFolder folder) {
        executor.execute(() -> favDao.deleteFolder(folder));
    }

    public void insertWord(FavWord word) {
        executor.execute(() -> favDao.insertWord(word));
    }

    public void getWordsByFolder(UUID folderId, DataCallback<List<FavWord>> callback) {
        executor.execute(() -> {
            List<FavWord> words = favDao.getWordsByFolder(folderId);
            callback.onDataLoaded(words);
        });
    }

    public void updateWord(FavWord word) {
        executor.execute(() -> favDao.updateWord(word));
    }

    public void getFoldersWithWordCount(DataCallback<List<FavFolderWithCount>> callback) {
        executor.execute(() -> {
            List<FavFolderWithCount> data = favDao.getFoldersWithWordCount();
            callback.onDataLoaded(data);
        });
    }

    public void insertWordIfNotExists(FavWord word, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            // Kiểm tra số lượng
            int count = favDao.checkWordExist(word.getFolderId(), word.getWordText());

            if (count == 0) {
                favDao.insertWord(word); // Chưa có thì thêm vào
                if (callback != null) callback.onDataLoaded(true); // Trả về true (Thành công)
            } else {
                if (callback != null) callback.onDataLoaded(false); // Trả về false (Đã tồn tại)
            }
        });
    }
}