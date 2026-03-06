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
import com.example.memoria.data.model.FavWord;
import com.example.memoria.ui.adapter.SelectFolderAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Date;
import java.util.UUID;

public class SelectFavFolderDialog extends BottomSheetDialogFragment {

    private SearchViewModel viewModel;
    private SelectFolderAdapter adapter;
    private String currentWord;
    private String currentPos;
    private String currentMeaning;

    public static SelectFavFolderDialog newInstance(String word, String pos, String meaning) {
        SelectFavFolderDialog dialog = new SelectFavFolderDialog();
        Bundle args = new Bundle();
        args.putString("WORD_TO_SAVE", word);
        args.putString("POS_TO_SAVE", pos);
        args.putString("MEANING_TO_SAVE", meaning);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_select_fav_folder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            currentWord = getArguments().getString("WORD_TO_SAVE");
            currentPos = getArguments().getString("POS_TO_SAVE");
            currentMeaning = getArguments().getString("MEANING_TO_SAVE");
        }

        // Dùng requireActivity() để lấy chung ViewModel với SearchFragment
        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        RecyclerView rvFolders = view.findViewById(R.id.rv_folders_to_select);
        rvFolders.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SelectFolderAdapter(folder -> {
            saveWordToSelectedFolder(folder.getFolderId());
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

                // Thay vì chỉ reload lại danh sách bắt người dùng bấm thêm 1 lần nữa,
                // UX tốt nhất là Tự động lưu luôn từ vựng vào folder vừa tạo và đóng Dialog
                saveWordToSelectedFolder(newFolder.getFolderId());
            }
        });
        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveWordToSelectedFolder(UUID folderId) {
        if (currentWord != null) {
            FavWord newWord = new FavWord();
            newWord.setFavId(UUID.randomUUID());
            newWord.setFolderId(folderId);
            newWord.setWordText(currentWord);
            newWord.setPartOfSpeech(currentPos);         // Lưu POS
            newWord.setShortMeaning(currentMeaning);     // Lưu Meaning
            newWord.setAddedAt(new Date());
            newWord.setPinStatus(false);

            viewModel.saveWordToFolder(newWord);
            Toast.makeText(getContext(), "Đã lưu từ: " + currentWord, Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }
}