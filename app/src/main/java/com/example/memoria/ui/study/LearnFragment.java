package com.example.memoria.ui.study;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.memoria.R;

public class LearnFragment extends Fragment {

    private CardView cardFlashcard;
    private View layoutFront;
    private View layoutBack;
    private boolean isShowingFront = true; // showing flag

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_study_fragment_learn, container, false);

        cardFlashcard = view.findViewById(R.id.card_flash_card);
        layoutFront = view.findViewById(R.id.layout_front);
        layoutBack = view.findViewById(R.id.layout_back);

        float scale = getResources().getDisplayMetrics().density;
        cardFlashcard.setCameraDistance(8000 * scale);

        cardFlashcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardFlashcard.setClickable(false);
                flipCard();
            }
        });

        return view;
    }

    private void flipCard() {
        final float originalElevation = cardFlashcard.getCardElevation();

        ObjectAnimator flipOut = ObjectAnimator.ofFloat(cardFlashcard, "rotationY", 0f, -90f);
        flipOut.setDuration(150);

        ObjectAnimator flipIn = ObjectAnimator.ofFloat(cardFlashcard, "rotationY", 90f, 0f);
        flipIn.setDuration(150);

        flipOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                cardFlashcard.setCardElevation(0f);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (isShowingFront) {
                    layoutFront.setVisibility(View.GONE);
                    layoutBack.setVisibility(View.VISIBLE);
                    isShowingFront = false;
                } else {
                    layoutFront.setVisibility(View.VISIBLE);
                    layoutBack.setVisibility(View.GONE);
                    isShowingFront = true;
                }

                flipIn.start();
            }
        });

        flipIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                cardFlashcard.setCardElevation(originalElevation);
                cardFlashcard.setClickable(true);
            }
        });

        flipOut.start();
    }
}