package com.example.memoria.ui.library;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.memoria.R;

import java.util.UUID;

public class FavDetailFragment extends Fragment {

    private FavDetailViewModel viewModel;
    private TextView tvFolderName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fav_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvFolderName = view.findViewById(R.id.tv_fav_name);
        ImageButton btnOptions = view.findViewById(R.id.btn_fav_detail_options);
        ImageButton btnBack = view.findViewById(R.id.btn_back);

        viewModel = new ViewModelProvider(this).get(FavDetailViewModel.class);

        // nhận ID từ Bundle
        if (getArguments() != null) {
            UUID folderId = (UUID) getArguments().getSerializable("FOLDER_ID");
            if (folderId != null) {
                viewModel.loadFolder(folderId); // Yêu cầu ViewModel lấy dữ liệu từ DB lên
            }
        }

        viewModel.getFolder().observe(getViewLifecycleOwner(), folder -> {
            if (folder != null) {
                tvFolderName.setText(folder.getFolderName());
            }
        });

        // Bấm nút Edit -> Hiện Popup Menu
        btnOptions.setOnClickListener(this::showPopupMenu);

        btnBack.setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(view).navigateUp();
        });
    }

    // Dropdown options
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenu().add(0, 1, 0, R.string.action_edit_name);
        popupMenu.getMenu().add(0, 2, 1, R.string.action_delete);

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                showEditNameDialog();
                return true;
            } else if (item.getItemId() == 2) {
                showDeleteConfirmDialog();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    // Dialog enter name
    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.dialog_title_edit_folder_name);

        final EditText input = new EditText(requireContext());
        input.setText(tvFolderName.getText().toString()); // Đổ tên cũ vào ô nhập liệu
        input.setHint(R.string.dialog_hint_folder_name);
        input.setPadding(50, 40, 50, 40);
        builder.setView(input);

        builder.setPositiveButton(R.string.action_save, (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                viewModel.updateFolderName(newName);
            }
        });
        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // dialog xác nhận xóa
    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_title_delete_folder)
                .setMessage(R.string.dialog_message_delete_folder)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    viewModel.deleteCurrentFolder();

                    // Back to LibraryFragment
                    androidx.navigation.Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}