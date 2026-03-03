package com.example.memoria.ui.study;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.memoria.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LearnFragment extends Fragment {

    private CardView cardTop, cardBottom;
    private TextView tvBadgeRemember, tvBadgeForgot;
    private View vBackgroundRemember, vBackgroundForgot;
    private TextView tvEmpty;

    private List<Map<String, Object>> flashcardList;
    private int currentIndex = 0;

    private float startX;
    private static final int CLICK_THRESHOLD = 15;

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

        initMockData();
        loadCards();

        setupTouchListener();

        return view;
    }

    private void initMockData() {
        flashcardList = new ArrayList<>();

        Map<String, Object> c1 = new HashMap<>();
        c1.put("front", "Scrumptious");
        c1.put("back", "Ngon tuyệt");
        c1.put("type", "(adj)");
        c1.put("def", "• (Of food) Very delicious\n\n• (Of a person) Very attractive");
        flashcardList.add(c1);

        Map<String, Object> c2 = new HashMap<>();
        c2.put("front", "Serendipity");
        c2.put("back", "Tình cờ");
        c2.put("type", "(noun)");
        c2.put("def", "• The occurrence of events by chance in a happy or beneficial way\n\n• (Context) Good luck in finding valuable things one was not looking for");
        flashcardList.add(c2);

        Map<String, Object> c3 = new HashMap<>();
        c3.put("front", "Petrichor");
        c3.put("back", "Mùi đất mưa");
        c3.put("type", "(noun)");
        c3.put("def", "• A pleasant smell that frequently accompanies the first rain after a long period of warm, dry weather\n\n• (Context) The distinct scent of rain on dry earth");
        flashcardList.add(c3);

        Map<String, Object> c4 = new HashMap<>();
        c4.put("front", "Ephemeral");
        c4.put("back", "Phù du");
        c4.put("type", "(adj)");
        c4.put("def", "• Lasting for a very short time\n\n• (Of plants) Having a very short life cycle");
        flashcardList.add(c4);

        Map<String, Object> c5 = new HashMap<>();
        c5.put("front", "Ineffable");
        c5.put("back", "Không thốt nên lời");
        c5.put("type", "(adj)");
        c5.put("def", "• Too great or extreme to be expressed in words\n\n• (Of a name) Too sacred to be spoken");
        flashcardList.add(c5);
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

    private void bindDataToView(View cardView, Map<String, Object> data) {
        if (cardView == null) return;

        TextView tvFront = cardView.findViewById(R.id.tv_word_front);
        TextView tvBack = cardView.findViewById(R.id.tv_word_back);
        TextView tvType = cardView.findViewById(R.id.tv_type);
        TextView tvDef = cardView.findViewById(R.id.tv_definition);

        if (tvFront != null) tvFront.setText((String) data.get("front"));
        if (tvBack != null) tvBack.setText((String) data.get("back"));
        if (tvType != null) tvType.setText((String) data.get("type"));
        if (tvDef != null) tvDef.setText((String) data.get("def"));

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
                        currentIndex++;
                        loadCards();

                        // TODO: Lưu vào Database ở đây (Nhớ/Quên dựa vào targetX > 0 hay < 0)
                    }
                })
                .start();
    }

    private void flipCard(CardView card) {
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
                card.setClickable(true);
            }
        });

        flipOut.start();
    }
}