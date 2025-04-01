package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.utils.GroceryDatabaseHelper;

public class GroceryIngredientAdapter extends RecyclerView.Adapter<GroceryIngredientAdapter.IngredientViewHolder> {
    private final Context context;
    private final List<String> items;
    private final String date;
    private final GroceryDatabaseHelper dbHelper;
    private final OnItemCheckListener onItemCheckListener;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public GroceryIngredientAdapter(Context context, List<String> items, String date, GroceryDatabaseHelper dbHelper, OnItemCheckListener listener) {
        this.context = context;
        this.items = items;
        this.date = date;
        this.dbHelper = dbHelper;
        this.onItemCheckListener = listener;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grocery_item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        String itemName = items.get(position);
        holder.ingredientName.setText(itemName);

        // Get the purchased state from the database
        boolean isPurchased = dbHelper.isItemPurchased(itemName, date);

        // Remove listener before setting checked state
        holder.ingredientCheckbox.setOnCheckedChangeListener(null);

        // Set checkbox state
        holder.ingredientCheckbox.setChecked(isPurchased);

        // Strike-through if purchased
        holder.ingredientName.setPaintFlags(isPurchased ?
                holder.ingredientName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG :
                holder.ingredientName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

        // Add listener back after setting checked state
        holder.ingredientCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Run database update in a background thread
            executorService.execute(() -> {
                dbHelper.updateItemPurchasedStatus(itemName, date, isChecked);

                // Notify listener on the main thread
                ((Activity) context).runOnUiThread(() -> {
                    if (onItemCheckListener != null) {
                        onItemCheckListener.onItemCheckChanged();
                    }
                    // Use payload to update only the checkbox state
                    notifyItemChanged(position, "checkboxUpdate");
                });
            });
        });
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if ("checkboxUpdate".equals(payload)) {
                    String itemName = items.get(position);
                    boolean isPurchased = dbHelper.isItemPurchased(itemName, date);
                    holder.ingredientCheckbox.setChecked(isPurchased);

                    holder.ingredientName.setPaintFlags(isPurchased ?
                            holder.ingredientName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG :
                            holder.ingredientName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        CheckBox ingredientCheckbox;
        MaterialTextView ingredientName;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientCheckbox = itemView.findViewById(R.id.ingredientCheckbox);
            ingredientName = itemView.findViewById(R.id.ingredientName);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        executorService.shutdown();
    }


}