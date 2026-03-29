package com.example.memoria.ui.auth;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.R;
import com.example.memoria.data.repository.SyncRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class AuthViewModel extends ViewModel {
    private final FirebaseAuth mAuth;
    private final SyncRepository syncRepository;
    private final Context context;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> snackbarMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToMain = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> showResetSuccessDialog = new MutableLiveData<>(false);

    @Inject
    public AuthViewModel(SyncRepository syncRepository, @ApplicationContext Context context) {
        this.mAuth = FirebaseAuth.getInstance();
        this.syncRepository = syncRepository;
        this.context = context;
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getSnackbarMessage() { return snackbarMessage; }
    public LiveData<Boolean> getNavigateToMain() { return navigateToMain; }
    public LiveData<Boolean> getShowResetSuccessDialog() { return showResetSuccessDialog; }

    public void resetNavigation() { navigateToMain.setValue(false); }
    public void resetDialogState() { showResetSuccessDialog.setValue(false); }

    // --- LOGIC ĐĂNG NHẬP ---
    public void login(String email, String password) {
        isLoading.setValue(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        handlePostAuthSync(context.getString(R.string.success_login));
                    } else {
                        isLoading.postValue(false);
                        snackbarMessage.postValue(context.getString(R.string.err_login));
                    }
                });
    }

    // --- LOGIC ĐĂNG KÝ ---
    public void signUp(String email, String password) {
        isLoading.setValue(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        handlePostAuthSync(context.getString(R.string.success_signup));
                    } else {
                        isLoading.postValue(false);
                        String errMsg = task.getException() != null ? task.getException().getMessage() : context.getString(R.string.err_signup);
                        snackbarMessage.postValue(errMsg);
                    }
                });
    }

    // --- LOGIC QUÊN MẬT KHẨU ---
    public void resetPassword(String email) {
        isLoading.setValue(true);
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            isLoading.postValue(false);
            if (task.isSuccessful()) {
                showResetSuccessDialog.postValue(true);
            } else {
                snackbarMessage.postValue(context.getString(R.string.err_forgot_password));
            }
        });
    }

    // --- LOGIC ĐỒNG BỘ DÙNG CHUNG ---
    private void handlePostAuthSync(String successMsg) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            syncRepository.pullAllDataFromCloud(user.getUid(), isSuccess -> {
                isLoading.postValue(false);
                if (!isSuccess) {
                    snackbarMessage.postValue(context.getString(R.string.err_get_data_from_firestore));
                } else {
                    snackbarMessage.postValue(successMsg);
                }
                navigateToMain.postValue(true);
            });
        } else {
            isLoading.postValue(false);
        }
    }
}
