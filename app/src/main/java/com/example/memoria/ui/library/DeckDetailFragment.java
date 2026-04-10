package com.example.memoria.ui.library;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.entity.Deck;
import com.example.memoria.ui.adapter.DeckCardAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DeckDetailFragment extends Fragment {
    private DeckDetailViewModel deckDetailViewModel;
    private SharedDeckViewModel sharedDeckViewModel;
    private TextView tvDeckName;
    private ProgressDialog progressDialog; // Để hiển thị vòng xoay loading

    private ProgressBar exportProgressBar = null;
    private Button btnExportGenerate = null;

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

        FloatingActionButton btnLearn = view.findViewById(R.id.btn_start_learn);
        FloatingActionButton btnQuiz = view.findViewById(R.id.btn_start_quiz);

        deckDetailViewModel = new ViewModelProvider(this).get(DeckDetailViewModel.class);
        sharedDeckViewModel = new ViewModelProvider(this).get(SharedDeckViewModel.class);

        CardViewModel cardViewModel = new ViewModelProvider(this).get(CardViewModel.class);

        // Nhận ID từ Bundle và load dữ liệu
        rvCards.setLayoutManager(new LinearLayoutManager(requireContext()));
        DeckCardAdapter cardAdapter = new DeckCardAdapter((card, position) -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("DECK_ID", card.getDeckId());
            bundle.putSerializable("CARD_ID", card.getCardId()); // Truyền ID của thẻ thay vì chỉ truyền vị trí
            bundle.putInt("SELECTED_POSITION", position);

            if (deckDetailViewModel.getDeck().getValue() != null) {
                bundle.putString("DECK_NAME", deckDetailViewModel.getDeck().getValue().getDeckName());
                bundle.putString("COVER_COLOR", deckDetailViewModel.getDeck().getValue().getCoverColor());
            }

            androidx.navigation.Navigation.findNavController(view)
                    .navigate(R.id.cardDetailFragment, bundle);
        });
        rvCards.setAdapter(cardAdapter);

        // Lắng nghe Tên Deck
        deckDetailViewModel.getDeck().observe(getViewLifecycleOwner(), deck -> {
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
                deckDetailViewModel.loadDeck(deckId); // Load tên Deck
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

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage(getString(R.string.msg_publishing_deck));
        progressDialog.setCancelable(false); // Không cho bấm ra ngoài để hủy

        // Lắng nghe trạng thái Publishing
        deckDetailViewModel.getIsPublishing().observe(getViewLifecycleOwner(), isPublishing -> {
            try {
                if (isPublishing) {
                    if (!progressDialog.isShowing()) {
                        progressDialog.show();
                    }
                } else {
                    // Loại bỏ điều kiện isShowing() vì Dialog có thể bị lệch state với Window Manager
                    progressDialog.dismiss();
                }
            } catch (IllegalArgumentException e) {
                // Bắt lỗi rò rỉ cửa sổ (Window Leak) nếu fragment/activity đã bị hủy trước khi dismiss
                e.printStackTrace();
            }
        });

        // Lắng nghe Message trả về
        deckDetailViewModel.getPublishMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                deckDetailViewModel.clearPublishMessage(); // Xóa message để tránh bị hiện Toast lại khi xoay màn hình
            }
        });

        sharedDeckViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (exportProgressBar != null && btnExportGenerate != null) {
                exportProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                btnExportGenerate.setEnabled(!isLoading);
            }
        });

        // Hiện Popup Menu 5 lựa chọn
        btnOptions.setOnClickListener(this::showPopupMenu);

        // Nút Back
        btnBack.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(view).navigateUp());

        btnLearn.setOnClickListener(v -> enterLearnFlashcardMode());

        btnQuiz.setOnClickListener(v -> enterQuizMode());
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);

        popupMenu.getMenu().add(0, 1, 0, R.string.action_edit_deck_name);
        popupMenu.getMenu().add(0, 2, 1, R.string.action_share_deck);
        popupMenu.getMenu().add(0, 3, 2, R.string.action_publish_deck);
        popupMenu.getMenu().add(0, 4, 3, R.string.action_edit_card_theme);
        popupMenu.getMenu().add(0, 5, 4, R.string.action_add_new_card);
        popupMenu.getMenu().add(0, 6, 5, R.string.action_delete_deck);

        UUID currentDeckId = deckDetailViewModel.getDeck().getValue() != null ?
                deckDetailViewModel.getDeck().getValue().getDeckId() : null;
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    showEditNameDialog();
                    return true;
                case 2:
                    showExportDialog();
                    return true;
                case 3:
                    showPublishDialog();
                    return true;
                case 4:
                    EditThemeDialog themeDialog = new EditThemeDialog();
                    themeDialog.show(getChildFragmentManager(), "EditThemeDialog");
                    return true;
                case 5:
                    // Chuyển sang trang CreateNewCardFragment
                    if (currentDeckId != null) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("DECK_ID", currentDeckId);
                        androidx.navigation.Navigation.findNavController(requireView())
                                .navigate(R.id.createNewCardFragment, bundle);
                    }
                    return true;
                case 6:
                    // Hiển thị Dialog xác nhận xóa
                    showDeleteConfirmDialog();
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

                deckDetailViewModel.updateDeckName(newName);
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
                     deckDetailViewModel.deleteCurrentDeck();

                    // Back về LibraryFragment
                    androidx.navigation.Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showPublishDialog(){
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.action_publish_deck)
                .setMessage(R.string.publish_deck_dialog_content)
                .setPositiveButton(R.string.action_publish, (dialog, which) -> {
                    deckDetailViewModel.publishCurrentDeck();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showExportDialog() {
        Deck currentDeck = deckDetailViewModel.getDeck().getValue();
        if (currentDeck == null) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_export_deck, null);

        TextView tvMessage = dialogView.findViewById(R.id.tv_export_message);
        View layoutCodeContainer = dialogView.findViewById(R.id.layout_code_container);
        TextView tvCode = dialogView.findViewById(R.id.tv_share_code);
        ImageButton btnCopy = dialogView.findViewById(R.id.btn_copy_code);

        exportProgressBar = dialogView.findViewById(R.id.pb_export_loading);
        btnExportGenerate = dialogView.findViewById(R.id.btn_dialog_generate);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);

        String currentCode = currentDeck.getShareCode();
        Date sharedDate = currentDeck.getSharedAt();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());

        if (currentCode != null && !currentCode.isEmpty() && sharedDate != null) {
            tvMessage.setText(getString(R.string.dialog_export_already_shared, sdf.format(sharedDate)));
            tvCode.setText(currentCode);
            layoutCodeContainer.setVisibility(View.VISIBLE);
        } else {
            tvMessage.setText(R.string.dialog_export_not_shared);
            layoutCodeContainer.setVisibility(View.GONE);
        }

        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Share Code", tvCode.getText().toString());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), getString(R.string.msg_copy_success, tvCode.getText().toString()), Toast.LENGTH_SHORT).show();
            }
        });

        androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialog.setOnDismissListener(d -> {
            exportProgressBar = null;
            btnExportGenerate = null;
        });

        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnExportGenerate.setOnClickListener(v -> {
            String localDeckId = currentDeck.getDeckId().toString();

            sharedDeckViewModel.exportDeckToCloud(localDeckId, (success, shareCode, message) -> {
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (success) {
                            deckDetailViewModel.updateShareCodeToLocal(shareCode);
                            tvCode.setText(shareCode);
                            layoutCodeContainer.setVisibility(View.VISIBLE);
                            tvMessage.setText(getString(R.string.dialog_export_already_shared, sdf.format(new Date())));

                            Toast.makeText(requireContext(), R.string.msg_export_deck_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("ExportError", "Lỗi từ hệ thống: " + message);
                            Toast.makeText(requireContext(), getString(R.string.msg_share_deck_error), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        });
    }

    private void enterLearnFlashcardMode()
    {
        UUID currentDeckId = deckDetailViewModel.getDeck().getValue() != null ?
                deckDetailViewModel.getDeck().getValue().getDeckId() : null;

        if (currentDeckId != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("DECK_ID", currentDeckId);
            String currentDeckName = "";
            if (deckDetailViewModel.getDeck().getValue() != null) {
                currentDeckName = deckDetailViewModel.getDeck().getValue().getDeckName();
            }
            bundle.putString("DECK_NAME", currentDeckName);

            androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(R.id.cardLearnModeFragment, bundle);
        }
    }

    private void enterQuizMode()
    {
        UUID currentDeckId = deckDetailViewModel.getDeck().getValue() != null ?
                deckDetailViewModel.getDeck().getValue().getDeckId() : null;

        if (currentDeckId != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("DECK_ID", currentDeckId);
            String currentDeckName = "";
            if (deckDetailViewModel.getDeck().getValue() != null) {
                currentDeckName = deckDetailViewModel.getDeck().getValue().getDeckName();
            }
            bundle.putString("DECK_NAME", currentDeckName);

            androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(R.id.cardQuizModeFragment, bundle);
        }
    }
}