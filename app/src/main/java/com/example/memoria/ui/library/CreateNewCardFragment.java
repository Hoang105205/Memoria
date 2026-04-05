package com.example.memoria.ui.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.memoria.R;
import com.example.memoria.data.model.entity.Card;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateNewCardFragment extends Fragment {

    private CardViewModel viewModel;

    private UUID deckId;
    private String deckName;
    private Card cardToEdit = null;

    private String selectedImageUri = null;
    private int currentCardType = 0; // 0: Text, 1: Image, 2: Audio (TTS)

    private View layoutFront, layoutBack;
    private CardView cardContainer;
    private ImageButton btnFlip;
    private boolean isFront = true;
    private boolean flippable = true;

    // Front UI
    private RadioGroup rgCardType;
    private EditText etFrontText;
    private ImageView imgFrontPicker;

    // Back UI
    private LinearLayout containerMeanings;
    private Button btnAddMeaning;

    private final String[] MEANING_TYPES = new String[]{"noun", "verb", "adj", "adv", "prep", "conj", "idiom"};

    private final ActivityResultLauncher<String[]> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    takePersistableUriPermission(uri);
                    selectedImageUri = uri.toString();
                    Glide.with(requireContext()).load(uri).fitCenter().into(imgFrontPicker);
                }
            }
    );

    private void takePersistableUriPermission(Uri uri) {
        requireActivity().getContentResolver().takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CardViewModel.class);
        return inflater.inflate(R.layout.fragment_create_new_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            deckId = (UUID) getArguments().getSerializable("DECK_ID");
            deckName = getArguments().getString("DECK_NAME");
            if (getArguments().containsKey("EDIT_CARD")) {
                cardToEdit = (Card) getArguments().getSerializable("EDIT_CARD");
            }
        }

        initViews(view);
        setupCardTypeListener();
        setupFlipAnimation();
        loadDataToUI();

        imgFrontPicker.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));
        btnAddMeaning.setOnClickListener(v -> addNewMeaningField("", ""));
        view.findViewById(R.id.btn_back).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        view.findViewById(R.id.btn_create_card).setOnClickListener(v -> saveCard());

        TextView tvDeckName = view.findViewById(R.id.tv_deck_name_header);
        tvDeckName.setText(deckName);
    }

    private void initViews(View view) {
        cardContainer = view.findViewById(R.id.card_creator_container);
        layoutFront = view.findViewById(R.id.layout_creator_front);
        layoutBack = view.findViewById(R.id.layout_creator_back);
        btnFlip = view.findViewById(R.id.btn_flip_card);

        rgCardType = view.findViewById(R.id.rg_card_type);
        etFrontText = view.findViewById(R.id.et_front_text);
        imgFrontPicker = view.findViewById(R.id.img_front_picker);

        containerMeanings = view.findViewById(R.id.container_meanings);
        btnAddMeaning = view.findViewById(R.id.btn_add_meaning);
    }

    private void setupCardTypeListener() {
        rgCardType.setOnCheckedChangeListener((group, checkedId) -> {
            imgFrontPicker.setVisibility(View.GONE);

            if (checkedId == R.id.rb_type_text) {
                currentCardType = 0;
                etFrontText.setHint(R.string.front_text_hint);
            }
            else if (checkedId == R.id.rb_type_image) {
                currentCardType = 1;
                etFrontText.setHint(R.string.front_text_image_hint);
                imgFrontPicker.setVisibility(View.VISIBLE);
            }
            else if (checkedId == R.id.rb_type_audio) {
                currentCardType = 2;
                etFrontText.setHint(R.string.front_audio_hint);
            }
        });
    }

    private void loadDataToUI() {
        if (cardToEdit != null) {
            Button btnCreate = requireView().findViewById(R.id.btn_create_card);
            btnCreate.setText(R.string.action_save);

            currentCardType = cardToEdit.getCardType();

            etFrontText.setVisibility(View.VISIBLE);
            etFrontText.setText(cardToEdit.getFrontText());

            if (currentCardType == 0) {
                rgCardType.check(R.id.rb_type_text);
            } else if (currentCardType == 1) {
                rgCardType.check(R.id.rb_type_image);
                selectedImageUri = cardToEdit.getFrontImage();
                Glide.with(requireContext()).load(selectedImageUri).fitCenter().into(imgFrontPicker);
            } else if (currentCardType == 2) {
                rgCardType.check(R.id.rb_type_audio);
            }

            containerMeanings.removeAllViews();
            List<String> meanings = cardToEdit.getBackMeanings();
            List<String> types = cardToEdit.getBackTypes();

            if (meanings != null && !meanings.isEmpty()) {
                for (int i = 0; i < meanings.size(); i++) {
                    String meaning = meanings.get(i);
                    String type = (types != null && i < types.size()) ? types.get(i) : "";
                    addNewMeaningField(type, meaning);
                }
            } else {
                addNewMeaningField("", "");
            }
        } else {
            addNewMeaningField("", "");
        }
    }

    private void addNewMeaningField(String initialType, String initialMeaning) {
        View meaningView = getLayoutInflater().inflate(R.layout.item_meaning_input, containerMeanings, false);
        TextView tvLabel = meaningView.findViewById(R.id.tv_meaning_label);
        AutoCompleteTextView autoType = meaningView.findViewById(R.id.auto_meaning_type);
        EditText etInput = meaningView.findViewById(R.id.et_meaning_input);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, MEANING_TYPES);
        autoType.setAdapter(adapter);

        int currentCount = containerMeanings.getChildCount() + 1;
        tvLabel.setText(getString(R.string.meaning_label, currentCount));

        autoType.setText(initialType);
        etInput.setText(initialMeaning);

        containerMeanings.addView(meaningView);
    }

    private void saveCard() {
        String frontText = etFrontText.getText().toString().trim();
        List<String> finalMeanings = new ArrayList<>();
        List<String> finalTypes = new ArrayList<>();

        for (int i = 0; i < containerMeanings.getChildCount(); i++) {
            View child = containerMeanings.getChildAt(i);
            AutoCompleteTextView autoType = child.findViewById(R.id.auto_meaning_type);
            EditText etInput = child.findViewById(R.id.et_meaning_input);

            String meaningText = etInput.getText().toString().trim();
            String typeText = autoType.getText().toString().trim();

            if (!meaningText.isEmpty()) {
                finalMeanings.add(meaningText);
                finalTypes.add(typeText.isEmpty() ? "general" : typeText);
            }
        }

        if (finalMeanings.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_empty_card, Toast.LENGTH_SHORT).show();
            return;
        }

        if (frontText.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_empty_card, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentCardType == 1 && selectedImageUri == null) {
            Toast.makeText(requireContext(), R.string.error_empty_image, Toast.LENGTH_SHORT).show();
            return;
        }

        Card newCard = (cardToEdit != null) ? cardToEdit : new Card();
        if (cardToEdit == null) {
            newCard.setCardId(UUID.randomUUID());
            newCard.setDeckId(deckId);
            newCard.setCreatedAt(new Date());
        } else {
            newCard.setUpdatedAt(new Date());
        }

        newCard.setCardType(currentCardType);
        newCard.setFrontText(frontText);

        if (currentCardType == 1) {
            newCard.setFrontImage(selectedImageUri);
        } else {
            newCard.setFrontImage(null);
        }

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
            if (!flippable) return;
            flippable = false;

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
                    flippable = true;
                }
            });

            flipOut.start();
        });
    }
}