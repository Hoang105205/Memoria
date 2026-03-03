package com.example.memoria.ui.search;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private EditText edtWord;
    private SearchViewModel viewModel;

    // Suggestions (dropdown)
    private RecyclerView listSuggestions;
    private SearchSuggestionAdapter adapter;
    private final List<String> suggestionWords = new ArrayList<>();
    private boolean suppressSuggestionFetch = false;

    public SearchFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        edtWord = view.findViewById(R.id.edt_search_searchBar);

        // Khởi tạo Shared ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        // Suggestions setup
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

        // Autocomplete logic
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

        // Search on keyboard action
        edtWord.setOnEditorActionListener((v, actionId, event) -> {
            boolean isSearch =
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
                            || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                            || (event != null
                            && event.getAction() == android.view.KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER);

            if (!isSearch) return false;

            String word = edtWord.getText().toString().trim();
            if (!word.isEmpty()) {
                hideSuggestions();
                searchWord(word);
            }

            return true; // QUAN TRỌNG: consume để tránh hệ thống xử lý tiếp gây "đẩy lên"
        });

        return view;
    }

    private void hideSuggestions() {
        suggestionWords.clear();
        adapter.submitList(new ArrayList<>());
        listSuggestions.setVisibility(View.GONE);
    }

    private void searchWord(String word) {
        RetrofitClient.getApi()
                .getMeaning(word)
                .enqueue(new Callback<List<DictionaryResponse>>() {
                    @Override
                    public void onResponse(Call<List<DictionaryResponse>> call,
                                           Response<List<DictionaryResponse>> response) {

                        if (isAdded() && response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            // Đẩy dữ liệu vào Shared ViewModel
                            viewModel.setSearchResult(response.body().get(0));
                            
                            // KIỂM TRA ĐIỂM ĐẾN HIỆN TẠI TRƯỚC KHI NAVIGATE
                            NavController navController = Navigation.findNavController(requireView());
                            if (navController.getCurrentDestination() != null && 
                                navController.getCurrentDestination().getId() == R.id.searchFragment) {
                                navController.navigate(R.id.action_searchFragment_to_searchResultFragment);
                            }
                        } else {
                            Log.e("SEARCH", "No result or API error");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<DictionaryResponse>> call, Throwable t) {
                        Log.e("API", t.getMessage() != null ? t.getMessage() : "API error");
                    }
                });
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
