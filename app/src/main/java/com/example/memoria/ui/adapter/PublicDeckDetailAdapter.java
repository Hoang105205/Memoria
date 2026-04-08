package com.example.memoria.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.dto.PublicDeck;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PublicDeckDetailAdapter extends RecyclerView.Adapter<PublicDeckDetailAdapter.ViewHolder> {

    private List<PublicDeck> publicDecks = new ArrayList<>();
    private final OnDeckClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnDeckClickListener {
        void onDeckClick(PublicDeck deck);
        void onDownloadClick(PublicDeck deck);
    }

    public PublicDeckDetailAdapter(OnDeckClickListener listener) {
        this.listener = listener;
    }

    public void setDecks(List<PublicDeck> decks) {
        this.publicDecks = decks;
        notifyDataSetChanged();
    }

    // Thêm data vào list hiện tại (Dùng cho Load More)
    public void addDecks(List<PublicDeck> newDecks) {
        int startPos = this.publicDecks.size();
        this.publicDecks.addAll(newDecks);
        notifyItemRangeInserted(startPos, newDecks.size());
    }

    public void clearDecks() {
        this.publicDecks.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deck_public_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PublicDeck deck = publicDecks.get(position);
        holder.bind(deck, listener, dateFormat);
    }

    @Override
    public int getItemCount() {
        return publicDecks == null ? 0 : publicDecks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeckName, tvAuthor, tvCardCount, tvDownloadCount, tvDate;
        View btnDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeckName = itemView.findViewById(R.id.item_tv_deck_name);
            tvAuthor = itemView.findViewById(R.id.item_tv_author);
            tvCardCount = itemView.findViewById(R.id.item_tv_card_count);
            tvDownloadCount = itemView.findViewById(R.id.item_tv_download_count);
            tvDate = itemView.findViewById(R.id.item_tv_date);
            btnDownload = itemView.findViewById(R.id.btn_download);
        }

        public void bind(PublicDeck deck, OnDeckClickListener listener, SimpleDateFormat dateFormat) {
            tvDeckName.setText(deck.getDeckName() != null ? deck.getDeckName() : "No Name");
            tvAuthor.setText(deck.getAuthorName() != null ? deck.getAuthorName() : "Unknown");
            tvCardCount.setText(deck.getTotalCards() + " cards");
            tvDownloadCount.setText(deck.getDownloadCount() + " downloads");

            if (deck.getPublishedAt() != null) {
                tvDate.setText(dateFormat.format(deck.getPublishedAt()));
            } else {
                tvDate.setText("");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onDeckClick(deck);
            });

            btnDownload.setOnClickListener(v -> {
                if (listener != null) listener.onDownloadClick(deck);
            });
        }
    }
}