package edu.ismt.sandeshbhusal.mealmate;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ismt.sandeshbhusal.mealmate.adapters.DelegateIngredientAdapter;
import edu.ismt.sandeshbhusal.mealmate.adapters.DelegateMealAdapter;
import edu.ismt.sandeshbhusal.mealmate.utils.GroceryDatabaseHelper;

public class DelegateActivity extends AppCompatActivity {
    private RecyclerView recyclerViewMeals, recyclerViewIngredients;
    private DelegateMealAdapter delegateMealAdapter;
    private DelegateIngredientAdapter delegateIngredientAdapter;
    private GroceryDatabaseHelper dbHelper;
    private Map<String, Map<String, List<String>>> weeklyGroceryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delegate);

        // Initialize database helper
        dbHelper = new GroceryDatabaseHelper(this);

        // Initialize RecyclerViews
        recyclerViewMeals = findViewById(R.id.recyclerViewMeals);
        recyclerViewIngredients = findViewById(R.id.recyclerViewIngredients);

        // Set layout managers
        recyclerViewMeals.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewIngredients.setLayoutManager(new LinearLayoutManager(this));

        // Fetch grocery items for the week
        weeklyGroceryMap = dbHelper.getGroceryItemsForWeek();

        // Extract meal categories (e.g., "Chicken Stir-Fry", "Vegetable Quinoa Bowl")
        List<String> meals = new ArrayList<>(weeklyGroceryMap.keySet());

        // Set up meal adapter
        delegateMealAdapter = new DelegateMealAdapter(meals);
        recyclerViewMeals.setAdapter(delegateMealAdapter);

        // Handle meal selection
        delegateMealAdapter.setOnMealSelectedListener(meal -> {
            // Fetch ingredients for the selected meal
            Map<String, List<String>> dateMap = weeklyGroceryMap.get(meal);
            List<String> ingredients = new ArrayList<>();

            for (Map.Entry<String, List<String>> entry : dateMap.entrySet()) {
                ingredients.addAll(entry.getValue());
            }

            // Log the fetched ingredients for debugging
            System.out.println("Fetched Ingredients for " + meal + ": " + ingredients);

            // Update the ingredients RecyclerView with the latest ingredients on top
            updateIngredientsRecyclerView(ingredients);
        });

        // Example: Get selected meals
        findViewById(R.id.buttonSendSMS).setOnClickListener(v -> {
            List<String> selectedMeals = delegateMealAdapter.getSelectedMeals();
            // Display selected meals (for testing)
            StringBuilder message = new StringBuilder("Selected Meals:\n");
            for (String meal : selectedMeals) {
                message.append(meal).append("\n");
            }
            // Show a toast with the selected meals
            Toast.makeText(this, message.toString(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateIngredientsRecyclerView(List<String> newIngredients) {
        // Get the current list of ingredients from the adapter (if any)
        List<String> currentIngredients = new ArrayList<>();
        if (delegateIngredientAdapter != null) {
            currentIngredients = delegateIngredientAdapter.getIngredientList();
        }

        // Use a Set to avoid duplicates
        Set<String> uniqueIngredients = new HashSet<>(currentIngredients);

        // Add new ingredients to the top of the list
        for (String ingredient : newIngredients) {
            if (!uniqueIngredients.contains(ingredient)) {
                currentIngredients.add(0, ingredient); // Add to the top
                uniqueIngredients.add(ingredient); // Add to the Set to avoid duplicates
            }
        }

        // Log the updated ingredients for debugging
        System.out.println("Updated Ingredients: " + currentIngredients);

        // Update the adapter for the ingredients RecyclerView
        if (delegateIngredientAdapter == null) {
            delegateIngredientAdapter = new DelegateIngredientAdapter(currentIngredients);
            recyclerViewIngredients.setAdapter(delegateIngredientAdapter);
        } else {
            delegateIngredientAdapter.updateIngredients(currentIngredients);
        }
    }
}