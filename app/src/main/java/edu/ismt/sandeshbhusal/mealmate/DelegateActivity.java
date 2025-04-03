package edu.ismt.sandeshbhusal.mealmate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.ismt.sandeshbhusal.mealmate.model.Ingredient;
import edu.ismt.sandeshbhusal.mealmate.adapters.DelegateStoreAdapter;
import edu.ismt.sandeshbhusal.mealmate.adapters.MealDelegateAdapter;
import edu.ismt.sandeshbhusal.mealmate.adapters.TableIngredientDelegateAdapter;
import edu.ismt.sandeshbhusal.mealmate.model.Ingredient;
import edu.ismt.sandeshbhusal.mealmate.model.SavedLocation;
import edu.ismt.sandeshbhusal.mealmate.utils.APIKey;
import edu.ismt.sandeshbhusal.mealmate.utils.GroceryDatabaseHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DelegateActivity extends AppCompatActivity {
    private static final String STATE_SELECTIONS = "selected_ingredients";
    private static final String STORE_STATE = "store_state";
    private static final int REQUEST_CONTACT = 101;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    // UI Components
    private RecyclerView rvMeals, rvIngredients;
    private MaterialButton btnConfirm, btnSelectStore;
    private TextInputEditText etStoreName, etStoreAddress;
    private CustomProgressDialog progressDialog;
    private FusedLocationProviderClient fusedLocationClient;

    // Adapters
    private MealDelegateAdapter mealAdapter;
    private TableIngredientDelegateAdapter ingredientAdapter;

    // Data
    private GroceryDatabaseHelper dbHelper;
    private Map<String, List<String>> mealsMap;
    private Map<String, List<Ingredient>> allIngredientsMap = new HashMap<>();
    private List<Ingredient> currentSelections = new ArrayList<>();
    private List<SavedLocation> storeList = new ArrayList<>();
    private List<String> selectedRecipients = new ArrayList<>();
    private double currentLat = 0.0, currentLng = 0.0;
    private boolean isStoreSectionExpanded = false;
    private SelectedStore currentStore = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_delegate_table);

        progressDialog = new CustomProgressDialog(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeViews();
        setupDatabase();
        setupMealRecyclerView();
        getCurrentLocation();
    }




//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        // Restore selections
//        ArrayList<Ingredient> savedSelections = (ArrayList<Ingredient>) savedInstanceState.getSerializable(STATE_SELECTIONS);
//        if (savedSelections != null) {
//            currentSelections.clear();
//            currentSelections.addAll(savedSelections);
//            restoreSelectionsToAdapter();
//        }
//        // Restore store info
//        if (savedInstanceState.containsKey("store_name")) {
//            currentStore = new SelectedStore(
//                    savedInstanceState.getString("store_name"),
//                    savedInstanceState.getString("store_address"),
//                    new LatLng(
//                            savedInstanceState.getDouble("store_lat"),
//                            savedInstanceState.getDouble("store_lng")
//                    ),
//                    false
//            );
//            updateStoreHeader();
//        }
//    }

