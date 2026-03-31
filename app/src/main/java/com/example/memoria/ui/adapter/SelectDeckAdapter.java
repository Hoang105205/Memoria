package com.example.memoria.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.memoria.R;
import com.example.memoria.data.model.entity.Deck;
import java.util.ArrayList;
import java.util.List;

public class SelectDeckAdapter extends RecyclerView.Adapter<SelectDeckAdapter.DeckViewHolder> {

    private List<Deck> decks = new ArrayList<>();
    private final OnDeckClickListener listener;

    public interface OnDeckClickListener {
        void onDeckClick(Deck deck);
    }

    public SelectDeckAdapter(OnDeckClickListener listener) {
        this.listener = listener;
    }

    public void setDecks(List<Deck> decks) {
        this.decks = decks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_deck, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        Deck deck = decks.get(position);
        holder.tvDeckName.setText(deck.getDeckName());
        holder.itemView.setOnClickListener(v -> listener.onDeckClick(deck));
    }

    @Override
    public int getItemCount() {
        return decks != null ? decks.size() : 0;
    }

    static class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeckName;
        DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeckName = itemView.findViewById(R.id.tv_deck_name);
        }
    }
}