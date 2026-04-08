package com.example.memoria.service.search;

import com.example.memoria.data.model.dto.Suggestion;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SuggestionApi {

    @GET("sug")
    Call<List<Suggestion>> getSuggestions(
            @Query("s") String query
    );
}