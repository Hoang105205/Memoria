package com.example.memoria.ui.search;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.FavFolder;
import com.example.memoria.ui.adapter.SelectFolderAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Date;
import java.util.UUID;

public class SelectFavFolderDialog extends BottomSheetDialogFragment {

    private SearchViewModel viewModel;
    private SelectFolderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_select_fav_folder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Dùng requireActivity() để lấy chung ViewModel với SearchFragment
        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        RecyclerView rvFolders = view.findViewById(R.id.rv_folders_to_select);
        rvFolders.setLayoutManager(new LinearLayoutManager(getContext()));

        // tao adapter Khi người dùng bấm vào 1 Folder có sẵn
        adapter = new SelectFolderAdapter(folder -> {
            viewModel.saveCurrentWordToFolder(folder.getFolderId(), isSuccess -> {
                // Ép chạy trên luồng giao diện chính (Main Thread)
                requireActivity().runOnUiThread(() -> {
                    if (isSuccess) {
                        Toast.makeText(getContext(), getString(R.string.msg_save_success), Toast.LENGTH_SHORT).show();
                        dismiss(); // Chỉ đóng Dialog khi lưu thành công
                    } else {
                        Toast.makeText(getContext(), getString(R.string.msg_word_exists), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
        rvFolders.setAdapter(adapter);

        // Lắng nghe dữ liệu
        viewModel.getFavFolders().observe(getViewLifecycleOwner(), folders -> {
            adapter.setFolders(folders);
        });
        viewModel.loadFavFolders(); // Load danh sách mới nhất

        // Nút tạo mới Folder
        view.findViewById(R.id.btn_create_new_folder).setOnClickListener(v -> showCreateFolderDialog());
    }

    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.dialog_title_add_folder);

        final EditText input = new EditText(requireContext());
        input.setPadding(60, 40, 60, 40);
        builder.setView(input);

        builder.setPositiveButton(R.string.action_create, (dialog, which) -> {
            String enteredName = input.getText().toString().trim();
            if (!enteredName.isEmpty()) {
                FavFolder newFolder = new FavFolder();
                newFolder.setFolderId(UUID.randomUUID());
                newFolder.setFolderName(enteredName);
                newFolder.setCreatedAt(new Date());

                // Thêm Folder vào DB
                viewModel.addNewFavFolder(newFolder);

                // Tự động lưu luôn từ vựng hiện tại vào folder vừa tạo
                viewModel.saveCurrentWordToFolder(newFolder.getFolderId(), isSuccess -> {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), getString(R.string.msg_save_success), Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                });
            }
        });
        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }
}