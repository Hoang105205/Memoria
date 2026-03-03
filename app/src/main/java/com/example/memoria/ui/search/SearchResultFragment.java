package com.example.memoria.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;

import java.util.ArrayList;
import java.util.List;

public class SearchResultFragment extends Fragment {

    private SearchViewModel viewModel;
    private TextView tvWord, tvPhonetic;
    private RecyclerView rvMeanings;
    private SearchWordResultAdapter adapter;
    private AutoCompleteTextView actvSpeed;
    private ImageButton btnFavorite, btnSave, btnPlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo UI
        tvWord = view.findViewById(R.id.tv_result_word);
        tvPhonetic = view.findViewById(R.id.tv_result_phonetic);
        rvMeanings = view.findViewById(R.id.rv_result_meanings);
        actvSpeed = view.findViewById(R.id.actv_speed);
        btnFavorite = view.findViewById(R.id.btn_result_favorite);
        btnSave = view.findViewById(R.id.btn_result_save);
        btnPlay = view.findViewById(R.id.btn_play_audio);

        adapter = new SearchWordResultAdapter();
        rvMeanings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMeanings.setAdapter(adapter);

        // Setup Speed Dropdown
        String[] speeds = {"0.5x", "1x", "1.5x"};
        ArrayAdapter<String> speedAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, speeds);
        actvSpeed.setAdapter(speedAdapter);
        actvSpeed.setOnItemClickListener((parent, v, position, id) -> {
            Log.d("TTS", "Speed changed to: " + speeds[position]);
        });

        // Sử dụng requireActivity() để dùng chung ViewModel với SearchFragment
        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        // Lắng nghe dữ liệu thay đổi
        viewModel.getSearchResult().observe(getViewLifecycleOwner(), this::updateUI);

        // Nút Back
        view.findViewById(R.id.btn_result_back).setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });

        // Action listeners
        btnFavorite.setOnClickListener(v -> Log.d("ACTION", "Add to favorites clicked"));
        btnSave.setOnClickListener(v -> Log.d("ACTION", "Create flashcard clicked"));
        btnPlay.setOnClickListener(v -> Log.d("TTS", "Play audio triggered"));
    }

    private void updateUI(DictionaryResponse data) {
        if (data == null) return;

        tvWord.setText(data.word);
        
        // Lấy phiên âm đầu tiên nếu có
        if (data.phonetics != null && !data.phonetics.isEmpty()) {
            tvPhonetic.setText(data.phonetics.get(0).text);
        } else {
            tvPhonetic.setText("");
        }

        // Chuyển đổi data sang danh sách item cho Adapter
        List<SearchWordResultAdapter.UiItem> uiItems = new ArrayList<>();
        if (data.meanings != null) {
            for (Meaning meaning : data.meanings) {
                uiItems.add(SearchWordResultAdapter.UiItem.header(meaning.partOfSpeech));
                for (Definition def : meaning.definitions) {
                    uiItems.add(SearchWordResultAdapter.UiItem.definition(def.definition, def.example));
                }
            }
        }
        adapter.submitItems(uiItems);
    }
}
