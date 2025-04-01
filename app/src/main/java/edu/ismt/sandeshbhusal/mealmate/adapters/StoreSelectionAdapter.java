package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.model.SavedLocation;

public class StoreSelectionAdapter extends RecyclerView.Adapter<StoreSelectionAdapter.ViewHolder> {

    private final Context context;
    private final List<SavedLocation> storeList;
    private int selectedPosition = -1;  // Track selected position

    public StoreSelectionAdapter(Context context, List<SavedLocation> storeList) {
        this.context = context;
        this.storeList = storeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (storeList == null || storeList.isEmpty()) {
            Log.e("StoreSelectionAdapter", "Store list is null or empty.");
            return;
        }

        SavedLocation store = storeList.get(position);

        // Store Details
        holder.storeName.setText(store.getName());
        holder.storeDistance.setText(store.getDistance() + " away");
        holder.storeAddress.setText(store.getAddress());

        // Load Image using Glide
        Glide.with(context)
                .load(store.getImageUrl())
                .placeholder(R.drawable.saved_store)
                .into(holder.storeImage);

        // Selection State
        if (selectedPosition == position) {
            holder.cardView.setBackgroundResource(R.drawable.card_selected); // Highlight Selected
            holder.checkmark.setVisibility(View.VISIBLE);  // Show Checkmark
        } else {
            holder.cardView.setBackgroundResource(R.drawable.card_unselected); // Default State
            holder.checkmark.setVisibility(View.GONE); // Hide Checkmark
        }

        // Handle Click Event for Selection
        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition == position) {
                // If already selected, unselect it
                selectedPosition = -1;
            } else {
                // Otherwise, select the current position
                int previousPosition = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return storeList != null ? storeList.size() : 0;
    }

    // Add this method inside StoreSelectionAdapter class
    public void updateList(List<SavedLocation> newList) {
        storeList.clear();
        storeList.addAll(newList);
        notifyDataSetChanged();
    }



    public SavedLocation getSelectedStore() {
        if (selectedPosition != -1 && selectedPosition < storeList.size()) {
            return storeList.get(selectedPosition);
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView storeImage, checkmark;
        TextView storeName, storeDistance, storeAddress;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            storeImage = itemView.findViewById(R.id.storeImage);
            storeName = itemView.findViewById(R.id.storeName);
            storeDistance = itemView.findViewById(R.id.storeDistance);
            storeAddress = itemView.findViewById(R.id.storeAddress);
            checkmark = itemView.findViewById(R.id.checkmark);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
