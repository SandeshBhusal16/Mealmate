package edu.ismt.sandeshbhusal.mealmate;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.ismt.sandeshbhusal.mealmate.MealSelectionBottomSheet;
import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.adapters.MealAdapter;
import edu.ismt.sandeshbhusal.mealmate.model.Meal;
import edu.ismt.sandeshbhusal.mealmate.model.Recipe;
import edu.ismt.sandeshbhusal.mealmate.utils.FirestoreHelper;

public class DailyPlanFragment extends Fragment  implements MealSelectionBottomSheet.MealSelectionListener, MealAdapter.OnMealRemoveListener {
    private String selectedDate;
    private RecyclerView breakfastRecyclerView, lunchRecyclerView, dinnerRecyclerView;
    private TextView noBreakfastText, noLunchText, noDinnerText;
    private MaterialButton addBreakfastMealBtn, addLunchMealBtn, addDinnerMealBtn;
    private List<Recipe> recipeLists = new ArrayList<>();
    private FirestoreHelper firestoreHelper;

    private CustomProgressDialog customProgressDialog;

    private View rootView;
    public static DailyPlanFragment newInstance(Date date) {
        DailyPlanFragment fragment = new DailyPlanFragment();
        Bundle args = new Bundle();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        args.putString("selectedDate", dateFormat.format(date));

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView= inflater.inflate(R.layout.fragment_daily_plan, container, false);
        if (getArguments() != null) {
            selectedDate = getArguments().getString("selectedDate");
            Log.d("DailyPlanFragment", "Fragment Date: " + selectedDate);
        }

        customProgressDialog = new CustomProgressDialog(getContext());
        initializeViews(rootView);
        setupFirestore();
        setupButtonListeners();

        loadMealPlan();
        return rootView;
    }

    private void initializeViews(View view) {
        breakfastRecyclerView = rootView.findViewById(R.id.breakfastRecyclerView);
        lunchRecyclerView = rootView.findViewById(R.id.lunchRecyclerView);
        dinnerRecyclerView = rootView.findViewById(R.id.dinnerRecyclerView);

        noBreakfastText = rootView.findViewById(R.id.noBreakfastText);
        noLunchText = rootView.findViewById(R.id.noLunchText);
        noDinnerText = rootView.findViewById(R.id.noDinnerText);

        addBreakfastMealBtn = rootView.findViewById(R.id.addBreakfastMealBtn);
        addLunchMealBtn = rootView.findViewById(R.id.addLunchMealBtn);
        addDinnerMealBtn = rootView.findViewById(R.id.addDinnerMealBtn);
    }

    private void setupFirestore() {
        customProgressDialog.show();
        firestoreHelper = new FirestoreHelper();
        firestoreHelper.loadRecipes(recipeList -> recipeLists = recipeList);
        customProgressDialog.dismiss();


    }

    private void setupButtonListeners() {
        addBreakfastMealBtn.setOnClickListener(v -> addMeal("Breakfast"));
        addLunchMealBtn.setOnClickListener(v -> addMeal("Lunch"));
        addDinnerMealBtn.setOnClickListener(v -> addMeal("Dinner"));
    }

    private void addMeal(String type) {
        Log.d("recipeList", recipeLists.toString());
        MealSelectionBottomSheet bottomSheet = MealSelectionBottomSheet.newInstance(recipeLists, type); // Pass meal type
        bottomSheet.setMealSelectionListener(this);
        bottomSheet.show(getParentFragmentManager(), "MealSelection");
    }

    private void loadMealPlan() {
        customProgressDialog.show();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.enableNetwork().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Firestore", "Network enabled. Fetching meals...");
               // loadMealPlan(); // Reload meals
            } else {
                Log.e("Firestore", "Failed to enable Firestore network", task.getException());
            }

        });
