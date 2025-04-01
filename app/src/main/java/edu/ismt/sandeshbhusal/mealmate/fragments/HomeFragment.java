package edu.ismt.sandeshbhusal.mealmate.fragments;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import edu.ismt.sandeshbhusal.mealmate.CustomProgressDialog;
import edu.ismt.sandeshbhusal.mealmate.GeoTagActivity;
import edu.ismt.sandeshbhusal.mealmate.GroceryActivity;
import edu.ismt.sandeshbhusal.mealmate.MapExplorerActivity;
import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.WeeklyPlanActivity;
import edu.ismt.sandeshbhusal.mealmate.adapters.MealAdapter;
import edu.ismt.sandeshbhusal.mealmate.adapters.StoreAdapter;
import edu.ismt.sandeshbhusal.mealmate.model.Meal;
import edu.ismt.sandeshbhusal.mealmate.model.Recipe;
import edu.ismt.sandeshbhusal.mealmate.model.SavedLocation;
import edu.ismt.sandeshbhusal.mealmate.utils.APIKey;
import edu.ismt.sandeshbhusal.mealmate.utils.GroceryDatabaseHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;



public class HomeFragment extends Fragment implements MealAdapter.OnMealRemoveListener {

    private RecyclerView todaysMealRecyclerView, favStoreRecyclerView;
    private MealAdapter mealAdapter;
    private List<Meal> mealList;
    private TextView noMealText;
    private MaterialButton viewWeeklyPlanButton;
    private FloatingActionButton shop;
    private CustomProgressDialog customProgressDialog;

    private int completedRequests = 0;
    private int totalRequests = 0;

    private ShapeableImageView addStore, explore, filterList;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private List<SavedLocation> storeList = new ArrayList<>();
    private StoreAdapter storeAdapter;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Views
        todaysMealRecyclerView = view.findViewById(R.id.todaysMealRecyclerView);
        todaysMealRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        noMealText = view.findViewById(R.id.noMealText);
        addStore = view.findViewById(R.id.addFavStoreIcon);
        filterList = view.findViewById(R.id.filterStoresIcon);

        explore = view.findViewById(R.id.viewMapIcon);


        favStoreRecyclerView = view.findViewById(R.id.favStoresRecyclerView);
        viewWeeklyPlanButton = view.findViewById(R.id.viewWeeklyPlanButton);
        shop = view.findViewById(R.id.shop);
        customProgressDialog = new CustomProgressDialog(getActivity());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        favStoreRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        loadFavStoreDataFromFireStore();
        loadDataMealToday(true);

        filterList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             if(storeAdapter!=null && !storeList.isEmpty()){
                 BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                 View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_sort_options, null);
                 bottomSheetDialog.setContentView(bottomSheetView);

                 // Set up click listeners for each option
                 bottomSheetView.findViewById(R.id.sort_by_distance).setOnClickListener(option -> {
                     sortStoresByDistance();
                     bottomSheetDialog.dismiss();
                 });

                 bottomSheetView.findViewById(R.id.sort_by_grocery).setOnClickListener(option -> {
                     sortStoresByGroceryMatches();
                     bottomSheetDialog.dismiss();
                 });

