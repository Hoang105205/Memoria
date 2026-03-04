package com.example.memoria.ui.library;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.Deck;
import com.example.memoria.ui.adapter.DeckAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.UUID;

public class DeckListFragment extends Fragment {

    private LibraryViewModel viewModel;
    private DeckAdapter deckAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deck_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Dùng chung ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);

        RecyclerView rvDeckList = view.findViewById(R.id.rv_deck_list);
        FloatingActionButton btnAdd = view.findViewById(R.id.btn_deck_add);

        rvDeckList.setLayoutManager(new LinearLayoutManager(getContext()));
        deckAdapter = new DeckAdapter();
        rvDeckList.setAdapter(deckAdapter);

        // Lắng nghe dữ liệu
        viewModel.getDecks().observe(getViewLifecycleOwner(), decks -> {
            deckAdapter.setDecks(decks);
        });

        btnAdd.setOnClickListener(v -> showAddDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadDecks();
        }
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.dialog_title_add_deck);

        final EditText input = new EditText(requireContext());
        input.setPadding(60, 40, 60, 40);
        builder.setView(input);

        builder.setPositiveButton(R.string.action_create, (dialog, which) -> {
            String enteredName = input.getText().toString().trim();
            if (!enteredName.isEmpty()) {
                Deck newDeck = new Deck();
                newDeck.setDeckId(UUID.randomUUID());
                newDeck.setDeckName(enteredName);
                newDeck.setCreatedAt(new Date());
                viewModel.addNewDeck(newDeck);
            }
        });

        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }
}