//
//        customProgressDialog.show();
        DocumentReference mealRef = db.collection("meals").document(selectedDate);

        mealRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    // Get timestamps for each meal category
                    List<Long> breakfastTimestamps = (List<Long>) document.get("Breakfast");
                    List<Long> lunchTimestamps = (List<Long>) document.get("Lunch");
                    List<Long> dinnerTimestamps = (List<Long>) document.get("Dinner");

                    // Initialize meal lists
                    List<Meal> breakfastMeals = new ArrayList<>();
                    List<Meal> lunchMeals = new ArrayList<>();
                    List<Meal> dinnerMeals = new ArrayList<>();

                    // Fetch full recipe details
                    fetchRecipes(breakfastTimestamps, breakfastMeals, "Breakfast");
                    fetchRecipes(lunchTimestamps, lunchMeals, "Lunch");
                    fetchRecipes(dinnerTimestamps, dinnerMeals, "Dinner");
                } else {
                    // No meals found, clear RecyclerViews
                    setupRecyclerView(breakfastRecyclerView, new ArrayList<>(), noBreakfastText);
                    setupRecyclerView(lunchRecyclerView, new ArrayList<>(), noLunchText);
                    setupRecyclerView(dinnerRecyclerView, new ArrayList<>(), noDinnerText);
                }
            } else {
                showSnackbar("Error loading meals");
            }
            customProgressDialog.dismiss();
        });
    }

    private void fetchRecipes(List<Long> timestamps, List<Meal> mealList, String mealType) {
        if (timestamps == null || timestamps.isEmpty()) {
            setupRecyclerView(getRecyclerViewForType(mealType), new ArrayList<>(), getNoMealTextForType(mealType));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (Long timestamp : timestamps) {
            db.collection("recipes").document(String.valueOf(timestamp))
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Recipe recipe = documentSnapshot.toObject(Recipe.class);
                            if (recipe != null) {
                                mealList.add(new Meal(recipe, mealType));
                                Log.d("Recipe Fetch", "Loaded recipe: " + recipe.getRecipeName());

                                // Update RecyclerView dynamically after loading recipes
                                setupRecyclerView(getRecyclerViewForType(mealType), mealList, getNoMealTextForType(mealType));
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching recipe", e));
        }
    }

    private RecyclerView getRecyclerViewForType(String mealType) {
        switch (mealType) {
            case "Breakfast": return breakfastRecyclerView;
            case "Lunch": return lunchRecyclerView;
            case "Dinner": return dinnerRecyclerView;
            default: return null;
        }
    }

    private TextView getNoMealTextForType(String mealType) {
        switch (mealType) {
            case "Breakfast": return noBreakfastText;
            case "Lunch": return noLunchText;
            case "Dinner": return noDinnerText;
            default: return null;
        }
    }



    private void setupRecyclerView(RecyclerView recyclerView, List<Meal> meals, TextView noMealText) {
        boolean hasMeals = !meals.isEmpty();
        noMealText.setVisibility(hasMeals ? View.GONE : View.VISIBLE);
        recyclerView.setVisibility(hasMeals ? View.VISIBLE : View.GONE);

        if (hasMeals) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
            recyclerView.setAdapter(new MealAdapter(getContext(), meals, true,this));
        }
    }

    @Override
    public void onMealsSelected(List<Recipe> selectedMeals, String type) {
        customProgressDialog.show();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference mealRef = db.collection("meals").document(selectedDate);

        // Extract timestamps from selected meals
        List<Long> newTimestamps = new ArrayList<>();
        List<Meal> newMeals = new ArrayList<>();

        for (Recipe recipe : selectedMeals) {
            newTimestamps.add(recipe.getTimestamp());
            newMeals.add(new Meal(recipe, type));
        }

        mealRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                Map<String, Object> mealData = new HashMap<>();

                if (document.exists()) {
                    // Get existing meal category data
                    List<Long> existingTimestamps = (List<Long>) document.get(type);

                    if (existingTimestamps == null) {
                        existingTimestamps = new ArrayList<>();
                    }

                    // 🚨 Remove duplicates within the same category
                    List<Long> uniqueNewTimestamps = new ArrayList<>();
                    for (Long timestamp : newTimestamps) {
                        if (!existingTimestamps.contains(timestamp)) {
                            uniqueNewTimestamps.add(timestamp);
                        } else {
                            // Optional: Display a message for duplicates
                            showSnackbar("Duplicate meal not allowed in " + type);
                            customProgressDialog.dismiss();
                            return;

                        }
                    }

                    // Add only unique timestamps to the existing list
                    existingTimestamps.addAll(uniqueNewTimestamps);

                    mealData.put(type, existingTimestamps);
                } else {
                    // If no meal exists for this date, create a new one
                    mealData.put(type, newTimestamps);
                }

                // Update Firestore
                mealRef.set(mealData, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            customProgressDialog.dismiss();
                            showSnackbar("Meals added successfully");

                            updateRecyclerView(getRecyclerViewForType(type), newMeals, getNoMealTextForType(type));
                        })
                        .addOnFailureListener(e -> {
                            customProgressDialog.dismiss();
                            showSnackbar("Error adding meals");
                        });
            } else {
                Exception e = task.getException();
                if (e != null) {
                    Log.e("FirestoreError", "Error retrieving document: " + e.getMessage(), e);
                }
                showSnackbar("Error retrieving document");
                customProgressDialog.dismiss();
            }
        });
    }



    private void updateRecyclerView(RecyclerView recyclerView, List<Meal> newMeals, TextView noMealText) {
        MealAdapter adapter = (MealAdapter) recyclerView.getAdapter();

        if (adapter != null) {
            adapter.addMeals(newMeals); // Add new meals
        } else {
            // If adapter is null, initialize a new one
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            adapter = new MealAdapter(getContext(), newMeals, true,this);
            recyclerView.setAdapter(adapter);
        }

        // Toggle visibility
        boolean hasMeals = !newMeals.isEmpty();
        noMealText.setVisibility(hasMeals ? View.GONE : View.VISIBLE);
        recyclerView.setVisibility(hasMeals ? View.VISIBLE : View.GONE);
    }




    private void showSnackbar(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }

    
    @Override
    public void onMealRemove(Meal meal, int position) {
        // 🔥 Show Material Design confirmation dialog before removing meal
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Remove Meal")
                .setMessage("Are you sure you want to remove this meal?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // 🔥 If user confirms, remove meal from Firestore
                    removeMealFromFirestore(meal, position);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // 🔥 If user cancels, dismiss the dialog
                    dialog.dismiss();
                })
                .show();
    }

    private void removeMealFromFirestore(Meal meal, int position) {
        customProgressDialog.show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference mealRef = db.collection("meals").document(selectedDate); // Use the selectedDate

        mealRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Get the list of timestamps for the specific meal category (e.g., Breakfast, Lunch, Dinner)
                    List<Long> mealTimestamps = (List<Long>) document.get(meal.getMealType());

                    if (mealTimestamps != null) {
                        // Remove the timestamp of the meal being deleted
                        Long mealTimestamp = meal.getRecipe().getTimestamp();
                        mealTimestamps.remove(mealTimestamp);

                        // Update Firestore with the modified list
                        Map<String, Object> updates = new HashMap<>();
                        updates.put(meal.getMealType(), mealTimestamps);

                        mealRef.update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    // Remove the meal from the RecyclerView
                                    RecyclerView recyclerView = getRecyclerViewForType(meal.getMealType());
                                    MealAdapter adapter = (MealAdapter) recyclerView.getAdapter();

                                    if (adapter != null) {
                                        adapter.removeMeal(position); // Remove the meal at the specified position
                                        adapter.notifyItemRemoved(position); // Notify the adapter of the removal

                                        // Update visibility of RecyclerView and noMealText
                                        TextView noMealText = getNoMealTextForType(meal.getMealType());
                                        boolean hasMeals = adapter.getItemCount() > 0;
                                        noMealText.setVisibility(hasMeals ? View.GONE : View.VISIBLE);
                                        recyclerView.setVisibility(hasMeals ? View.VISIBLE : View.GONE);
                                    }

                                    customProgressDialog.dismiss();
                                    showSnackbar("Meal removed successfully!");
                                })
                                .addOnFailureListener(e -> {
                                    customProgressDialog.dismiss();
                                    showSnackbar("Error removing meal from Firestore");
                                });
                    } else {
                        customProgressDialog.dismiss();
                        showSnackbar("No meals found in this category");
                    }
                } else {
                    customProgressDialog.dismiss();
                    showSnackbar("No meal plan found for this date");
                }
            } else {
                customProgressDialog.dismiss();
                showSnackbar("Error retrieving meal plan");
            }
        });
    }

    // Helper function to get today's date in "YYYY-MM-DD" format
    private String getTodayDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

}
