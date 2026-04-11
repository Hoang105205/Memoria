package com.example.memoria.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.memoria.R;
import com.example.memoria.data.model.entity.FavFolder;
import java.util.ArrayList;
import java.util.List;

public class SelectFolderAdapter extends RecyclerView.Adapter<SelectFolderAdapter.FolderViewHolder> {

    private List<FavFolder> folders = new ArrayList<>();
    private final OnFolderClickListener listener;

    public interface OnFolderClickListener {
        void onFolderClick(FavFolder folder);
    }

    public SelectFolderAdapter(OnFolderClickListener listener) {
        this.listener = listener;
    }

    public void setFolders(List<FavFolder> folders) {
        this.folders = folders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        FavFolder folder = folders.get(position);
        holder.tvFolderName.setText(folder.getFolderName());
        holder.itemView.setOnClickListener(v -> listener.onFolderClick(folder));
    }

    @Override
    public int getItemCount() {
        return folders != null ? folders.size() : 0;
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView tvFolderName;
        FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolderName = itemView.findViewById(R.id.tv_folder_name);
        }
    }
}