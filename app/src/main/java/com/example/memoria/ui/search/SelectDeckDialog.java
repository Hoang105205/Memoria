package com.example.memoria.ui.search;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.entity.Deck;
import com.example.memoria.ui.adapter.SelectDeckAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Date;
import java.util.UUID;

public class SelectDeckDialog extends BottomSheetDialogFragment {

    private SearchViewModel viewModel;
    private SelectDeckAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_select_deck, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Dùng requireActivity() để lấy chung ViewModel với SearchFragment
        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        RecyclerView rvDecks = view.findViewById(R.id.rv_decks_to_select);
        rvDecks.setLayoutManager(new LinearLayoutManager(getContext()));

        // tao adapter Khi người dùng bấm vào 1 Deck có sẵn
        adapter = new SelectDeckAdapter(deck -> {
            viewModel.saveCurrentWordToDeck(deck.getDeckId(), isSuccess -> {
                // Ép chạy trên luồng giao diện chính (Main Thread)
                requireActivity().runOnUiThread(() -> {
                    if (isSuccess) {
                        Toast.makeText(getContext(), getString(R.string.msg_save_success), Toast.LENGTH_SHORT).show();
                        dismiss(); // Chỉ đóng Dialog khi lưu thành công
                    } else {
                        Toast.makeText(getContext(), getString(R.string.msg_card_exists), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
        rvDecks.setAdapter(adapter);

        // Lắng nghe dữ liệu
        viewModel.getDecks().observe(getViewLifecycleOwner(), decks -> {
            adapter.setDecks(decks);
        });
        viewModel.loadDecks(); // Load danh sách mới nhất
    }
}