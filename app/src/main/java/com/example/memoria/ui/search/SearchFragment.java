package com.example.memoria.ui.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.SearchHistory;
import com.example.memoria.data.repository.SearchHistoryRepository;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

@AndroidEntryPoint
public class SearchFragment extends Fragment {

    private static final int MAX_SUGGESTIONS = 7;

    private EditText edtWord;
    private SearchViewModel viewModel;

    // Recent
    private RecyclerView rvRecent;
    private TextView tvRecent;
    private RecentSearchAdapter recentAdapter;

    @Inject
    SearchHistoryRepository historyRepo;

    // Suggestions
    private RecyclerView listSuggestions;
    private SearchSuggestionAdapter adapter;
    private final List<String> suggestionWords = new ArrayList<>();
    private boolean suppressSuggestionFetch = false;

    public SearchFragment() {}

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                hideSuggestions();
                hideKeyboardAndClearFocus();
            }
            return false;
        });

        edtWord = view.findViewById(R.id.edt_search_searchBar);

        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        // Recent setup
        tvRecent = view.findViewById(R.id.tv_search_recent);
        rvRecent = view.findViewById(R.id.rv_search_recent);

        FlexboxLayoutManager flexbox = new FlexboxLayoutManager(requireContext());
        flexbox.setFlexWrap(FlexWrap.WRAP);
        flexbox.setFlexDirection(FlexDirection.ROW);
        flexbox.setJustifyContent(JustifyContent.FLEX_START);

        rvRecent.setLayoutManager(flexbox);
        recentAdapter = new RecentSearchAdapter(word -> {
            suppressSuggestionFetch = true;
            edtWord.setText(word);
            edtWord.setSelection(word.length());
            hideSuggestions();
            searchWord(word);
        });
        rvRecent.setAdapter(recentAdapter);

        loadRecent();

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
            return true;
        });

        ImageView ivCamera = view.findViewById(R.id.ivCamera);
        ivCamera.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_searchFragment_to_cameraFragment);
        });

        // Su dung logic observe để lấy dữ liệu từ ViewModel
        viewModel.getExternalSearchQuery().observe(getViewLifecycleOwner(), queryToSearch -> {
            if (queryToSearch != null && !queryToSearch.isEmpty()) {
                edtWord.setText(queryToSearch);
                edtWord.setSelection(queryToSearch.length());
                searchWord(queryToSearch);

                // Xóa dữ liệu sau khi tìm xong
                viewModel.setExternalSearchQuery(null);
            }
        });
        return view;
    }

    private void loadRecent() {
        historyRepo.getRecentSearchHistories(histories -> {
            if (!isAdded()) return;

            List<String> words = new ArrayList<>();
            if (histories != null) {
                for (SearchHistory h : histories) {
                    if (h == null) continue;
                    String w = h.getWordText();
                    if (w != null && !w.trim().isEmpty()) words.add(w.trim());
                }
            }

            requireActivity().runOnUiThread(() -> {
                recentAdapter.submitList(words);
                boolean empty = words.isEmpty();
                tvRecent.setVisibility(empty ? View.GONE : View.VISIBLE);
                rvRecent.setVisibility(empty ? View.GONE : View.VISIBLE);
            });
        });
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

                        if (isAdded() && response.isSuccessful()
                                && response.body() != null && !response.body().isEmpty()) {

                            historyRepo.saveOrMoveToTop(word);
                            loadRecent();

                            viewModel.setSearchResult(response.body().get(0));

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

                            // CHỈ LẤY TỐI ĐA MAX_SUGGESTIONS
                            for (Suggestion s : response.body()) {
                                if (s == null || s.word == null) continue;

                                suggestionWords.add(s.word);
                                if (suggestionWords.size() >= MAX_SUGGESTIONS) break;
                            }

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

    private void hideKeyboardAndClearFocus() {
        if (!isAdded()) return;

        View view = requireView();
        View focused = requireActivity().getCurrentFocus();
        if (focused == null) focused = edtWord;

        // clear focus trước để cursor biến mất
        edtWord.clearFocus();

        InputMethodManager imm =
                (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
    }
}