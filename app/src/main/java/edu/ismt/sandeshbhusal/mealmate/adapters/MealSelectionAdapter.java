package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.model.Recipe;

public class MealSelectionAdapter extends RecyclerView.Adapter<MealSelectionAdapter.ViewHolder> {

    private List<Recipe> mealList;
    private List<Recipe> filteredMealList;
    private List<Recipe> selectedMeals;
    private TextView selectedCountText;
    private String categoryType;

    public MealSelectionAdapter(List<Recipe> mealList, List<Recipe> selectedMeals, TextView selectedCountText, String categoryType) {
        this.mealList = new ArrayList<>(mealList);
        this.filteredMealList = new ArrayList<>(mealList);
        this.selectedMeals = selectedMeals;
        this.selectedCountText = selectedCountText;
        this.categoryType = categoryType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = filteredMealList.get(position);
        boolean isSelected = selectedMeals.contains(recipe);

        // Set Meal Name
        holder.mealName.setText(recipe.getRecipeName());

        // Set Cook Time
        holder.cookTime.setText(recipe.getCookTime());

        // Set Instruction Count
        holder.instructionCount.setText(recipe.getInstructions().size() + " Steps");

        // Calculate Total Ingredients
        int totalIngredients = 0;
        for (Map.Entry<String, List<String>> entry : recipe.getIngredients().entrySet()) {
            totalIngredients += entry.getValue().size();
        }
        holder.ingredientCount.setText(totalIngredients + " Ingredients");

        // Load Image using Glide
        Glide.with(holder.itemView.getContext())
                .load(recipe.getPhotoUrl())
                .placeholder(R.drawable.input_background)
                .into(holder.mealImage);

        // Change Background Color if Selected
        holder.itemView.setBackgroundColor(isSelected ?
                ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_variant)
                : ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));

        // Checkmark Animation
        holder.checkmark.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        if (isSelected) {
            holder.checkmark.animate().alpha(1.0f).setDuration(300).start();
        } else {
            holder.checkmark.animate().alpha(0.0f).setDuration(300).start();
        }

        // Handle Click for Selection
        holder.itemView.setOnClickListener(v -> {
            if (isSelected) {
                selectedMeals.remove(recipe);
            } else {
                selectedMeals.add(recipe);
            }
            notifyItemChanged(position);
            selectedCountText.setText("Selected: " + selectedMeals.size() + " meals for "+categoryType);
        });
    }

    @Override
    public int getItemCount() {
        return filteredMealList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mealName, cookTime, instructionCount, ingredientCount;
        ImageView mealImage, checkmark;

        public ViewHolder(View itemView) {
            super(itemView);
            mealName = itemView.findViewById(R.id.mealName);
            cookTime = itemView.findViewById(R.id.cookTime);
            instructionCount = itemView.findViewById(R.id.instructionCount);
            ingredientCount = itemView.findViewById(R.id.ingredientCount);
            mealImage = itemView.findViewById(R.id.mealImage);
            checkmark = itemView.findViewById(R.id.checkmark);
        }
    }

    // 🔍 Search Filter Function
    public void filter(String query) {
        filteredMealList.clear();
        if (query.isEmpty()) {
            filteredMealList.addAll(mealList);
        } else {
            for (Recipe meal : mealList) {
                if (meal.getRecipeName().toLowerCase().contains(query.toLowerCase())) {
                    filteredMealList.add(meal);
                }
            }
        }
        notifyDataSetChanged();
    }
}
