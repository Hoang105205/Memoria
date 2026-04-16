package com.example.memoria.service.search;

import com.example.memoria.data.model.dto.WordnikWODResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WordnikService {
    @GET("words.json/wordOfTheDay")
    Call<WordnikWODResponse> getWordOfTheDay(@Query("api_key") String apiKey);
}
