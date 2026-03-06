package com.example.memoria.ui.library;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memoria.data.model.Deck;
import com.example.memoria.data.model.FavFolder;
import com.example.memoria.data.model.FavFolderWithCount;
import com.example.memoria.data.repository.DeckRepository;
import com.example.memoria.data.repository.FavRepository;

import java.util.List;

public class LibraryViewModel extends AndroidViewModel {
    private final DeckRepository deckRepository;
    private final FavRepository favRepository;

    private final MutableLiveData<List<Deck>> decks = new MutableLiveData<>();
    // Sửa List<FavFolder> thành List<FavFolderWithCount>
    private final MutableLiveData<List<FavFolderWithCount>> folders = new MutableLiveData<>();

    public LiveData<List<FavFolderWithCount>> getFavFolders() { return folders; }

    public void loadFavFolders() {
        // Gọi hàm có đếm số lượng thay vì hàm cũ
        favRepository.getFoldersWithWordCount(folders::postValue);
    }

    // Ngay khi ViewModel được khởi tạo, lấy dữ liệu từ DB
    public LibraryViewModel(@NonNull Application application) {
        super(application);
        deckRepository = new DeckRepository(application);
        favRepository = new FavRepository(application);

        loadDecks();
        loadFavFolders();
    }

    public LiveData<List<Deck>> getDecks() { return decks; }

    public void loadDecks() {
        deckRepository.getAllDecks(decks::postValue);
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