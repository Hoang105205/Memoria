package com.example.memoria.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.dto.PublicDeck;
import com.example.memoria.ui.adapter.PublicDeckDetailAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PublicDeckFragment extends Fragment {
    private PublicDeckViewModel viewModel;
    private PublicDeckDetailAdapter adapter;
    private RecyclerView rvPublicDecks;
    private EditText etSearch;

    // Handler dùng để tạo độ trễ (debounce) khi tìm kiếm
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public PublicDeckFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deck_public, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel (Hilt sẽ tự lo tiêm PublicService vào đây)
        viewModel = new ViewModelProvider(this).get(PublicDeckViewModel.class);

        // Ánh xạ
        rvPublicDecks = view.findViewById(R.id.rv_public_decks);
        etSearch = view.findViewById(R.id.et_search_deck);

        setupRecyclerView();
        setupSearch();
        observeViewModel();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvPublicDecks.setLayoutManager(layoutManager);

        adapter = new PublicDeckDetailAdapter(new PublicDeckDetailAdapter.OnDeckClickListener() {
            @Override
            public void onDeckClick(PublicDeck deck) {
                // TODO: Chuyển qua màn hình Preview Card
                Toast.makeText(getContext(), "Preview: " + deck.getDeckName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadClick(PublicDeck deck) {
                // TODO: Chuyển sang tính năng Download qua CloneService
                Toast.makeText(getContext(), "Downloading: " + deck.getDeckName(), Toast.LENGTH_SHORT).show();
            }
        });

        rvPublicDecks.setAdapter(adapter);

        // Lắng nghe sự kiện cuộn để xử lý phân trang (Load more)
        rvPublicDecks.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) { // Đang cuộn xuống
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Nếu người dùng cuộn đến gần cuối list -> Kích hoạt load page tiếp theo
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2) {
                        viewModel.loadDecks(true);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboardAndClearFocus();
                }
            }
        });

        rvPublicDecks.setOnTouchListener((v, event) -> {
            hideKeyboardAndClearFocus();
            return false; // Trả về false để không chặn sự kiện click vào các item bên trong
        });
    }

    private void hideKeyboardAndClearFocus() {
        // Bỏ nhấp nháy con trỏ ở ô search
        etSearch.clearFocus();

        // Gọi hệ thống thu bàn phím xuống
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Hủy lệnh tìm kiếm cũ nếu người dùng vẫn đang gõ liên tục
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Tạo lệnh tìm kiếm mới sau 500ms (Debounce)
                searchRunnable = () -> {
                    String keyword = s.toString().trim();
                    adapter.clearDecks(); // Xóa list cũ trên UI đi trước khi load kết quả mới
                    viewModel.searchDecks(keyword);
                };

                searchHandler.postDelayed(searchRunnable, 500);
            }
        });
    }

    private void observeViewModel() {
        viewModel.publicDecks.observe(getViewLifecycleOwner(), decks -> {
            if (decks != null) {
                adapter.setDecks(decks);
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Tránh rò rỉ bộ nhớ
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}