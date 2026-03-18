package com.example.memoria.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.data.model.FavFolder;
import com.example.memoria.data.model.FavWord;
import com.example.memoria.data.repository.FavRepository;

import android.content.Context;
import dagger.hilt.android.qualifiers.ApplicationContext;
import com.example.memoria.utils.SyncHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SearchViewModel extends ViewModel {
    // MutableLiveData cho phép chúng ta thay đổi giá trị bên trong (dùng trong ViewModel)
    private final MutableLiveData<DictionaryResponse> _searchResult = new MutableLiveData<>();
    private final FavRepository favRepository;
    private final Context context;
    private final MutableLiveData<List<FavFolder>> folders = new MutableLiveData<>();

    @Inject
    public SearchViewModel(FavRepository favRepository, @ApplicationContext Context context) {
        this.favRepository = favRepository;
        this.context = context;
    }
    
    // LiveData chỉ cho phép đọc (expose ra bên ngoài cho Fragment quan sát)
    public LiveData<DictionaryResponse> getSearchResult() {
        return _searchResult;
    }

    // Hàm này để các module (Text, Voice, OCR) đẩy kết quả vào
    public void setSearchResult(DictionaryResponse result) {
        _searchResult.setValue(result);
    }

    // --- Các hàm xử lý sự kiện yêu cầu lưu vào thư mục favorite ---
    public LiveData<List<FavFolder>> getFavFolders() {
        return folders;
    }

    public void loadFavFolders() {
        favRepository.getAllFolders(folders::postValue);
    }

    public void addNewFavFolder(FavFolder folder) {
        favRepository.insertFolder(folder, () -> {
            loadFavFolders(); // Chỉ gọi load lại UI KHI ĐÃ GHI XONG vào SQLite
            triggerSync();
        });
    }

    // Tách logic từ SelectFavFolderDialog ra để dễ quản lý
    public void saveCurrentWordToFolder(UUID folderId, FavRepository.DataCallback<Boolean> callback) {
        // ViewModel tự lấy dữ liệu hiện tại của nó
        DictionaryResponse currentData = _searchResult.getValue();

        if (currentData != null && currentData.word != null) {
            String pos = "";
            String shortMeaning = "";

            // Logic bóc tách dữ liệu nay đã được mang vào ViewModel
            if (currentData.meanings != null && !currentData.meanings.isEmpty()) {
                Meaning firstMeaning = currentData.meanings.get(0);
                pos = firstMeaning.partOfSpeech;
                if (firstMeaning.definitions != null && !firstMeaning.definitions.isEmpty()) {
                    shortMeaning = firstMeaning.definitions.get(0).definition;
                }
            }

            // Khởi tạo Object
            FavWord newWord = new FavWord();
            newWord.setFavId(UUID.randomUUID());
            newWord.setFolderId(folderId);
            newWord.setWordText(currentData.word);
            newWord.setPartOfSpeech(pos);
            newWord.setShortMeaning(shortMeaning);
            newWord.setAddedAt(new Date());
            newWord.setPinStatus(false);

            // Gọi Repository để lưu
            favRepository.insertWordIfNotExists(newWord, callback);
        }
    }

    // <--- Logic phuc vu goi search tu cac tab khac) --->
    private final MutableLiveData<String> externalSearchQuery = new MutableLiveData<>();

    public LiveData<String> getExternalSearchQuery() {
        return externalSearchQuery;
    }

    public void setExternalSearchQuery(String query) {
        externalSearchQuery.setValue(query);
    }

    private void triggerSync() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            SyncHelper.triggerImmediateSync(context, user.getUid());
        }
    }
}
