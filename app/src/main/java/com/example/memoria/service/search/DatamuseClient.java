package com.example.memoria.service.search;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DatamuseClient {
    private static DatamuseService api;
    public static DatamuseService getApi() {
        if (api == null) {
            api = new Retrofit.Builder()
                    .baseUrl("https://api.datamuse.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(DatamuseService.class);
        }
        return api;
    }
}