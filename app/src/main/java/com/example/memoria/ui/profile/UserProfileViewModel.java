package com.example.memoria.ui.profile;

import static android.provider.Settings.System.getString;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.R;
import com.example.memoria.data.database.dao.QuizDao;
import com.example.memoria.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import com.example.memoria.data.repository.CardRepository;
import com.example.memoria.data.repository.QuizRepository;
import com.example.memoria.data.repository.CardRepository.DataCallback;

import java.util.ArrayList;
import java.util.List;

@HiltViewModel
public class UserProfileViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final FirebaseAuth mAuth;

    private final CardRepository cardRepository;
    private final QuizRepository quizRepository;
    // Biến LiveData để báo cho UI biết lúc nào đang up ảnh (hiện vòng xoay)
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateBack = new MutableLiveData<>(false);

    private final MutableLiveData<String> userName = new MutableLiveData<>();
    private final MutableLiveData<Uri> userAvatar = new MutableLiveData<>();
    // Khai báo biến để chứa dữ liệu
    private final MutableLiveData<Integer> learnedTodayLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> streakLiveData = new MutableLiveData<>(0);
    private MutableLiveData<List<Long>> monthlyStudyDays = new MutableLiveData<>();
    private final MutableLiveData<List<float[]>> weeklyChartData = new MutableLiveData<>();
    public LiveData<List<float[]>> getWeeklyChartData() { return weeklyChartData; }
    private Uri newSelectedAvatarUri = null;

    @Inject
    public UserProfileViewModel(UserRepository userRepository, CardRepository cardRepository, QuizRepository quizRepository) {
        this.cardRepository = cardRepository;
        this.quizRepository = quizRepository;
        mAuth = FirebaseAuth.getInstance();
        this.userRepository = userRepository;
        loadUserInfo();
        loadUserStats();
    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            userName.setValue((name != null && !name.isEmpty()) ? name : user.getEmail());
            userAvatar.setValue(user.getPhotoUrl());
        }
    }
    // Trong UserProfileViewModel.java
    public void loadUserStats() {
        // Lấy số từ học hôm nay
        cardRepository.getWordsLearnedToday(count -> {
            learnedTodayLiveData.postValue(count);
        });

        // Lấy Streak
        quizRepository.getCurrentStreak(streak -> {
            streakLiveData.postValue(streak);
        });
    }
    // Các hàm Getter để Fragment đọc dữ liệu
    public LiveData<String> getUserName() { return userName; }
    public LiveData<Uri> getUserAvatar() { return userAvatar; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Boolean> getNavigateBack() { return navigateBack; }
    public LiveData<Integer> getLearnedToday() { return learnedTodayLiveData; }
    public LiveData<Integer> getStreakLiveData() { return streakLiveData; }

    public LiveData<List<Long>> getMonthlyStudyDays() {
        return monthlyStudyDays;
    }
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

    public void loadMonthlyProgress(long startOfMonth, long endOfMonth) {
        quizRepository.getStudyDaysInMonth(startOfMonth, endOfMonth, days -> {
            monthlyStudyDays.postValue(days);
        });
    }

    public void loadWeeklyProgress(long startOfWeek, long endOfWeek) {
        quizRepository.getWeeklyReport(startOfWeek, endOfWeek, data -> {
            List<float[]> chartValues = new ArrayList<>();

            java.util.Map<Long, float[]> dataMap = new java.util.HashMap<>();
            for (QuizDao.WeeklyChartData d : data) {
                dataMap.put(d.date, new float[]{d.total_correct, d.total_incorrect});
            }

            for (int i = 0; i < 7; i++) {
                long currentDay = startOfWeek + (i * 86400000L);
                if (dataMap.containsKey(currentDay)) {
                    chartValues.add(dataMap.get(currentDay));
                } else {
                    chartValues.add(new float[]{0f, 0f}); // Ngày không học
                }
            }
            weeklyChartData.postValue(chartValues);
        });
    }


}
