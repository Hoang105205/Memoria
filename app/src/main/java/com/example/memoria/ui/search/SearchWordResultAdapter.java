package com.example.memoria.ui.search;

import android.content.Context;
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

    public static class UiItem {
        public final int type;
        public final String title;      // header
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
            View v = inf.inflate(R.layout.item_search_result_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = inf.inflate(R.layout.item_search_result_definition, parent, false);
            return new DefVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UiItem item = items.get(position);
        Context context = holder.itemView.getContext();

        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).tv.setText(item.title);
        } else if (holder instanceof DefVH) {
            DefVH vh = (DefVH) holder;
            // Add dash prefix to definition
            String definitionText = "- " + (item.definition != null ? item.definition : "");
            vh.tvDef.setText(definitionText);

            if (item.example != null && !item.example.trim().isEmpty()) {
                vh.tvEx.setVisibility(View.VISIBLE);
                // Format example using localized string
                vh.tvEx.setText(context.getString(R.string.example_label, item.example));
            } else {
                vh.tvEx.setVisibility(View.GONE);
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
            tv = itemView.findViewById(R.id.tv_header);
        }
    }

    static class DefVH extends RecyclerView.ViewHolder {
        TextView tvDef, tvEx;
        DefVH(@NonNull View itemView) {
            super(itemView);
            tvDef = itemView.findViewById(R.id.tv_definition);
            tvEx = itemView.findViewById(R.id.tv_example);
        }
    }
}
