package com.example.memoria.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.memoria.data.model.dto.DictionaryResponse;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WodCacheManager {
    private static final String PREF_NAME = "wod_cache";
    private static final String KEY_DATE = "last_date";
    private static final String KEY_DATA = "wod_data";
    private final SharedPreferences prefs;
    private final Gson gson;

    public WodCacheManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveWod(DictionaryResponse data) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        prefs.edit()
                .putString(KEY_DATE, today)
                .putString(KEY_DATA, gson.toJson(data))
                .apply();
    }

    public DictionaryResponse getCachedWod() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastDate = prefs.getString(KEY_DATE, "");

        if (today.equals(lastDate)) {
            String json = prefs.getString(KEY_DATA, null);
            return json != null ? gson.fromJson(json, DictionaryResponse.class) : null;
        }
        return null;
    }
}