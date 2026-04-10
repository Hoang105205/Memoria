package com.example.memoria.ui.library;

import android.app.ProgressDialog;
import android.app.AlertDialog;
//import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.entity.Card;
import com.example.memoria.data.model.entity.Deck;
import com.example.memoria.ui.adapter.DeckAdapter;
import com.example.memoria.utils.GeminiHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DeckListFragment extends Fragment {
    private LibraryViewModel libraryViewModel;
    private SharedDeckViewModel sharedDeckViewModel;
    private DeckAdapter deckAdapter;

    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deck_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);
        sharedDeckViewModel = new ViewModelProvider(requireActivity()).get(SharedDeckViewModel.class);

        RecyclerView rvDeckList = view.findViewById(R.id.rv_deck_list);
        FloatingActionButton btnAdd = view.findViewById(R.id.btn_deck_add);
        FloatingActionButton btnAddAi = view.findViewById(R.id.btn_deck_add_ai);
        FloatingActionButton btnImport = view.findViewById(R.id.btn_deck_import);

        rvDeckList.setLayoutManager(new LinearLayoutManager(getContext()));

        deckAdapter = new DeckAdapter(deck -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("DECK_ID", deck.getDeckId());
            androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(R.id.deckDetailFragment, bundle);
        });
        rvDeckList.setAdapter(deckAdapter);

        // Lắng nghe dữ liệu
        libraryViewModel.getDecks().observe(getViewLifecycleOwner(), decks -> {
            deckAdapter.setDecks(decks);
        });

        // Progress pop-up when importing
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage(getString(R.string.msg_import_loading));
        progressDialog.setCancelable(false);

        sharedDeckViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                if (!progressDialog.isShowing()) progressDialog.show();
            } else {
                progressDialog.dismiss();
            }
        });

        btnAdd.setOnClickListener(v -> showAddDialog());

        btnAddAi.setOnClickListener(v -> showDeckCreatedByAIDialog());

        btnImport.setOnClickListener(v -> showCustomImportDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (libraryViewModel != null) {
            libraryViewModel.loadDecks();
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
                libraryViewModel.addNewDeck(newDeck);
            }
        });

        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeckCreatedByAIDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Nạp layout XML
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_deck_ai, null);
        builder.setView(dialogView);

        EditText edtTopic = dialogView.findViewById(R.id.edt_ai_topic);
        EditText edtCount = dialogView.findViewById(R.id.edt_ai_card_count);

        // Thiết lập các nút (khoan gắn sự kiện logic cho nút Positive để tránh Dialog tự đóng khi validate sai)
        builder.setPositiveButton(R.string.action_create, null);
        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Ghi đè sự kiện click của nút "Tạo" sau khi show dialog để kiểm tra dữ liệu
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String topic = edtTopic.getText().toString().trim();
            String countStr = edtCount.getText().toString().trim();

            // Kiểm tra trường chủ đề (Bắt buộc)
            if (TextUtils.isEmpty(topic)) {
                edtTopic.setError(getString(R.string.error_topic_required));
                edtTopic.requestFocus();
                return; // Dừng lại, không đóng dialog
            }

            // Xử lý trường số lượng (Mặc định 10, tối đa 20)
            int cardCount = 10;
            if (!TextUtils.isEmpty(countStr)) {
                try {
                    cardCount = Integer.parseInt(countStr);
                } catch (NumberFormatException e) {
                    cardCount = 10;
                }
            }

            if (cardCount > 20) {
                edtCount.setError(getString(R.string.error_card_count_max));
                edtCount.requestFocus();
                return; // Dừng lại, không đóng dialog
            }

            dialog.dismiss();

            ProgressDialog progressDialog = new ProgressDialog(requireContext());
            progressDialog.setMessage(getString(R.string.msg_ai_deck_loading, cardCount, topic));
            progressDialog.setCancelable(false); // Khóa màn hình, không cho bấm ra ngoài hủy
            progressDialog.show();

            GeminiHelper geminiHelper = new GeminiHelper();
            geminiHelper.generateAIDeck(topic, cardCount, new GeminiHelper.GeminiDeckCallback() {
                @Override
                public void onSuccess(List<GeminiHelper.AICardResponse> generatedCards) {
                    // Tạo Entity Deck
                    Deck newDeck = new Deck();
                    newDeck.setDeckId(UUID.randomUUID());
                    newDeck.setDeckName(topic); // Đặt tên Deck bằng chủ đề user nhập
                    newDeck.setCreatedAt(new Date());

                    // Ánh xạ sang danh sách Entity Card
                    List<Card> cardsToSave = new ArrayList<>();
                    for (GeminiHelper.AICardResponse aiCard : generatedCards) {
                        Card card = new Card();
                        card.setCardId(UUID.randomUUID());
                        card.setDeckId(newDeck.getDeckId()); // Nối vào Deck vừa tạo
                        card.setCardType(0); // 0: Mặt trước chỉ có text
                        card.setFrontText(aiCard.frontText);
                        card.setBackTypes(aiCard.backTypes);
                        card.setBackMeanings(aiCard.backMeanings);

                        // Cấu hình các thông số mặc định cho thuật toán học
                        card.setCreatedAt(new Date());
                        card.setEaseFactor(2.5);
                        card.setIntervalDays(0);
                        card.setReviewCount(0);
                        card.setSyncStatus(0);

                        cardsToSave.add(card);
                    }

                    // Đẩy qua ViewModel để lưu vào SQLite
                    libraryViewModel.saveAIDeckWithCards(newDeck, cardsToSave, () -> {
                        if (isAdded() && getActivity() != null) {
                            requireActivity().runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(requireContext(), getString(R.string.msg_ai_deck_success, topic), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }

                @Override
                public void onError(Throwable t) {
                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(requireContext(), getString(R.string.msg_ai_deck_error, t.getMessage()), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });
        });
    }

    private void showCustomImportDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_import_deck, null);
        EditText edtImportCode = dialogView.findViewById(R.id.edt_import_code);
        Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        Button btnDownload = dialogView.findViewById(R.id.btn_dialog_download);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDownload.setOnClickListener(v -> {
            String code = edtImportCode.getText() != null ? edtImportCode.getText().toString().trim() : "";
            if (code.isEmpty()) {
                edtImportCode.setError(getString(R.string.error_empty_export_code));
                edtImportCode.requestFocus();
            } else {
                dialog.dismiss();

                sharedDeckViewModel.importDeckFromCode(code, (success, newDeckId, message) -> {
                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(requireContext(), getString(R.string.msg_import_deck_success), Toast.LENGTH_SHORT).show();
                                libraryViewModel.loadDecks();
                            } else {
                                Log.e("ImportError", "Lỗi từ hệ thống: " + message);
                                Toast.makeText(requireContext(), getString(R.string.msg_share_deck_error), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        });
    }
}