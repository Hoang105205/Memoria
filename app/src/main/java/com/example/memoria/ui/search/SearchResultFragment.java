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
import com.example.memoria.ui.adapter.SearchWordResultAdapter;
import com.example.memoria.utils.PronunciationManager;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchResultFragment extends Fragment {

    private SearchViewModel viewModel;
    private TextView tvWord, tvPhonetic;
    private RecyclerView rvMeanings;
    private SearchWordResultAdapter adapter;
    private AutoCompleteTextView actvSpeed;
    private ImageButton btnFavorite, btnSave, btnPlay;

    private PronunciationManager audioManager;
    private float currentSpeed = 1.0f;

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

        audioManager = new PronunciationManager(requireContext());

        // Sử dụng requireActivity() để dùng chung ViewModel với SearchFragment
        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        // Lắng nghe dữ liệu thay đổi
        viewModel.getSearchResult().observe(getViewLifecycleOwner(), this::updateUI);

        // Nút Back
        view.findViewById(R.id.btn_result_back).setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });

        // Action listeners
        btnFavorite.setOnClickListener(v -> {
            // Chỉ đơn giản là gọi Dialog hiện lên.
            // Dialog không cần nhận param, ViewModel sẽ tự lo phần data.
            SelectFavFolderDialog dialog = new SelectFavFolderDialog();
            dialog.show(getChildFragmentManager(), "SelectFavFolderDialog");
        });

        btnSave.setOnClickListener(v -> Log.d("ACTION", "Create flashcard clicked"));

        btnPlay.setOnClickListener(v -> {
            playAudio();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Mỗi lần quay lại màn hình này, nạp lại danh sách tốc độ
        setUpSpeedDropdown();
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

    private void setUpSpeedDropdown() {
        // 1. Dùng mảng String cố định để bảo toàn thứ tự hiển thị
        String[] speedOptions = {"0.5x", "1.0x", "1.5x", "2.0x"};

        ArrayAdapter<String> speedAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, speedOptions);
        actvSpeed.setAdapter(speedAdapter);

        // 2. Set giá trị mặc định hiển thị trên ô text
        actvSpeed.setText("1.0x", false);

        // 3. Lắng nghe sự kiện người dùng bấm chọn tốc độ mới
        actvSpeed.setOnItemClickListener((parent, view, position, id) -> {
            // Lấy chữ user vừa chọn (VD: "1.5x")
            String selectedSpeedStr = (String) parent.getItemAtPosition(position);

            // Cắt bỏ chữ "x" và chuyển phần số thành kiểu float
            String numberOnly = selectedSpeedStr.replace("x", ""); // "1.5x" -> "1.5"
            currentSpeed = Float.parseFloat(numberOnly); // Lưu vào biến toàn cục: 1.5f
        });
    }

    private void playAudio() {
        if (viewModel.getSearchResult().getValue() != null) {
            String word = viewModel.getSearchResult().getValue().word;
            String audioUrl = "";

            if (viewModel.getSearchResult().getValue().phonetics != null
                    && !viewModel.getSearchResult().getValue().phonetics.isEmpty()) {
                audioUrl = viewModel.getSearchResult().getValue().phonetics.get(0).audio;
            }

            audioManager.playSound(word, audioUrl, currentSpeed);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (audioManager != null) {
            audioManager.releaseResources();
        }
    }
}
