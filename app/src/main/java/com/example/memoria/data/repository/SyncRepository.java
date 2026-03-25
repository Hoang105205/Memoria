package com.example.memoria.data.repository;

import com.example.memoria.data.database.dao.CardDao;
import com.example.memoria.data.database.dao.DeckDao;
import com.example.memoria.data.database.dao.FavDao;
import com.example.memoria.data.model.Card;
import com.example.memoria.data.model.Deck;
import com.example.memoria.data.model.FavFolder;
import com.example.memoria.data.model.FavWord;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SyncRepository {
    private final FavDao favDao;
    private final DeckDao deckDao;
    private final CardDao cardDao;

    private final FirebaseFirestore firestore;
    private final ExecutorService executor;

    @Inject
    public SyncRepository(FavDao favDao, DeckDao deckDao, CardDao cardDao, FirebaseFirestore firestore) {
        this.favDao = favDao;
        this.deckDao = deckDao;
        this.cardDao = cardDao;
        // this.quizDao = quizDao;
        this.firestore = firestore;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }

    // --- Hàm 1: Đồng bộ Favorite (Folder & Word) ---
    public void syncFavorites(String userId, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            List<FavFolder> unsyncedFolders = favDao.getUnsyncedFolders();
            List<FavWord> unsyncedWords = favDao.getUnsyncedWords();

            if (unsyncedFolders.isEmpty() && unsyncedWords.isEmpty()) {
                if (callback != null) callback.onDataLoaded(true);
                return;
            }

            WriteBatch batch = firestore.batch();

            // 1. Chuẩn bị dữ liệu Folder: users/{userId}/fav_folders/{folderId}
            for (FavFolder folder : unsyncedFolders) {
                String folderIdStr = folder.getFolderId().toString();

                DocumentReference folderRef = firestore
                        .collection("users").document(userId)
                        .collection("fav_folders").document(folderIdStr);

                if (folder.getSyncStatus() == 2) {
                    batch.delete(folderRef); // Ra lệnh xóa trên Firestore
                } else {
                    folder.setSyncStatus(1);
                    folder.setFirestoreId(folderIdStr);
                    batch.set(folderRef, folder); // Ra lệnh thêm/sửa
                }
            }

            // 2. Chuẩn bị dữ liệu Word: users/{userId}/fav_folders/{folderId}/fav_words/{wordId}
            for (FavWord word : unsyncedWords) {
                String folderIdStr = word.getFolderId().toString(); // Lấy ID của thư mục chứa từ này
                String wordIdStr = word.getFavId().toString();

                DocumentReference wordRef = firestore
                        .collection("users").document(userId)
                        .collection("fav_folders").document(folderIdStr) // Lồng vào trong fav_folders
                        .collection("fav_words").document(wordIdStr);    // Collection con: fav_words

                if (word.getSyncStatus() == 2) {
                    batch.delete(wordRef);
                } else {
                    word.setSyncStatus(1);
                    word.setFirestoreId(wordIdStr);
                    batch.set(wordRef, word);
                }
            }

            // 3. Thực thi Batch, tận dụng để xóa dưới DB luôn
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        executor.execute(() -> {
                            for (FavFolder folder : unsyncedFolders) {
                                if (folder.getSyncStatus() == 2) {
                                    favDao.deleteFolder(folder); // Xóa thật dưới Room
                                } else {
                                    favDao.updateFolder(folder); // Lưu lại trạng thái 1
                                }
                            }
                            for (FavWord word : unsyncedWords) {
                                if (word.getSyncStatus() == 2) {
                                    favDao.deleteWord(word); // Xóa thật dưới Room
                                } else {
                                    favDao.updateWord(word); // Lưu lại trạng thái 1
                                }
                            }
                            if (callback != null) callback.onDataLoaded(true);
                        });
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        if (callback != null) callback.onDataLoaded(false);
                    });
        });
    }

    // --- Hàm 2: Đồng bộ Học tập (Deck & Card) ---
    public void syncDecksAndCards(String userId, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            List<Deck> unsyncedDecks = deckDao.getUnsyncedDecks();
            List<Card> unsyncedCards = cardDao.getUnsyncedCards();

            if (unsyncedDecks.isEmpty() && unsyncedCards.isEmpty()) {
                if (callback != null) callback.onDataLoaded(true);
                return;
            }

            WriteBatch batch = firestore.batch();

            // 1. Chuẩn bị dữ liệu Deck: users/{userId}/decks/{deckId}
            for (Deck deck : unsyncedDecks) {
                String deckIdStr = deck.getDeckId().toString();

                DocumentReference deckRef = firestore
                        .collection("users").document(userId)
                        .collection("decks").document(deckIdStr);

                if (deck.getSyncStatus() == 2) {
                    batch.delete(deckRef); // Ra lệnh xóa trên Firestore
                } else {
                    deck.setSyncStatus(1);
                    deck.setFirestoreId(deckIdStr);
                    batch.set(deckRef, deck); // Ra lệnh thêm/sửa
                }
            }

            // 2. Chuẩn bị dữ liệu Card: users/{userId}/decks/{deckId}/cards/{cardId}
            for (Card card : unsyncedCards) {
                String deckIdStr = card.getDeckId().toString(); // Lấy ID của bộ thẻ chứa thẻ này
                String cardIdStr = card.getCardId().toString();

                DocumentReference cardRef = firestore
                        .collection("users").document(userId)
                        .collection("decks").document(deckIdStr)
                        .collection("cards").document(cardIdStr);

                if (card.getSyncStatus() == 2) {
                    batch.delete(cardRef);
                } else {
                    card.setSyncStatus(1);
                    card.setFirestoreId(cardIdStr);
                    batch.set(cardRef, card);
                }
            }

            // 3. Thực thi Batch và tận dụng để xóa DB
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        executor.execute(() -> {
                            // Cập nhật lại trạng thái hoặc xóa thật dưới Room DB
                            for (Deck deck : unsyncedDecks) {
                                if (deck.getSyncStatus() == 2) {
                                    deckDao.deleteDeck(deck); // Xóa thật (Sẽ trigger CASCADE xóa các Card local)
                                } else {
                                    deckDao.updateDeck(deck); // Lưu lại trạng thái 1
                                }
                            }
                            for (Card card : unsyncedCards) {
                                if (card.getSyncStatus() == 2) {
                                    cardDao.deleteCard(card); // Xóa thật
                                } else {
                                    cardDao.updateCard(card); // Lưu lại trạng thái 1
                                }
                            }
                            if (callback != null) callback.onDataLoaded(true);
                        });
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        if (callback != null) callback.onDataLoaded(false);
                    });
        });
    }

    // --- Hàm 3: Đồng bộ Lịch sử & Thống kê (QuizStat & QuizHis) ---
    public void syncQuizData(String userId, DataCallback<Boolean> callback) {
        // Chứa WriteBatch xử lý QuizStat và QuizHis
    }
}
