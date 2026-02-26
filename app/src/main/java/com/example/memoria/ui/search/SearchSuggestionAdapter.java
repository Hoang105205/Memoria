package com.example.memoria.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;

import java.util.ArrayList;
import java.util.List;

public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.VH> {

    public interface OnItemClickListener {
        void onClick(String word);
    }

    private final List<String> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public SearchSuggestionAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<String> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_suggestion, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String word = items.get(position);
        holder.tvWord.setText(word);

        holder.itemView.setOnClickListener(view -> {
            if (listener != null) listener.onClick(word);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvWord;

        VH(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tv_search_suggestion);
        }
    }
}