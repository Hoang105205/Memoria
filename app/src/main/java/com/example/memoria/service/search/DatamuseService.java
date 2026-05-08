package com.example.memoria.service.search;

import com.example.memoria.data.model.dto.DatamuseWord;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DatamuseService {
    @GET("words")
    Call<List<DatamuseWord>> getRandomWords(
            @Query("sp") String pattern,
            @Query("max") int max
    );
}