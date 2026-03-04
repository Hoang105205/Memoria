//package com.example.memoria.ui.library;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.TextView;
//import android.app.AlertDialog;
//import android.widget.EditText;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.memoria.R;
//import com.example.memoria.data.model.Deck;
//import com.example.memoria.data.model.FavFolder;
//import com.example.memoria.ui.adapter.FavFolderAdapter;
//import com.example.memoria.ui.adapter.DeckAdapter;
//
//import java.util.Date;
//import java.util.UUID;
//
//public class LibraryFragment extends Fragment {
//
//    private LibraryViewModel viewModel;
//    private DeckAdapter deckAdapter;
//    private FavFolderAdapter favFolderAdapter;
//    private RecyclerView recyclerView;
//
//    private TextView tvTabFavFolders, tvTabDecks;
//
//    private boolean isFavFolderTabActive = true; // Trạng thái hiện tại: true = Tab FavFolders, false = Tab Decks
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_library, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        viewModel = new ViewModelProvider(this).get(LibraryViewModel.class);
//
//        // Ánh xạ View
//        recyclerView = view.findViewById(R.id.rv_library_list);
//        tvTabFavFolders = view.findViewById(R.id.tv_tab_favfolders);
//        tvTabDecks = view.findViewById(R.id.tv_tab_decks);
//        ImageButton btnAdd = view.findViewById(R.id.btn_library_add);
//
//        // Setup Adapter
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        // deck adapter
//        deckAdapter = new DeckAdapter();
//
//        // favfolder adapter
//        favFolderAdapter = new FavFolderAdapter(folder -> {
//            // Tạo Bundle chứa ID của folder
//            Bundle bundle = new Bundle();
//            bundle.putSerializable("FOLDER_ID", folder.getFolderId());
//
//            // Chuyển sang màn hình FavDetailFragment và mang theo bundle
//            androidx.navigation.Navigation.findNavController(requireView())
//                    .navigate(R.id.favDetailFragment, bundle);
//        });
//
//        // Lắng nghe dữ liệu
//        viewModel.getDecks().observe(getViewLifecycleOwner(), decks -> {
//            deckAdapter.setDecks(decks);
//        });
//
//        viewModel.getFavFolders().observe(getViewLifecycleOwner(), folders -> {
//            favFolderAdapter.setFolders(folders);
//        });
//
//        // Sự kiện Click khi chuyển Tabs chế độ
//        tvTabFavFolders.setOnClickListener(v -> switchTab(true));
//        tvTabDecks.setOnClickListener(v -> switchTab(false));
//
//        // Nút Thêm
//        btnAdd.setOnClickListener(v -> {
//            showAddDialog(isFavFolderTabActive);
//        });
//
//        // Mở app lên thì mặc định gọi tab Favorite Folder
//        switchTab(true);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        // Mỗi khi màn hình Library hiển thị lại, ép ViewModel lấy data mới nhất từ DB
//        if (viewModel != null) {
//            viewModel.loadDecks();
//            viewModel.loadFavFolders();
//        }
//    }
//
//    // Hàm đổi Tab
//    private void switchTab(boolean toFavFolder) {
//        isFavFolderTabActive = toFavFolder;
//
//        if (toFavFolder) {
//            // Đổi nút bấm
//            tvTabFavFolders.setBackgroundResource(R.drawable.bg_rounded_white);
//            tvTabDecks.setBackground(null);
//
//            // Đổi Adapter của RecyclerView thành FavFolder
//            recyclerView.setAdapter(favFolderAdapter);
//        } else {
//            // Đổi nút bấm
//            tvTabDecks.setBackgroundResource(R.drawable.bg_rounded_white);
//            tvTabFavFolders.setBackground(null);
//
//            // Đổi Adapter của RecyclerView thành Deck
//            recyclerView.setAdapter(deckAdapter);
//        }
//    }
//
//    private void showAddDialog(boolean isFavFolderTab){
//        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//
//        // Lấy tiêu đề từ resources tùy thuộc vào tab đang mở
//        int titleResId = isFavFolderTab ? R.string.dialog_title_add_folder : R.string.dialog_title_add_deck;
//        builder.setTitle(titleResId);
//
//        // Tạo ô nhập liệu
//        final EditText input = new EditText(requireContext());
//        input.setPadding(60, 40, 60, 40);
//        builder.setView(input);
//
//        // Nút Create
//        builder.setPositiveButton(R.string.action_create, (dialog, which) -> {
//            String enteredName = input.getText().toString().trim();
//
//            // Chỉ tạo mới nếu người dùng thực sự nhập tên
//            if (!enteredName.isEmpty()) {
//                if (isFavFolderTab) {
//                    FavFolder newFolder = new FavFolder();
//                    newFolder.setFolderId(UUID.randomUUID());
//                    newFolder.setFolderName(enteredName); // Gán tên người dùng nhập
//                    newFolder.setCreatedAt(new Date());
//                    viewModel.addNewFavFolder(newFolder);
//                } else {
//                    Deck newDeck = new Deck();
//                    newDeck.setDeckId(UUID.randomUUID());
//                    newDeck.setDeckName(enteredName); // Gán tên người dùng nhập
//                    newDeck.setCreatedAt(new Date());
//                    viewModel.addNewDeck(newDeck);
//                }
//            }
//        });
//
//        // Nút Cancel
//        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.cancel());
//
//        builder.show();
//    }
//}

package com.example.memoria.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.memoria.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class LibraryFragment extends Fragment {

    private TabLayout tlLibraryTabs;
    private ViewPager2 vpLibraryPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        tlLibraryTabs = view.findViewById(R.id.tl_library_tabs);
        vpLibraryPager = view.findViewById(R.id.vp_library_pager);

        // Cài đặt Adapter
        LibraryPagerAdapter pagerAdapter = new LibraryPagerAdapter(this);
        vpLibraryPager.setAdapter(pagerAdapter);

        // Kết nối Tab và ViewPager
        new TabLayoutMediator(tlLibraryTabs, vpLibraryPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.tab_favfolders);
            } else {
                tab.setText(R.string.tab_decks);
            }
        }).attach();
    }
}