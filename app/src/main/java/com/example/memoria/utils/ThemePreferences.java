package com.example.memoria.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemePreferences {
    private static final String PREF_NAME = "appearance_pref";
    private static final String KEY_NIGHT_MODE = "night_mode";

    private ThemePreferences() {
    }

    public static int getNightMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static void setNightMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_NIGHT_MODE, mode).apply();
    }

    public static void applySavedNightMode(Context context) {
        AppCompatDelegate.setDefaultNightMode(getNightMode(context));
    }
}

