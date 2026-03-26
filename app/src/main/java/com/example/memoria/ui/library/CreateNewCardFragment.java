package com.example.memoria.ui.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.memoria.R;
import com.example.memoria.data.model.Card;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

// Thu viện hỗ trợ lấy ảnh từ Gallery
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

@AndroidEntryPoint
public class CreateNewCardFragment extends Fragment {

    private CardViewModel viewModel;

    private UUID deckId;
    private String deckName;

    // Xử lý Edit và Image
    private Card cardToEdit = null;
    private String selectedImageUri = null;

    private View layoutFront, layoutBack;
    private CardView cardContainer;
    private ImageButton btnFlip;
    private boolean isFront = true;
    private boolean flipable = true;

    // Front UI
    private EditText etFrontText;
    private ImageView imgFront;

    // Back UI
    private LinearLayout containerMeanings;
    private Button btnAddMeaning;

    // --- Khai báo bộ ảnh chọn từ thư viện ---
    private final ActivityResultLauncher<String[]> pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.OpenDocument(),
        uri -> {
            if (uri != null) {
                requireActivity().getContentResolver().takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                );

                selectedImageUri = uri.toString();
                Glide.with(requireContext())
                        .load(Uri.parse(selectedImageUri))
                        .fitCenter()
                        .into(imgFront);
            }
        }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CardViewModel.class);
        return inflater.inflate(R.layout.fragment_create_new_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // xác định xem đang là mode edit hay create
        if (getArguments() != null) {
            deckId = (UUID) getArguments().getSerializable("DECK_ID");
            deckName = getArguments().getString("DECK_NAME");
            if (getArguments().containsKey("EDIT_CARD")) {
                cardToEdit = (Card) getArguments().getSerializable("EDIT_CARD");
            }
        }

        if (getArguments() != null) {
            deckId = (UUID) getArguments().getSerializable("DECK_ID");
        }

        initViews(view);
        setupFlipAnimation();
        loadDataToUI(); // Tự động điền dữ liệu nếu là Edit, hoặc tạo field rỗng nếu Create

        btnAddMeaning.setOnClickListener(v -> addNewMeaningField());

        Button btnCreate = view.findViewById(R.id.btn_create_card);
        btnCreate.setOnClickListener(v -> saveCard());

        TextView tvDeckName = view.findViewById(R.id.tv_deck_name_header);
        tvDeckName.setText(deckName);

        view.findViewById(R.id.btn_back).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Bấm vào icon ảnh để mở thư viện
        imgFront.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));
    }

    private void initViews(View view) {
        cardContainer = view.findViewById(R.id.card_creator_container);
        layoutFront = view.findViewById(R.id.layout_creator_front);
        layoutBack = view.findViewById(R.id.layout_creator_back);
        btnFlip = view.findViewById(R.id.btn_flip_card);

        etFrontText = view.findViewById(R.id.et_front_text);
        imgFront = view.findViewById(R.id.img_front_picker);

        containerMeanings = view.findViewById(R.id.container_meanings);
        btnAddMeaning = view.findViewById(R.id.btn_add_meaning);
    }

    // điền dữ liệu cũ nếu đang là mode edit
    private void loadDataToUI() {
        if (cardToEdit != null) {
            // Đổi chữ nút Create thành Save
            Button btnCreate = requireView().findViewById(R.id.btn_create_card);
            btnCreate.setText(R.string.action_save);

            // Điền mặt trước
            etFrontText.setText(cardToEdit.getFrontText());
            if (cardToEdit.getFrontImage() != null) {
                selectedImageUri = cardToEdit.getFrontImage();
                Glide.with(requireContext())
                        .load(Uri.parse(selectedImageUri))
                        .fitCenter() // Tự động cắt ảnh cho đẹp mắt
                        .into(imgFront);
            }

            // Điền mặt sau (Meanings)
            containerMeanings.removeAllViews();
            if (cardToEdit.getBackMeanings() != null && !cardToEdit.getBackMeanings().isEmpty()) {
                for (String meaning : cardToEdit.getBackMeanings()) {
                    View meaningView = getLayoutInflater().inflate(R.layout.item_meaning_input, containerMeanings, false);
                    TextView tvLabel = meaningView.findViewById(R.id.tv_meaning_label);
                    EditText etInput = meaningView.findViewById(R.id.et_meaning_input);

                    int currentCount = containerMeanings.getChildCount() + 1;
                    tvLabel.setText(getString(R.string.meaning_label, currentCount));
                    etInput.setText(meaning);

                    containerMeanings.addView(meaningView);
                }
            } else {
                addNewMeaningField();
            }
        } else {
            // Create Mode mặc định
            addNewMeaningField();
        }
    }

    private void addNewMeaningField() {
        View meaningView = getLayoutInflater().inflate(R.layout.item_meaning_input, containerMeanings, false);
        TextView tvLabel = meaningView.findViewById(R.id.tv_meaning_label);

        int currentCount = containerMeanings.getChildCount() + 1;
        tvLabel.setText(getString(R.string.meaning_label, currentCount));

        containerMeanings.addView(meaningView);
    }

    private void saveCard() {
        String frontText = etFrontText.getText().toString().trim();
        List<String> finalMeanings = new ArrayList<>();
        List<String> finalTypes = new ArrayList<>();

        for (int i = 0; i < containerMeanings.getChildCount(); i++) {
            View child = containerMeanings.getChildAt(i);
            EditText etInput = child.findViewById(R.id.et_meaning_input);
            String text = etInput.getText().toString().trim();
            if (!text.isEmpty()) {
                finalMeanings.add(text);
                finalTypes.add("general");
            }
        }

        if (frontText.isEmpty() || finalMeanings.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_empty_card, Toast.LENGTH_SHORT).show();
            return;
        }

        Card newCard;
        if (cardToEdit != null) {
            newCard = cardToEdit; // Dùng lại ID cũ để Update
            newCard.setUpdatedAt(new Date());
        } else {
            newCard = new Card();
            newCard.setCardId(UUID.randomUUID()); // ID mới để Insert
            newCard.setDeckId(deckId);
            newCard.setCreatedAt(new Date());
        }

        newCard.setFrontText(frontText);
        newCard.setFrontImage(selectedImageUri); // Gắn ảnh vào thẻ
        newCard.setBackMeanings(finalMeanings);
        newCard.setBackTypes(finalTypes);

        if (cardToEdit != null) {
            viewModel.updateCard(newCard);
            Toast.makeText(requireContext(), R.string.card_update_success, Toast.LENGTH_SHORT).show();
        } else {
            viewModel.insertCard(newCard);
            Toast.makeText(requireContext(), R.string.card_add_success, Toast.LENGTH_SHORT).show();
        }

        Navigation.findNavController(requireView()).navigateUp();
    }

    private void setupFlipAnimation() {
        btnFlip.setOnClickListener(v -> {
            if (!flipable) return;
            flipable = false;

            ObjectAnimator flipOut = ObjectAnimator.ofFloat(cardContainer, "rotationY", 0f, -90f);
            flipOut.setDuration(150);
            ObjectAnimator flipIn = ObjectAnimator.ofFloat(cardContainer, "rotationY", 90f, 0f);
            flipIn.setDuration(150);

            flipOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (isFront) {
                        layoutFront.setVisibility(View.GONE);
                        layoutBack.setVisibility(View.VISIBLE);
                    } else {
                        layoutFront.setVisibility(View.VISIBLE);
                        layoutBack.setVisibility(View.GONE);
                    }
                    isFront = !isFront;
                    flipIn.start();
                }
            });

            flipIn.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    flipable = true;
                }
            });

            flipOut.start();
        });
    }
}