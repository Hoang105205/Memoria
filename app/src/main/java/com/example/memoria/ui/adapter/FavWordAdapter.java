package com.example.memoria.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.memoria.R;
import com.example.memoria.data.model.FavWord;
import java.util.ArrayList;
import java.util.List;

public class FavWordAdapter extends RecyclerView.Adapter<FavWordAdapter.WordViewHolder> {

    private List<FavWord> words = new ArrayList<>();
    private final OnWordInteractionListener listener;

    public interface OnWordInteractionListener {
        void onPinClick(FavWord word);
        // Có thể thêm onWordClick nếu muốn bấm vào từ để xem lại chi tiết
    }

    public FavWordAdapter(OnWordInteractionListener listener) {
        this.listener = listener;
    }

    public void setWords(List<FavWord> words) {
        this.words = words;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fav_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        FavWord word = words.get(position);

        holder.tvWord.setText(word.getWordText());

        // Format hiển thị: (noun) Định nghĩa...
        String pos = word.getPartOfSpeech() != null ? "(" + word.getPartOfSpeech() + ") " : "";
        String meaning = word.getShortMeaning() != null ? word.getShortMeaning() : "";
        holder.tvMeaning.setText(pos + meaning);

        // Xử lý UI cho nút Pin (Ghim)
        if (word.isPinStatus()) {
            // Thay bằng icon ngôi sao đặc của bạn nếu có (vd: R.drawable.ic_star_filled)
            holder.btnPin.setImageResource(android.R.drawable.star_on);
        } else {
            // Thay bằng icon ngôi sao rỗng của bạn nếu có (vd: R.drawable.ic_star_outline)
            holder.btnPin.setImageResource(android.R.drawable.star_off);
        }

        holder.btnPin.setOnClickListener(v -> listener.onPinClick(word));
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvMeaning;
        ImageButton btnPin;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tv_word_text);
            tvMeaning = itemView.findViewById(R.id.tv_word_meaning);
            btnPin = itemView.findViewById(R.id.btn_pin);
        }
    }
}