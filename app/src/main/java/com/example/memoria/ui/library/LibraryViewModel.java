package com.example.memoria.ui.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.data.model.entity.Deck;
import com.example.memoria.data.model.entity.DeckWithCount;
import com.example.memoria.data.model.entity.FavFolder;
import com.example.memoria.data.model.entity.FavFolderWithCount;
import com.example.memoria.data.repository.DeckRepository;
import com.example.memoria.data.repository.FavRepository;

import android.content.Context;
import dagger.hilt.android.qualifiers.ApplicationContext;
import com.example.memoria.utils.SyncHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LibraryViewModel extends ViewModel {
    private final DeckRepository deckRepository;
    private final FavRepository favRepository;
    private final Context context;

    private final MutableLiveData<List<DeckWithCount>> decks = new MutableLiveData<>();
    private final MutableLiveData<List<FavFolderWithCount>> folders = new MutableLiveData<>();

    private String currentSearchKeyword = "";

    public LiveData<List<FavFolderWithCount>> getFavFolders() { return folders; }

    public LiveData<List<DeckWithCount>> getDecks() {
        return decks;
    }

    public void loadFavFolders() {
        searchFavFolders(currentSearchKeyword);
        // Gọi hàm có đếm số lượng thay vì hàm cũ
        // favRepository.getFoldersWithWordCount(folders::postValue);
    }

    public void loadDecks() {
        searchDecks(currentSearchKeyword);
    }

    public void searchFavFolders(String keyword) {
        this.currentSearchKeyword = keyword; // Lưu lại trạng thái

        if (keyword == null || keyword.trim().isEmpty()) {
            // Nếu ô search rỗng, lấy tất cả
            favRepository.getFoldersWithWordCount(folders::postValue);
        } else {
            // Nếu có chữ, gọi hàm search
            favRepository.searchFolders(keyword.trim(), folders::postValue);
        }
    }

    public void searchDecks(String keyword) {
        this.currentSearchKeyword = keyword; // Lưu lại trạng thái

        if (keyword == null || keyword.trim().isEmpty()) {
            // Nếu ô search rỗng, lấy tất cả
            deckRepository.getAllDecksWithCount(decks::postValue);
        } else {
            // Nếu có chữ, gọi hàm search
            deckRepository.searchDecks(keyword.trim(), decks::postValue);
        }
    }

    // Ngay khi ViewModel được khởi tạo, lấy dữ liệu từ DB
    @Inject
    public LibraryViewModel(DeckRepository deckRepository, FavRepository favRepository, @ApplicationContext Context context) {
        this.deckRepository = deckRepository;
        this.favRepository = favRepository;
        this.context = context;

        loadDecks();
        loadFavFolders();
    }

    public void addNewDeck(Deck deck) {
        deckRepository.insertDeck(deck, () -> {
            loadDecks(); // Chỉ gọi load lại UI KHI ĐÃ GHI XONG vào SQLite
            triggerSync();
        });
        loadDecks(); // gọi load lại để cập nhật lên UI
    }

    public void addNewFavFolder(FavFolder folder) {
        favRepository.insertFolder(folder, () -> {
            loadFavFolders(); // Chỉ gọi load lại UI KHI ĐÃ GHI XONG vào SQLite
            triggerSync();
        });
    }

    private void triggerSync() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            SyncHelper.triggerImmediateSync(context, user.getUid());
        }
    }
}