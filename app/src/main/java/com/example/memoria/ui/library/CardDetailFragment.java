package com.example.memoria.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.memoria.R;
import com.example.memoria.data.model.entity.Card;
import com.example.memoria.ui.adapter.CardPagerAdapter;
import com.example.memoria.utils.PronunciationManager;

import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CardDetailFragment extends Fragment {
    private UUID targetCardId;
    private PronunciationManager pronunciationManager;
    private CardViewModel viewModel;
    private ViewPager2 viewPager;
    private CardPagerAdapter pagerAdapter;
    private UUID deckId;
    private String deckName;
    private int startPosition;
    private String coverColor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_detail_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            deckId = (UUID) getArguments().getSerializable("DECK_ID");
            deckName = getArguments().getString("DECK_NAME");
            startPosition = getArguments().getInt("SELECTED_POSITION", 0);
            coverColor = getArguments().getString("COVER_COLOR", "");
            targetCardId = (UUID) getArguments().getSerializable("CARD_ID");
        }

        pronunciationManager = new PronunciationManager(requireContext());

        viewPager = view.findViewById(R.id.view_pager_cards);
        ImageButton btnOptions = view.findViewById(R.id.btn_card_detail_options);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigateUp());
        }

        pagerAdapter = new CardPagerAdapter();
        pagerAdapter.setThemeColor(coverColor);

        pagerAdapter.setOnAudioPlayListener(textToRead -> {
            if (textToRead != null && !textToRead.isEmpty()) {
                pronunciationManager.playSound(textToRead, null, 1.0f);
            }
        });

        viewPager.setAdapter(pagerAdapter);

        viewModel = new ViewModelProvider(this).get(CardViewModel.class);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                startPosition = position;

                if (getArguments() != null) {
                    getArguments().putInt("SELECTED_POSITION", position);
                }
            }
        });

        if (deckId != null) {
            // Lắng nghe dữ liệu Card trả về từ DB
            viewModel.getCardsByDeckId(deckId).observe(getViewLifecycleOwner(), cards -> {
                if (cards != null && !cards.isEmpty()) {
                    pagerAdapter.setCards(cards);

                    // Tìm chính xác vị trí của thẻ bằng ID
                    if (targetCardId != null) {
                        for (int i = 0; i < cards.size(); i++) {
                            if (cards.get(i).getCardId().equals(targetCardId)) {
                                startPosition = i;
                                targetCardId = null; // Xóa đi để tránh tìm lại vào những lần update sau

                                if (getArguments() != null) {
                                    getArguments().remove("CARD_ID");
                                }

                                break;
                            }
                        }
                    }

                    // Nhảy đến đúng vị trí thẻ mà người dùng đã click ngoài danh sách
                    viewPager.setCurrentItem(startPosition, false);
                }
            });
        }

        TextView tvDeckName = view.findViewById(R.id.tv_deck_name_header);
        tvDeckName.setText(deckName);

        btnOptions.setOnClickListener(this::showPopupMenu);
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenu().add(0, 1, 0, R.string.edit_card);
        popupMenu.getMenu().add(0, 2, 1, R.string.action_delete_card);

        popupMenu.setOnMenuItemClickListener(item -> {
            Card currentCard = pagerAdapter.getCardAt(viewPager.getCurrentItem());
            if (currentCard == null) return false;

            if (item.getItemId() == 1) {
                // open edit mode
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("DECK_ID", deckId);
                    bundle.putString("DECK_NAME", deckName);
                    bundle.putSerializable("EDIT_CARD", currentCard); // Truyền thẻ cần edit sang
                    androidx.navigation.Navigation.findNavController(view)
                            .navigate(R.id.createNewCardFragment, bundle);
                return true;
            } else if (item.getItemId() == 2) {
                // open delete confirm dialog
                showDeleteConfirmDialog(currentCard);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showDeleteConfirmDialog(Card card) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_title_delete_card)
                .setMessage(R.string.dialog_message_delete_card)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    viewModel.deleteCard(card);
                    // Xóa xong thì quay về trang chi tiết Deck
                    androidx.navigation.Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pronunciationManager != null) pronunciationManager.releaseResources();
    }
}