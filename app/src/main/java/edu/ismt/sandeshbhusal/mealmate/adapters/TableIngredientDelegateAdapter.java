package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.model.Ingredient;


public class TableIngredientDelegateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Ingredient> ingredients;
    private List<Ingredient> selectedIngredients = new ArrayList<>();

    public TableIngredientDelegateAdapter(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.delegate_item_ingredient_table, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.delegate_item_ingredient_row, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            // Adjust position for header
            int itemPosition = position - 1;
            Ingredient ingredient = ingredients.get(itemPosition);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            // Remove previous listeners
            itemHolder.cbSelect.setOnCheckedChangeListener(null);
            itemHolder.etQuantity.removeTextChangedListener(itemHolder.quantityWatcher);
            itemHolder.etPrice.removeTextChangedListener(itemHolder.priceWatcher);

            // Set current values
            itemHolder.cbSelect.setChecked(ingredient.isSelected());
            itemHolder.tvName.setText(ingredient.getName());
            itemHolder.etQuantity.setText(String.valueOf(ingredient.getQuantity()));
            itemHolder.etPrice.setText(String.format(Locale.getDefault(), "%.2f", ingredient.getPrice()));

            // Add new listeners
            itemHolder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ingredient.setSelected(isChecked);
                updateSelections();
            });

            itemHolder.quantityWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        ingredient.setQuantity(s.toString());
                        updateSelections();
                    } catch (NumberFormatException e) {
                        ingredient.setQuantity("1");
                    }
                }
            };

            itemHolder.priceWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        ingredient.setPrice(Double.parseDouble(s.toString()));
                        updateSelections();
                    } catch (NumberFormatException e) {
                        ingredient.setPrice(0.0);
                    }
                }
            };

            itemHolder.etQuantity.addTextChangedListener(itemHolder.quantityWatcher);
            itemHolder.etPrice.addTextChangedListener(itemHolder.priceWatcher);
        }
        // Header doesn't need binding
    }

    public List<Ingredient> getIngredients() {
        return ingredients; // Return your list of ingredients
    }

    @Override
    public int getItemCount() {
        return ingredients.size() + 1; // +1 for header
    }

    public List<Ingredient> getSelectedIngredients() {
        return selectedIngredients;
    }

    private void updateSelections() {
        selectedIngredients.clear();
        for (Ingredient ingredient : ingredients) {
            if (ingredient.isSelected()) {
                selectedIngredients.add(ingredient);
            }
        }
    }

    // ViewHolder for header
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    // ViewHolder for items
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        TextView tvName;
        EditText etQuantity;
        EditText etPrice;
        TextWatcher quantityWatcher;
        TextWatcher priceWatcher;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            tvName = itemView.findViewById(R.id.tvName);
            etQuantity = itemView.findViewById(R.id.etQuantity);
            etPrice = itemView.findViewById(R.id.etPrice);
        }
    }
}