package com.example.memoria;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.memoria.service.ReminderManager;
import com.example.memoria.ui.auth.LoginActivity;
import com.example.memoria.ui.onboarding.OnboardingActivity;
import com.example.memoria.utils.SyncHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.memoria.ui.study.LearnFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Kiểm tra Onboarding trước
        SharedPreferences pref = getSharedPreferences("onboarding_pref", MODE_PRIVATE);
        boolean isFinished = pref.getBoolean("isFinished", false);
        //boolean isFinished = false;
        if (!isFinished) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish(); // Kết thúc MainActivity ngay lập tức
            return;   // Ngăn không cho các dòng code bên dưới chạy tiếp
        }

        // 2. Kiểm tra Login
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Gọi hàm để thực hiện đồng bộ dữ liệu lên Firebase: Ngay lập tức và chạy ngầm
        SyncHelper.triggerImmediateSync(this, currentUser.getUid());
        SyncHelper.startPeriodicSync(this, currentUser.getUid());

        setContentView(R.layout.activity_main);

        // Khởi tạo các View sau khi đã setContentView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        ReminderManager.scheduleDailyReminder(this);



    }
}