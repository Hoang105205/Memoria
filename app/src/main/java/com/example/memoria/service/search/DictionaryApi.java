package com.example.memoria.service.search;

import com.example.memoria.ui.search.DictionaryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DictionaryApi {
    @GET("api/v2/entries/en/{word}")
    Call<List<DictionaryResponse>> getMeaning(
            @Path("word") String word
    );
}
