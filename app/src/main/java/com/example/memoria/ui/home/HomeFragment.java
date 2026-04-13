package com.example.memoria.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.dto.PublicDeck;
import com.example.memoria.service.PublicService;
import com.example.memoria.ui.profile.UserProfileViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;
import com.example.memoria.ui.adapter.PublicAdapter;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private UserProfileViewModel userViewModel;
    @Inject
    PublicService publicService;
    private RecyclerView rvDiscover;
    private PublicAdapter adapter;

    public HomeFragment() {
        // Để trống
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Ánh xạ View
        TextView tvWords = view.findViewById(R.id.tv_words_count);
        TextView tvStreak = view.findViewById(R.id.tv_streak_count);
        rvDiscover = view.findViewById(R.id.rv_discover_decks);

        // LayoutManager dạng cuộn ngang đã được set sẵn trong XML (app:layoutManager),
        // nhưng set lại ở code cho chắc chắn và dễ kiểm soát.
        rvDiscover.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 2. ViewModel lấy Progress (giống code ProgressFragment của bạn)
        userViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        userViewModel.getLearnedToday().observe(getViewLifecycleOwner(), count -> tvWords.setText(String.valueOf(count)));
        userViewModel.getStreakLiveData().observe(getViewLifecycleOwner(), streak -> tvStreak.setText(String.valueOf(streak)));

        // 3. Navigation
        view.findViewById(R.id.btn_go_to_discover).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_deckPublicFragment);
        });

        view.findViewById(R.id.btn_create_deck).setOnClickListener(v -> {
            // Logic mở màn hình tạo deck mới
        });

        // 4. Load dữ liệu Discover từ PublicService
        setupDiscoverList();

        return view;
    }

    private void setupDiscoverList() {
        // Lấy 5 decks hiển thị ở trang chủ, truyền null cho neo phân trang và từ khóa
        int limit = 5;
        publicService.getPublicDecks(limit, null, null, (success, data, message) -> {
            // Đảm bảo chạy cập nhật UI trên Main Thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (success && data != null) {
                        List<PublicDeck> parsedDecks = new ArrayList<>();
                        for (DocumentSnapshot doc : data) {
                            PublicDeck deck = doc.toObject(PublicDeck.class);
                            if (deck != null) {
                                // Đề phòng trường hợp id trên object bị null, map luôn id của document
                                deck.setPublicDocId(doc.getId());
                                parsedDecks.add(deck);
                            }
                        }

                        // Khởi tạo Adapter với danh sách vừa parse
                        adapter = new PublicAdapter(parsedDecks, deck -> {
                            Bundle bundle = new Bundle();
                            bundle.putString("publicDocId", deck.getPublicDocId()); // Có thể không cần thiết
                            bundle.putString("searchDeckName", deck.getDeckName());
                            Navigation.findNavController(requireView()).navigate(R.id.deckPublicFragment, bundle);

                            Log.d("HomeFragment", "Clicked deck: " + deck.getDeckName());
                        });

                        rvDiscover.setAdapter(adapter);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.action_fail_load_five_public_deck) + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}