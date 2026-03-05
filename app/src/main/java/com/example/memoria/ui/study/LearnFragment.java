package com.example.memoria.ui.study;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.memoria.data.model.Card;
import com.example.memoria.data.repository.CardRepository;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.memoria.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LearnFragment extends Fragment {

    private final UUID currentDeckId;
    private static CardRepository localCardRepo;
    private CardView cardTop, cardBottom;
    private TextView tvBadgeRemember, tvBadgeForgot;
    private View vBackgroundRemember, vBackgroundForgot;
    private TextView tvEmpty;

    private List<Card> flashcardList;

    private int currentIndex = 0;
    private boolean flippable = true;
    private float startX;
    private static final int CLICK_THRESHOLD = 15;

    public LearnFragment(UUID currentDeckId) {
        this.currentDeckId = currentDeckId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (localCardRepo == null) {
            localCardRepo = CardRepository.getInstance(requireActivity().getApplication());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_study_fragment_learn, container, false);

        cardTop = view.findViewById(R.id.card_top);
        cardBottom = view.findViewById(R.id.card_bottom);

        tvBadgeRemember = view.findViewById(R.id.tv_badge_remember);
        tvBadgeForgot = view.findViewById(R.id.tv_badge_forgot);
        vBackgroundRemember = view.findViewById(R.id.v_background_gradient_remember);
        vBackgroundForgot = view.findViewById(R.id.v_background_gradient_forgot);
        tvEmpty = view.findViewById(R.id.tv_empty);

        float scale = getResources().getDisplayMetrics().density;
        cardTop.setCameraDistance(8000 * scale);
        cardBottom.setCameraDistance(8000 * scale);

        settingScrollView(cardTop);
        initCardData();

        setupTouchListener();

        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void settingScrollView(View cardView) {
        ScrollView scrollView = cardView.findViewById(R.id.scroll_definition);
        if (scrollView != null) {
            scrollView.setOnTouchListener(new View.OnTouchListener() {
                private float startX, startY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            startY = event.getY();
                            // Allow ScrollView continue operate
                            break;
                        case MotionEvent.ACTION_UP:
                            float endX = event.getX();
                            float endY = event.getY();
                            float distance = (float) Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));

                            if (distance <= CLICK_THRESHOLD) {
                                cardBottom.setAlpha(0f);
                                flipCard((CardView) cardView);
                                return true;
                            }
                            break;
                    }
                    return v.onTouchEvent(event); // Give back control to ScrollView
                }
            });
        }
    }

    private void initCardData() {
        LiveData<List<Card>> liveData = localCardRepo.getCardsByDeckId(currentDeckId);
        liveData.observe(getViewLifecycleOwner(), cardsFromDB -> {
            if (cardsFromDB == null) return;

            flashcardList = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            long oneDayMillis = 24 * 60 * 60 * 1000L;

            for (Card card : cardsFromDB) {
                long lastReviewTime = card.getLastReviewAt() != null ? card.getLastReviewAt().getTime() : 0;
                long nextReviewTime = lastReviewTime + ((long) card.getIntervalDays() * oneDayMillis);

                if (nextReviewTime <= currentTime) {
                    flashcardList.add(card);
                }
            }

            loadCards();

            // Ngắt kết nối để tránh lỗi cập nhật liên tục
            liveData.removeObservers(getViewLifecycleOwner());
        });
    }

    private void loadCards() {
        if (currentIndex >= flashcardList.size()) {
            if(cardTop != null) cardTop.setVisibility(View.GONE);
            if(cardBottom != null) cardBottom.setVisibility(View.GONE);
            if(tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        bindDataToView(cardTop, flashcardList.get(currentIndex));
        cardTop.setVisibility(View.VISIBLE);
        resetCardPosition(cardTop);

        if (currentIndex + 1 < flashcardList.size()) {
            bindDataToView(cardBottom, flashcardList.get(currentIndex + 1));
            cardBottom.setVisibility(View.VISIBLE);
            resetCardPosition(cardBottom);
        } else {
            cardBottom.setVisibility(View.GONE);
        }

        if (cardTop != null) {
            tvBadgeRemember = cardTop.findViewById(R.id.tv_badge_remember);
            tvBadgeForgot = cardTop.findViewById(R.id.tv_badge_forgot);
        }
    }

    private void bindDataToView(View cardView, Card data) {
        if (cardView == null) return;

        List<String> types = data.getBackTypes();
        List<String> meanings = data.getBackMeanings();
        int len = meanings.size();
        StringBuilder backText = new StringBuilder();
        for (int i = 0; i < len; i++) {
            backText.append(String.format("• %s - %s", types.get(i), meanings.get(i)));

            if (i < len - 1) {
                backText.append("\n\n");
            }
        }

        TextView tvFront = cardView.findViewById(R.id.tv_word_front);
        TextView tvBack = cardView.findViewById(R.id.tv_word_back);
        TextView tvDef = cardView.findViewById(R.id.tv_definition);

        if (tvFront != null) tvFront.setText(data.getFrontText());
        if (tvBack != null) tvBack.setText(data.getFrontText());
        if (tvDef != null) tvDef.setText(backText.toString());

        View front = cardView.findViewById(R.id.layout_front);
        View back = cardView.findViewById(R.id.layout_back);
        if(front != null) front.setVisibility(View.VISIBLE);
        if(back != null) back.setVisibility(View.GONE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListener() {
        if (cardTop == null) return;

        cardTop.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaX = event.getRawX() - startX;

                    if (Math.abs(deltaX) > CLICK_THRESHOLD) {
                        v.setTranslationX(deltaX);
                        v.setRotation(deltaX * 0.05f);

                        float alpha = Math.min(Math.abs(deltaX) / 250f, 1f);
                        cardBottom.setAlpha(alpha * 0.8f);

                        if (deltaX > 0) {
                            tvBadgeRemember.setAlpha(alpha);
                            vBackgroundRemember.setAlpha(alpha * 0.4f);
                            tvBadgeForgot.setAlpha(0f);
                            vBackgroundForgot.setAlpha(0f);
                        } else {
                            tvBadgeForgot.setAlpha(alpha);
                            vBackgroundForgot.setAlpha(alpha * 0.4f);
                            tvBadgeRemember.setAlpha(0f);
                            vBackgroundRemember.setAlpha(0f);
                        }
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    float finalDeltaX = event.getRawX() - startX;

                    if (Math.abs(finalDeltaX) <= CLICK_THRESHOLD) {
                        cardBottom.setAlpha(0f);
                        animateReset(v);
                        v.performClick();
                        flipCard((CardView) v);
                    }
                    else if (Math.abs(finalDeltaX) > 300) {
                        float targetX = (finalDeltaX > 0) ? 1000f : -1000f;
                        cardBottom.setAlpha(1f);
                        animateOut(v, targetX);
                    }
                    else {
                        animateReset(v);
                    }

                    resetBadges();
                    return true;
            }
            return false;
        });
    }

    private void animateReset(View v) {
        v.animate()
            .translationX(0f)
            .translationY(0f)
            .rotation(0f)
            .alpha(1f)
            .setDuration(300)
            .setListener(null)
            .start();
    }

    private void resetCardPosition(View v) {
        v.animate().cancel();

        v.setTranslationX(0f);
        v.setTranslationY(0f);
        v.setRotation(0f);
        v.setAlpha(1f);
    }

    private void resetBadges() {
        tvBadgeRemember.animate().alpha(0f).setDuration(300).start();
        tvBadgeForgot.animate().alpha(0f).setDuration(300).start();
        vBackgroundRemember.animate().alpha(0f).setDuration(300).start();
        vBackgroundForgot.animate().alpha(0f).setDuration(300).start();
    }

    private void animateOut(View v, float targetX) {
        v.animate()
                .translationX(targetX)
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Card currentCard = flashcardList.get(currentIndex);

                        // targetX > 0: Right (Remember)
                        int quality = (targetX > 0) ? 4 : 1;

                        processCardReview(currentCard, quality);

                        currentIndex++;
                        loadCards();
                    }
                })
                .start();
    }

    private void processCardReview(Card card, int quality) {
        String word = card.getFrontText();

        if (quality == 4) {
            // TODO: Mô phỏng: Set next_review_date lên 3 ngày sau
        } else {
            // TODO: Mô phỏng: Set next_review_date về 1 phút sau để học lại ngay
        }

        // TODO: GỌI DAO LƯU DATABASE
    }

    private void flipCard(CardView card) {
        if (!flippable) return;
        if (card == null) return;

        final View front = card.findViewById(R.id.layout_front);
        final View back = card.findViewById(R.id.layout_back);

        if (front == null || back == null) return;

        card.setClickable(false);

        final float originalElevation = card.getCardElevation();

        ObjectAnimator flipOut = ObjectAnimator.ofFloat(card, "rotationY", 0f, -90f);
        flipOut.setDuration(150);

        ObjectAnimator flipIn = ObjectAnimator.ofFloat(card, "rotationY", 90f, 0f);
        flipIn.setDuration(150);

        flipOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                card.setCardElevation(0f);
                cardBottom.setCardElevation(0f);
                flippable = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (front.getVisibility() == View.VISIBLE) {
                    front.setVisibility(View.GONE);
                    back.setVisibility(View.VISIBLE);
                } else {
                    front.setVisibility(View.VISIBLE);
                    back.setVisibility(View.GONE);
                }
                flipIn.start();
            }
        });

        flipIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                card.setCardElevation(originalElevation);
                cardBottom.setCardElevation(originalElevation);
                flippable = true;
                card.setClickable(true);
            }
        });

        flipOut.start();
    }
}