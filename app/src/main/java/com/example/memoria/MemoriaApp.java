package com.example.memoria;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

import dagger.hilt.android.HiltAndroidApp;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;
import javax.inject.Inject;

@HiltAndroidApp
public class MemoriaApp extends Application implements Configuration.Provider {
    // Không cần viết gì thêm bên trong này nếu bạn chưa cần logic khởi tạo đặc biệt
    @Inject
    HiltWorkerFactory workerFactory;

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }
  
    @Override
    public void onCreate() {
        super.onCreate();

        setUpCloudinary();
        com.jakewharton.threetenabp.AndroidThreeTen.init(this);
    }

    private void setUpCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", BuildConfig.CLOUD_NAME);
        config.put("api_key", BuildConfig.API_KEY);
        config.put("api_secret", BuildConfig.API_SECRET);

        MediaManager.init(this, config);
    }
}