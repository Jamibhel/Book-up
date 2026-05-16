package com.example.bookup.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.bookup.fragments.MaterialSearchResultsFragment;
import com.example.bookup.fragments.TutorSearchResultsFragment;

public class SearchFragmentStateAdapter extends FragmentStateAdapter {

    private final int NUM_TABS = 3; // For Materials, Tutors, and Students

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
                return TutorSearchResultsFragment.newInstance(1); // Tutors tab
            case 2:
                return TutorSearchResultsFragment.newInstance(2); // Students tab
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
