package com.example.memoria.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.data.model.FavFolder;
import com.example.memoria.data.model.FavWord;
import com.example.memoria.data.repository.FavRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SearchViewModel extends ViewModel {
    // MutableLiveData cho phép chúng ta thay đổi giá trị bên trong (dùng trong ViewModel)
    private final MutableLiveData<DictionaryResponse> _searchResult = new MutableLiveData<>();
    private final FavRepository favRepository;
    private final MutableLiveData<List<FavFolder>> folders = new MutableLiveData<>();

    @Inject
    public SearchViewModel(FavRepository favRepository) {
        this.favRepository = favRepository;
    }
    
    // LiveData chỉ cho phép đọc (expose ra bên ngoài cho Fragment quan sát)
    public LiveData<DictionaryResponse> getSearchResult() {
        return _searchResult;
    }

    // Hàm này để các module (Text, Voice, OCR) đẩy kết quả vào
    public void setSearchResult(DictionaryResponse result) {
        _searchResult.setValue(result);
    }

    // --- CÁC HÀM XỬ LÝ FAVORITE THÊM VÀO ---
    public LiveData<List<FavFolder>> getFavFolders() {
        return folders;
    }

    public void loadFavFolders() {
        favRepository.getAllFolders(folders::postValue);
    }

    public void addNewFavFolder(FavFolder folder) {
        favRepository.insertFolder(folder);
        loadFavFolders(); // Cập nhật lại UI
    }

    public void saveWordToFolder(FavWord word) {
        favRepository.insertWord(word);
    }
}
