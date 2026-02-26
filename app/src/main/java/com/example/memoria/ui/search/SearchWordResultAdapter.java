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

public class SearchWordResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VT_HEADER = 1;
    private static final int VT_DEFINITION = 2;
    private static final int VT_EMPTY = 3;

    public static class UiItem {
        public final int type;
        public final String title;      // header / empty
        public final String definition; // definition
        public final String example;    // optional

        private UiItem(int type, String title, String definition, String example) {
            this.type = type;
            this.title = title;
            this.definition = definition;
            this.example = example;
        }

        public static UiItem header(String partOfSpeech) {
            return new UiItem(VT_HEADER, partOfSpeech, null, null);
        }

        public static UiItem definition(String definition, String example) {
            return new UiItem(VT_DEFINITION, null, definition, example);
        }

        public static UiItem empty(String message) {
            return new UiItem(VT_EMPTY, message, null, null);
        }
    }

    private final List<UiItem> items = new ArrayList<>();

    public void submitItems(List<UiItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());

        if (viewType == VT_HEADER) {
            View v = inf.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new HeaderVH(v);
        } else if (viewType == VT_EMPTY) {
            View v = inf.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new EmptyVH(v);
        } else {
            View v = inf.inflate(android.R.layout.simple_list_item_2, parent, false);
            return new DefVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UiItem item = items.get(position);

        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).tv.setText(item.title);
        } else if (holder instanceof EmptyVH) {
            ((EmptyVH) holder).tv.setText(item.title);
        } else {
            DefVH vh = (DefVH) holder;
            vh.tv1.setText(item.definition != null ? item.definition : "");
            if (item.example != null && !item.example.trim().isEmpty()) {
                vh.tv2.setVisibility(View.VISIBLE);
                vh.tv2.setText(item.example);
            } else {
                vh.tv2.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tv;
        HeaderVH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(android.R.id.text1);
            tv.setTextSize(16);
            tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        }
    }

    static class EmptyVH extends RecyclerView.ViewHolder {
        TextView tv;
        EmptyVH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(android.R.id.text1);
        }
    }

    static class DefVH extends RecyclerView.ViewHolder {
        TextView tv1, tv2;
        DefVH(@NonNull View itemView) {
            super(itemView);
            tv1 = itemView.findViewById(android.R.id.text1);
            tv2 = itemView.findViewById(android.R.id.text2);
        }
    }
}