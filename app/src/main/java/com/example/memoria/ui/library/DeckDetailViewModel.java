package com.example.memoria.ui.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.content.Context;

import com.example.memoria.data.model.entity.Deck;
import com.example.memoria.data.repository.DeckRepository;
import com.example.memoria.utils.SyncHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class DeckDetailViewModel extends ViewModel {
    private final DeckRepository repository;
    private final MutableLiveData<Deck> currentDeck = new MutableLiveData<>();
    private final Context context;

    @Inject
    public DeckDetailViewModel(DeckRepository repository, @ApplicationContext Context context) {
        this.repository = repository;
        this.context = context;
    }

    public LiveData<Deck> getDeck() {
        return currentDeck;
    }

    // Tải thông tin của Deck
    public void loadDeck(UUID deckId) {
        repository.getDeckById(deckId, currentDeck::postValue);
    }

    // Đổi tên Deck
    public void updateDeckName(String newName) {
        Deck deck = currentDeck.getValue();
        if (deck != null) {
            deck.setDeckName(newName);
            repository.updateDeck(deck, () -> {
                currentDeck.postValue(deck);
                triggerSync();
            });
        }
    }

    // Xóa Deck
    public void deleteCurrentDeck() {
        Deck deck = currentDeck.getValue();
        if (deck != null) {
            repository.deleteDeck(deck, () -> {
                triggerSync();
            });
        }
    }

    public void updateDeckTheme(String newColor) {
        Deck deck = currentDeck.getValue();
        if (deck != null) {
            deck.setCoverColor(newColor);
            repository.updateDeck(deck, () -> {
                currentDeck.postValue(deck); // Update LiveData
                triggerSync();
            });
        }
    }

    private void triggerSync() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Chỉ cần user đang đăng nhập, tự động kích hoạt tiến trình sync và delete
            SyncHelper.triggerImmediateSync(context, user.getUid());
        }
    }
}