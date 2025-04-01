package edu.ismt.sandeshbhusal.mealmate;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textview.MaterialTextView;

import java.util.Arrays;
import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.adapters.GroceryPageAdapter;
import edu.ismt.sandeshbhusal.mealmate.fragments.ProgressUpdateListener;


public class GroceryActivity extends AppCompatActivity implements ProgressUpdateListener {

    private ViewPager2 viewPager;
    private GroceryPageAdapter groceryPagerAdapter;
    private TabLayout tabLayout;

    private LinearProgressIndicator progressIndicator;
    private MaterialTextView summaryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery);

        progressIndicator = findViewById(R.id.progressIndicator);
        summaryTextView = findViewById(R.id.summary);

        // Set up Toolbar with Default Back Arrow
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize TabLayout and ViewPager2
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
     //   LinearProgressIndicator progressIndicator = findViewById(R.id.progressIndicator);
       // progressIndicator.setProgress(0); // Set dynamically based on purchased items


        // Grocery List Types
        List<String> groceryTypes = Arrays.asList("Today", "Tomorrow", "Week");

        // Set Up ViewPager Adapter with GroceryListFragment
        groceryPagerAdapter = new GroceryPageAdapter(this, groceryTypes);
        viewPager.setAdapter(groceryPagerAdapter);

        // Link ViewPager with TabLayout
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(groceryTypes.get(position))).attach();
    }

    @Override
    public void onProgressUpdated(int progress, String summaryText) {
        progressIndicator.setProgressCompat(progress, true);
        summaryTextView.setText(summaryText);
    }



}
