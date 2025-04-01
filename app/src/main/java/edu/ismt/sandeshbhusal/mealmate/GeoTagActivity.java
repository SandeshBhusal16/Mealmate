package edu.ismt.sandeshbhusal.mealmate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.ismt.sandeshbhusal.mealmate.adapters.CategoryExpandableListAdapter;
import edu.ismt.sandeshbhusal.mealmate.utils.CustomExpandableListView;

public class GeoTagActivity extends AppCompatActivity {
    private EditText storeNameInput, addressInput;
    private TextView latLongText;
    private CustomExpandableListView expandableCategoryList;
    private Button saveStoreButton;

    private FusedLocationProviderClient fusedLocationClient;
    private List<String> categoryList;
    private HashMap<String, List<String>> ingredientMap;
    private List<String> selectedIngredients = new ArrayList<>();

    private CategoryExpandableListAdapter expandableListAdapter;
    private final String othersCategory = "🆕 Others";

    private Button addIngredientButton;
    private EditText newIngredientInput;
    CustomProgressDialog customProgressDialog;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_geo_tag);

        storeNameInput = findViewById(R.id.storeNameInput);
        addressInput = findViewById(R.id.addressInput);
        latLongText = findViewById(R.id.latLongText);
        newIngredientInput = findViewById(R.id.newIngredientInput);
        expandableCategoryList = findViewById(R.id.expandableCategoryList);
        saveStoreButton = findViewById(R.id.saveStoreButton);
        addIngredientButton =findViewById(R.id.addIngredientButton);
        customProgressDialog = new CustomProgressDialog(GeoTagActivity.this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(GeoTagActivity.this);
        loadCategoryAndIngredients();
        addIngredientButton.setOnClickListener(v -> {
            String newIngredient = newIngredientInput.getText().toString().trim();

            if (!newIngredient.isEmpty()) {
                // Check if "Others" category exists, if not, add it
                if (!categoryList.contains(othersCategory)) {
                    categoryList.add(othersCategory);
                    ingredientMap.put(othersCategory, new ArrayList<>());
                }

                String ingredientWithEmoji = "🆕 " + newIngredient;

                // Add new ingredient under "Others" and check it by default
                ingredientMap.get(othersCategory).add(ingredientWithEmoji);
                selectedIngredients.add(ingredientWithEmoji); // ✅ Auto-check it

                // Notify adapter and expand the "Others" category
                expandableListAdapter.notifyDataSetChanged();
                expandableCategoryList.expandGroup(categoryList.indexOf(othersCategory));

                // Clear input field for next entry
                newIngredientInput.setText("");
            } else {
                showSnackbar("Please enter an ingredient!");
            }
        });

        // Get Current Location
        checkLocationPermission();



        saveStoreButton.setOnClickListener(v -> saveStore());


    }



    private void saveStore(){
        String storeName = storeNameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String latLong = latLongText.getText().toString().trim();
        customProgressDialog.show();
        if(storeName.isEmpty()){
            customProgressDialog.dismiss();
            showSnackbar("Please enter a store name!");
            return;
        }

        if(address.isEmpty()){
            customProgressDialog.dismiss();
            showSnackbar("Please enter an address!");
            return;
        }

        if(latLong.isEmpty()){
            customProgressDialog.dismiss();
            showSnackbar("Please get location!");
            return;
        }

        if (selectedIngredients.isEmpty()) {
            customProgressDialog.dismiss(); // Dismiss progress dialog
            showSnackbar("Please select at least one ingredient!");
            return;
        }

        List<String> selectedIngredientsList = new ArrayList<>();

        for (String category : categoryList) {
            if (ingredientMap.containsKey(category)) {
                for (String ingredient : ingredientMap.get(category)) {
                    if (selectedIngredients.contains(ingredient)) {
                        // Trim whitespace and remove emojis from ingredient names
                        selectedIngredientsList.add(ingredient.replaceAll("[^a-zA-Z ]", "").trim());
                    }
                }
            }
        }

// Check if any ingredients are selected
        if (selectedIngredientsList.isEmpty()) {
            customProgressDialog.dismiss();
            showSnackbar("Please select at least one ingredient!");
            return;
        }

// Create store details without category map
        Map<String, Object> storeDetail = new HashMap<>();
        storeDetail.put("storeName", storeName);
        storeDetail.put("address", address);
        storeDetail.put("latLong", latLong);
        storeDetail.put("ingredients", selectedIngredientsList);

        db = FirebaseFirestore.getInstance();

        String documentId = String.valueOf(System.currentTimeMillis());

db.collection("favstore") // Collection name
                .document(documentId) // Document ID
                .set(storeDetail) // Data to save
                .addOnSuccessListener(aVoid -> {
                    // Success callback
                    customProgressDialog.dismiss(); // Dismiss progress dialog
                    showSnackbar("Store saved successfully!");
                    Log.d("Firestore", "Store saved with ID: " + documentId);
                    finish(); // Close the activity

                    // Clear input fields after saving
                    storeNameInput.setText("");
                    addressInput.setText("");
                    latLongText.setText("");
                    selectedIngredientsList.clear();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Failure callback
                    customProgressDialog.dismiss(); // Dismiss progress dialog
                    showSnackbar("Failed to save store: " + e.getMessage());
                    Log.e("Firestore", "Error saving store", e);
                });
    }


    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request both permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, proceed with location retrieval
            getCurrentLocation();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with getting location
                getCurrentLocation();
            } else {
                // Permission denied, show a message to the user
                latLongText.setText("Location permission denied. Please enable it in settings to get location.");
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(GeoTagActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(GeoTagActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request it again
            checkLocationPermission();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                latLongText.setText(String.format(Locale.getDefault(), "Lat: %.4f, Long: %.4f", latitude, longitude));

                Geocoder geocoder = new Geocoder(GeoTagActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);

                        String street = address.getThoroughfare(); // e.g., "New Road"

                        // Get Area/Locality
                        String area = address.getSubLocality(); // e.g., "Thamel"

                        // Get City
                        String city = address.getLocality(); // e.g., "Kathmandu"

                        // Combine them for a shorter address
                        String shortAddress = "";

                        if (street != null) {
                            shortAddress += street;
                        }
                        if (area != null) {
                            shortAddress += ", " + area;
                        }
                        if (city != null) {
                            shortAddress += ", " + city;
                        }

                        // Set the shorter address to the input field
                        addressInput.setText(shortAddress);

                       // addressInput.setText(addresses.get(0).getAddressLine(0));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                latLongText.setText("Unable to get location. Make sure location is enabled on the device.");
            }
        });
    }
    private void loadCategoryAndIngredients() {

        categoryList = new ArrayList<>();
        ingredientMap = new HashMap<>();


        categoryList.add("🥦 Vegetables");
        categoryList.add("🍎 Fruits");
        categoryList.add("🌾 Grains & Legumes");
        categoryList.add("🍗 Proteins");
        categoryList.add("🧀 Dairy");
        categoryList.add("🌿 Herbs & Spices");
        categoryList.add("🛢️ Oils & Condiments");


        ingredientMap.put("🥦 Vegetables", List.of("🥕 Carrot", "🥦 Broccoli", "🌿 Spinach", "🍅 Tomato", "🧅 Onion", "🧄 Garlic", "🌶 Bell Pepper", "🥒 Zucchini", "🥬 Cabbage", "🥬 Kale", "🥗 Lettuce", "🥔 Cauliflower"));
        ingredientMap.put("🍎 Fruits", List.of("🍏 Apple", "🍌 Banana", "🍊 Orange", "🍓 Strawberries", "🍇 Grapes", "🥭 Mango", "🍍 Pineapple", "🍋 Lemon/Lime"));
        ingredientMap.put("🌾 Grains & Legumes", List.of("🍚 Rice", "🌾 Quinoa", "🥣 Oats", "🌰 Lentils", "🫘 Chickpeas", "🌽 Corn", "🥜 Peanuts"));
        ingredientMap.put("🍗 Proteins", List.of("🍗 Chicken", "🥩 Beef", "🐖 Pork", "🐟 Fish", "🍳 Eggs", "🌱 Tofu", "🫘 Beans"));
        ingredientMap.put("🧀 Dairy", List.of("🥛 Milk", "🍦 Yogurt", "🧀 Cheese", "🧈 Butter", "🥥 Coconut Milk", "🌱 Soy/Oat Milk"));
        ingredientMap.put("🌿 Herbs & Spices", List.of("🌿 Basil", "🌿 Oregano", "🌿 Thyme", "🌿 Rosemary", "🧂 Salt", "🌶 Chili Powder", "🟠 Turmeric", "🟡 Ginger", "🟤 Cumin"));

        // ✅ NEW: Oils & Condiments List
        ingredientMap.put("🛢️ Oils & Condiments", List.of("🫒 Olive Oil", "🥥 Coconut Oil", "🥫 Soy Sauce", "🔥 Hot Sauce", "🍯 Honey", "🥄 Mayonnaise", "🍶 Vinegar", "🧂 Salt", "🍚 Sugar"));

        expandableListAdapter = new CategoryExpandableListAdapter(GeoTagActivity.this, categoryList, ingredientMap, selectedIngredients);

        expandableListAdapter = new CategoryExpandableListAdapter(GeoTagActivity.this, categoryList, ingredientMap, selectedIngredients);

        expandableCategoryList.setAdapter(expandableListAdapter);

        // Handle ingredient selection
        expandableCategoryList.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String selectedIngredient = ingredientMap.get(categoryList.get(groupPosition)).get(childPosition);

            if (!selectedIngredients.contains(selectedIngredient)) {
                selectedIngredients.add(selectedIngredient);
                Toast.makeText(GeoTagActivity.this, selectedIngredient + " added!", Toast.LENGTH_SHORT).show();
            } else {
                selectedIngredients.remove(selectedIngredient);
                Toast.makeText(GeoTagActivity.this, selectedIngredient + " removed!", Toast.LENGTH_SHORT).show();
            }

            return true;
        });
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .show();
    }
}