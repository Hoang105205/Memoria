package com.example.memoria;

import android.app.Application;
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
}