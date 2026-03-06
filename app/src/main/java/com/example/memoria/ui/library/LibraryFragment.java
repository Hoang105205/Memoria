package com.example.memoria.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.memoria.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

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

        // Ánh xạ View
        tlLibraryTabs = view.findViewById(R.id.tl_library_tabs);
        vpLibraryPager = view.findViewById(R.id.vp_library_pager);

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
    }
}