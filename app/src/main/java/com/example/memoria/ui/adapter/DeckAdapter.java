package com.example.memoria.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.memoria.R;
import com.example.memoria.data.model.Deck;
import com.example.memoria.data.model.DeckWithCount;

import java.util.ArrayList;
import java.util.List;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {

    private List<DeckWithCount> deckList = new ArrayList<>();
    private OnDeckClickListener listener;

    public interface OnDeckClickListener {
        void onDeckClick(Deck deck);
    }

    public DeckAdapter(OnDeckClickListener listener) {
        this.listener = listener;
    }

    public void setDecks(List<DeckWithCount> decks) {
        this.deckList = decks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library_deck, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        DeckWithCount item = deckList.get(position);
        Deck deck = item.deck;
        Context context = holder.itemView.getContext(); // Lấy context để gọi string

        holder.tvName.setText(deck.getDeckName());

        // Đếm các thẻ trong deck để hiển thị lên thanh tiến độ, hiện tại chưa có chức năng học nên learnedCards = 0
        int totalCards = item.totalCards;
        int learnedCards = 0;

        if (totalCards == 0) {
            // Deck rỗng -> Ẩn thanh tiến độ, hiện "Empty deck"
            holder.pbProgress.setVisibility(View.GONE);
            holder.tvCount.setText(context.getString(R.string.empty_deck));
        } else {
            // Deck có thẻ -> Hiện thanh tiến độ, tính % và chữ "x/y"
            holder.pbProgress.setVisibility(View.VISIBLE);
            holder.pbProgress.setMax(totalCards);
            holder.pbProgress.setProgress(learnedCards);

            holder.tvCount.setText(context.getString(R.string.deck_progress, learnedCards, totalCards));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeckClick(deck);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deckList == null ? 0 : deckList.size();
    }

    static class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCount;
        ProgressBar pbProgress;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_deck_name);
            tvCount = itemView.findViewById(R.id.tv_deck_count);
            pbProgress = itemView.findViewById(R.id.pb_deck_progress);
        }
    }
}