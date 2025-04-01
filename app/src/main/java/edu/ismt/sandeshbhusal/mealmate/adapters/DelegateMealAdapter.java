package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.R;

public class DelegateMealAdapter extends RecyclerView.Adapter<DelegateMealAdapter.MealViewHolder> {
    private List<String> mealList;
    private List<String> selectedMeals = new ArrayList<>();
    private OnMealSelectedListener onMealSelectedListener;

    public DelegateMealAdapter(List<String> mealList) {
        this.mealList = mealList;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delegate_item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        String meal = mealList.get(position);
        holder.chipMeal.setText(meal);

        // Set the chip as checked if it's in the selected list
        holder.chipMeal.setChecked(selectedMeals.contains(meal));

        // Handle chip selection/deselection
        holder.chipMeal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedMeals.add(meal); // Add to selected list
            } else {
                selectedMeals.remove(meal); // Remove from selected list
            }

            // Notify listener about the selected meal
            if (onMealSelectedListener != null) {
                onMealSelectedListener.onMealSelected(meal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public List<String> getSelectedMeals() {
        return selectedMeals;
    }

    public void setOnMealSelectedListener(OnMealSelectedListener listener) {
        this.onMealSelectedListener = listener;
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        Chip chipMeal;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            chipMeal = itemView.findViewById(R.id.chipMeal);
        }
    }

    public interface OnMealSelectedListener {
        void onMealSelected(String meal);
    }
}