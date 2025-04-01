package edu.ismt.sandeshbhusal.mealmate.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.adapters.StoreAdapter;
import edu.ismt.sandeshbhusal.mealmate.adapters.StoreSelectionAdapter;
import edu.ismt.sandeshbhusal.mealmate.model.SavedLocation;

public class StoreSelectionFragment extends Fragment {

    private RecyclerView recyclerViewStores;
    private RadioGroup radioGroupStoreMode;
    private View layoutManualEntry;
    private View layoutStoreList;

    private StoreSelectionAdapter storeAdapter;
    private List<SavedLocation> storeList = new ArrayList<>();
    private TextInputEditText editTextSearchStores;
    private ViewSwitcher viewSwitcher;

    private ArrayList<String> selectedIngredients;


    private FusedLocationProviderClient fusedLocationClient;
    private View view;
    private List<SavedLocation> masterStoreList = new ArrayList<>();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    public static StoreSelectionFragment newInstance(ArrayList<String> selectedIngredients) {
        StoreSelectionFragment fragment = new StoreSelectionFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("selectedIngredients", selectedIngredients);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


            selectedIngredients = getArguments() != null ?
                    getArguments().getStringArrayList("selectedIngredients") :
                    new ArrayList<>();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.fragment_store_selection, container, false);

        // Initialize Views
        recyclerViewStores = view.findViewById(R.id.recyclerview_stores);
        editTextSearchStores = view.findViewById(R.id.edittext_search_stores);

        radioGroupStoreMode = view.findViewById(R.id.radiogroup_store_mode);
        viewSwitcher = view.findViewById(R.id.view_switcher);

        // Setup RecyclerView
        recyclerViewStores.setLayoutManager(new LinearLayoutManager(requireContext()));
        storeAdapter = new StoreSelectionAdapter(requireContext(), storeList);
        recyclerViewStores.setAdapter(storeAdapter);

        // Load Stores from Firebase

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Store Mode Selection Logic
        radioGroupStoreMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_manual_entry) {
                viewSwitcher.setDisplayedChild(0); // Show Manual Entry
            } else if (checkedId == R.id.radio_select_from_server) {
                viewSwitcher.setDisplayedChild(1); // Show Store List
            }
        });
        // Search Functionality
        editTextSearchStores.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStores(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    // Reset to full list when search text is cleared
                    storeAdapter.updateList(storeList);
                }

            }
        });

        loadFavStoreDataFromFirebase();

        return view;
    }



    private void loadFavStoreDataFromFirebase() {
        storeList.clear();
        //fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double currentLat = location.getLatitude();
                    double currentLng = location.getLongitude();


                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("favstore")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                storeList.clear();
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    String storeName = document.getString("storeName");
                                    String address = document.getString("address");
                                    String latLong = document.getString("latLong");
                                    List<String> ingredients = (List<String>) document.get("ingredients");

                                    // Extract Latitude and Longitude from latLong string
                                    String[] latLngParts = latLong.replace("Lat:", "").replace("Long:", "").split(",");
                                    double storeLat = Double.parseDouble(latLngParts[0].trim());
                                    double storeLng = Double.parseDouble(latLngParts[1].trim());
                                    String distance = calculateDistance(currentLat, currentLng, storeLat, storeLng);


                                    // Calculate Matched Ingredients
                                    List<String> matchedIngredients = new ArrayList<>();
                                    if (selectedIngredients != null) {
                                        for (String ingredient : ingredients) {
                                            for (String selectedItem : selectedIngredients) {
                                                if (ingredient.equalsIgnoreCase(selectedItem.trim())) {
                                                    matchedIngredients.add(ingredient);
                                                }
                                            }
                                        }
                                    }

                                    SavedLocation savedLocation = new SavedLocation(
                                            storeName,
                                            "https://example.com/default_image.jpg",  // Use a default image or fetch it
                                            address,
                                            storeLat,
                                            storeLng,
                                            distance,  // Distance calculation can be improved later
                                            ingredients,
                                            matchedIngredients.size()  // Matching Count
                                    );

                                    savedLocation.setMatchedIngredients(matchedIngredients);

                                    storeList.add(savedLocation);
                                    masterStoreList.add(savedLocation);  // Also add to master list

                                }
                                storeAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                showSnackbar("Failed to load data");
                              //  Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                                Log.e("StoreSelectionFragment", "Error loading data", e);
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


    private void filterStores(String query) {
        if (query.isEmpty()) {
            // If search query is empty, show full master list
            storeAdapter.updateList(masterStoreList);
        } else {
            // Filter the list based on the query
            List<SavedLocation> filteredList = new ArrayList<>();
            for (SavedLocation location : masterStoreList) {
                if (location.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(location);
                }
            }
            storeAdapter.updateList(filteredList);
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


    private void showSnackbar(String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

}
