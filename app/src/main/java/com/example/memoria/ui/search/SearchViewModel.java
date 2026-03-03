package com.example.memoria.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;

public class SearchViewModel extends ViewModel {
    // MutableLiveData cho phép chúng ta thay đổi giá trị bên trong (dùng trong ViewModel)
    private final MutableLiveData<DictionaryResponse> _searchResult = new MutableLiveData<>();
    
    // LiveData chỉ cho phép đọc (expose ra bên ngoài cho Fragment quan sát)
    public LiveData<DictionaryResponse> getSearchResult() {
        return _searchResult;
    }

    // Hàm này để các module (Text, Voice, OCR) đẩy kết quả vào
    public void setSearchResult(DictionaryResponse result) {
        _searchResult.setValue(result);
    }
}
