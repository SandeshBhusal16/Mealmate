package edu.ismt.sandeshbhusal.mealmate.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ismt.sandeshbhusal.mealmate.CustomProgressDialog;
import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.adapters.CategoryExpandableListAdapter;
import edu.ismt.sandeshbhusal.mealmate.adapters.InstructionAdapter;
import edu.ismt.sandeshbhusal.mealmate.model.InstructionStep;
import edu.ismt.sandeshbhusal.mealmate.utils.CustomExpandableListView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class AddRecipeFragment extends Fragment {

    private RecyclerView recyclerView;
    private InstructionAdapter instructionAdapter;
    private List<InstructionStep> instructionList;
    private Button addInstructionButton;
    private CustomExpandableListView expandableListView; // Use CustomExpandableListView
    private CategoryExpandableListAdapter expandableListAdapter;
    private List<String> categoryList;
    private HashMap<String, List<String>> ingredientMap;
    private List<String> selectedIngredients = new ArrayList<>();

    private EditText newIngredientInput;
    private Button addIngredientButton;
    private final String othersCategory = "🆕 Others";

    private EditText recipeNameInput, cookTimeInput;

    private Uri cameraImageUri;
    private Uri selectedImageUri = null;

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final String IMGUR_CLIENT_ID = "786326e0fb8b35b"; // Replace with your Client ID
    private static final String IMGUR_UPLOAD_URL = "https://api.imgur.com/3/image";

    ImageView recipeImage;

    private View rootView;
    private CustomProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         rootView = inflater.inflate(R.layout.fragment_add_recipe, container, false);

        // Initialize UI components
        recyclerView = rootView.findViewById(R.id.instructionRecyclerView);
        addInstructionButton = rootView.findViewById(R.id.addInstructionButton);
        expandableListView = rootView.findViewById(R.id.expandableListView); // Use CustomExpandableListView
        newIngredientInput = rootView.findViewById(R.id.newIngredientInput);
        addIngredientButton = rootView.findViewById(R.id.addIngredientButton);
        recipeNameInput = rootView.findViewById(R.id.recipeName);
        cookTimeInput = rootView.findViewById(R.id.cookTime);
        recipeImage = rootView.findViewById(R.id.recipeImage);
        Button saveRecipeButton = rootView.findViewById(R.id.saveRecipeButton);
        Button uploadImageButton = rootView.findViewById(R.id.uploadImageButton);
        progressDialog = new CustomProgressDialog(getActivity()); // Initialize Progress Dialog


        // Setup Expandable ListView for ingredients
        setupExpandableListView();

        // Setup RecyclerView for instructions
        setupRecyclerView();

        // Handle Add Instruction button click
        addInstructionButton.setOnClickListener(v -> addInstruction());

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
                expandableListView.expandGroup(categoryList.indexOf(othersCategory));

                // Clear input field for next entry
                newIngredientInput.setText("");
            } else {
                showSnackbar("Please enter an ingredient!");
            }
        });

        uploadImageButton.setOnClickListener(v -> showImagePickerDialog());

        saveRecipeButton.setOnClickListener(v -> saveRecipe());

        return rootView;
    }

    private void showImagePickerDialog() {
        Intent pickGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickGalleryIntent.setType("image/*");

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraImageUri = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

        Intent chooser = Intent.createChooser(pickGalleryIntent, "Select or Capture Image");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});
        imagePickerLauncher.launch(chooser);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getData() != null) { // Picked from Gallery
                        selectedImageUri = data.getData();
                    } else { // Captured from Camera
                        selectedImageUri = cameraImageUri;
                    }

                    if (selectedImageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                            recipeImage.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error loading image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });






    private void saveRecipe() {
        // Get values from input fields
        String recipeName = recipeNameInput.getText().toString().trim();
        String cookTime = cookTimeInput.getText().toString().trim();

        // 1️⃣ Check if Recipe Name is Empty
        if (recipeName.isEmpty()) {
            progressDialog.dismiss(); // Dismiss progress dialog
            showSnackbar("Please enter a recipe name!");
            return;
        }

        // 2️⃣ Check if Cook Time is Empty
        if (cookTime.isEmpty()) {
            progressDialog.dismiss(); // Dismiss progress dialog
            showSnackbar("Please enter cook time!");
            return;
        }

        // 3️⃣ Check if Any Ingredient is Selected
        if (selectedIngredients.isEmpty()) {
            progressDialog.dismiss(); // Dismiss progress dialog
            showSnackbar("Please select at least one ingredient!");
            return;
        }

        // 4️⃣ Categorize Selected Ingredients
        HashMap<String, List<String>> selectedCategoryMap = new HashMap<>();

        for (String category : categoryList) {
            List<String> selectedItems = new ArrayList<>();

            if (ingredientMap.containsKey(category)) {
                for (String ingredient : ingredientMap.get(category)) {
                    if (selectedIngredients.contains(ingredient)) {
                        // Trim whitespace and remove emojis from ingredient names
                        selectedItems.add(ingredient.replaceAll("[^a-zA-Z ]", "").trim());
                    }
                }
            }

            // Only add the category to the map if it has selected ingredients
            if (!selectedItems.isEmpty()) {
                selectedCategoryMap.put(category, selectedItems);
            }
        }

        // 5️⃣ Check if Any Instruction is Empty
        if (instructionList.isEmpty()) {
            progressDialog.dismiss(); // Dismiss progress dialog
            showSnackbar("Please add at least one instruction step!");
            return;
        }

        for (InstructionStep step : instructionList) {
            String stepDescription = step.getInstruction(); // Store value

            if (stepDescription == null || stepDescription.trim().isEmpty()) {
                progressDialog.dismiss(); // Dismiss progress dialog
                showSnackbar("Please fill all instruction steps!");
                return;
            }
        }

        // 6️⃣ Prepare data for sending to the server
        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("recipeName", recipeName);
        recipeData.put("cookTime", cookTime);
        recipeData.put("ingredients", selectedCategoryMap); // Include categorized ingredients
        recipeData.put("instructions", instructionList); // Include instruction steps

        // 7️⃣ Upload image if available, otherwise send recipe data directly
        if (selectedImageUri != null) {
            uploadImageToImgur(selectedImageUri, recipeData); // Pass recipeData to uploadImageToImgur
        } else {
            RecipeDataSend("", recipeData); // Pass empty string as photoUrl
        }
    }
    private void uploadImageToImgur(Uri imageUri, Map<String, Object> recipeData) {
        progressDialog.show();
        File file = getFileFromUri(imageUri);

        if (file == null || !file.exists()) {
            showSnackbar("Error: Unable to process image.");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.getName(),
                        RequestBody.create(MediaType.parse("image/*"), file))
                .build();

        Request request = new Request.Builder()
                .url(IMGUR_UPLOAD_URL)
                .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                .post(requestBody)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    String imgUrl = extractImageUrl(responseBody);
                    requireActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        RecipeDataSend(imgUrl, recipeData);}); // Pass trimmed gender
                } else {
                    requireActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        showSnackbar("Upload failed!");});
                }
            } catch (IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showSnackbar("Network error. Try again.");});
            }

        }).start();
    }


    private void resetScreen() {
        // Clear input fields
        recipeNameInput.setText("");
        cookTimeInput.setText("");
        newIngredientInput.setText("");

        // Clear selected image
        recipeImage.setImageResource(R.drawable.input_background); // Set a placeholder image
        selectedImageUri = null;

        // Clear selected ingredients
        selectedIngredients.clear();
        expandableListAdapter.notifyDataSetChanged();

        // Clear instructions
        instructionList.clear();
        instructionAdapter.notifyDataSetChanged();

        // Optionally, collapse all groups in the expandable list view
        for (int i = 0; i < expandableListAdapter.getGroupCount(); i++) {
            expandableListView.collapseGroup(i);
        }
    }
    void RecipeDataSend(String photoUrl, Map<String, Object> recipeData) {
        progressDialog.show();
        long timestamp = System.currentTimeMillis();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            recipeData.put("photoUrl", photoUrl); // Add the photo URL if available
        } else {
            recipeData.put("photoUrl", ""); // Save empty string if no photo is selected
        }
        recipeData.put("timestamp", timestamp);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("recipes")
                .document(String.valueOf(timestamp)) // Use the timestamp as the document ID
                .set(recipeData) // Set the recipe data
                .addOnSuccessListener(aVoid -> {
                    // Recipe saved successfully
                    progressDialog.dismiss(); // Dismiss progress dialog
                    showSnackbar("Recipe saved to Firebase!");
                    Log.d("Firestore", "Recipe saved with ID: " + timestamp);

                    // Reset the screen to its original state
                    resetScreen();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    progressDialog.dismiss(); // Dismiss progress dialog
                    showSnackbar("Failed to save recipe: " + e.getMessage());
                    Log.e("Firestore", "Error saving recipe", e);
                });
    }
    private String extractImageUrl(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject.getJSONObject("data").getString("link");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    private File getFileFromUri(Uri uri) {
        try {
            // Create a temporary file in the cache directory
            File tempFile = new File(requireContext().getCacheDir(), "upload_image.jpg");

            // Open an input stream from the URI
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);

            // Open an output stream to the temporary file
            OutputStream outputStream = new FileOutputStream(tempFile);

            // Copy the file contents
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Flush and close the streams
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Log.d("FILE_PATH", "Temp file created: " + tempFile.getAbsolutePath());
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Sets up the ExpandableListView for selecting ingredients.
     */
    private void setupExpandableListView() {
        categoryList = new ArrayList<>();
        ingredientMap = new HashMap<>();


        List<String> orderedCategories = List.of(
                "🥦 Vegetables", "🍎 Fruits", "🌾 Grains & Legumes", "🍗 Proteins",
                "🧀 Dairy", "🌿 Herbs & Spices", "🛢️ Oils & Condiments", "🆕 Others"
        );


        // Add categories
        categoryList.add("🥦 Vegetables");
        categoryList.add("🍎 Fruits");
        categoryList.add("🌾 Grains & Legumes");
        categoryList.add("🍗 Proteins");
        categoryList.add("🧀 Dairy");
        categoryList.add("🌿 Herbs & Spices");
        categoryList.add("🛢️ Oils & Condiments"); // ✅ NEW CATEGORY ADDED

        // Add ingredients under each category
        ingredientMap.put("🥦 Vegetables", List.of("🥕 Carrot", "🥦 Broccoli", "🌿 Spinach", "🍅 Tomato", "🧅 Onion", "🧄 Garlic", "🌶 Bell Pepper", "🥒 Zucchini", "🥬 Cabbage", "🥬 Kale", "🥗 Lettuce", "🥔 Cauliflower"));
        ingredientMap.put("🍎 Fruits", List.of("🍏 Apple", "🍌 Banana", "🍊 Orange", "🍓 Strawberries", "🍇 Grapes", "🥭 Mango", "🍍 Pineapple", "🍋 Lemon/Lime"));
        ingredientMap.put("🌾 Grains & Legumes", List.of("🍚 Rice", "🌾 Quinoa", "🥣 Oats", "🌰 Lentils", "🫘 Chickpeas", "🌽 Corn", "🥜 Peanuts"));
        ingredientMap.put("🍗 Proteins", List.of("🍗 Chicken", "🥩 Beef", "🐖 Pork", "🐟 Fish", "🍳 Eggs", "🌱 Tofu", "🫘 Beans"));
        ingredientMap.put("🧀 Dairy", List.of("🥛 Milk", "🍦 Yogurt", "🧀 Cheese", "🧈 Butter", "🥥 Coconut Milk", "🌱 Soy/Oat Milk"));
        ingredientMap.put("🌿 Herbs & Spices", List.of("🌿 Basil", "🌿 Oregano", "🌿 Thyme", "🌿 Rosemary", "🧂 Salt", "🌶 Chili Powder", "🟠 Turmeric", "🟡 Ginger", "🟤 Cumin"));

        // ✅ NEW: Oils & Condiments List
        ingredientMap.put("🛢️ Oils & Condiments", List.of("🫒 Olive Oil", "🥥 Coconut Oil", "🥫 Soy Sauce", "🔥 Hot Sauce", "🍯 Honey", "🥄 Mayonnaise", "🍶 Vinegar", "🧂 Salt", "🍚 Sugar"));

        expandableListAdapter = new CategoryExpandableListAdapter(getContext(), categoryList, ingredientMap, selectedIngredients);

        expandableListView.setAdapter(expandableListAdapter);

        // Handle ingredient selection
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String selectedIngredient = ingredientMap.get(categoryList.get(groupPosition)).get(childPosition);

            if (!selectedIngredients.contains(selectedIngredient)) {
                selectedIngredients.add(selectedIngredient);
                Toast.makeText(getContext(), selectedIngredient + " added!", Toast.LENGTH_SHORT).show();
            } else {
                selectedIngredients.remove(selectedIngredient);
                Toast.makeText(getContext(), selectedIngredient + " removed!", Toast.LENGTH_SHORT).show();
            }

            return true;
        });
    }

    /**
     * Sets up the RecyclerView for dynamic instructions.
     */
    private void setupRecyclerView() {
        instructionList = new ArrayList<>();
        instructionAdapter = new InstructionAdapter(instructionList, position -> removeInstruction(position));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(instructionAdapter);
    }

    /**
     * Adds a new instruction step dynamically.
     */
    private void addInstruction() {
        instructionList.add(new InstructionStep(instructionList.size() + 1, ""));
        instructionAdapter.notifyItemInserted(instructionList.size() - 1);
    }

    /**
     * Removes an instruction step dynamically.
     */
    private void removeInstruction(int position) {
        instructionList.remove(position);
        instructionAdapter.notifyDataSetChanged();
    }

    private void showSnackbar(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }
}