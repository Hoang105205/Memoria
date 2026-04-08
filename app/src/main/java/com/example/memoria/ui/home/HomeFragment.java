package com.example.memoria.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.service.PublicService;
import com.example.memoria.ui.profile.UserProfileViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;
import com.example.memoria.ui.home.PublicAdapter;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private UserProfileViewModel userViewModel;
    private PublicService publicService;
    private RecyclerView rvDiscover;
    private PublicAdapter adapter;

    public HomeFragment() {
        // Để trống
    }
    @Inject
    public HomeFragment(PublicService publicService) {
        this.publicService = publicService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Ánh xạ View
        TextView tvWords = view.findViewById(R.id.tv_words_count);
        TextView tvStreak = view.findViewById(R.id.tv_streak_count);
        rvDiscover = view.findViewById(R.id.rv_discover_decks);

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
        //setupDiscoverList();

        return view;
    }

    private void setupDiscoverList() {
        publicService.getPublicDecks(10, null, null, (success, data, message) -> {
            if (success && data != null) {
                // data ở đây là List<DocumentSnapshot> từ Firestore
                adapter = new PublicAdapter(data);
                rvDiscover.setAdapter(adapter);
            }
        });
    }
}