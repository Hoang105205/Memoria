package com.example.memoria.ui.quiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.memoria.R;
import com.example.memoria.utils.SoundManager;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuizResultFragment extends Fragment {

    private CircularProgressIndicator progressOverall;
    private TextView tvScoreNumber, tvReviewList;
    private ImageButton btnClose;
    private AppCompatButton btnTestAgain, btnBackToLibrary;

    private UUID currentDeckId;
    private String currentDeckName;

    @Inject
    SoundManager soundManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadScoreData();
        soundManager.playSound(SoundManager.SoundEvent.COMPLETE_QUIZ);
        setupClickListeners();
    }

    private void initViews(View view) {
        progressOverall = view.findViewById(R.id.progressOverall);
        tvScoreNumber = view.findViewById(R.id.tvScoreNumber);
        tvReviewList = view.findViewById(R.id.tvReviewList);
        btnClose = view.findViewById(R.id.btnClose);
        btnTestAgain = view.findViewById(R.id.btnTestAgain);
        btnBackToLibrary = view.findViewById(R.id.btnBackToLibrary);
    }

    private void loadScoreData() {
        if (getArguments() != null) {
            int score = getArguments().getInt("SCORE", 0);
            int total = getArguments().getInt("TOTAL_QUESTIONS", 1);
            ArrayList<String> wrongWords = getArguments().getStringArrayList("WRONG_WORDS");
            currentDeckId = (UUID) getArguments().getSerializable("DECK_ID");
            currentDeckName = getArguments().getString("DECK_NAME");

            progressOverall.setMax(total);
            progressOverall.setProgress(score, true);

            String scoreFormat = String.format(Locale.getDefault(), "%d/%d", score, total);
            tvScoreNumber.setText(scoreFormat);

            if (wrongWords == null || wrongWords.isEmpty()) {
                tvReviewList.setText(R.string.quiz_result_tv_congratulate);
                tvReviewList.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_success));
            } else {
                StringBuilder reviewText = new StringBuilder();
                for (String word : wrongWords) {
                    reviewText.append("• ").append(word).append("\n");
                }
                tvReviewList.setText(reviewText.toString().trim());
            }
        }
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnBackToLibrary.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnTestAgain.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("DECK_ID", currentDeckId);
            bundle.putString("DECK_NAME", currentDeckName);

            androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.scoreResultFragment, true)
                    .build();

            androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(R.id.cardQuizModeFragment, bundle, navOptions);
        });
    }
}