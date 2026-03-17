package com.example.memoria.ui.library;

import android.content.Context;
import com.example.memoria.utils.SyncHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import dagger.hilt.android.qualifiers.ApplicationContext;

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
    private final Context context;
    private final FavRepository repository;
    private final MutableLiveData<FavFolder> currentFolder = new MutableLiveData<>();
    private final MutableLiveData<List<FavWord>> folderWords = new MutableLiveData<>();

    @Inject
    public FavDetailViewModel(FavRepository repository, @ApplicationContext Context context) {
        this.context = context;
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

        // Gọi sync để cập nhật dữ liệu lên FireStore
        triggerSync();
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

            // Gọi sync để cập nhật dữ liệu lên FireStore
            triggerSync();
        }
    }

    // Xóa thư mục
    public void deleteCurrentFolder() {
        FavFolder folder = currentFolder.getValue();
        if (folder != null) {
            repository.deleteFolder(folder);

            // Gọi sync để cập nhật dữ liệu lên FireStore
            triggerSync();
        }
    }

    // hàm helper dùng chung cho ViewModel này
    private void triggerSync() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Chỉ cần user đang đăng nhập, tự động kích hoạt tiến trình sync và delete
            SyncHelper.triggerImmediateSync(context, user.getUid());
        }
    }
}