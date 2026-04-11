package com.example.memoria.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.memoria.R;
import com.example.memoria.data.model.entity.FavFolder;
import com.example.memoria.data.model.entity.FavFolderWithCount;
import java.util.ArrayList;
import java.util.List;

public class FavFolderAdapter extends RecyclerView.Adapter<FavFolderAdapter.FavFolderViewHolder> {
    private List<FavFolderWithCount> folderList = new ArrayList<>();
    private OnFolderClickListener listener;

    public interface OnFolderClickListener {
        void onFolderClick(FavFolder folder);
    }

    public FavFolderAdapter(OnFolderClickListener listener) {
        this.listener = listener;
    }
    public void setFolders(List<FavFolderWithCount> folders) {
        this.folderList = folders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavFolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library_favfolder, parent, false);
        return new FavFolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavFolderViewHolder holder, int position) {
        FavFolderWithCount item = folderList.get(position);
        FavFolder folder = item.folder;
        Context context = holder.itemView.getContext();

        holder.tvName.setText(folder.getFolderName());

        // LẤY SỐ LƯỢNG THỰC TẾ TỪ DB
        int wordCount = item.wordCount;

        holder.tvName.setText(folder.getFolderName());

        // Hiển thị số lượng thực tế
        holder.tvCount.setText(context.getString(R.string.word_count, wordCount));

        // click vào dòng folder
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFolderClick(folder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return folderList == null ? 0 : folderList.size();
    }

    static class FavFolderViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCount;

        public FavFolderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_favfolder_name);
            tvCount = itemView.findViewById(R.id.tv_favfolder_count);
        }
    }
}