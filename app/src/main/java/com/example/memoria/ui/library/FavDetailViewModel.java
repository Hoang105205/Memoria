package com.example.memoria.ui.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.data.model.FavFolder;
import com.example.memoria.data.model.FavWord;
import com.example.memoria.data.repository.FavRepository;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FavDetailViewModel extends ViewModel {
    private final FavRepository repository;
    private final MutableLiveData<FavFolder> currentFolder = new MutableLiveData<>();
    private final MutableLiveData<List<FavWord>> folderWords = new MutableLiveData<>();

    @Inject
    public FavDetailViewModel(FavRepository repository) {
        this.repository = repository;
    }

    public LiveData<FavFolder> getFolder() {
        return currentFolder;
    }

    public LiveData<List<FavWord>> getFolderWords() {
        return folderWords;
    }

    public void loadWords(UUID folderId) {
        repository.getWordsByFolder(folderId, folderWords::postValue);
    }

    public void togglePinStatus(FavWord word) {
        word.setPinStatus(!word.isPinStatus());
        repository.updateWord(word);
        // Load lại danh sách sau khi update để Room tự sort lại ghim lên đầu
        loadWords(word.getFolderId());
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