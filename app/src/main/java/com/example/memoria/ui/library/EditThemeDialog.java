package com.example.memoria.ui.library;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.memoria.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

public class EditThemeDialog extends BottomSheetDialogFragment {

    private DeckDetailViewModel viewModel;
    private MaterialCardView cardPreview;
    private String selectedColor = "";

    // 10 màu chủ đạo + 1 nút Không màu (chuỗi rỗng)

    private final String[] THEME_COLORS = {
            "",           // No color / mặc định
            "#F44336",    // Red (Đỏ)
            "#E91E63",    // Pink (Hồng)
            "#9C27B0",    // Purple (Tím)
            "#673AB7",    // Deep Purple (Tím đậm)
            "#3F51B5",    // Indigo (Chàm)
            "#2196F3",    // Blue (Xanh dương)
            "#00BCD4",    // Cyan (Xanh ngọc)
            "#4CAF50",    // Green (Xanh lá)
            "#FFEB3B",    // Yellow (Vàng)
            "#FF9800"     // Orange (Cam)
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_theme, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Dùng requireParentFragment() để lấy đúng ViewModel của DeckDetailFragment đang mở nó
        viewModel = new ViewModelProvider(requireParentFragment()).get(DeckDetailViewModel.class);

        cardPreview = view.findViewById(R.id.card_preview);
        LinearLayout llColorPalette = view.findViewById(R.id.ll_color_palette);
        Button btnSave = view.findViewById(R.id.btn_save_theme);

        // Load màu hiện tại của Deck (nếu có)
        if (viewModel.getDeck().getValue() != null && viewModel.getDeck().getValue().getCoverColor() != null) {
            selectedColor = viewModel.getDeck().getValue().getCoverColor();
            updatePreview();
        }

        // Tạo động các nút hình tròn để chọn màu
        for (String colorHex : THEME_COLORS) {
            View colorCircle = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 120);
            params.setMargins(16, 0, 16, 0);
            colorCircle.setLayoutParams(params);

            // Vẽ hình tròn
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            if (colorHex.isEmpty()) {
                shape.setColor(Color.WHITE);
                shape.setStroke(4, Color.LTGRAY); // Viền xám cho nút "Không màu"
            } else {
                shape.setColor(Color.parseColor(colorHex));
            }
            colorCircle.setBackground(shape);

            // Sự kiện click chọn màu
            colorCircle.setOnClickListener(v -> {
                selectedColor = colorHex;
                updatePreview();
            });

            llColorPalette.addView(colorCircle);
        }

        btnSave.setOnClickListener(v -> {
            viewModel.updateDeckTheme(selectedColor);
            dismiss();
        });
    }

    private void updatePreview() {
        if (selectedColor == null || selectedColor.isEmpty()) {
            cardPreview.setStrokeWidth(0);
        } else {
            cardPreview.setStrokeColor(Color.parseColor(selectedColor));
            cardPreview.setStrokeWidth(12); // Tương đương khoảng 4dp
        }
    }
}