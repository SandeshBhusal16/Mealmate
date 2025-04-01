package edu.ismt.sandeshbhusal.mealmate;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import edu.ismt.sandeshbhusal.mealmate.model.SavedLocation;

public class MapExplorerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<SavedLocation> storeList;
    private FloatingActionButton fabMyLocation;
    private CardView markerInfoCard;
    private TextView infoTitle, infoAddress, infoMatches;
    private ImageView infoImage;
    private GridLayout infoIngredientsGrid;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_explorer);

        // Retrieve the list of stores passed from HomeFragment
        storeList = getIntent().getParcelableArrayListExtra("storeList");

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up the "My Location" FloatingActionButton
        fabMyLocation = findViewById(R.id.fab_my_location);
        fabMyLocation.setOnClickListener(v -> centerMapOnUserLocation());

        // Initialize the marker info card views
        markerInfoCard = findViewById(R.id.marker_info_card);
        infoTitle = findViewById(R.id.info_title);
        infoAddress = findViewById(R.id.info_address);
        infoImage = findViewById(R.id.info_image);
        infoIngredientsGrid = findViewById(R.id.info_ingredients_grid);
        infoMatches = findViewById(R.id.matchMsg);

        // Hide the info card initially
        markerInfoCard.setVisibility(View.GONE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable My Location layer if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            centerMapOnUserLocation(); // Center map on user location when ready
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Store all marker positions for drawing the polygon
        List<LatLng> markerPositions = new ArrayList<>();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        // Add store markers
        if (storeList != null && !storeList.isEmpty()) {
            for (SavedLocation store : storeList) {
                LatLng position = new LatLng(store.getLatitude(), store.getLongitude());
                String title = store.getName();
                String snippet = store.getDistance() + " away";

                Marker marker = mMap.addMarker(new MarkerOptions().position(position).title(title).snippet(snippet));
                marker.setTag(store.getImageUrl()); // Set image URL as tag

                // Store marker position for polygon
                markerPositions.add(position);
                boundsBuilder.include(position);
            }
        }

        // Get and show current location with custom marker
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Marker youAreHereMarker = mMap.addMarker(new MarkerOptions()
                        .position(currentLocation)
                        .title("You are here")
                        .icon(resizeMarkerIcon(R.drawable.ic_current_location, 100, 100)));

                // Include current location in the polygon
                markerPositions.add(currentLocation);
                boundsBuilder.include(currentLocation);
                drawNavigationLines(currentLocation);


                // Adjust camera to fit all markers
                LatLngBounds bounds = boundsBuilder.build();
                int padding = 100;
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                // Draw the polygon connecting all markers
               // drawPolygonBetweenMarkers(markerPositions, currentLocation);

            }
        });

        // Show info window on marker click
        mMap.setOnMarkerClickListener(marker -> {
            if ("You are here".equals(marker.getTitle())) {
                return true; // Skip showing info card for the current location marker
            }

            markerInfoCard.setVisibility(View.VISIBLE);
            infoTitle.setText(marker.getTitle());


            // Load store image using Glide
            String imageUrl = (String) marker.getTag();
            if (imageUrl != null) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.saved_store)
                        .into(infoImage);
            } else {
                infoImage.setImageResource(R.drawable.saved_store);
            }

            for (SavedLocation store : storeList) {
                if (store.getName().equals(marker.getTitle())) {
                    displayIngredientsInGrid(store);
                    int matchedCount = store.getMatchingCount();
                    if(matchedCount==0){
                        infoMatches.setText("There are no items on the grocery list that match");

                    }
                    else if(matchedCount==1){
                        infoMatches.setText(matchedCount + " item match with Grocery List");

                    }
                    else {
                        infoMatches.setText(matchedCount + " items match with Grocery List");

                    }
                    infoAddress.setText(store.getAddress());
                    break;
                }
            }
            return false;
        });

        // Hide the info card when clicking on the map background
        mMap.setOnMapClickListener(latLng -> markerInfoCard.setVisibility(View.GONE));
    }

    private void drawNavigationLines(LatLng currentLocation) {
        if (storeList == null || storeList.isEmpty()) return;

        for (SavedLocation store : storeList) {
            LatLng storePosition = new LatLng(store.getLatitude(), store.getLongitude());

            // Draw polyline from current location to each store
            mMap.addPolyline(new PolylineOptions()
                    .add(currentLocation, storePosition)
                    .width(8)
                    .color(ContextCompat.getColor(this, R.color.primary))
                    .geodesic(true)); // Makes the line follow the Earth's curvature
        }
    }



    private BitmapDescriptor resizeMarkerIcon(int drawableResId, int width, int height) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableResId);
        if (drawable == null) return null;

        // Tint the drawable with the primary color
        drawable.setTint(ContextCompat.getColor(this, R.color.primary));

        // Create a bitmap with the specified width and height
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private void displayIngredientsInGrid(SavedLocation store) {
        infoIngredientsGrid.removeAllViews(); // Clear existing views

        // Get ingredients and matched list from the store object
        List<String> ingredients = store.getAvailableIngredients();
        List<String> matchedIngredients = store.getMatchedIngredients(); // Get matched list

        for (String ingredient : ingredients) {
            TextView chip = new TextView(this);
            chip.setText(ingredient);
            chip.setPadding(24, 12, 24, 12);
            chip.setTextSize(12);

            // Check if the ingredient is in the matched list
            if (matchedIngredients != null && matchedIngredients.contains(ingredient)) {
                chip.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_avilable)); // Matched -> Green
                chip.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                chip.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_background));
                chip.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
            }

            // Set LayoutParams for each chip
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(8, 8, 8, 8);
            params.width = GridLayout.LayoutParams.WRAP_CONTENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Allow columns to expand
            chip.setLayoutParams(params);

            // Add chip to GridLayout
            infoIngredientsGrid.addView(chip);
        }

        // Force the GridLayout to update its layout
        infoIngredientsGrid.requestLayout();
    }
    private void centerMapOnUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null && mMap != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
}
