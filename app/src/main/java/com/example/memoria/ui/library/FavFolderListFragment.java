package com.example.memoria.ui.library;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.FavFolder;
import com.example.memoria.ui.adapter.FavFolderAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.UUID;

public class FavFolderListFragment extends Fragment {

    private LibraryViewModel viewModel;
    private FavFolderAdapter favFolderAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favfolder_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // LƯU Ý QUAN TRỌNG: Dùng requireActivity() để dùng chung ViewModel với các Fragment khác
        viewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);

        RecyclerView rvFavFolderList = view.findViewById(R.id.rv_favfolder_list);
        FloatingActionButton btnAdd = view.findViewById(R.id.btn_favfolder_add);

        rvFavFolderList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo Adapter và GIỮ NGUYÊN LOGIC điều hướng (Navigation) cũ
        favFolderAdapter = new FavFolderAdapter(folder -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("FOLDER_ID", folder.getFolderId());
            androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(R.id.favDetailFragment, bundle);
        });
        rvFavFolderList.setAdapter(favFolderAdapter);

        // Lắng nghe dữ liệu
        viewModel.getFavFolders().observe(getViewLifecycleOwner(), folders -> {
            favFolderAdapter.setFolders(folders);
        });

        btnAdd.setOnClickListener(v -> showAddDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadFavFolders();
        }
    }

    private void showAddDialog() {
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
                viewModel.addNewFavFolder(newFolder);
            }
        });

        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }
}