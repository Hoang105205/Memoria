package com.example.memoria;

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

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView =
                findViewById(R.id.bottomNavigationView);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragmentContainerView);

        NavController navController =
                navHostFragment.getNavController();

        // Khai báo các top-level destinations
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(
                        R.id.home_graph,
                        R.id.search_graph,
                        R.id.library_graph,
                        R.id.profile_graph
                ).build();


        // Kết nối BottomNavigation với Navigation
        NavigationUI.setupWithNavController(
                bottomNavigationView, navController);
    }
}