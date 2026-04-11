package com.example.memoria.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.dto.PublicDeck;

import java.util.List;

public class PublicAdapter extends RecyclerView.Adapter<PublicAdapter.ViewHolder> {

    private List<PublicDeck> publicDecks;
    private OnDeckClickListener listener;

    public interface OnDeckClickListener {
        void onDeckClick(PublicDeck deck);
    }

    public PublicAdapter(List<PublicDeck> publicDecks, OnDeckClickListener listener) {
        this.publicDecks = publicDecks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deck_public, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PublicDeck deck = publicDecks.get(position);
        holder.bind(deck, listener);
    }

    @Override
    public int getItemCount() {
        return publicDecks == null ? 0 : publicDecks.size();
    }

    // Hàm này dùng để cập nhật lại list khi kéo thêm data
    public void updateData(List<PublicDeck> newData) {
        this.publicDecks = newData;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeckName, tvCardCount, tvDownloadCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeckName = itemView.findViewById(R.id.item_tv_deck_name);
            tvCardCount = itemView.findViewById(R.id.item_tv_card_count);
            tvDownloadCount = itemView.findViewById(R.id.item_tv_download_count);
        }

        public void bind(PublicDeck deck, OnDeckClickListener listener) {
            tvDeckName.setText(deck.getDeckName() != null ? deck.getDeckName() : "No Name");
            tvCardCount.setText(deck.getTotalCards() + " cards");
            tvDownloadCount.setText(deck.getDownloadCount() + " downloads");

            // TODO: Nếu sau này bạn muốn đổi màu background của CardView dựa vào deck.getCoverColor(), làm ở đây

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeckClick(deck);
                }
            });
        }
    }
}