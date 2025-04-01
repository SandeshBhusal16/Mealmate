package edu.ismt.sandeshbhusal.mealmate.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;


import java.util.ArrayList;

import edu.ismt.sandeshbhusal.mealmate.ContactSelectionFragment;
import edu.ismt.sandeshbhusal.mealmate.fragments.StoreSelectionFragment;

public class SelectionPagerAdapter extends FragmentStateAdapter {
    private final ArrayList<String> selectedIngredients;

    public SelectionPagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<String> selectedIngredients ) {
        super(fragmentActivity);
        this.selectedIngredients = selectedIngredients;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ContactSelectionFragment();
            case 1:
                return StoreSelectionFragment.newInstance(selectedIngredients);
            default:
                return new ContactSelectionFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
