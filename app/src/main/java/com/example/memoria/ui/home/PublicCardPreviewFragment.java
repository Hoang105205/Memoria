package com.example.memoria.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.memoria.R;
import com.example.memoria.data.model.entity.Card;
import com.example.memoria.ui.adapter.CardPagerAdapter;
import com.example.memoria.utils.PronunciationManager;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PublicCardPreviewFragment extends Fragment {

    private PublicDeckViewModel viewModel;
    private ViewPager2 viewPager;
    private CardPagerAdapter pagerAdapter;
    private ProgressBar progressBar;
    private PronunciationManager pronunciationManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_public_card_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel (Dùng chung với Activity để tận dụng instance nếu cần)
        viewModel = new ViewModelProvider(this).get(PublicDeckViewModel.class);
        pronunciationManager = new PronunciationManager(requireContext());

        // Ánh xạ View
        ImageButton btnBack = view.findViewById(R.id.btn_back_preview);
        TextView tvDeckName = view.findViewById(R.id.tv_deck_name_preview);
        viewPager = view.findViewById(R.id.view_pager_preview);
        progressBar = view.findViewById(R.id.progress_bar_preview);

        // Khởi tạo Adapter
        pagerAdapter = new CardPagerAdapter();

        // Gắn chức năng phát âm thanh (tái sử dụng từ CardPagerAdapter)
        pagerAdapter.setOnAudioPlayListener(textToRead -> {
            if (textToRead != null && !textToRead.isEmpty()) {
                pronunciationManager.playSound(textToRead, null, 1.0f);
            }
        });
        viewPager.setAdapter(pagerAdapter);

        // Bắt sự kiện Back
        btnBack.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigateUp());

        // Lấy dữ liệu từ Bundle
        if (getArguments() != null) {
            String publicDocId = getArguments().getString("PUBLIC_DOC_ID");
            String deckName = getArguments().getString("DECK_NAME");
            String coverColor = getArguments().getString("COVER_COLOR");

            tvDeckName.setText(deckName != null ? deckName : getString(R.string.preview_title));
            pagerAdapter.setThemeColor(coverColor);

            if (publicDocId != null) {
                // KHI NÀO MỚI CẦN LOAD LẠI API?
                // 1. Chưa có data nào cả
                // 2. Data đang có không thuộc về cái Deck đang mở (User vừa bấm sang Deck khác)
                boolean needToLoad = true;

                // So sánh trực tiếp ID muốn mở với ID đang lưu trong ViewModel
                if (publicDocId.equals(viewModel.currentPreviewDocId)) {
                    List<Card> currentCards = viewModel.previewCards.getValue();
                    if (currentCards != null && !currentCards.isEmpty()) {
                        needToLoad = false; // Đã có data và đang xem đúng bộ này
                    }
                }

                if (needToLoad) {
                    viewModel.clearPreviewCards();
                    viewModel.loadPreviewCards(publicDocId);
                }
            }
        }

        // Quan sát dữ liệu Loading
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            viewPager.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        });

        // Quan sát dữ liệu Card trả về
        viewModel.previewCards.observe(getViewLifecycleOwner(), cards -> {
            if (cards != null) {
                if (cards.isEmpty()) {
                    Toast.makeText(getContext(), R.string.msg_no_cards_preview, Toast.LENGTH_SHORT).show();
                } else {
                    pagerAdapter.setCards(cards);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pronunciationManager != null) pronunciationManager.releaseResources();
    }
}