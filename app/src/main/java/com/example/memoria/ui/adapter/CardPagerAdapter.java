package com.example.memoria.ui.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.Card;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pager_card, parent, false);
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
        TextView tvFrontText, tvMeanings;
        ImageView imgFront;

        boolean isFront = true;
        boolean flipable = true;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_container);
            layoutFront = itemView.findViewById(R.id.layout_front);
            layoutBack = itemView.findViewById(R.id.layout_back);
            btnFlip = itemView.findViewById(R.id.btn_flip_card);
            tvFrontText = itemView.findViewById(R.id.tv_front_text);
            tvMeanings = itemView.findViewById(R.id.tv_meanings);
            imgFront = itemView.findViewById(R.id.img_front);

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
            }

            if (card.getBackMeanings() != null) {
                StringBuilder meaningsBuilder = new StringBuilder();
                for (int i = 0; i < card.getBackMeanings().size(); i++) {
                    meaningsBuilder.append(i + 1).append(". ").append(card.getBackMeanings().get(i)).append("\n\n");
                }
                tvMeanings.setText(meaningsBuilder.toString().trim());
            }
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
}