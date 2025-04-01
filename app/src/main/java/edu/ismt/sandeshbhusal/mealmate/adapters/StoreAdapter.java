package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.model.SavedLocation;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private final Context context;
    private  List<SavedLocation> storeList;

    public StoreAdapter(Context context, List<SavedLocation> storeList) {
        this.context = context;
        this.storeList = storeList;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        SavedLocation store = storeList.get(position);

        // Store Name, Address, and Distance
        holder.storeName.setText(store.getName());
        holder.storeDistance.setText(store.getDistance() + " away");
        holder.storeAddress.setText(store.getAddress());


        // Display Matching Count Badge
        int matchingCount = store.getMatchingCount();
        holder.matchingCount.setText(String.valueOf(matchingCount));


        // Load Image using Glide
        Glide.with(context)
                .load(store.getImageUrl())
                .placeholder(R.drawable.saved_store)
                .into(holder.storeImage);

        // Display Available Ingredients as Grid
        holder.gridContainer.removeAllViews(); // Clear previous views
        List<String> ingredients = store.getAvailableIngredients();
        List<String> matchedIngredients = store.getMatchedIngredients(); // Get matched list

        for (String ingredient : ingredients) {
            TextView chip = new TextView(context);
            chip.setText(ingredient);
            chip.setPadding(24, 12, 24, 12);
            chip.setTextSize(12);

            // Check if the ingredient is in the matched list
            if (matchedIngredients != null && matchedIngredients.contains(ingredient)) {
                chip.setBackground(context.getDrawable(R.drawable.chip_avilable)); // Matched -> Green
                chip.setTextColor(context.getColor(R.color.white));
            } else {

                chip.setBackground(context.getDrawable(R.drawable.chip_background));
                chip.setTextColor(context.getColor(R.color.on_surface));
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(8, 8, 8, 8);
            chip.setLayoutParams(params);

            holder.gridContainer.addView(chip);
        }

        // Set initial state of ingredients grid and icon
        holder.gridContainer.setVisibility(View.GONE);
        holder.expandIcon.setRotation(0f);

        // Toggle the grid visibility on clicking the ingredients card
        holder.ingredientsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.gridContainer.getVisibility() == View.GONE) {
                    holder.gridContainer.setVisibility(View.VISIBLE);
                    holder.expandIcon.setRotation(180f);
                } else {
                    holder.gridContainer.setVisibility(View.GONE);
                    holder.expandIcon.setRotation(0f);
                }
            }
        });

        // Handle Get Directions Button
        holder.getDirectionsButton.setOnClickListener(v -> {
            String uri = "google.navigation:q=" + store.getLatitude() + "," + store.getLongitude() + "&mode=d";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");

            // Check if Google Maps is installed to handle the intent
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                String webUri = "https://www.google.com/maps/dir/?api=1&destination="
                        + store.getLatitude() + "," + store.getLongitude() + "&travelmode=driving";
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
                context.startActivity(webIntent);    }



        });
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    public void updateList(List<SavedLocation> filteredList) {
        storeList = filteredList;  // Update the internal list
        notifyDataSetChanged();    // Notify RecyclerView to refresh
    }

    public static class StoreViewHolder extends RecyclerView.ViewHolder {
        ImageView storeImage, expandIcon;
        TextView storeName, storeDistance, matchingCount, storeAddress;
        GridLayout gridContainer;
        Button getDirectionsButton;
        CardView ingredientsCard;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            storeImage = itemView.findViewById(R.id.storeImage);
            storeName = itemView.findViewById(R.id.storeName);
            storeAddress = itemView.findViewById(R.id.storeAddress);
            storeDistance = itemView.findViewById(R.id.storeDistance);
            matchingCount = itemView.findViewById(R.id.matchingCount);
            gridContainer = itemView.findViewById(R.id.ingredientsGrid);
            getDirectionsButton = itemView.findViewById(R.id.getDirectionsButton);
            // New views for collapsible ingredients section
            ingredientsCard = itemView.findViewById(R.id.ingredientsCard);
            expandIcon = itemView.findViewById(R.id.expandIcon);
        }
    }
}
