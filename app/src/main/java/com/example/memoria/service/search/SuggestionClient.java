package com.example.memoria.service.search;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SuggestionClient {

    private static final String BASE_URL = "https://api.datamuse.com/";
    private static Retrofit retrofit;

    public static SuggestionApi getApi() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(SuggestionApi.class);
    }
}
