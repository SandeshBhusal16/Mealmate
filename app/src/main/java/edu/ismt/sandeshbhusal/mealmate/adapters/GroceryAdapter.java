package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.utils.GroceryDatabaseHelper;

public class GroceryAdapter extends RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder> {

    private final Context context;
    private final GroceryDatabaseHelper dbHelper;
    private final Map<String, Map<String, List<String>>> weeklyGroceryMap;
    private final OnItemCheckListener onItemCheckListener;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public GroceryAdapter(Context context, Map<String, Map<String, List<String>>> weeklyGroceryMap, OnItemCheckListener listener) {
        this.context = context;
        this.weeklyGroceryMap = weeklyGroceryMap;
        this.dbHelper = new GroceryDatabaseHelper(context);
        this.onItemCheckListener = listener;
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grocery, parent, false);
        return new GroceryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
        String category = (String) weeklyGroceryMap.keySet().toArray()[position];
        Map<String, List<String>> dateMap = weeklyGroceryMap.get(category);

        int totalItems = 0;
        int purchasedItems = 0;

        // Flatten the list of items for the nested RecyclerView
        List<String> allItems = new ArrayList<>();
        for (String date : dateMap.keySet()) {
            List<String> items = dateMap.get(date);
            allItems.addAll(items);
            totalItems += items.size();

            for (String itemName : items) {
                if (dbHelper.isItemPurchased(itemName, date)) {
                    purchasedItems++;
                }
            }
        }

        // Update Category Title with Purchased/Total
        holder.categoryTitle.setText(category + " (" + purchasedItems + "/" + totalItems + " Purchased)");

        // Set up the nested RecyclerView
        GroceryIngredientAdapter groceryIngredientAdapter = new GroceryIngredientAdapter(context, allItems, dateMap.keySet().iterator().next(), dbHelper, onItemCheckListener);
        holder.ingredientRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.ingredientRecyclerView.setAdapter(groceryIngredientAdapter);

        // Calculate and display category progress
        int progress = totalItems > 0 ? (int) ((purchasedItems / (float) totalItems) * 100) : 0;
        holder.categoryProgress.setProgress(progress);
        holder.progressText.setText(progress + "% Purchased");
    }


    @Override
    public int getItemCount() {
        return weeklyGroceryMap.size();
    }

    public static class GroceryViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView categoryTitle;
        CircularProgressIndicator categoryProgress;
        TextView progressText;
        RecyclerView ingredientRecyclerView;

        public GroceryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.mealTitle);
            categoryProgress = itemView.findViewById(R.id.mealProgressIndicator);
            progressText = itemView.findViewById(R.id.mealProgressText);
            ingredientRecyclerView = itemView.findViewById(R.id.ingredientRecyclerView);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        executorService.shutdown();
    }


}