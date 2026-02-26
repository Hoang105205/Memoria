package com.example.memoria.ui.search;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    EditText edtWord;

    MediaPlayer mediaPlayer;

    // Suggestions (dropdown)
    RecyclerView listSuggestions;
    SearchSuggestionAdapter adapter;
    final List<String> suggestionWords = new ArrayList<>();
    private boolean suppressSuggestionFetch = false;

    // Search result (meaning list)
    RecyclerView rvResult;
    SearchWordResultAdapter resultAdapter;

    public SearchFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        edtWord = view.findViewById(R.id.edt_search_searchBar);

        // suggestions
        listSuggestions = view.findViewById(R.id.rv_search_suggestion);
        adapter = new SearchSuggestionAdapter(word -> {
            suppressSuggestionFetch = true;

            edtWord.setText(word);
            edtWord.setSelection(word.length());

            hideSuggestions();
            searchWord(word);
        });
        listSuggestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        listSuggestions.setAdapter(adapter);
        listSuggestions.setVisibility(View.GONE);

        // result recycler
        rvResult = view.findViewById(R.id.rv_search_result);
        resultAdapter = new SearchWordResultAdapter();
        rvResult.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResult.setAdapter(resultAdapter);

        // autocomplete
        edtWord.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (suppressSuggestionFetch) {
                    suppressSuggestionFetch = false;
                    return;
                }

                if (s.length() >= 2) {
                    fetchSuggestions(s.toString());
                } else {
                    hideSuggestions();
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        // search on keyboard action
        edtWord.setOnEditorActionListener((v, actionId, event) -> {
            String word = edtWord.getText().toString().trim();
            if (!word.isEmpty()) {
                hideSuggestions();
                searchWord(word);
            }
            return false;
        });

        return view;
    }

    private void hideSuggestions() {
        suggestionWords.clear();
        adapter.submitList(new ArrayList<>());
        listSuggestions.setVisibility(View.GONE);
    }

    private void searchWord(String word) {
        hideSuggestions();

        RetrofitClient.getApi()
                .getMeaning(word)
                .enqueue(new Callback<List<DictionaryResponse>>() {
                    @Override
                    public void onResponse(Call<List<DictionaryResponse>> call,
                                           Response<List<DictionaryResponse>> response) {

                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            DictionaryResponse data = response.body().get(0);

                            // Build UI list for RecyclerView
                            List<SearchWordResultAdapter.UiItem> uiItems = new ArrayList<>();

                            if (data.meanings != null) {
                                for (Meaning meaning : data.meanings) {
                                    if (meaning == null) continue;

                                    String pos = meaning.partOfSpeech != null ? meaning.partOfSpeech : "";
                                    uiItems.add(SearchWordResultAdapter.UiItem.header(pos));

                                    if (meaning.definitions != null) {
                                        for (Definition def : meaning.definitions) {
                                            if (def == null) continue;
                                            uiItems.add(SearchWordResultAdapter.UiItem.definition(
                                                    def.definition,
                                                    def.example
                                            ));
                                        }
                                    }
                                }
                            }

                            if (uiItems.isEmpty()) {
                                uiItems.add(SearchWordResultAdapter.UiItem.empty("No result"));
                            }

                            resultAdapter.submitItems(uiItems);

                        } else {
                            resultAdapter.submitItems(
                                    java.util.Collections.singletonList(
                                            SearchWordResultAdapter.UiItem.empty("No result")
                                    )
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<List<DictionaryResponse>> call, Throwable t) {
                        Log.e("API", t.getMessage() != null ? t.getMessage() : "API error");
                        resultAdapter.submitItems(
                                java.util.Collections.singletonList(
                                        SearchWordResultAdapter.UiItem.empty("API error")
                                )
                        );
                    }
                });
    }

    private void playAudio(String audioUrl) {
        if (audioUrl == null || audioUrl.isEmpty()) return;

        try {
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchSuggestions(String query) {
        SuggestionClient.getApi()
                .getSuggestions(query)
                .enqueue(new Callback<List<Suggestion>>() {
                    @Override
                    public void onResponse(Call<List<Suggestion>> call,
                                           Response<List<Suggestion>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            suggestionWords.clear();
                            for (Suggestion s : response.body()) suggestionWords.add(s.word);

                            adapter.submitList(new ArrayList<>(suggestionWords));
                            listSuggestions.setVisibility(suggestionWords.isEmpty() ? View.GONE : View.VISIBLE);
                        } else {
                            hideSuggestions();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Suggestion>> call, Throwable t) {
                        Log.e("SUGGEST", t.getMessage() != null ? t.getMessage() : "Suggest error");
                        hideSuggestions();
                    }
                });
    }
}