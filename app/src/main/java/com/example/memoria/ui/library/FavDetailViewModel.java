package com.example.memoria.ui.library;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memoria.data.model.FavFolder;
import com.example.memoria.data.repository.FavRepository;

import java.util.UUID;

public class FavDetailViewModel extends AndroidViewModel {
    private final FavRepository repository;
    private final MutableLiveData<FavFolder> currentFolder = new MutableLiveData<>();

    public FavDetailViewModel(@NonNull Application application) {
        super(application);
        repository = FavRepository.getInstance(application);
    }

    public LiveData<FavFolder> getFolder() {
        return currentFolder;
    }

    // Tải dữ liệu thư mục lên
    public void loadFolder(UUID folderId) {
        repository.getFolderById(folderId, currentFolder::postValue);
    }

    // Đổi tên thư mục
    public void updateFolderName(String newName) {
        FavFolder folder = currentFolder.getValue();
        if (folder != null) {
            folder.setFolderName(newName);
            repository.updateFolder(folder);
            currentFolder.setValue(folder); // Cập nhật ngay lên UI
        }
    }

    // Xóa thư mục
    public void deleteCurrentFolder() {
        FavFolder folder = currentFolder.getValue();
        if (folder != null) {
            repository.deleteFolder(folder);
        }
    }
}