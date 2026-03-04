package com.example.memoria.ui.library;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class LibraryPagerAdapter extends FragmentStateAdapter {

    public LibraryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new FavFolderListFragment();
        }
        return new DeckListFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // FavFolder tab & Deck tab
    }
}
