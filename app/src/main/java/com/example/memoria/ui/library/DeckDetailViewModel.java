package com.example.memoria.ui.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.content.Context;

import com.example.memoria.data.model.entity.Deck;
import com.example.memoria.data.repository.CardRepository;
import com.example.memoria.data.repository.DeckRepository;
import com.example.memoria.service.PublicService;
import com.example.memoria.utils.SyncHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class DeckDetailViewModel extends ViewModel {
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final PublicService publicService;
    private final MutableLiveData<Deck> currentDeck = new MutableLiveData<>();
    private final Context context;

    // Các trạng thái liên quan đến Publish
    private final MutableLiveData<Boolean> isPublishing = new MutableLiveData<>(false);
    private final MutableLiveData<String> publishMessage = new MutableLiveData<>();

    @Inject
    public DeckDetailViewModel(DeckRepository deckRepository, CardRepository cardRepository, PublicService publicService, @ApplicationContext Context context) {
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
        this.publicService = publicService;
        this.context = context;
    }

    public LiveData<Deck> getDeck() {
        return currentDeck;
    }

    public LiveData<Boolean> getIsPublishing() {
        return isPublishing;
    }

    public LiveData<String> getPublishMessage() {
        return publishMessage;
    }

    public void clearPublishMessage() {
        publishMessage.setValue(null); // Reset message sau khi đã hiển thị Toast
    }

    // Tải thông tin của Deck
    public void loadDeck(UUID deckId) {
        deckRepository.getDeckById(deckId, currentDeck::postValue);
    }

    // Đổi tên Deck
    public void updateDeckName(String newName) {
        Deck deck = currentDeck.getValue();
        if (deck != null) {
            deck.setDeckName(newName);
            deckRepository.updateDeck(deck, () -> {
                currentDeck.postValue(deck);
                triggerSync();
            });
        }
    }

    // Xóa Deck
    public void deleteCurrentDeck() {
        Deck deck = currentDeck.getValue();
        if (deck != null) {
            deckRepository.deleteDeck(deck, () -> {
                cardRepository.markCardsForDeleted(deck.getDeckId(), () -> triggerSync());
            });
        }
    }

    public void updateDeckTheme(String newColor) {
        Deck deck = currentDeck.getValue();
        if (deck != null) {
            deck.setCoverColor(newColor);
            deckRepository.updateDeck(deck, () -> {
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

    // Hàm xử lý Publish
    public void publishCurrentDeck() {
        Deck deck = currentDeck.getValue();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (deck == null) {
            publishMessage.postValue("Lỗi: Không tìm thấy bộ thẻ!");
            return;
        }

        if (user == null) {
            publishMessage.postValue("Lỗi: Bạn cần đăng nhập để chia sẻ bộ thẻ!");
            return;
        }

        // Bật trạng thái Loading
        isPublishing.postValue(true);

        String userId = user.getUid();
        // Lấy tên hiển thị của user, nếu không có thì lấy phần đầu của Email làm tên
        String authorName = user.getDisplayName();
        if (authorName == null || authorName.isEmpty()) {
            authorName = user.getEmail() != null ? user.getEmail().split("@")[0] : "Anonymous";
        }
        String localDeckId = deck.getDeckId().toString();

        // Gọi Service đẩy dữ liệu lên Firebase
        publicService.publishDeck(userId, authorName, localDeckId, (success, data, message) -> {
            // Tắt trạng thái Loading và gửi thông báo về UI
            isPublishing.postValue(false);
            publishMessage.postValue(message);
        });
    }
}