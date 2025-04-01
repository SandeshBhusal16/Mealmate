package edu.ismt.sandeshbhusal.mealmate;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.adapters.MealSelectionAdapter;
import edu.ismt.sandeshbhusal.mealmate.model.Recipe;

public class MealSelectionBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private MealSelectionAdapter adapter;
    private List<Recipe> recipeList;
    private List<Recipe> selectedMeals = new ArrayList<>();
    private SearchView searchView;
    private Button confirmButton;
    private TextView selectedCountText;
    private MealSelectionListener mealSelectionListener; // 👈 Declare Listener
    private String mealType;

    public static MealSelectionBottomSheet newInstance(List<Recipe> recipes, String mealType) {
        MealSelectionBottomSheet fragment = new MealSelectionBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable("recipeList", (ArrayList<Recipe>) recipes);
        args.putString("mealType", mealType); // Store meal type
        fragment.setArguments(args);
        return fragment;
    }

    public void setMealSelectionListener(MealSelectionListener listener) {
        this.mealSelectionListener = listener; // 👈 Set Listener
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recipeList = (List<Recipe>) getArguments().getSerializable("recipeList");
        mealType = getArguments().getString("mealType");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_selection, container, false);

        recyclerView = view.findViewById(R.id.mealRecyclerView);
        searchView = view.findViewById(R.id.searchMeal);
        confirmButton = view.findViewById(R.id.confirmMealSelection);
        selectedCountText = view.findViewById(R.id.selectedCountText);
        Log.d("BottomShhetRecipe",recipeList.toString());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new MealSelectionAdapter(recipeList, selectedMeals, selectedCountText, mealType);
        recyclerView.setAdapter(adapter);


        setupSearchView();
        setupConfirmButton();

        return view;
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    private void setupConfirmButton() {
        confirmButton.setOnClickListener(v -> {
            if (mealSelectionListener != null) {
                mealSelectionListener.onMealsSelected(selectedMeals,mealType); // 👈 Send Data to Fragment
            }
            dismiss();;
        });
    }

    public interface MealSelectionListener {
        void onMealsSelected(List<Recipe> selectedMeals, String mealType);
    }
}
