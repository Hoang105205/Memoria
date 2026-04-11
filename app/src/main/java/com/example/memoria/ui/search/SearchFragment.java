package com.example.memoria.ui.search;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.dto.DictionaryResponse;
import com.example.memoria.data.model.dto.Suggestion;
import com.example.memoria.data.model.entity.SearchHistory;
import com.example.memoria.data.repository.SearchHistoryRepository;
import com.example.memoria.service.search.RetrofitClient;
import com.example.memoria.service.search.SuggestionClient;
import com.example.memoria.ui.adapter.RecentSearchAdapter;
import com.example.memoria.ui.adapter.SearchSuggestionAdapter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    // No results UI
    private View layoutNoResults;
    private TextView tvNoResults;

    // Voice
    private ImageView ivMic;
    private ActivityResultLauncher<String> requestAudioPermissionLauncher;
    private ActivityResultLauncher<Intent> speechLauncher;

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

        // --- No results views ---
        layoutNoResults = view.findViewById(R.id.layout_no_results);
        tvNoResults = view.findViewById(R.id.tv_no_results);
        hideNoResults();

        // --- Register Activity Result Launchers (permission + speech) ---
        registerVoiceLaunchers();

        // --- Mic icon ---
        ivMic = view.findViewById(R.id.ivMic);
        if (ivMic != null) {
            ivMic.setOnClickListener(v -> onMicClicked());
        }

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
            hideNoResults();
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
            hideNoResults();
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

                hideNoResults();

                if (s.length() >= 2) {
                    fetchSuggestions(s.toString());

                    loadRecent();
                } else {
                    hideSuggestions();
                    loadRecent();
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
                hideNoResults();
                searchWord(word);
            }
            return true;
        });

        ImageView ivCamera = view.findViewById(R.id.ivCamera);
        ivCamera.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_searchFragment_to_cameraFragment);
        });

        // Observe external query
        viewModel.getExternalSearchQuery().observe(getViewLifecycleOwner(), queryToSearch -> {
            if (queryToSearch != null && !queryToSearch.isEmpty()) {
                suppressSuggestionFetch = true;
                edtWord.setText(queryToSearch);
                edtWord.setSelection(queryToSearch.length());
                hideNoResults();
                searchWord(queryToSearch);

                viewModel.setExternalSearchQuery(null);
            }
        });

        getParentFragmentManager().setFragmentResultListener(
                CameraFragment.OCR_REQUEST_KEY,
                getViewLifecycleOwner(),
                (key, bundle) -> {
                    String ocrText = bundle.getString(CameraFragment.OCR_RESULT_TEXT, "");
                    edtWord.setText(ocrText);
                    edtWord.setSelection(ocrText.length());
                    hideSuggestions();
                    hideNoResults();
                    searchWord(ocrText);
                }
        );

        return view;
    }

    private void showNoResults(String word) {
        if (!isAdded()) return;

        String w = (word == null) ? "" : word.trim();

        if (tvNoResults != null) {
            tvNoResults.setText(getString(R.string.no_results, "\"" + w + "\""));
        }

        if (layoutNoResults != null) layoutNoResults.setVisibility(View.VISIBLE);

        hideSuggestions();
        hideKeyboardAndClearFocus();

        // Ẩn recent khi not found
        if (tvRecent != null) tvRecent.setVisibility(View.GONE);
        if (rvRecent != null) rvRecent.setVisibility(View.GONE);
    }

    private void hideNoResults() {
        if (layoutNoResults != null) layoutNoResults.setVisibility(View.GONE);
    }

    private void registerVoiceLaunchers() {
        // Permission launcher
        requestAudioPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        startGoogleSpeechPopup();
                    } else {
                        boolean canAskAgain = shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);
                        if (!canAskAgain) {
                            showGoToSettingsDialog();
                        } else {
                            showPermissionDeniedDialog();
                        }
                    }
                });

        // Speech launcher
        speechLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != android.app.Activity.RESULT_OK || result.getData() == null) {
                        return;
                    }

                    ArrayList<String> matches =
                            result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    if (matches == null || matches.isEmpty()) return;

                    String spokenText = matches.get(0);
                    if (spokenText == null) return;

                    spokenText = spokenText.trim();
                    if (spokenText.isEmpty()) return;

                    suppressSuggestionFetch = true;
                    edtWord.setText(spokenText);
                    edtWord.setSelection(spokenText.length());
                    hideSuggestions();
                    hideNoResults();
                    searchWord(spokenText);
                });
    }

    private void onMicClicked() {
        if (!isAdded()) return;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startGoogleSpeechPopup();
        } else {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startGoogleSpeechPopup() {
        if (!isAdded()) return;

        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt));

            speechLauncher.launch(intent);
        } catch (Exception e) {
            Log.e("VOICE", "Cannot start speech recognizer: " + e.getMessage());
            showSpeechNotAvailableDialog();
        }
    }

    private void showPermissionDeniedDialog() {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.mic_permission)
                .setMessage(R.string.mic_permission_message)
                .setPositiveButton("OK", (d, w) -> d.dismiss())
                .show();
    }

    private void showGoToSettingsDialog() {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.mic_permission)
                .setMessage(R.string.mic_permission_cancel)
                .setNegativeButton(R.string.btn_cancel, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.btn_setting, (d, w) -> {
                    d.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.fromParts("package", requireContext().getPackageName(), null));
                    startActivity(intent);
                })
                .show();
    }

    private void showSpeechNotAvailableDialog() {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Không thể nhận diện giọng nói")
                .setMessage("Thiết bị không hỗ trợ hoặc thiếu Google Speech/Google app.")
                .setPositiveButton("OK", (d, w) -> d.dismiss())
                .show();
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

                        boolean hasData = response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty();

                        if (isAdded() && hasData) {
                            hideNoResults();

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
                            showNoResults(word);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<DictionaryResponse>> call, Throwable t) {
                        Log.e("API", t.getMessage() != null ? t.getMessage() : "API error");
                        showNoResults(word);
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

        View focused = requireActivity().getCurrentFocus();
        if (focused == null) focused = edtWord;

        edtWord.clearFocus();

        InputMethodManager imm =
                (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
    }
}