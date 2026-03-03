package com.example.memoria.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;

public class SearchResultFragment extends Fragment {

    private SearchViewModel viewModel;
    private TextView tvWord, tvPhonetic, tvSpeedLabel;
    private RecyclerView rvMeanings;
    private SearchWordResultAdapter adapter;
    private Slider speedSlider;

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
        tvSpeedLabel = view.findViewById(R.id.tv_speed_label);
        rvMeanings = view.findViewById(R.id.rv_result_meanings);
        speedSlider = view.findViewById(R.id.slider_speed);

        adapter = new SearchWordResultAdapter();
        rvMeanings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMeanings.setAdapter(adapter);

        // Sử dụng requireActivity() để dùng chung ViewModel với SearchFragment
        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        // Lắng nghe dữ liệu thay đổi
        viewModel.getSearchResult().observe(getViewLifecycleOwner(), this::updateUI);

        // Nút Back
        view.findViewById(R.id.btn_result_back).setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });

        // Nút Save to favorites
        view.findViewById(R.id.btn_result_save).setOnClickListener(v -> {
            Log.d("SAVE", "Save to favorites clicked");
            // BottomSheetDialogFragment will be implemented here later
        });

        // UI chỉnh tốc độ (Speed Slider)
        speedSlider.addOnChangeListener((slider, value, fromUser) -> {
            String speed = String.format("%.1fx", value);
            tvSpeedLabel.setText(speed);
            Log.d("TTS", "Speed set to: " + speed);
        });

        view.findViewById(R.id.fab_play_audio).setOnClickListener(v -> {
            Log.d("TTS", "Play audio triggered");
            // Sau này bạn sẽ implement TTS tại đây
        });
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
