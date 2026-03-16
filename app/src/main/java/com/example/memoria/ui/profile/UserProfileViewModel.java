package com.example.memoria.ui.profile;

import static android.provider.Settings.System.getString;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.R;
import com.example.memoria.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class UserProfileViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final FirebaseAuth mAuth;


    // Biến LiveData để báo cho UI biết lúc nào đang up ảnh (hiện vòng xoay)
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateBack = new MutableLiveData<>(false);

    private final MutableLiveData<String> userName = new MutableLiveData<>();
    private final MutableLiveData<Uri> userAvatar = new MutableLiveData<>();

    private Uri newSelectedAvatarUri = null;

    @Inject
    public UserProfileViewModel(UserRepository userRepository) {
        mAuth = FirebaseAuth.getInstance();
        this.userRepository = userRepository;
        loadUserInfo();
    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            userName.setValue((name != null && !name.isEmpty()) ? name : user.getEmail());
            userAvatar.setValue(user.getPhotoUrl());
        }
    }

    // Các hàm Getter để Fragment đọc dữ liệu
    public LiveData<String> getUserName() { return userName; }
    public LiveData<Uri> getUserAvatar() { return userAvatar; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Boolean> getNavigateBack() { return navigateBack; }

    // Xử lý khi user chọn ảnh mới từ thư viện máy
    public void updateTemporaryAvatar(Uri uri) {
        newSelectedAvatarUri = uri;
    }

    public void clearTemporaryData() {
        newSelectedAvatarUri = null;
    }

    public void resetNavigateBack() {
        navigateBack.setValue(false);
    }

    // Xử lý đăng xuất
    public void signOut() {
        mAuth.signOut();
        navigateBack.postValue(true);
    }

    public void saveProfile(String newName) {
        isLoading.setValue(true); // Báo UI hiện vòng xoay

        // Gửi Name và newSelectedAvatarUri (có thể null nếu ko đổi ảnh) xuống Repo
        userRepository.updateUserProfile(newName, newSelectedAvatarUri, new UserRepository.UpdateCallback() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                toastMessage.postValue("Success");

                newSelectedAvatarUri = null; // Up xong thì reset

                loadUserInfo();

                navigateBack.postValue(true); // RA LỆNH CHO FRAGMENT THOÁT MÀN HÌNH
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                toastMessage.postValue(message);
            }
        });
    }
}
