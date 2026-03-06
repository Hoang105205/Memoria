package com.example.memoria.data.repository;

import com.example.memoria.data.database.dao.FavDao;
import com.example.memoria.data.model.FavFolder;
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
}