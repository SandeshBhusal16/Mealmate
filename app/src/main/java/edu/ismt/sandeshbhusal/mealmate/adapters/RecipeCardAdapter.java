package edu.ismt.sandeshbhusal.mealmate.adapters;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.RecipeDetailActivity;
import edu.ismt.sandeshbhusal.mealmate.model.Recipe;

public class RecipeCardAdapter extends RecyclerView.Adapter<RecipeCardAdapter.RecipeViewHolder> {

    private final Context context;
    private final List<Recipe> recipeList;

    public RecipeCardAdapter(Context context, List<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recipe_card, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        // Load recipe image
        if (recipe.getPhotoUrl() != null && !recipe.getPhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(recipe.getPhotoUrl())
                    .placeholder(R.drawable.input_background) // Fallback image
                    // Error image
                    .into(holder.recipeImage);
        } else {
            holder.recipeImage.setImageResource(R.drawable.no_image_placeholder); // Default image
        }

        // Set recipe name
        holder.recipeName.setText(recipe.getRecipeName());

        // Calculate and set total number of ingredients
        int totalIngredients = 0;
        for (List<String> ingredients : recipe.getIngredients().values()) {
            totalIngredients += ingredients.size();
        }
        holder.totalIngredients.setText(totalIngredients+ " Ingrdedients");

        // Set total number of instructions
        int totalInstructions = recipe.getInstructions().size();
        holder.totalInstructions.setText( totalInstructions+" Steps");
        holder.cookTime.setText(recipe.getCookTime()+" Minuets");
        holder.recipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RecipeDetailActivity.class);

                intent.putExtra("RECIPE", recipe);

                // Start the activity
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView recipeName, totalIngredients, totalInstructions, cookTime ;

        Button recipeBtn;
        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeName = itemView.findViewById(R.id.recipeTitle);
            totalIngredients = itemView.findViewById(R.id.recipeIngredients);
            totalInstructions = itemView.findViewById(R.id.recipeSteps);
            cookTime = itemView.findViewById(R.id.recipeCookTime);
            recipeBtn = itemView.findViewById(R.id.recipeButton);


        }
    }
}