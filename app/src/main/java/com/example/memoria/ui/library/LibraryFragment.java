package com.example.memoria.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.memoria.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LibraryFragment extends Fragment {

    private TabLayout tlLibraryTabs;
    private ViewPager2 vpLibraryPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LibraryViewModel viewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);

        // Ánh xạ View
        tlLibraryTabs = view.findViewById(R.id.tl_library_tabs);
        vpLibraryPager = view.findViewById(R.id.vp_library_pager);
        android.widget.EditText edtSearch = view.findViewById(R.id.edt_search);

        // Cài đặt Adapter
        LibraryPagerAdapter pagerAdapter = new LibraryPagerAdapter(this);
        vpLibraryPager.setAdapter(pagerAdapter);

        // Kết nối Tab và ViewPager
        new TabLayoutMediator(tlLibraryTabs, vpLibraryPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.tab_favfolders);
            } else {
                tab.setText(R.string.tab_decks);
            }
        }).attach();

        edtSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString();
                // Gọi hàm search cho FavFolder
                viewModel.searchFavFolders(keyword);

                viewModel.searchDecks(keyword);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
}