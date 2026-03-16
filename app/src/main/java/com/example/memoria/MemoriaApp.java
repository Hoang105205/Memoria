package com.example.memoria;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MemoriaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        setUpCloudinary();
    }

    private void setUpCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", BuildConfig.CLOUD_NAME);
        config.put("api_key", BuildConfig.API_KEY);
        config.put("api_secret", BuildConfig.API_SECRET);

        MediaManager.init(this, config);
    }

}