                 bottomSheetDialog.show();
             }
             else {
                 Snackbar.make(view, "No stores available to sort", Snackbar.LENGTH_SHORT).show();
             }
            }
        });

        // Button Listeners
        viewWeeklyPlanButton.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), WeeklyPlanActivity.class));
        });

        shop.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), GroceryActivity.class));
        });

        addStore.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), GeoTagActivity.class));

        });

        explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MapExplorerActivity.class);
                intent.putParcelableArrayListExtra("storeList", new ArrayList<>(storeList));
                startActivity(intent);
            }
        });

        return view;
    }

    void loadFavStoreDataFromFireStore() {
        customProgressDialog.show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        storeList.clear();

        GroceryDatabaseHelper groceryDbHelper = new GroceryDatabaseHelper(requireContext());

        // Fetch Unpurchased Grocery Items for the Week
        Map<String, Map<String, List<String>>> weeklyGroceryMap = groceryDbHelper.getGroceryItemsForWeekUnpurchased();
        // Use a Set to ensure uniqueness
        Set<String> uniqueGroceryItems = new HashSet<>();

        for (Map<String, List<String>> categoryMap : weeklyGroceryMap.values()) {
            for (List<String> items : categoryMap.values()) {
                uniqueGroceryItems.addAll(items); // Set automatically handles duplicates
            }
        }

// Convert the Set back to a List for further processing
        List<String> groceryItems = new ArrayList<>(uniqueGroceryItems);


        // Get Current Location
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double currentLat = location.getLatitude();
                    double currentLng = location.getLongitude();

                    db.collection("favstore")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    String storeName = document.getString("storeName");
                                    String address = document.getString("address");
                                    String latLong = document.getString("latLong");
                                    Log.d("latlong", latLong);
                                    List<String> ingredients = (List<String>) document.get("ingredients");

                                    // Extract Latitude and Longitude from latLong string
                                    String[] latLngParts = latLong.replace("Lat:", "").replace("Long:", "").split(",");
                                    double storeLat = Double.parseDouble(latLngParts[0].trim());
                                    double storeLng = Double.parseDouble(latLngParts[1].trim());

                                    // Calculate Distance
                                    String distance = calculateDistance(currentLat, currentLng, storeLat, storeLng);

                                    // Get Matched Ingredients
                                    List<String> matchedIngredients = new ArrayList<>();
                                    for (String ingredient : ingredients) {
                                        for (String dbItem : groceryItems) {
                                            if (ingredient.equalsIgnoreCase(dbItem.trim())) {
                                                matchedIngredients.add(ingredient);
                                            }
                                        }
                                    }

                                    // Get Photo URL
                                    getPhotoReference(String.valueOf(storeLat), String.valueOf(storeLng), APIKey.GOOGLE_API_KEY, new OnPhotoUrlReceivedListener() {
                                        @Override
                                        public void onPhotoUrlReceived(String imageUrl) {
                                            // Add to List
                                            SavedLocation savedLocation = new SavedLocation(
                                                    storeName,
                                                    imageUrl != null ? imageUrl : "https://example.com/default_image.jpg", // Use default image if null
                                                    address,
                                                    storeLat,
                                                    storeLng,
                                                    distance,
                                                    ingredients,
                                                    matchedIngredients.size()  // Matching Count
                                            );

                                            // Set matched ingredients
                                            savedLocation.setMatchedIngredients(matchedIngredients);
                                            storeList.add(savedLocation);

                                            // Refresh Adapter on the UI Thread
                                            requireActivity().runOnUiThread(() -> {
                                                if (storeAdapter == null) {
                                                    storeAdapter = new StoreAdapter(requireContext(), storeList);
                                                    favStoreRecyclerView.setAdapter(storeAdapter);
                                                } else {
                                                    storeAdapter.notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    });
                                }

                                // Set Adapter (In case there are no stores to display)
                                StoreAdapter adapter = new StoreAdapter(requireContext(), storeList);
                                favStoreRecyclerView.setAdapter(adapter);

                                customProgressDialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                customProgressDialog.dismiss();
                                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                            });
                }
            });
        }
    }






    private String calculateDistance(double currentLat, double currentLng, double storeLat, double storeLng) {
        float[] results = new float[1];
        Location.distanceBetween(currentLat, currentLng, storeLat, storeLng, results);
        float distanceInMeters = results[0];

        if (distanceInMeters < 1000) {
            return String.format(Locale.getDefault(), "%.0f m", distanceInMeters);
        } else {
            return String.format(Locale.getDefault(), "%.1f km", distanceInMeters / 1000);
        }
    }


    private float parseDistance(String distanceStr) {
        // Assumes distanceStr is in the format "500 m" or "1.2 km"
        if (distanceStr.toLowerCase().contains("km")) {
            // Remove "km", trim, and convert to float then multiply by 1000 to get meters.
            try {
                float km = Float.parseFloat(distanceStr.toLowerCase().replace("km", "").trim());
                return km * 1000;
            } catch (NumberFormatException e) {
                return Float.MAX_VALUE;
            }
        } else if (distanceStr.toLowerCase().contains("m")) {
            // Remove "m", trim, and convert to float
            try {
                return Float.parseFloat(distanceStr.toLowerCase().replace("m", "").trim());
            } catch (NumberFormatException e) {
                return Float.MAX_VALUE;
            }
        }
        return Float.MAX_VALUE; // Fallback if the format is unrecognized
    }

    private void sortStoresByDistance() {
        Collections.sort(storeList, (s1, s2) -> {
            float d1 = parseDistance(s1.getDistance());
            float d2 = parseDistance(s2.getDistance());
            return Float.compare(d1, d2);
        });
        if (storeAdapter != null) {
            storeAdapter.notifyDataSetChanged();
        }
    }
    private void sortStoresByGroceryMatches() {
        Collections.sort(storeList, (s1, s2) ->
                Integer.compare(s2.getMatchingCount(), s1.getMatchingCount())
        );
        if (storeAdapter != null) {
            storeAdapter.notifyDataSetChanged();
        }
    }




    private void getPhotoReference(String latitude, String longitude, String apiKey, OnPhotoUrlReceivedListener listener) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + latitude + "," + longitude
                + "&radius=1000&types=store|grocery_or_supermarket|shopping_mall&key=" + apiKey;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                listener.onPhotoUrlReceived(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    listener.onPhotoUrlReceived(null);
                    return;
                }

                String jsonResponse = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONArray results = jsonObject.optJSONArray("results");

                    if (results != null && results.length() > 0) {
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);

                            // Filter by relevant place types
                            JSONArray types = place.getJSONArray("types");
                            for (int j = 0; j < types.length(); j++) {
                                String type = types.getString(j);
                                if (type.equals("store") || type.equals("grocery_or_supermarket") || type.equals("shopping_mall")) {
                                    JSONArray photos = place.optJSONArray("photos");
                                    if (photos != null && photos.length() > 0) {
                                        JSONObject photo = photos.getJSONObject(0);
                                        String photoReference = photo.getString("photo_reference");

                                        String imageUrl = getPhotoUrl(photoReference);
                                        listener.onPhotoUrlReceived(imageUrl);
                                        return; // Return first found image URL
                                    }
                                }
                            }
                        }
                        listener.onPhotoUrlReceived(null); // No image found for relevant types
                    } else {
                        listener.onPhotoUrlReceived(null); // No results
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onPhotoUrlReceived(null);
                }
            }
        });
    }






    public String getPhotoUrl(String photoReference) {
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="
                + photoReference + "&key=" + APIKey.GOOGLE_API_KEY;
    }




    @Override
    public void onResume() {
        super.onResume();
        if (mealList == null || mealList.isEmpty()) {
            loadDataMealToday(false);
        }
    }

    private void loadDataMealToday(boolean showLoad) {
        completedRequests = 0;
        totalRequests = 0;

        if (showLoad) {
            customProgressDialog.show();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = dateFormat.format(new Date());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.enableNetwork();
        DocumentReference mealRef = db.collection("meals").document(todayDate);

        mealRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    List<Long> breakfastTimestamps = (List<Long>) document.get("Breakfast");
                    List<Long> lunchTimestamps = (List<Long>) document.get("Lunch");
                    List<Long> dinnerTimestamps = (List<Long>) document.get("Dinner");

                    if (breakfastTimestamps == null) breakfastTimestamps = new ArrayList<>();
                    if (lunchTimestamps == null) lunchTimestamps = new ArrayList<>();
                    if (dinnerTimestamps == null) dinnerTimestamps = new ArrayList<>();

                    totalRequests = breakfastTimestamps.size() + lunchTimestamps.size() + dinnerTimestamps.size();
                    List<Meal> allMeals = new ArrayList<>();

                    fetchAllMeals(breakfastTimestamps, lunchTimestamps, dinnerTimestamps, allMeals);
                } else {
                    updateMealRecyclerView(new ArrayList<>());
                    if (showLoad) customProgressDialog.dismiss();
                }
            } else {
                showSnackbar("error");
                if (showLoad) customProgressDialog.dismiss();
            }
        });
    }

    private void fetchAllMeals(List<Long> breakfastTimestamps, List<Long> lunchTimestamps,
                               List<Long> dinnerTimestamps, List<Meal> allMeals) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (totalRequests == 0) {
            updateMealRecyclerView(new ArrayList<>());
            customProgressDialog.dismiss();
            return;
        }

        for (Long timestamp : breakfastTimestamps) {
            fetchRecipeByTimestamp(db, timestamp, "Breakfast", allMeals);
        }
        for (Long timestamp : lunchTimestamps) {
            fetchRecipeByTimestamp(db, timestamp, "Lunch", allMeals);
        }
        for (Long timestamp : dinnerTimestamps) {
            fetchRecipeByTimestamp(db, timestamp, "Dinner", allMeals);
        }
    }

    private void fetchRecipeByTimestamp(FirebaseFirestore db, Long timestamp, String mealType, List<Meal> allMeals) {
        db.collection("recipes")
                .whereEqualTo("timestamp", timestamp)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Recipe recipe = document.toObject(Recipe.class);
                            if (recipe != null) {
                                allMeals.add(new Meal(recipe, mealType));
                            }
                        }
                    }
                    checkAndUpdateRecyclerView(allMeals);
                })
                .addOnFailureListener(e -> checkAndUpdateRecyclerView(allMeals));
    }

    private void checkAndUpdateRecyclerView(List<Meal> allMeals) {
        completedRequests++;
        if (completedRequests == totalRequests) {
            updateMealRecyclerView(allMeals);
            customProgressDialog.dismiss();
        }
    }

    private void updateMealRecyclerView(List<Meal> allMeals) {
        boolean hasMeals = !allMeals.isEmpty();
        noMealText.setVisibility(hasMeals ? View.GONE : View.VISIBLE);
        todaysMealRecyclerView.setVisibility(hasMeals ? View.VISIBLE : View.GONE);

        if (hasMeals) {
            mealAdapter = new MealAdapter(requireContext(), allMeals, false, this);
            todaysMealRecyclerView.setAdapter(mealAdapter);
        } else {
            todaysMealRecyclerView.setAdapter(null);
        }
    }



    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request Permissions
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with getting location

            } else {
                // Permission denied, show a message to the user
                showSnackbar("Location permission denied. Please enable it in settings to get location.");

            }
        }
    }



    @Override
    public void onMealRemove(Meal meal, int position) {
        // Implement meal removal logic if needed
    }

    public interface OnPhotoUrlReceivedListener {
        void onPhotoUrlReceived(String imageUrl);
    }

    private void showSnackbar(String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

}