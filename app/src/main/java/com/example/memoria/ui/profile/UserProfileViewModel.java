package com.example.memoria.ui.profile;

import static android.provider.Settings.System.getString;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.R;
import com.example.memoria.data.database.dao.QuizDao;
import com.example.memoria.data.repository.UserRepository;
import com.example.memoria.service.MemoriaWidgetProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

import com.example.memoria.data.database.AppDatabase;
import com.example.memoria.data.repository.CardRepository;
import com.example.memoria.data.repository.QuizRepository;
import com.example.memoria.data.repository.CardRepository.DataCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@HiltViewModel
public class UserProfileViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final FirebaseAuth mAuth;
    private final AppDatabase appDatabase;
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

    private final Context context;

    @Inject
    public UserProfileViewModel(UserRepository userRepository, CardRepository cardRepository, QuizRepository quizRepository, AppDatabase appDatabase, @ApplicationContext Context context) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.quizRepository = quizRepository;
        this.appDatabase = appDatabase;
        this.mAuth = FirebaseAuth.getInstance();
        this.context = context;

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
        // Chạy ngầm việc xóa Database để không block UI
        Executors.newSingleThreadExecutor().execute(() -> {
            // Sau khi xóa xong mới chuyển về LoginActivity
            appDatabase.clearAllTables();
            MemoriaWidgetProvider.forceUpdateWidget(context);
        });
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
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

            // 1. Đưa dữ liệu từ DB vào Map với Key là chuỗi "2026-04-15"
            java.util.Map<String, float[]> dataMap = new java.util.HashMap<>();
            if (data != null) {
                for (QuizDao.WeeklyChartData d : data) {
                    dataMap.put(d.date_str, new float[]{d.total_correct, d.total_incorrect});
                }
            }

            // 2. Chạy vòng lặp 7 ngày
            for (int i = 0; i < 7; i++) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTimeInMillis(startOfWeek);
                cal.add(java.util.Calendar.DAY_OF_YEAR, i);
                String dateKey = sdf.format(cal.getTime()); // Tạo key "2026-04-15"

                if (dataMap.containsKey(dateKey)) {
                    chartValues.add(dataMap.get(dateKey));
                } else {
                    chartValues.add(new float[]{0f, 0f});
                }
            }
            weeklyChartData.postValue(chartValues);
        });
    }

}
