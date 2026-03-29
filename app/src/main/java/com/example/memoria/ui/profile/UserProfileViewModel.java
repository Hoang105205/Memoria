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


    private final LiveData<Integer> learnedToday;
    private final LiveData<Integer> streak;
    private MutableLiveData<List<Long>> monthlyStudyDays = new MutableLiveData<>();
    private final MutableLiveData<List<float[]>> weeklyChartData = new MutableLiveData<>();
    public LiveData<List<float[]>> getWeeklyChartData() { return weeklyChartData; }
    private Uri newSelectedAvatarUri = null;

    @Inject
    public UserProfileViewModel(UserRepository userRepository, CardRepository cardRepository, QuizRepository quizRepository) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.quizRepository = quizRepository;
        this.mAuth = FirebaseAuth.getInstance();

        // Lấy mốc 0h hôm nay
        long startOfToday = getStartOfToday();

        // 1. Kết nối "sống" với DB để lấy số từ học hôm nay
        this.learnedToday = cardRepository.getWordsLearnedTodayLiveData(startOfToday);

        // 2. Kết nối "sống" với DB để tính Streak tự động
        // Chúng ta quan sát danh sách ngày học từ CardRepository
        this.streak = androidx.lifecycle.Transformations.map(
                cardRepository.getAllReviewDaysLiveData(),
                this::calculateStreak
        );

        loadUserInfo();
    }


    // --- GETTERS CHO FRAGMENT ---

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            userName.setValue((name != null && !name.isEmpty()) ? name : user.getEmail());
            userAvatar.setValue(user.getPhotoUrl());
        }
    }

    // Các hàm Getter để Fragment đọc dữ liệu
    public LiveData<Integer> getLearnedToday() { return learnedToday; }
    public LiveData<Integer> getStreakLiveData() { return streak; }
    public LiveData<String> getUserName() { return userName; }
    public LiveData<Uri> getUserAvatar() { return userAvatar; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Boolean> getNavigateBack() { return navigateBack; }


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

    private long getStartOfToday() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private int calculateStreak(List<Long> dates) {
        if (dates == null || dates.isEmpty()) return 0;
        int currentStreak = 0;
        long oneDay = 86400000L;
        long today = (System.currentTimeMillis() / oneDay) * oneDay;
        long yesterday = today - oneDay;

        if (dates.get(0) < yesterday) return 0;

        long expectedDate = dates.get(0);
        for (Long date : dates) {
            if (date.equals(expectedDate)) {
                currentStreak++;
                expectedDate -= oneDay;
            } else {
                break;
            }
        }
        return currentStreak;
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
