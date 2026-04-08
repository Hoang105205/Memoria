package com.example.memoria.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.memoria.R;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public class PublicAdapter extends RecyclerView.Adapter<PublicAdapter.ViewHolder> {

    private final List<DocumentSnapshot> decks;

    public PublicAdapter(List<DocumentSnapshot> decks) {
        this.decks = decks;
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
        DocumentSnapshot doc = decks.get(position);

        // Lấy dữ liệu từ DocumentSnapshot của Firestore
        String name = doc.getString("deckName");
        Long downloads = doc.getLong("downloadCount");
        // Giả sử bạn lưu số lượng card trong metadata hoặc query riêng, ở đây demo lấy tạm:
        long cardCount = 0; // Bạn có thể update thêm logic đếm card ở đây

        holder.tvName.setText(name != null ? name : "Unnamed Deck");
        holder.tvDownload.setText((downloads != null ? downloads : 0) + " downloads");
        holder.tvCards.setText("Cards: " + cardCount);
    }

    @Override
    public int getItemCount() {
        return decks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCards, tvDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.item_tv_deck_name);
            tvCards = itemView.findViewById(R.id.item_tv_card_count);
            tvDownload = itemView.findViewById(R.id.item_tv_download_count);
        }
    }
}