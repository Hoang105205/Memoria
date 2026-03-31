package com.example.memoria.data.repository;

import com.example.memoria.data.database.dao.FavDao;
import com.example.memoria.data.model.entity.FavFolder;
import com.example.memoria.data.model.entity.FavFolderWithCount;
import com.example.memoria.data.model.entity.FavWord;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FavRepository {
    private final FavDao favDao;
    private final ExecutorService executor;
    private final FirebaseFirestore firestore;

    @Inject
    public FavRepository(FavDao favDao, FirebaseFirestore firestore) {
        this.favDao = favDao;
        this.firestore = firestore;
        executor = Executors.newSingleThreadExecutor();
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }

    public void getAllFolders(DataCallback<List<FavFolder>> callback) {
        executor.execute(() -> {
            List<FavFolder> data = favDao.getAllFolders();
            callback.onDataLoaded(data);
        });
    }

    public void insertFolder(FavFolder folder, Runnable onComplete) {
        executor.execute(() -> {
            folder.setSyncStatus(0);
            favDao.insertFolder(folder);

            if (onComplete != null) onComplete.run();
        });
    }

    public void getFolderById(UUID folderId, DataCallback<FavFolder> callback) {
        executor.execute(() -> {
            FavFolder folder = favDao.getFavFolderById(folderId);
            callback.onDataLoaded(folder);
        });
    }

    public void updateFolder(FavFolder folder, Runnable onComplete) {
        executor.execute(() -> {
            folder.setSyncStatus(0);
            favDao.updateFolder(folder);

            // Báo cáo đã ghi DB Local xong
            if (onComplete != null) onComplete.run();
        }); // name only
    }

    public void deleteFolder(FavFolder folder, Runnable onComplete) {
        executor.execute(() -> {
            folder.setSyncStatus(2); // Chuyển thành trạng thái Chờ xóa
            favDao.updateFolder(folder);

            // Báo cáo đã ghi DB Local xong
            if (onComplete != null) onComplete.run();
        });
    }

    public void insertWord(FavWord word, Runnable onComplete) {
        executor.execute(() -> {
            word.setSyncStatus(0);
            favDao.insertWord(word);

            if (onComplete != null) onComplete.run();
        });
    }

    public void getWordsByFolder(UUID folderId, DataCallback<List<FavWord>> callback) {
        executor.execute(() -> {
            List<FavWord> words = favDao.getWordsByFolder(folderId);
            callback.onDataLoaded(words);
        });
    }

    public void updateWord(FavWord word, Runnable onComplete) {
        executor.execute(() -> {
            word.setSyncStatus(0);
            favDao.updateWord(word);

            if (onComplete != null) onComplete.run();
        });
    }

    public void deleteWord(FavWord word, Runnable onComplete) {
        executor.execute(() -> {
            word.setSyncStatus(2);
            favDao.updateWord(word);

            if (onComplete != null) onComplete.run();
        });
    }

    public void getFoldersWithWordCount(DataCallback<List<FavFolderWithCount>> callback) {
        executor.execute(() -> {
            List<FavFolderWithCount> data = favDao.getFoldersWithWordCount();
            callback.onDataLoaded(data);
        });
    }

    public void insertWordIfNotExists(FavWord word, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            // Kiểm tra số lượng
            int count = favDao.checkWordExist(word.getFolderId(), word.getWordText());

            if (count == 0) {
                word.setSyncStatus(0);
                favDao.insertWord(word); // Chưa có thì thêm vào
                if (callback != null) callback.onDataLoaded(true); // Trả về true (Thành công)
            } else {
                if (callback != null) callback.onDataLoaded(false); // Trả về false (Đã tồn tại)
            }
        });
    }

    // Hàm thực hiện đồng bộ (Chạy trên Background Thread của Executor)
//    public void syncDataToCloud(String userId, DataCallback<Boolean> callback) {
//        executor.execute(() -> {
//            List<FavFolder> unsyncedFolders = favDao.getUnsyncedFolders();
//            List<FavWord> unsyncedWords = favDao.getUnsyncedWords();
//
//            if (unsyncedFolders.isEmpty() && unsyncedWords.isEmpty()) {
//                if (callback != null) callback.onDataLoaded(true);
//                return;
//            }
//
//            WriteBatch batch = firestore.batch();
//
//            // 1. Chuẩn bị dữ liệu Folder: users/{userId}/fav_folders/{folderId}
//            for (FavFolder folder : unsyncedFolders) {
//                String folderIdStr = folder.getFolderId().toString();
//
//                DocumentReference folderRef = firestore
//                        .collection("users").document(userId)
//                        .collection("fav_folders").document(folderIdStr);
//
//                if (folder.getSyncStatus() == 2) {
//                    batch.delete(folderRef); // Ra lệnh xóa trên Firestore
//                } else {
//                    folder.setSyncStatus(1);
//                    folder.setFirestoreId(folderIdStr);
//                    batch.set(folderRef, folder); // Ra lệnh thêm/sửa
//                }
//            }
//
//            // 2. Chuẩn bị dữ liệu Word: users/{userId}/fav_folders/{folderId}/fav_words/{wordId}
//            for (FavWord word : unsyncedWords) {
//                String folderIdStr = word.getFolderId().toString(); // Lấy ID của thư mục chứa từ này
//                String wordIdStr = word.getFavId().toString();
//
//                DocumentReference wordRef = firestore
//                        .collection("users").document(userId)
//                        .collection("fav_folders").document(folderIdStr) // Lồng vào trong fav_folders
//                        .collection("fav_words").document(wordIdStr);    // Collection con: fav_words
//
//                if (word.getSyncStatus() == 2) {
//                    batch.delete(wordRef);
//                } else {
//                    word.setSyncStatus(1);
//                    word.setFirestoreId(wordIdStr);
//                    batch.set(wordRef, word);
//                }
//            }
//
//            // 3. Thực thi Batch
//            batch.commit()
//                    .addOnSuccessListener(aVoid -> {
//                        executor.execute(() -> {
//                            for (FavFolder folder : unsyncedFolders) {
//                                if (folder.getSyncStatus() == 2) {
//                                    favDao.deleteFolder(folder); // Xóa thật dưới Room
//                                } else {
//                                    favDao.updateFolder(folder); // Lưu lại trạng thái 1
//                                }
//                            }
//                            for (FavWord word : unsyncedWords) {
//                                if (word.getSyncStatus() == 2) {
//                                    favDao.deleteWord(word); // Xóa thật dưới Room
//                                } else {
//                                    favDao.updateWord(word); // Lưu lại trạng thái 1
//                                }
//                            }
//                            if (callback != null) callback.onDataLoaded(true);
//                        });
//                    })
//                    .addOnFailureListener(e -> {
//                        e.printStackTrace();
//                        if (callback != null) callback.onDataLoaded(false);
//                    });
//        });
//    }
}