package com.example.memoria.data.repository;

import android.util.Log;

import com.example.memoria.data.database.dao.CardDao;
import com.example.memoria.data.database.dao.DeckDao;
import com.example.memoria.data.database.dao.FavDao;
import com.example.memoria.data.database.dao.QuizDao;
import com.example.memoria.data.model.entity.Card;
import com.example.memoria.data.model.entity.Deck;
import com.example.memoria.data.model.entity.FavFolder;
import com.example.memoria.data.model.entity.FavWord;
import com.example.memoria.data.model.entity.QuizHistory;
import com.example.memoria.data.model.entity.QuizStat;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SyncRepository {
    private final FavDao favDao;
    private final DeckDao deckDao;
    private final CardDao cardDao;
    private final QuizDao quizDao;

    private final FirebaseFirestore firestore;
    private final ExecutorService executor;

    @Inject
    public SyncRepository(FavDao favDao, DeckDao deckDao, CardDao cardDao, QuizDao quizDao, FirebaseFirestore firestore) {
        this.favDao = favDao;
        this.deckDao = deckDao;
        this.cardDao = cardDao;
        this.quizDao = quizDao;
        this.firestore = firestore;
        this.executor = Executors.newSingleThreadExecutor();
    }

    private static final String TAG = "SYNC_DEBUG";

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
        executor.execute(() -> {
            List<QuizHistory> unsyncedHistories = quizDao.getUnsyncedHistories();
            QuizStat unsyncedStat = quizDao.getUnsyncedStats();

            if (unsyncedHistories.isEmpty() && unsyncedStat == null) {
                if (callback != null) callback.onDataLoaded(true);
                return;
            }

            WriteBatch batch = firestore.batch();

            // 1. Chuẩn bị dữ liệu QuizHistory: users/{userId}/quiz_histories/{resultId}
            for (QuizHistory history : unsyncedHistories) {
                String resultIdStr = history.getResultId().toString();

                DocumentReference historyRef = firestore
                        .collection("users").document(userId)
                        .collection("quiz_histories").document(resultIdStr);

                if (history.getSyncStatus() == 2) {
                    batch.delete(historyRef);
                } else {
                    history.setSyncStatus(1);
                    history.setFirestoreId(resultIdStr);

                    if (history.getExpireAt() == null) {
                        Calendar calendar = Calendar.getInstance();
                        if (history.getTakenAt() != null) {
                            calendar.setTime(history.getTakenAt());
                        }
                        calendar.add(Calendar.DAY_OF_YEAR, 30);
                        history.setExpireAt(calendar.getTime());
                    }

                    batch.set(historyRef, history);
                }
            }

            // 2. Chuẩn bị dữ liệu QuizStat: users/{userId}/quiz_stats/{statId}
            if (unsyncedStat != null) {
                String statIdStr = String.valueOf(unsyncedStat.getStatId());

                DocumentReference statRef = firestore
                        .collection("users").document(userId)
                        .collection("quiz_stats").document(statIdStr);

                if (unsyncedStat.getSyncStatus() == 2) {
                    batch.delete(statRef);
                } else {
                    unsyncedStat.setSyncStatus(1);
                    unsyncedStat.setFirestoreId(statIdStr);
                    batch.set(statRef, unsyncedStat);
                }
            }

            // 3. Thực thi Batch và cập nhật lại Local Database (Room)
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        executor.execute(() -> {
                            for (QuizHistory history : unsyncedHistories) {
                                if (history.getSyncStatus() == 2) {
                                    quizDao.deleteHistory(history);
                                } else {
                                    quizDao.updateHistory(history);
                                }
                            }

                            if (unsyncedStat != null) {
                                if (unsyncedStat.getSyncStatus() == 2) {
                                    quizDao.deleteStat(unsyncedStat);
                                } else {
                                    quizDao.updateStat(unsyncedStat);
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

    // --- Hàm Pull: Kéo dữ liệu từ Cloud về Local ---
    public void pullAllDataFromCloud(String userId, DataCallback<Boolean> callback) {
        Log.d(TAG, "🚀 Start syncing for userId = " + userId);
        // 1. Kéo FavFolders
        Task<com.google.firebase.firestore.QuerySnapshot> foldersTask = firestore
                .collection("users").document(userId).collection("fav_folders").get();

        // 2. Kéo Decks
        Task<com.google.firebase.firestore.QuerySnapshot> decksTask = firestore
                .collection("users").document(userId).collection("decks").get();

        // 3. Kéo QuizHistory
        Task<com.google.firebase.firestore.QuerySnapshot> historyTask = firestore
                .collection("users").document(userId).collection("quiz_histories").get();

        // 4. Kéo QuizStat
        Task<com.google.firebase.firestore.QuerySnapshot> statTask = firestore
                .collection("users").document(userId).collection("quiz_stats").get();

        // Chờ cả Folder và Deck tải về xong
        Tasks.whenAllSuccess(foldersTask, decksTask, historyTask, statTask)
                .addOnSuccessListener(results -> {
                    executor.execute(() -> {
                        List<Task<com.google.firebase.firestore.QuerySnapshot>> subCollectionTasks = new ArrayList<>();

                        // Xử lý FavFolders
                        for (QueryDocumentSnapshot doc : (com.google.firebase.firestore.QuerySnapshot) results.get(0)) {
                            FavFolder folder = doc.toObject(FavFolder.class);
                            folder.setFolderId(UUID.fromString(doc.getId()));
                            folder.setSyncStatus(1); // Đánh dấu đã đồng bộ
                            try {
                                favDao.insertFolder(folder);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // Tạo task để tải FavWords nằm trong Folder này
                            Task<com.google.firebase.firestore.QuerySnapshot> wordsTask = doc.getReference().collection("fav_words").get();
                            subCollectionTasks.add(wordsTask);
                        }

                        // Xử lý Decks
                        for (QueryDocumentSnapshot doc : (com.google.firebase.firestore.QuerySnapshot) results.get(1)) {
                            Deck deck = doc.toObject(Deck.class);
                            deck.setDeckId(UUID.fromString(doc.getId()));
                            deck.setSyncStatus(1); // Đánh dấu đã đồng bộ
                            try {
                                deckDao.insertDeck(deck);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // Tạo task để tải Cards nằm trong Deck này
                            Task<com.google.firebase.firestore.QuerySnapshot> cardsTask = doc.getReference().collection("cards").get();
                            subCollectionTasks.add(cardsTask);
                        }

                        // Xử lý Quiz History
                        for (QueryDocumentSnapshot doc : (com.google.firebase.firestore.QuerySnapshot) results.get(2)) {
                            QuizHistory history = doc.toObject(QuizHistory.class);
                            history.setResultId(UUID.fromString(doc.getId()));
                            history.setSyncStatus(1);
                            try {
                                quizDao.insertHistory(history);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // Xử lý Quiz Stat
                        for (QueryDocumentSnapshot doc : (com.google.firebase.firestore.QuerySnapshot) results.get(3)) {
                            QuizStat stat = doc.toObject(QuizStat.class);
                            stat.setStatId(1);
                            stat.setSyncStatus(1);
                            try {
                                quizDao.insertStat(stat);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (subCollectionTasks.isEmpty()) {
                            if (callback != null) callback.onDataLoaded(true);
                            return;
                        }

                        // Chờ tất cả các sub-collection (Words và Cards) tải về xong
                        Tasks.whenAllSuccess(subCollectionTasks).addOnSuccessListener(subResults -> {

                            executor.execute(() -> {
                                int wordCount = 0;
                                int cardCount = 0;

                                for (Object subResult : subResults) {
                                    for (QueryDocumentSnapshot subDoc : (com.google.firebase.firestore.QuerySnapshot) subResult) {
                                        // Kiểm tra xem nó là Card hay FavWord dựa vào Collection Path
                                        String path = subDoc.getReference().getPath();
                                        if (path.contains("fav_words")) {
                                            FavWord word = subDoc.toObject(FavWord.class);
                                            word.setFavId(UUID.fromString(subDoc.getId()));
                                            // Gan lai khoa ngoai
                                            String parentFolderId = subDoc.getReference().getParent().getParent().getId();
                                            word.setFolderId(UUID.fromString(parentFolderId));
                                            word.setSyncStatus(1);
                                            try {
                                                favDao.insertWord(word);
                                                wordCount++;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        } else if (path.contains("cards")) {
                                            Card card = subDoc.toObject(Card.class);
                                            card.setCardId(UUID.fromString(subDoc.getId()));
                                            // Gan lai khoa ngoai
                                            String parentDeckId = subDoc.getReference().getParent().getParent().getId();
                                            card.setDeckId(UUID.fromString(parentDeckId));
                                            card.setSyncStatus(1);
                                            try {
                                                cardDao.insertCard(card);
                                                cardCount++;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                                if (callback != null) callback.onDataLoaded(true); // Thành công toàn bộ
                            });
                        }).addOnFailureListener(e -> {
                            if (callback != null) callback.onDataLoaded(false);
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onDataLoaded(false);
                });
    }
}
