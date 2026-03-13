package com.example.memoria.ui.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.data.model.Deck;
import com.example.memoria.data.model.DeckWithCount;
import com.example.memoria.data.model.FavFolder;
import com.example.memoria.data.model.FavFolderWithCount;
import com.example.memoria.data.repository.DeckRepository;
import com.example.memoria.data.repository.FavRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LibraryViewModel extends ViewModel {
    private final DeckRepository deckRepository;
    private final FavRepository favRepository;

    private final MutableLiveData<List<DeckWithCount>> decks = new MutableLiveData<>();
    private final MutableLiveData<List<FavFolderWithCount>> folders = new MutableLiveData<>();

    public LiveData<List<FavFolderWithCount>> getFavFolders() { return folders; }

    public LiveData<List<DeckWithCount>> getDecks() {
        return decks;
    }

    public void loadFavFolders() {
        // Gọi hàm có đếm số lượng thay vì hàm cũ
        favRepository.getFoldersWithWordCount(folders::postValue);
    }

    // Ngay khi ViewModel được khởi tạo, lấy dữ liệu từ DB
    @Inject
    public LibraryViewModel(DeckRepository deckRepository, FavRepository favRepository) {
        this.deckRepository = deckRepository;
        this.favRepository = favRepository;

        loadDecks();
        loadFavFolders();
    }

    public void loadDecks() {
        // Đổi hàm gọi sang getAllDecksWithCount để lấy cả số thẻ trong deck
        deckRepository.getAllDecksWithCount(decks::postValue);
    }

    public void addNewDeck(Deck deck) {
        deckRepository.insertDeck(deck);
        loadDecks(); // gọi load lại để cập nhật lên UI
    }

    public void addNewFavFolder(FavFolder folder) {
        favRepository.insertFolder(folder);
        loadFavFolders(); // gọi load lại để cập nhật lên UI
    }
}