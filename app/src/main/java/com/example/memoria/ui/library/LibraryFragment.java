package com.example.memoria.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.Deck;
import com.example.memoria.data.model.FavFolder;
import com.example.memoria.ui.adapter.CollectionAdapter;
import com.example.memoria.ui.adapter.DeckAdapter;

import java.util.Date;
import java.util.UUID;

public class LibraryFragment extends Fragment {

    private LibraryViewModel viewModel;
    private DeckAdapter deckAdapter;
    private CollectionAdapter collectionAdapter;
    private RecyclerView recyclerView;

    private TextView tvTabCollections, tvTabDecks;

    private boolean isCollectionTabActive = true; // Trạng thái hiện tại: true = Tab Collections, false = Tab Decks

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LibraryViewModel.class);

        // Ánh xạ View
        recyclerView = view.findViewById(R.id.rv_library_list);
        tvTabCollections = view.findViewById(R.id.tv_tab_collections);
        tvTabDecks = view.findViewById(R.id.tv_tab_decks);
        ImageButton btnAdd = view.findViewById(R.id.btn_add_deck);

        // Setup Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // deck adapter
        deckAdapter = new DeckAdapter();

        // collection adapter
        collectionAdapter = new CollectionAdapter(folder -> {
            // Tạo Bundle chứa ID của folder
            Bundle bundle = new Bundle();
            bundle.putSerializable("FOLDER_ID", folder.getFolderId());

            // Chuyển sang màn hình FavDetailFragment và mang theo bundle
            androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(R.id.favDetailFragment, bundle);
        });

        // Lắng nghe dữ liệu
        viewModel.getDecks().observe(getViewLifecycleOwner(), decks -> {
            deckAdapter.setDecks(decks);
        });

        viewModel.getCollections().observe(getViewLifecycleOwner(), folders -> {
            collectionAdapter.setFolders(folders);
        });

        // Sự kiện Click khi chuyển Tabs chế độ
        tvTabCollections.setOnClickListener(v -> switchTab(true));
        tvTabDecks.setOnClickListener(v -> switchTab(false));

        // Nút Thêm (Logic rẽ nhánh tTùy thuộc tab nào đang mở)
        btnAdd.setOnClickListener(v -> {
            if (isCollectionTabActive) {
                // Tạo FavFolder mới (0 words)
                FavFolder newFolder = new FavFolder();
                newFolder.setFolderId(UUID.randomUUID());
                newFolder.setFolderName("Collection " + System.currentTimeMillis());
                newFolder.setCreatedAt(new Date());
                viewModel.addNewCollection(newFolder);
            } else {
                // Tạo Deck mới (Empty deck)
                Deck newDeck = new Deck();
                newDeck.setDeckId(UUID.randomUUID());
                newDeck.setDeckName("Deck " + System.currentTimeMillis());
                newDeck.setCreatedAt(new Date());
                viewModel.addNewDeck(newDeck);
            }
        });

        // Mở app lên thì mặc định gọi tab Collection
        switchTab(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Mỗi khi màn hình Library hiển thị lại, ép ViewModel lấy data mới nhất từ DB
        if (viewModel != null) {
            viewModel.loadDecks();
            viewModel.loadCollections();
        }
    }

    // Hàm đổi Tab
    private void switchTab(boolean toCollection) {
        isCollectionTabActive = toCollection;

        if (toCollection) {
            // Đổi nút bấm
            tvTabCollections.setBackgroundResource(R.drawable.bg_rounded_white);
            tvTabDecks.setBackground(null);

            // Đổi Adapter của RecyclerView thành Collection
            recyclerView.setAdapter(collectionAdapter);
        } else {
            // Đổi nút bấm
            tvTabDecks.setBackgroundResource(R.drawable.bg_rounded_white);
            tvTabCollections.setBackground(null);

            // Đổi Adapter của RecyclerView thành Deck
            recyclerView.setAdapter(deckAdapter);
        }
    }
}