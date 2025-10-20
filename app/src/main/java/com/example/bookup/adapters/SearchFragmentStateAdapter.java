package com.example.bookup.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.bookup.fragments.MaterialSearchResultsFragment;
import com.example.bookup.fragments.TutorSearchResultsFragment;

public class SearchFragmentStateAdapter extends FragmentStateAdapter {

    private final int NUM_TABS = 2; // For Materials and Tutors

    public SearchFragmentStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new MaterialSearchResultsFragment();
            case 1:
                return new TutorSearchResultsFragment();
            default:
                return new Fragment(); // Should not happen
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
