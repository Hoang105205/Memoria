package com.example.memoria.ui.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.data.model.Card;
import com.example.memoria.data.model.Deck;
import com.example.memoria.data.repository.DeckRepository;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DeckDetailViewModel extends ViewModel {
    private final DeckRepository repository;
    private final MutableLiveData<Deck> currentDeck = new MutableLiveData<>();

    @Inject
    public DeckDetailViewModel(DeckRepository repository) {
        this.repository = repository;
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
            repository.updateDeck(deck);
            currentDeck.setValue(deck); // Cập nhật ngay lên UI
        }
    }

    // Xóa Deck
    public void deleteCurrentDeck() {
        Deck deck = currentDeck.getValue();
        if (deck != null) {
            repository.deleteDeck(deck);
        }
    }
}