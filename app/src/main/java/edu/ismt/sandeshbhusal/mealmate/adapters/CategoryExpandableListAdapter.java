package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.R;

public class CategoryExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> categoryList;
    private HashMap<String, List<String>> ingredientMap;
    private List<String> selectedIngredients;

    public CategoryExpandableListAdapter(Context context, List<String> categoryList, HashMap<String, List<String>> ingredientMap, List<String> selectedIngredients) {
        this.context = context;
        this.categoryList = categoryList;
        this.ingredientMap = ingredientMap;
        this.selectedIngredients = selectedIngredients;
    }

    @Override
    public int getGroupCount() {
        return categoryList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // Divide by 2 because each row contains 2 ingredients
        return (int) Math.ceil(ingredientMap.get(categoryList.get(groupPosition)).size() / 2.0);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return categoryList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return ingredientMap.get(categoryList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    // Custom View for Category Header

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String categoryTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_category, null);
        }

        TextView textView = convertView.findViewById(R.id.categoryText);
        textView.setText(categoryTitle);

        // Set expand/collapse icon dynamically
        TextView icon = convertView.findViewById(R.id.expandCollapseIcon);
        if (isExpanded) {
            icon.setText("⬇");  // Collapse icon
        } else {
            icon.setText("➡");  // Expand icon
        }

        return convertView;
    }

    // Custom View for Ingredient with Checkbox

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_ingredient, null);
        }

        GridLayout ingredientGrid = convertView.findViewById(R.id.ingredientGrid);
        ingredientGrid.removeAllViews(); // Clear previous checkboxes to prevent duplication

        List<String> ingredients = ingredientMap.get(categoryList.get(groupPosition));

        // Start from the current child position and only get a subset for this row
        int index = childPosition * 2; // Two items per row
        if (index >= ingredients.size()) return convertView; // Prevent out-of-bounds errors

        // Create first checkbox
        CheckBox checkBox1 = new CheckBox(context);
        checkBox1.setText(ingredients.get(index));
        checkBox1.setPadding(8, 8, 8, 8);
        checkBox1.setChecked(selectedIngredients.contains(ingredients.get(index)));
        checkBox1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedIngredients.add(ingredients.get(index));
            else selectedIngredients.remove(ingredients.get(index));
        });
        ingredientGrid.addView(checkBox1);

        // Check if there is a second ingredient in this row
        if (index + 1 < ingredients.size()) {
            CheckBox checkBox2 = new CheckBox(context);
            checkBox2.setText(ingredients.get(index + 1));
            checkBox2.setPadding(8, 8, 8, 8);
            checkBox2.setChecked(selectedIngredients.contains(ingredients.get(index + 1)));
            checkBox2.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedIngredients.add(ingredients.get(index + 1));
                else selectedIngredients.remove(ingredients.get(index + 1));
            });
            ingredientGrid.addView(checkBox2);
        }

        return convertView;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
