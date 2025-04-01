package edu.ismt.sandeshbhusal.mealmate.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.fragments.GroceryListFragment;


public class GroceryPageAdapter extends FragmentStateAdapter {

    private final List<String> groceryTypes;

    public GroceryPageAdapter(@NonNull FragmentActivity fragmentActivity, List<String> groceryTypes) {
        super(fragmentActivity);
        this.groceryTypes = groceryTypes;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return GroceryListFragment.newInstance(groceryTypes.get(position));
    }

    @Override
    public int getItemCount() {
        return groceryTypes.size();
    }
}
