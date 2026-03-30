package com.example.memoria.ui.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.data.model.entity.Card;
import com.example.memoria.data.repository.CardRepository;
import com.example.memoria.utils.SyncHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.Context;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class CardViewModel extends ViewModel {

    private final CardRepository repository;
    private final Context context;

    @Inject
    public CardViewModel(CardRepository repository, @ApplicationContext Context context) {
        this.context = context;
        this.repository = repository;
    }

    // Lấy danh sách thẻ theo Deck ID, dùng LiveData và Serializable để có thể truyền nguyên 1 card qua fragment
    public LiveData<List<Card>> getCardsByDeckId(UUID deckId) {
        return repository.getCardsByDeckId(deckId);
    }

    public void insertCard(Card card) {
        repository.insertCard(card, () -> {
            triggerSync();
        });
    }

    public void updateCard(Card card) {
        repository.updateCard(card, () -> {
            triggerSync();
        });
    }

    public void deleteCard(Card card) {
        repository.deleteCard(card, () -> {
            triggerSync();
        });
    }

    private void triggerSync() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Chỉ cần user đang đăng nhập, tự động kích hoạt tiến trình sync và delete
            SyncHelper.triggerImmediateSync(context, user.getUid());
        }
    }
}