//    private void restoreSelectionsToAdapter() {
//        if (ingredientAdapter != null) {
//            // Get current ingredients from adapter
//            List<Ingredient> currentIngredients = ingredientAdapter.getIngredients();
//
//            // Restore selections
//            for (Ingredient selected : currentSelections) {
//                for (Ingredient current : currentIngredients) {
//                    if (current.getName().equals(selected.getName())) {
//                        current.setSelected(true);
//                        current.setQuantity(selected.getQuantity());
//                        current.setPrice(selected.getPrice());
//                        break;
//                    }
//                }
//            }
//            ingredientAdapter.notifyDataSetChanged();
//        }
//    }




    private void initializeViews() {
        rvMeals = findViewById(R.id.rvMeals);
        rvIngredients = findViewById(R.id.rvIngredients);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnSelectStore = findViewById(R.id.btnSelectStore);
        etStoreName = findViewById(R.id.etStoreName);
        etStoreAddress = findViewById(R.id.etStoreAddress);

        btnSelectStore.setOnClickListener(v -> showStoreBottomSheet());
        btnConfirm.setOnClickListener(v -> validateAndSendShoppingList());
    }


    private void validateAndSendShoppingList() {
        // Validate ingredients
        if (ingredientAdapter == null || ingredientAdapter.getSelectedIngredients().isEmpty()) {
            showSnackbar("Please select at least one ingredient");
            return;
        }

        // Validate store info
        String storeName = etStoreName.getText().toString().trim();
        if (TextUtils.isEmpty(storeName)) {
            showSnackbar("Please select or enter a store");
            return;
        }

        // Save current selections
        currentSelections.clear();
        currentSelections.addAll(ingredientAdapter.getSelectedIngredients());
        selectedRecipients.clear();

        // Launch contact picker
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_CONTACT);
    }


    private void setupDatabase() {
        dbHelper = new GroceryDatabaseHelper(this);
        mealsMap = dbHelper.getWeeklyUnpurchasedMeals();
    }

    private void setupMealRecyclerView() {
        rvMeals.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        // Pre-load all ingredients
        for (Map.Entry<String, List<String>> entry : mealsMap.entrySet()) {
            List<Ingredient> ingredients = new ArrayList<>();
            for (String name : entry.getValue()) {
                ingredients.add(new Ingredient(name));
            }
            allIngredientsMap.put(entry.getKey(), ingredients);
        }

        mealAdapter = new MealDelegateAdapter(new ArrayList<>(mealsMap.keySet()));
        rvMeals.setAdapter(mealAdapter);

        mealAdapter.setOnMealSelectedListener(meal -> {
            // Save current selections before switching
            if (ingredientAdapter != null) {
                currentSelections.clear();
                currentSelections.addAll(ingredientAdapter.getSelectedIngredients());
            }

            List<Ingredient> ingredients = new ArrayList<>(allIngredientsMap.get(meal));

            // Apply saved selections to new meal's ingredients
            for (Ingredient existing : currentSelections) {
                for (Ingredient newIng : ingredients) {
                    if (newIng.getName().equals(existing.getName())) {
                        newIng.setSelected(true);
                        newIng.setQuantity(existing.getQuantity());
                        newIng.setPrice(existing.getPrice());
                    }
                }
            }

            ingredientAdapter = new TableIngredientDelegateAdapter(ingredients);
            rvIngredients.setLayoutManager(new LinearLayoutManager(this));
            rvIngredients.setAdapter(ingredientAdapter);
        });
    }

    private void setupConfirmationButton() {
        btnConfirm.setOnClickListener(v -> sendShoppingList());
    }

    public void toggleStoreSection(View view) {
        LinearLayout storeDetails = findViewById(R.id.storeDetails);
        TextView toggleIndicator = findViewById(R.id.txtStoreToggleIndicator);

        isStoreSectionExpanded = !isStoreSectionExpanded;

        if (isStoreSectionExpanded) {
            storeDetails.setVisibility(View.VISIBLE);
            toggleIndicator.setText("▲");
        } else {
            storeDetails.setVisibility(View.GONE);
            toggleIndicator.setText("▼");
        }
    }



    private void selectRecipients() {
        saveCurrentSelections();

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_CONTACT);
    }

    private void saveCurrentSelections() {
        if (ingredientAdapter != null) {
            currentSelections.clear();
            currentSelections.addAll(ingredientAdapter.getSelectedIngredients());
        }
        // Save other important state if needed
    }

    private void showStoreBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_stores);

        RecyclerView rvStores = dialog.findViewById(R.id.rvStores);
        rvStores.setLayoutManager(new LinearLayoutManager(this));

        loadFavoriteStores(stores -> {
            DelegateStoreAdapter adapter = new DelegateStoreAdapter(this, stores, store -> {
                currentStore = new SelectedStore(
                        store.getName(),
                        store.getAddress(),
                        new LatLng(store.getLatitude(), store.getLongitude()),
                        false
                );

                etStoreName.setText(store.getName());
                etStoreAddress.setText(store.getAddress());
                updateStoreHeader();
                dialog.dismiss();
            });

            rvStores.setAdapter(adapter);
        });

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateStoreHeader() {
        TextView txtSelectedStore = findViewById(R.id.txtSelectedStore);
        if (currentStore != null) {
            txtSelectedStore.setText(currentStore.name);
        } else {
            txtSelectedStore.setText("[Not selected]");
        }
    }



    private void loadFavoriteStores(StoresLoadListener listener) {
        progressDialog.show();

        FirebaseFirestore.getInstance().collection("favstore")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    storeList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String storeName = document.getString("storeName");
                        String address = document.getString("address");
                        String latLong = document.getString("latLong");

                        String[] latLngParts = latLong.replace("Lat:", "")
                                .replace("Long:", "")
                                .split(",");
                        double storeLat = Double.parseDouble(latLngParts[0].trim());
                        double storeLng = Double.parseDouble(latLngParts[1].trim());

                        String distance = calculateDistance(currentLat, currentLng, storeLat, storeLng);

                        SavedLocation store = new SavedLocation(
                                storeName,
                                "", // Will be updated async
                                address,
                                storeLat,
                                storeLng,
                                distance,
                                new ArrayList<>(),
                                0
                        );

                        getStorePhoto(storeLat, storeLng, photoUrl -> {
                            store.setImageUrl(photoUrl != null ? photoUrl : "");
                            if (listener != null) {
                                listener.onStoresLoaded(storeList);
                            }
                        });

                        storeList.add(store);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Collections.sort(storeList, Comparator.comparingDouble(
                                store -> parseDistance(store.getDistance()))
                        );
                    }
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to load stores", Toast.LENGTH_SHORT).show();
                });
    }

    private void getStorePhoto(double lat, double lng, OnPhotoUrlReceivedListener listener) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + lat + "," + lng +
                "&radius=150&types=grocery_or_supermarket&key=" + APIKey.GOOGLE_API_KEY;

        new OkHttpClient().newCall(new Request.Builder().url(url).build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> listener.onPhotoUrlReceived(null));
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            JSONArray results = json.optJSONArray("results");
                            if (results != null && results.length() > 0) {
                                JSONObject place = results.getJSONObject(0);
                                JSONArray photos = place.optJSONArray("photos");
                                if (photos != null && photos.length() > 0) {
                                    String photoRef = photos.getJSONObject(0).getString("photo_reference");
                                    String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?" +
                                            "maxwidth=400&photoreference=" + photoRef +
                                            "&key=" + APIKey.GOOGLE_API_KEY;
                                    runOnUiThread(() -> listener.onPhotoUrlReceived(photoUrl));
                                    return;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(() -> listener.onPhotoUrlReceived(null));
                    }
                });
    }

    private String calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        float meters = results[0];
        return meters < 1000 ?
                String.format(Locale.getDefault(), "%.0f m", meters) :
                String.format(Locale.getDefault(), "%.1f km", meters/1000);
    }

    private float parseDistance(String distance) {
        try {
            if (distance.contains("km")) {
                return Float.parseFloat(distance.replace("km", "").trim()) * 1000;
            } else if (distance.contains("m")) {
                return Float.parseFloat(distance.replace("m", "").trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Float.MAX_VALUE;
    }

    private void sendShoppingList() {
        // Validate ingredients
        if (ingredientAdapter == null || ingredientAdapter.getSelectedIngredients().isEmpty()) {
            showSnackbar("Please select at least one ingredient");
            return;
        }

        // Validate recipients

        // Get selected ingredients
        currentSelections.clear();
        currentSelections.addAll(ingredientAdapter.getSelectedIngredients());

        // Build message based on store selection type
        StringBuilder smsBody = new StringBuilder("Will you shop this for me?\n\n");
        double totalCost = 0.0;

        // Add ingredients list
        smsBody.append("🛒 Shopping List:\n");
        for (Ingredient item : currentSelections) {
            try {
                String quantity = item.getQuantity();

                double itemTotal =  item.getPrice();
                totalCost += itemTotal;

                smsBody.append(String.format(Locale.getDefault(),
                        "• %s\n  Qty: %d | Price: $%.2f |\n",
                        item.getName(),
                        quantity,
                        item.getPrice()

                        ));
            } catch (Exception e) {
                showSnackbar("Invalid quantity/price for " + item.getName());
                return;
            }
        }

        // Add total
        smsBody.append("\n══════════════════\n");
        smsBody.append(String.format(Locale.getDefault(),
                "GRAND TOTAL: $%.2f\n", totalCost));
        smsBody.append("══════════════════\n\n");

        // Add store information
        String storeName = etStoreName.getText().toString().trim();
        String storeAddress = etStoreAddress.getText().toString().trim();

        if (!TextUtils.isEmpty(storeName)) {
            smsBody.append("📍 Store Information:\n");
            smsBody.append(storeName);

            if (!TextUtils.isEmpty(storeAddress)) {
                smsBody.append("\n").append(storeAddress);
            }

            // Add Google Maps link if we have coordinates (from favorite store selection)
            if (currentStore != null && currentStore.location != null) {
                String mapsLink = String.format(Locale.getDefault(),
                        "https://www.google.com/maps/dir/?api=1&destination=%f,%f",
                        currentStore.location.latitude,
                        currentStore.location.longitude);

                smsBody.append("\n\nDirections: ").append(mapsLink);
            }

            smsBody.append("\n");
        }

        smsBody.append("\nThank you!");

        // Send SMS
        sendSmsToRecipients(smsBody.toString());
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setAction("OK", v -> {})
                .show();
    }

    private void sendSmsToRecipients(String message) {
        try {
            Uri smsUri = Uri.parse("smsto:" + TextUtils.join(",", selectedRecipients));

            // Try ACTION_SENDTO first (standard way)
            Intent sendIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
            sendIntent.putExtra("sms_body", message);

            // Check if any app can handle the intent
            PackageManager pm = getPackageManager();
            List<ResolveInfo> handlers = pm.queryIntentActivities(sendIntent, 0);

            if (!handlers.isEmpty()) {
                startActivity(sendIntent);
                return;
            }

            // Fallback to ACTION_VIEW if no app handles ACTION_SENDTO
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setData(Uri.parse("sms:" + TextUtils.join(",", selectedRecipients)));
            viewIntent.putExtra("sms_body", message);

            if (viewIntent.resolveActivity(pm) != null) {
                startActivity(viewIntent);
            } else {
                // Final fallback: Show message in dialog
                new AlertDialog.Builder(this)
                        .setTitle("SMS Preview (No SMS App)")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show();
                Log.d("SMS_TEST", "No SMS app available, showing fallback");
            }

        } catch (Exception e) {
            showSnackbar("Failed to send SMS: " + e.getMessage());
            Log.e("SMS_TEST", "SMS Error", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONTACT && resultCode == RESULT_OK && data != null) {
            selectedRecipients.clear();

            if (data.getClipData() != null) {
                // Multiple contacts selected
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    addRecipientFromUri(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                // Single contact selected
                addRecipientFromUri(data.getData());
            }

            if (!selectedRecipients.isEmpty()) {
                prepareAndSendSMS();
            } else {
                showSnackbar("No recipients selected");
            }
        }
    }

    private void addRecipientFromUri(Uri contactUri) {
        try (Cursor cursor = getContentResolver().query(contactUri,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                String phone = cursor.getString(0);
                if (!TextUtils.isEmpty(phone)) {
                    selectedRecipients.add(phone);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void prepareAndSendSMS() {
        // Build the message
        StringBuilder smsBody = new StringBuilder("Will you shop this for me?\n\n");
        double totalCost = 0.0;

        // Add ingredients list
        smsBody.append("🛒 Shopping List:\n");
        for (Ingredient item : currentSelections) {
            try {
                String quantity = item.getQuantity();
                double itemTotal = item.getPrice();
                totalCost += itemTotal;

                smsBody.append(String.format(Locale.getDefault(),
                        "• %s\n  Qty: %s | Total: $%.2f\n",
                        item.getName(),
                        quantity,
                        itemTotal));
            } catch (Exception e) {
                showSnackbar("Invalid entry for " + item.getName());
                return;
            }
        }

        // Add total
        smsBody.append("\n══════════════════\n");
        smsBody.append(String.format(Locale.getDefault(), "GRAND TOTAL: $%.2f\n", totalCost));
        smsBody.append("══════════════════\n\n");

        // Add store information
        String storeName = etStoreName.getText().toString().trim();
        String storeAddress = etStoreAddress.getText().toString().trim();

        smsBody.append("📍 Store Information:\n").append(storeName);
        if (!TextUtils.isEmpty(storeAddress)) {
            smsBody.append("\n").append(storeAddress);
        }

        // Add Google Maps link if available
        if (currentStore != null && currentStore.location != null) {
            String mapsLink = String.format(Locale.getDefault(),
                    "https://www.google.com/maps/dir/?api=1&destination=%f,%f",
                    currentStore.location.latitude,
                    currentStore.location.longitude);
            smsBody.append("\n\nDirections: ").append(mapsLink);
        }

        smsBody.append("\n\nThank you!");

        // Send SMS
        sendSmsToRecipients(smsBody.toString());
    }





    private void restoreSelections() {
        if (ingredientAdapter != null && !currentSelections.isEmpty()) {
            List<Ingredient> currentIngredients = new ArrayList<>();
            // Get current ingredients from adapter - you'll need to implement getIngredients() in your adapter
            // currentIngredients = ingredientAdapter.getIngredients();

            for (Ingredient selected : currentSelections) {
                for (Ingredient current : currentIngredients) {
                    if (current.getName().equals(selected.getName())) {
                        current.setSelected(true);
                        current.setQuantity(selected.getQuantity());
                        current.setPrice(selected.getPrice());
                        break;
                    }
                }
            }
            ingredientAdapter.notifyDataSetChanged();
        }
    }



    private void getCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLng = location.getLongitude();
                }
            });
        }
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Helper classes and interfaces
    private static class SelectedStore {
        String name;
        String address;
        LatLng location;
        boolean isManual;

        SelectedStore(String name, String address, LatLng location, boolean isManual) {
            this.name = name;
            this.address = address;
            this.location = location;
            this.isManual = isManual;
        }
    }

    interface OnPhotoUrlReceivedListener {
        void onPhotoUrlReceived(String url);
    }

    interface StoresLoadListener {
        void onStoresLoaded(List<SavedLocation> stores);
    }
}