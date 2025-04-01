package edu.ismt.sandeshbhusal.mealmate;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import edu.ismt.sandeshbhusal.mealmate.adapters.RecipeCardAdapter;
import edu.ismt.sandeshbhusal.mealmate.adapters.WeeklyPlanAdapter;
import edu.ismt.sandeshbhusal.mealmate.model.Recipe;
import edu.ismt.sandeshbhusal.mealmate.utils.FirestoreHelper;

public class WeeklyPlanActivity extends AppCompatActivity {

    private TabLayout tabLayoutDays;
    private ViewPager2 viewPagerMeals;
    private WeeklyPlanAdapter weeklyPlanAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_plan);

        tabLayoutDays = findViewById(R.id.tabLayoutDays);
        viewPagerMeals = findViewById(R.id.viewPagerMeals);

        // Set up Adapter for ViewPager
        weeklyPlanAdapter = new WeeklyPlanAdapter(this);
        viewPagerMeals.setAdapter(weeklyPlanAdapter);

        // Attach TabLayout with ViewPager
        new TabLayoutMediator(tabLayoutDays, viewPagerMeals, (tab, position) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, position);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
            tab.setText(dateFormat.format(calendar.getTime())); // Exa


        }).attach();




    }




}
