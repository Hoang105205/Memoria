package com.example.memoria.ui.adapter;

import static com.example.memoria.ui.study.LearnFragment.CLICK_THRESHOLD;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memoria.R;
import com.example.memoria.data.model.entity.Card;

import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends RecyclerView.Adapter<CardPagerAdapter.CardViewHolder> {
    private List<Card> cardList = new ArrayList<>();
    private String themeColor = "";

    public interface OnAudioPlayListener {
        void onPlayAudio(String textToRead);
    }
    private OnAudioPlayListener audioPlayListener;

    public void setOnAudioPlayListener(OnAudioPlayListener listener) {
        this.audioPlayListener = listener;
    }

    public void setCards(List<Card> cards) {
        this.cardList = cards;
        notifyDataSetChanged();
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cardList.get(position);
        holder.bind(card, themeColor, audioPlayListener);
    }

    @Override
    public int getItemCount() {
        return cardList == null ? 0 : cardList.size();
    }

    public Card getCardAt(int position) {
        if (position >= 0 && position < cardList.size()) {
            return cardList.get(position);
        }
        return null;
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView cardContainer;
        View layoutFront, layoutBack;
        ImageButton btnPlayAudio;
        TextView tvFrontText, tvBackText, tvMeanings;
        ImageView imgFront;

        boolean isFront = true;
        boolean flipable = true;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_flash_card);
            layoutFront = itemView.findViewById(R.id.layout_front);
            layoutBack = itemView.findViewById(R.id.layout_back);
            tvFrontText = itemView.findViewById(R.id.tv_word_front);
            tvBackText = itemView.findViewById(R.id.tv_word_back);
            tvMeanings = itemView.findViewById(R.id.tv_definition);
            imgFront = itemView.findViewById(R.id.img_flash_card);
            btnPlayAudio = itemView.findViewById(R.id.btn_play_audio);

            setupFlipAnimation();
        }

        void bind(Card card, String themeColor, OnAudioPlayListener listener) {
            // Đảm bảo luôn bắt đầu từ mặt trước
            isFront = true;
            layoutFront.setVisibility(View.VISIBLE);
            layoutBack.setVisibility(View.GONE);
            cardContainer.setRotationY(0f);

            // Bất kể thẻ gì, mặt sau luôn hiển thị FrontText (Dùng làm đáp án)
            if (card.getFrontText() != null) {
                tvBackText.setText(card.getFrontText());
            }

            int cardType = card.getCardType();

            if (cardType == 0) {
                tvFrontText.setVisibility(View.VISIBLE);
                imgFront.setVisibility(View.GONE);
                btnPlayAudio.setVisibility(View.GONE);

                tvFrontText.setText(card.getFrontText());

            } else if (cardType == 1) {
                tvFrontText.setVisibility(View.GONE);
                imgFront.setVisibility(View.VISIBLE);
                btnPlayAudio.setVisibility(View.GONE);

                if (card.getFrontImage() != null && !card.getFrontImage().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(Uri.parse(card.getFrontImage()))
                            .fitCenter()
                            .into(imgFront);
                } else {
                    Glide.with(itemView.getContext()).clear(imgFront);
                }

            } else if (cardType == 2) {
                tvFrontText.setVisibility(View.GONE);
                imgFront.setVisibility(View.GONE);
                btnPlayAudio.setVisibility(View.VISIBLE);

                // Gắn sự kiện phát âm thanh
                btnPlayAudio.setOnClickListener(v -> {
                    if (listener != null && card.getFrontText() != null) {
                        listener.onPlayAudio(card.getFrontText());
                    }
                });
            }

            if (card.getBackMeanings() != null) {
                StringBuilder meaningsBuilder = new StringBuilder();
                for (int i = 0; i < card.getBackMeanings().size(); i++) {
                    // Kiểm tra an toàn cho mảng backTypes
                    String type = "";
                    if (card.getBackTypes() != null && i < card.getBackTypes().size()) {
                        type = card.getBackTypes().get(i);
                    }

                    String meaning = card.getBackMeanings().get(i);

                    // Xử lý UI đồng nhất: Ẩn dấu gạch ngang nếu Type rỗng
                    if (type == null || type.trim().isEmpty()) {
                        meaningsBuilder.append(String.format("• %s\n\n", meaning));
                    } else {
                        meaningsBuilder.append(String.format("• %s - %s\n\n", type.trim(), meaning));
                    }
                }
                tvMeanings.setText(meaningsBuilder.toString().trim());
            }

            // Áp dụng Theme Color
            if (themeColor != null && !themeColor.isEmpty()) {
                cardContainer.setStrokeColor(android.graphics.Color.parseColor(themeColor));
                cardContainer.setStrokeWidth(12);
            } else {
                cardContainer.setStrokeWidth(0);
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private void setupFlipAnimation() {
            ScrollView scrollView = itemView.findViewById(R.id.scroll_definition);
            if (scrollView != null) {
                scrollView.setOnTouchListener(new View.OnTouchListener() {
                    private float startX, startY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                startX = event.getX();
                                startY = event.getY();
                                break;
                            case MotionEvent.ACTION_UP:
                                float endX = event.getX();
                                float endY = event.getY();
                                float distance = (float) Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));

                                if (distance <= CLICK_THRESHOLD) {
                                    flipCard((CardView) itemView);
                                    return true;
                                }
                                break;
                        }
                        return v.onTouchEvent(event);
                    }
                });
            }

            itemView.setOnClickListener(v -> flipCard((CardView) v));
        }

        private void flipCard(CardView card) {
            if (!flipable) return;
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
                    flipable = false;
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
                    flipable = true;
                    card.setClickable(true);
                }
            });

            flipOut.start();
        }
    }
}