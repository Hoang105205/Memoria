package com.example.memoria.ui.onboarding;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.memoria.R;
import com.example.memoria.data.model.entity.OnboardingItem;
import com.example.memoria.ui.auth.LoginActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // Sau khi xong 2 khung hệ thống (Camera & Mic), dù cho hay từ chối cũng đi tiếp
                navigateToLogin();
            });


    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding); // Layout chứa ViewPager2 của bạn

        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        Button btnNext = findViewById(R.id.btnNext);

        // 1. Dữ liệu
        List<OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingItem(R.drawable.bg_onboarding_img1, getString(R.string.instant_word_lookup), getString(R.string.instant_word_lookup_des)));
        items.add(new OnboardingItem(R.drawable.bg_onboarding_img2, getString(R.string.master_vocabulary), getString(R.string.master_vocabulary_des)));
        items.add(new OnboardingItem(R.drawable.bg_onboarding_img3, getString(R.string.personalized_learning), getString(R.string.personalized_learning_des)));

        // 2. Adapter
        OnboardingAdapter adapter = new OnboardingAdapter(items);
        viewPager.setAdapter(adapter);

        // 3. Dots Indicator
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

        // 4. Xử lý nút Next
        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < items.size() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                markOnboardingFinished();

                // Tạo danh sách quyền dựa trên phiên bản Android
                List<String> permissionsList = new ArrayList<>();
                permissionsList.add(Manifest.permission.CAMERA);
                permissionsList.add(Manifest.permission.RECORD_AUDIO);

                // Chỉ thêm quyền POST_NOTIFICATIONS nếu máy chạy Android 13 (API 33) trở lên
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionsList.add(Manifest.permission.POST_NOTIFICATIONS);
                }

                // Chuyển List thành Array để truyền vào launcher
                String[] permissionsArray = permissionsList.toArray(new String[0]);

                requestPermissionsLauncher.launch(permissionsArray);
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (position == items.size() - 1) {
                    btnNext.setText(R.string.get_started);
                } else {
                    btnNext.setText(R.string.next);
                }
            }
        });
        TextView tvLogin = findViewById(R.id.tvLoginOnboarding);
        tvLogin.setOnClickListener(v -> {
            markOnboardingFinished(); //  đánh dấu để lần sau không hiện lại Onboarding
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }




    private void markOnboardingFinished() {
        SharedPreferences pref = getSharedPreferences("onboarding_pref", MODE_PRIVATE);
        pref.edit().putBoolean("isFinished", true).apply();
    }
}
