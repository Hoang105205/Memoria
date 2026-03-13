package com.example.memoria.ui.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.data.model.Card;
import com.example.memoria.data.repository.CardRepository;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CardViewModel extends ViewModel {

    private final CardRepository repository;

    @Inject
    public CardViewModel(CardRepository repository) {
        this.repository = repository;
    }

    // Lấy danh sách thẻ theo Deck ID, dùng LiveData và Serializable để có thể truyền nguyên 1 card qua fragment
    public LiveData<List<Card>> getCardsByDeckId(UUID deckId) {
        return repository.getCardsByDeckId(deckId);
    }

    public void insertCard(Card card) {
         repository.insertCard(card);
    }

    public void updateCard(Card card) {
        repository.updateCard(card);
    }

    public void deleteCard(Card card) {
        repository.deleteCard(card);
    }
}