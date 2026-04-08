package com.example.memoria.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.data.model.dto.PublicDeck;
import com.example.memoria.service.PublicService;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PublicDeckViewModel extends ViewModel {

    private final PublicService publicService;

    private final MutableLiveData<List<PublicDeck>> _publicDecks = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<PublicDeck>> publicDecks = _publicDecks;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private DocumentSnapshot lastVisibleDoc = null;
    private boolean isLastPage = false;
    private String currentSearchQuery = null;
    private final int PAGE_LIMIT = 10; // Load 10 item mỗi lần

    @Inject
    public PublicDeckViewModel(PublicService publicService) {
        this.publicService = publicService;
        loadDecks(false); // Init load trang đầu tiên
    }

    public void searchDecks(String keyword) {
        currentSearchQuery = keyword;
        // Đặt lại trạng thái phân trang khi search mới
        lastVisibleDoc = null;
        isLastPage = false;
        loadDecks(false);
    }

    public void loadDecks(boolean isLoadMore) {
        if (Boolean.TRUE.equals(_isLoading.getValue())) return; // Đang load thì chặn
        if (isLoadMore && isLastPage) return; // Hết trang thì không kéo nữa

        _isLoading.postValue(true);

        publicService.getPublicDecks(PAGE_LIMIT, lastVisibleDoc, currentSearchQuery, (success, docs, message) -> {
            _isLoading.postValue(false);

            if (success && docs != null) {
                // Đánh dấu hết trang nếu số lượng trả về ít hơn giới hạn
                if (docs.size() < PAGE_LIMIT) {
                    isLastPage = true;
                }

                // Lưu lại mốc cuối cùng để lần sau cuộn sẽ kéo tiếp từ đây
                if (!docs.isEmpty()) {
                    lastVisibleDoc = docs.get(docs.size() - 1);
                }

                // Map DocumentSnapshot sang PublicDeck
                List<PublicDeck> mappedDecks = new ArrayList<>();
                for (DocumentSnapshot doc : docs) {
                    PublicDeck deck = doc.toObject(PublicDeck.class);
                    if (deck != null) {
                        deck.setPublicDocId(doc.getId());
                        mappedDecks.add(deck);
                    }
                }

                // Nếu là load mới thì chèn thêm, nếu search/refresh thì làm mới list
                if (isLoadMore) {
                    List<PublicDeck> currentList = _publicDecks.getValue();
                    if (currentList == null) currentList = new ArrayList<>();
                    currentList.addAll(mappedDecks);
                    _publicDecks.postValue(currentList);
                } else {
                    _publicDecks.postValue(mappedDecks);
                }

            } else {
                _errorMessage.postValue(message);
            }
        });
    }
}