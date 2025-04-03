package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.R;


public class MealDelegateAdapter extends RecyclerView.Adapter<MealDelegateAdapter.ViewHolder> {
    private List<String> meals;
    private OnMealSelectedListener listener;
    private int selectedPosition = -1; // Track selected position

    public interface OnMealSelectedListener {
        void onMealSelected(String meal);
    }

    public MealDelegateAdapter(List<String> meals) {
        this.meals = meals;
    }

    public void setOnMealSelectedListener(OnMealSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_header, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String meal = meals.get(position);
        holder.tvMealName.setText(meal);

        // Update background based on selection
        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.selected_meal_bg);
            holder.tvMealName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.unselected_meal_bg);
            holder.tvMealName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
        }

        holder.itemView.setOnClickListener(v -> {
            // Update selected position
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Notify previous and new selections to update
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onMealSelected(meal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealName = itemView.findViewById(R.id.tvMealName);
        }
    }
}