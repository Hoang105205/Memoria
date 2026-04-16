package com.example.memoria.ui.profile;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.memoria.R;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.imageview.ShapeableImageView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import android.util.Log;
import java.util.Arrays;


public class ProgressFragment extends Fragment {

    private UserProfileViewModel viewModel;
    private ShapeableImageView ivAvatar;
    private TextView tvUsername, tvWordsLearned, tvStreak;
    private MaterialCalendarView calendarView;
    private java.util.Calendar currentWeekCalendar = java.util.Calendar.getInstance();
    private TextView tvWeekRange;
    private com.github.mikephil.charting.charts.BarChart barChart;
    public ProgressFragment() {
        super(R.layout.fragment_progress);
    }
    @Override

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // BƯỚC 1: KHỞI TẠO VIEWMODEL
        viewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);

        // BƯỚC 2: ÁNH XẠ TẤT CẢ CÁC VIEW (PHẢI LÀM TRƯỚC TIÊN)
        ivAvatar = view.findViewById(R.id.profile_iv_avatar);
        tvUsername = view.findViewById(R.id.profile_tv_username);
        tvWordsLearned = view.findViewById(R.id.profile_tv_words_count);
        tvStreak = view.findViewById(R.id.profile_tv_streak_count);
        calendarView = view.findViewById(R.id.calendarView);

        // CỰC KỲ QUAN TRỌNG: Phải ánh xạ 2 cái này thì mới chạy được Chart và Text tuần
        barChart = view.findViewById(R.id.barChart);
        tvWeekRange = view.findViewById(R.id.tv_week_range);

        // BƯỚC 3: CÀI ĐẶT GIAO DIỆN (CHỈ CHẠY KHI ĐÃ CÓ VIEW Ở BƯỚC 2)
        setupStackedBarChart(); // Bây giờ gọi hàm này sẽ KHÔNG bị Null nữa

        // BƯỚC 4: LOGIC LỊCH THÁNG
        java.util.Calendar now = java.util.Calendar.getInstance();
        fetchMonthlyData(now.get(java.util.Calendar.YEAR), now.get(java.util.Calendar.MONTH));

        calendarView.setOnMonthChangedListener((widget, date) -> {
            fetchMonthlyData(date.getYear(), date.getMonth() - 1);
        });

        // BƯỚC 5: LOGIC BIỂU ĐỒ TUẦN
        loadDataForSelectedWeek();

        view.findViewById(R.id.btn_prev_week).setOnClickListener(v -> {
            currentWeekCalendar.add(java.util.Calendar.WEEK_OF_YEAR, -1);
            loadDataForSelectedWeek();
        });

        view.findViewById(R.id.btn_next_week).setOnClickListener(v -> {
            currentWeekCalendar.add(java.util.Calendar.WEEK_OF_YEAR, 1);
            loadDataForSelectedWeek();
        });

        // BƯỚC 6: LẮNG NGHE DỮ LIỆU
        setupObservers();
    }
    private void fetchMonthlyData(int year, int month) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month, 1, 0, 0, 0);
        long start = cal.getTimeInMillis();

        cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        long end = cal.getTimeInMillis();

        viewModel.loadMonthlyProgress(start, end);
    }
    private void loadDataForSelectedWeek() {
        java.util.Calendar cal = (java.util.Calendar) currentWeekCalendar.clone();
        cal.setFirstDayOfWeek(java.util.Calendar.MONDAY);
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        long start = cal.getTimeInMillis();

        cal.add(java.util.Calendar.DAY_OF_WEEK, 6);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        long end = cal.getTimeInMillis();


        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault());
        tvWeekRange.setText(sdf.format(start) + " - " + sdf.format(end));

        viewModel.loadWeeklyProgress(start, end);
    }
    private void setupObservers() {
        Log.d("MEMORIA_DEBUG", "setupObservers: Start observing...");
        // Dùng lại thông tin Tên
        viewModel.getUserName().observe(getViewLifecycleOwner(), name -> tvUsername.setText(name));

        // Dùng lại thông tin Avatar
        viewModel.getUserAvatar().observe(getViewLifecycleOwner(), uri -> {
            Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(ivAvatar);
        });

        // Dùng lại chỉ số thống kê
        viewModel.getLearnedToday().observe(getViewLifecycleOwner(), count ->
                tvWordsLearned.setText(String.valueOf(count)));

        viewModel.getStreakLiveData().observe(getViewLifecycleOwner(), streak ->
                tvStreak.setText(String.valueOf(streak)));

        viewModel.getMonthlyStudyDays().observe(getViewLifecycleOwner(), days -> {
            if (days != null) {
                setupCalendarProgress(days);
            }
        });

        viewModel.getWeeklyChartData().observe(getViewLifecycleOwner(), data -> {
            Log.d("MEMORIA_DEBUG", "setupObservers: Data received! Size = " + (data != null ? data.size() : "null"));
            updateChartData(data);
        });
    }

    private void setupCalendarProgress(List<Long> studyDaysMilis) {
        MaterialCalendarView calendarView = getView().findViewById(R.id.calendarView);

        // 1. Chuyển list Miliseconds từ DB sang List CalendarDay của thư viện
        HashSet<CalendarDay> studyDates = new HashSet<>();
        for (Long milis : studyDaysMilis) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(milis);
            studyDates.add(CalendarDay.from(
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH) + 1, // Tháng trong Calendar chạy từ 0-11
                    cal.get(java.util.Calendar.DAY_OF_MONTH)
            ));
        }

        // 2. Lấy icon ngọn lửa từ drawable
        Drawable fireIcon = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_yellow);

        // 3. Xóa các decorator cũ và thêm cái mới vào
        calendarView.removeDecorators();
        calendarView.addDecorator(new StreakDecorator(fireIcon, studyDates));
    }

    private void setupStackedBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(false);

        // Cấu hình trục X
        com.github.mikephil.charting.components.XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        final String[] days = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(days));

        // --- PHẦN QUAN TRỌNG ĐỂ FIX SỐ ---
        com.github.mikephil.charting.components.YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Bắt đầu từ 0
        leftAxis.setGranularity(1f); // Chỉ cho phép bước nhảy là 1 đơn vị
        leftAxis.setDrawLabels(true);

        // Nếu vẫn hiện số lớn, hãy ép giá trị tối đa dựa trên dữ liệu hoặc set cứng để test
        // leftAxis.setAxisMaximum(50f);

        barChart.getAxisRight().setEnabled(false);
    }
    private void updateChartData(List<float[]> weeklyData) {
        if (weeklyData == null || weeklyData.isEmpty()) {
            Log.d("MEMORIA_DEBUG", "Weekly Data is NULL");
            // Nếu không có dữ liệu, hiển thị biểu đồ trống với các cột bằng 0 để tránh lỗi UI
            weeklyData = new ArrayList<>();
            for (int i = 0; i < 7; i++) weeklyData.add(new float[]{0f, 0f});
        }
        Log.d("MEMORIA_DEBUG", "Weekly Data Size: " + weeklyData.size());
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < weeklyData.size(); i++) {
            // float[] gồm: [số câu đúng, số câu sai]
            Log.d("MEMORIA_DEBUG", "Day " + i + ": " + Arrays.toString(weeklyData.get(i)));
            entries.add(new BarEntry(i, weeklyData.get(i)));
        }

        com.github.mikephil.charting.data.BarDataSet set = new com.github.mikephil.charting.data.BarDataSet(entries, "Retention");
        set.setColors(new int[]{
                ContextCompat.getColor(requireContext(), R.color.chart_remembered),
                ContextCompat.getColor(requireContext(), R.color.chart_forgotten)
        });
        set.setStackLabels(new String[]{"Remembered", "Forgotten"});

        com.github.mikephil.charting.data.BarData data = new com.github.mikephil.charting.data.BarData(set);


        data.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (value > 0) ? String.valueOf((int) value) : ""; // Chỉ hiện số > 0
            }
        });

        data.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        data.setValueTextSize(12f);


        barChart.setData(data);
        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }
}
