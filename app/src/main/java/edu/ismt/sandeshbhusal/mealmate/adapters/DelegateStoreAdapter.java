package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.model.SavedLocation;


public class DelegateStoreAdapter extends RecyclerView.Adapter<DelegateStoreAdapter.ViewHolder> {
    private List<SavedLocation> stores;
    private Context context;
    private OnStoreSelectedListener listener;

    public interface OnStoreSelectedListener {
        void onStoreSelected(SavedLocation store);
    }

    public DelegateStoreAdapter(Context context, List<SavedLocation> stores, OnStoreSelectedListener listener) {
        this.context = context;
        this.stores = stores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.delegate_item_store, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedLocation store = stores.get(position);

        holder.tvStoreName.setText(store.getName());
        holder.tvAddress.setText(store.getAddress());
        holder.tvDistance.setText(store.getDistance());

        // Load image with fallback
        if (!TextUtils.isEmpty(store.getImageUrl())) {
            Glide.with(context)
                    .load(store.getImageUrl())
                    .placeholder(R.drawable.saved_store) // Show while loading
                    .error(R.drawable.saved_store) // Show if error
                    .into(holder.ivStore);
        } else {
            holder.ivStore.setImageResource(R.drawable.saved_store);
        }

        holder.itemView.setOnClickListener(v -> listener.onStoreSelected(store));
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivStore;
        TextView tvStoreName, tvAddress, tvDistance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStore = itemView.findViewById(R.id.ivStore);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }
    }
}
