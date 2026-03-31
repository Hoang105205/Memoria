package com.example.memoria.ui.library;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.ui.adapter.DeckCardAdapter;

import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DeckDetailFragment extends Fragment {
    private DeckDetailViewModel viewModel;
    private TextView tvDeckName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deck_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDeckName = view.findViewById(R.id.tv_deck_name);
        ImageButton btnOptions = view.findViewById(R.id.btn_deck_detail_options);
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        RecyclerView rvCards = view.findViewById(R.id.rv_deck_cards);
        android.widget.EditText edtSearchCard = view.findViewById(R.id.edt_search_card);

        viewModel = new ViewModelProvider(this).get(DeckDetailViewModel.class);
        CardViewModel cardViewModel = new ViewModelProvider(this).get(CardViewModel.class);

        // Nhận ID từ Bundle và load dữ liệu
        rvCards.setLayoutManager(new LinearLayoutManager(requireContext()));
        DeckCardAdapter cardAdapter = new DeckCardAdapter((card, position) -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("DECK_ID", card.getDeckId());
            bundle.putInt("SELECTED_POSITION", position);

            if (viewModel.getDeck().getValue() != null) {
                bundle.putString("DECK_NAME", viewModel.getDeck().getValue().getDeckName());
                bundle.putString("COVER_COLOR", viewModel.getDeck().getValue().getCoverColor());
            }

            androidx.navigation.Navigation.findNavController(view)
                    .navigate(R.id.cardDetailFragment, bundle);
        });
        rvCards.setAdapter(cardAdapter);

        // Lắng nghe Tên Deck
        viewModel.getDeck().observe(getViewLifecycleOwner(), deck -> {
            if (deck != null) {
                tvDeckName.setText(deck.getDeckName());
            }
        });

        // Lắng nghe Danh sách Card (Đã được search/load)
        cardViewModel.getDeckCards().observe(getViewLifecycleOwner(), cards -> {
            if (cards != null) {
                cardAdapter.setCards(cards);
            }
        });

        // Lấy ID từ Bundle, Load dữ liệu và gắn bộ Search (Gộp lại 1 lần duy nhất)
        if (getArguments() != null) {
            UUID deckId = (UUID) getArguments().getSerializable("DECK_ID");
            if (deckId != null) {
                viewModel.loadDeck(deckId); // Load tên Deck
                cardViewModel.loadCards(deckId); // Load danh sách Card (Dùng hàm mới)

                // Lắng nghe sự kiện gõ tìm kiếm
                if (edtSearchCard != null) {
                    edtSearchCard.addTextChangedListener(new android.text.TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            // Gọi hàm search khi text thay đổi
                            cardViewModel.searchCards(deckId, s.toString());
                        }

                        @Override
                        public void afterTextChanged(android.text.Editable s) {}
                    });
                }
            }
        }

        // Hiện Popup Menu 5 lựa chọn
        btnOptions.setOnClickListener(this::showPopupMenu);

        // Nút Back
        btnBack.setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(view).navigateUp();
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);

        popupMenu.getMenu().add(0, 1, 0, R.string.action_edit_deck_name);
        popupMenu.getMenu().add(0, 2, 1, R.string.action_share_deck);
        popupMenu.getMenu().add(0, 3, 2, R.string.action_edit_card_theme);
        popupMenu.getMenu().add(0, 4, 3, R.string.action_add_new_card);
        popupMenu.getMenu().add(0, 5, 4, R.string.action_delete_deck);
        popupMenu.getMenu().add(0, 6, 5, "Enter learn mode");
        popupMenu.getMenu().add(0, 7, 6, "Enter quiz mode");

        UUID currentDeckId = viewModel.getDeck().getValue() != null ?
                viewModel.getDeck().getValue().getDeckId() : null;
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    showEditNameDialog();
                    return true;
                case 2:
                    // TODO: Xử lý Share deck
                    Toast.makeText(requireContext(), "Share deck clicked", Toast.LENGTH_SHORT).show();
                    return true;
                case 3:
                    EditThemeDialog themeDialog = new EditThemeDialog();
                    themeDialog.show(getChildFragmentManager(), "EditThemeDialog");
                    return true;
                case 4:
                    // Chuyển sang trang CreateNewCardFragment
                    if (currentDeckId != null) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("DECK_ID", currentDeckId);
                        androidx.navigation.Navigation.findNavController(requireView())
                                .navigate(R.id.createNewCardFragment, bundle);
                    }
                    return true;
                case 5:
                    // Hiển thị Dialog xác nhận xóa
                    showDeleteConfirmDialog();
                    return true;
                case 6:
                    if (currentDeckId != null) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("DECK_ID", currentDeckId);
                        String currentDeckName = "";
                        if (viewModel.getDeck().getValue() != null) {
                            currentDeckName = viewModel.getDeck().getValue().getDeckName();
                        }
                        bundle.putString("DECK_NAME", currentDeckName);

                        androidx.navigation.Navigation.findNavController(requireView())
                                .navigate(R.id.cardLearnModeFragment, bundle);
                    }
                    return true;
                case 7:
                    // TODO: Xử lý Quiz mode
                    Toast.makeText(requireContext(), "Quiz mode clicked", Toast.LENGTH_SHORT).show();
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.show();
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.dialog_title_edit_deck_name);

        final EditText input = new EditText(requireContext());
        input.setText(tvDeckName.getText().toString());
        input.setHint(R.string.dialog_hint_deck_name);
        input.setPadding(50, 40, 50, 40);
        builder.setView(input);

        builder.setPositiveButton(R.string.action_save, (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {

                viewModel.updateDeckName(newName);
            }
        });
        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_title_delete_deck)
                .setMessage(R.string.dialog_message_delete_deck)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    // Xóa thông qua ViewModel
                     viewModel.deleteCurrentDeck();

                    // Back về LibraryFragment
                    androidx.navigation.Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}