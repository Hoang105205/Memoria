package com.example.memoria.ui.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.service.CloneService;
import com.example.memoria.service.SharedDeckService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SharedDeckViewModel extends ViewModel {
    private final SharedDeckService sharedDeckService;
    private final CloneService cloneService;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    public interface ImportResultCallback {
        void onResult(boolean success, UUID newDeckId, String message);
    }

    @Inject
    public SharedDeckViewModel(SharedDeckService sharedDeckService, CloneService cloneService) {
        this.sharedDeckService = sharedDeckService;
        this.cloneService = cloneService;
    }

    public void importDeckFromCode(String shareCode, ImportResultCallback callback) {
        _isLoading.postValue(true);

        sharedDeckService.downloadSharedDeck(shareCode, (downloadSuccess, data, message) -> {
            if (downloadSuccess && data != null) {
                cloneService.cloneDeckToLocal(data, (cloneSuccess, newDeckId) -> {
                    _isLoading.postValue(false);

                    if (cloneSuccess) {
                        if (callback != null) callback.onResult(true, newDeckId, null);
                    } else {
                        if (callback != null) callback.onResult(false, null, "Lỗi khi lưu dữ liệu vào bộ nhớ máy!");
                    }
                });

            } else {
                _isLoading.postValue(false);
                if (callback != null) callback.onResult(false, null, message);
            }
        });
    }

    public interface ExportResultCallback {
        void onResult(boolean success, String shareCode, String message);
    }

    public void exportDeckToCloud(String localDeckId, ExportResultCallback callback) {
        _isLoading.postValue(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            _isLoading.postValue(false);
            if (callback != null) callback.onResult(false, null, "Bạn cần đăng nhập để chia sẻ bộ thẻ!");
            return;
        }

        String userId = user.getUid();
        String authorName = user.getDisplayName();
        if (authorName == null || authorName.isEmpty()) {
            authorName = user.getEmail() != null ? user.getEmail().split("@")[0] : "Anonymous";
        }

        sharedDeckService.exportDeck(userId, authorName, localDeckId, (success, shareCode, message) -> {
            _isLoading.postValue(false);
            if (callback != null) {
                callback.onResult(success, shareCode, message);
            }
        });
    }
}