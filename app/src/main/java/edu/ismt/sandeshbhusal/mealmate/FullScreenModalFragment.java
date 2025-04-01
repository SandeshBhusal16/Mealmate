package edu.ismt.sandeshbhusal.mealmate;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.adapters.SelectionPagerAdapter;

public class FullScreenModalFragment extends BottomSheetDialogFragment {

    private ArrayList<String> selectedIngredientsWithPrices;
    private ArrayList<String> selectedIngredients;

    public static FullScreenModalFragment newInstance(ArrayList<String> ingredientsWithPrices, ArrayList<String> ingredients) {
        FullScreenModalFragment fragment = new FullScreenModalFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("selectedIngredientsWithPrices", ingredientsWithPrices);
        args.putStringArrayList("selectedIngredients", ingredients);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the Data from Bundle
        if (getArguments() != null) {
            selectedIngredientsWithPrices = getArguments().getStringArrayList("selectedIngredientsWithPrices");
            selectedIngredients = getArguments().getStringArrayList("selectedIngredients");
        }

        setCancelable(false);
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_full_screen_modal, container, false);
        view.findViewById(R.id.button_close).setOnClickListener(v -> {
            dismiss(); // Manually dismiss the modal
        });
        // Initialize Views
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.viewpager);

        Log.d("valuesOFSelecteIngriPrice",selectedIngredientsWithPrices.toString());
        Log.d("valuesOFSelecteIngri",selectedIngredients.toString());

        // Setup ViewPager and Adapter
        SelectionPagerAdapter pagerAdapter = new SelectionPagerAdapter(requireActivity(),selectedIngredients);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Contacts");
                    } else {
                        tab.setText("Stores");
                    }
                }).attach();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Make Full-Screen Modal
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        // Make BottomSheet Fully Expanded
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setPeekHeight(WindowManager.LayoutParams.MATCH_PARENT); // Make Full Screen by Default
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Expanded by Default
            behavior.setSkipCollapsed(true); // Skip Collapsed State
        }
    }



    @Override
    public int getTheme() {
        return R.style.FullScreenModalStyle;
    }
}
