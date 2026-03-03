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

import com.example.memoria.ui.auth.LoginActivity;
import com.example.memoria.ui.onboarding.OnboardingActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.memoria.ui.study.LearnFragment;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Kiểm tra Onboarding trước
        //SharedPreferences pref = getSharedPreferences("onboarding_pref", MODE_PRIVATE);
        //boolean isFinished = pref.getBoolean("isFinished", false);
        boolean isFinished = false;
        if (!isFinished) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish(); // Kết thúc MainActivity ngay lập tức
            return;   // Ngăn không cho các dòng code bên dưới chạy tiếp
        }

        // 2. Kiểm tra Login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }


        setContentView(R.layout.activity_main);

        // Khởi tạo các View sau khi đã setContentView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }
    }
}