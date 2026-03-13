package com.example.memoria.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.memoria.R;
import com.example.memoria.data.model.Card;
import java.util.ArrayList;
import java.util.List;

public class DeckCardAdapter extends RecyclerView.Adapter<DeckCardAdapter.CardViewHolder> {

    private List<Card> cardList = new ArrayList<>();
    private final OnCardClickListener listener;

    public interface OnCardClickListener {
        void onCardClick(Card card, int position);
    }

    public DeckCardAdapter(OnCardClickListener listener) {
        this.listener = listener;
    }

    public void setCards(List<Card> cards) {
        this.cardList = cards;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deck_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cardList.get(position);
        holder.tvFrontText.setText(card.getFrontText());
        holder.itemView.setOnClickListener(v -> listener.onCardClick(card, position));
    }

    @Override
    public int getItemCount() {
        return cardList == null ? 0 : cardList.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView tvFrontText;
        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFrontText = itemView.findViewById(R.id.tv_card_front_text);
        }
    }
}