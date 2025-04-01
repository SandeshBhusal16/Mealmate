package edu.ismt.sandeshbhusal.mealmate.grok;



import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.R;

class GrokIngredientAdapter extends RecyclerView.Adapter<GrokIngredientAdapter.ViewHolder> {
    private List<GrokIngredient> ingredients;
    private List<GrokIngredient> selectedIngredients; // Reference to activity's list for updating prices

    GrokIngredientAdapter(List<GrokIngredient> ingredients, List<GrokIngredient> selectedIngredients) {
        this.ingredients = ingredients;
        this.selectedIngredients = selectedIngredients;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grok_item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GrokIngredient ingredient = ingredients.get(position);
        String displayText = ingredient.name + " (" + ingredient.category + ")" +
                (ingredient.price > 0 ? " - $" + String.format("%.2f", ingredient.price) : "");
        holder.textView.setText(displayText);

        if (ingredient.isPurchased) {
            holder.textView.setPaintFlags(holder.textView.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.checkBox.setEnabled(false);
            holder.priceInputContainer.setVisibility(View.GONE);
            holder.checkBox.setChecked(false); // Ensure purchased items aren't checked
        } else {
            holder.textView.setPaintFlags(holder.textView.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            holder.checkBox.setEnabled(true);
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    holder.priceInputContainer.setVisibility(View.VISIBLE);
                    holder.priceEditText.setText(ingredient.price > 0 ? String.valueOf(ingredient.price) : "");
                } else {
                    holder.priceInputContainer.setVisibility(View.GONE);
                    // Optionally remove price if unchecked
                    ingredient.price = 0.0f;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        selectedIngredients.removeIf(i -> i.name.equals(ingredient.name) && i.date.equals(ingredient.date));
                    }
                    notifyItemChanged(position);
                }
            });

            holder.setPriceButton.setOnClickListener(v -> {
                String priceStr = holder.priceEditText.getText().toString();
                if (!priceStr.isEmpty()) {
                    float price = Float.parseFloat(priceStr);
                    ingredient.price = price;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        selectedIngredients.removeIf(i -> i.name.equals(ingredient.name) && i.date.equals(ingredient.date));
                    }
                    selectedIngredients.add(ingredient);
                    holder.priceInputContainer.setVisibility(View.GONE);
                    notifyItemChanged(position); // Refresh to show updated price
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCheckBox checkBox;
        TextView textView;
        LinearLayout priceInputContainer;
        TextInputEditText priceEditText;
        MaterialButton setPriceButton;

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.ingredientCheckBox);
            textView = itemView.findViewById(R.id.ingredientTextView);
            priceInputContainer = itemView.findViewById(R.id.priceInputContainer);
            priceEditText = itemView.findViewById(R.id.priceEditText);
            setPriceButton = itemView.findViewById(R.id.setPriceButton);
        }
    }
}