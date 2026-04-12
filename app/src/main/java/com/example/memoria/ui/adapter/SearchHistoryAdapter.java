package com.example.memoria.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.dto.HistoryListItem;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onClickWord(String word);
        void onDeleteWord(String word);
    }

    private final Listener listener;
    private final List<HistoryListItem> items = new ArrayList<>();

    public SearchHistoryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<HistoryListItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == HistoryListItem.TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_history_header, parent, false);
            return new HeaderVH(v);
        }
        View v = inf.inflate(R.layout.item_history_row, parent, false);
        return new RowVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HistoryListItem item = items.get(position);

        if (holder instanceof HeaderVH) {
            HeaderVH vh = (HeaderVH) holder;
            HistoryListItem.Header h = (HistoryListItem.Header) item;
            vh.tvHeader.setText(h.title);
            return;
        }

        RowVH vh = (RowVH) holder;
        HistoryListItem.Row r = (HistoryListItem.Row) item;

        String word = r.history.getWordText() == null ? "" : r.history.getWordText().trim();
        vh.tvWord.setText(word);

        vh.itemView.setOnClickListener(v -> listener.onClickWord(word));
        vh.ivDelete.setOnClickListener(v -> listener.onDeleteWord(word));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderVH(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }
    }

    static class RowVH extends RecyclerView.ViewHolder {
        TextView tvWord;
        ImageView ivDelete;
        RowVH(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tvWord);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
