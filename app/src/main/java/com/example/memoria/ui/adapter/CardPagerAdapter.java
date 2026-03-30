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

    public void setCards(List<Card> cards) {
        this.cardList = cards;
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
        holder.bind(card);
    }

    @Override
    public int getItemCount() {
        return cardList == null ? 0 : cardList.size();
    }

    // Hàm này để CardDetailFragment có thể lấy thẻ đang hiển thị
    public Card getCardAt(int position) {
        if (position >= 0 && position < cardList.size()) {
            return cardList.get(position);
        }
        return null;
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cardContainer;
        View layoutFront, layoutBack;
        ImageButton btnFlip;
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

            setupFlipAnimation();
        }

        void bind(Card card) {
            // Đảm bảo luôn bắt đầu từ mặt trước
            isFront = true;
            layoutFront.setVisibility(View.VISIBLE);
            layoutBack.setVisibility(View.GONE);
            cardContainer.setRotationY(0f);

            // Gán dữ liệu
            if (card.getFrontText() != null) {
                tvFrontText.setText(card.getFrontText());
                tvBackText.setText(card.getFrontText());
            }

            String imageUriString = card.getFrontImage();
            if (imageUriString != null && !imageUriString.isEmpty()) {

                Uri imageUri = Uri.parse(imageUriString);

                Glide.with(itemView.getContext())
                        .load(imageUri)
                        .fitCenter()
                        // .placeholder(R.drawable.ic_loading) // (Tùy chọn) Ảnh hiển thị trong lúc chờ load
                        // .error(R.drawable.ic_error_image)   // (Tùy chọn) Ảnh hiển thị nếu load lỗi/mất file
                        .into(imgFront);

            } else {
                // No image
                imgFront.setVisibility(View.GONE);
                Glide.with(itemView.getContext()).clear(imgFront);
            }

            if (card.getBackMeanings() != null) {
                StringBuilder meaningsBuilder = new StringBuilder();
                for (int i = 0; i < card.getBackMeanings().size(); i++) {
                    meaningsBuilder.append(i + 1).append(". ").append(card.getBackMeanings().get(i)).append("\n\n");
                }
                tvMeanings.setText(meaningsBuilder.toString().trim());
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private void setupFlipAnimation() {
//            btnFlip.setOnClickListener(v -> {
//                if (!flipable) return;
//                flipable = false;
//
//                ObjectAnimator flipOut = ObjectAnimator.ofFloat(cardContainer, "rotationY", 0f, -90f);
//                flipOut.setDuration(150);
//                ObjectAnimator flipIn = ObjectAnimator.ofFloat(cardContainer, "rotationY", 90f, 0f);
//                flipIn.setDuration(150);
//
//                flipOut.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        if (isFront) {
//                            layoutFront.setVisibility(View.GONE);
//                            layoutBack.setVisibility(View.VISIBLE);
//                        } else {
//                            layoutFront.setVisibility(View.VISIBLE);
//                            layoutBack.setVisibility(View.GONE);
//                        }
//                        isFront = !isFront;
//                        flipIn.start();
//                    }
//                });
//
//                flipIn.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        flipable = true;
//                    }
//                });
//
//                flipOut.start();
//            });

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
                                // Allow ScrollView continue operate
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
                        return v.onTouchEvent(event); // Give back control to ScrollView
                    }
                });
            }

            itemView.setOnClickListener(v -> {
                flipCard((CardView) v);
            });
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