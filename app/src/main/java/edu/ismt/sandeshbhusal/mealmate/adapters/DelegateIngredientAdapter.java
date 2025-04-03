package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.R;

public class DelegateIngredientAdapter extends RecyclerView.Adapter<DelegateIngredientAdapter.IngredientViewHolder> {
    private List<String> ingredientList;

    public DelegateIngredientAdapter(List<String> ingredientList) {
        this.ingredientList = ingredientList;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(edu.ismt.sandeshbhusal.mealmate.R.layout.delegate_item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        String ingredient = ingredientList.get(position);
        boolean isPurchased = ingredient.endsWith("|true");

        // Remove the purchased status from the ingredient name
        String ingredientName = ingredient.split("\\|")[0];

        // Set ingredient name
        holder.textViewIngredientName.setText(ingredientName);

        // Set checkbox state
        holder.checkBoxIngredient.setChecked(isPurchased);

        // Strikethrough text if purchased
        if (isPurchased) {
            holder.textViewIngredientName.setPaintFlags(holder.textViewIngredientName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.checkBoxIngredient.setEnabled(false); // Disable checkbox for purchased items
        } else {
            holder.textViewIngredientName.setPaintFlags(holder.textViewIngredientName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.checkBoxIngredient.setEnabled(true); // Enable checkbox for unpurchased items
        }

        // Handle checkbox click
        holder.checkBoxIngredient.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update the ingredient's purchased status in the database (if needed)
            // You can call a method in GroceryDatabaseHelper to update the status
        });
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    public List<String> getIngredientList() {
        return ingredientList;
    }

    public void updateIngredients(List<String> newIngredients) {
        this.ingredientList.clear();
        this.ingredientList.addAll(newIngredients);
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }


    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxIngredient;
        TextView textViewIngredientName;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            //checkBoxIngredient = itemView.findViewById(R.id.checkBoxIngredient);
            //textViewIngredientName = itemView.findViewById(R.id.textViewIngredientName);
        }
    }
}