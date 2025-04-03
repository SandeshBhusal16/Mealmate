package edu.ismt.sandeshbhusal.mealmate.grok;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.utils.GroceryDatabaseHelper;

public class GrokDelegateActivity extends AppCompatActivity {
    private GroceryDatabaseHelper dbHelper;
    private RecyclerView mealRecyclerView, ingredientRecyclerView;
    private ChipGroup contactChipGroup;
    private GrokMealAdapter mealAdapter;
    private GrokIngredientAdapter ingredientAdapter;
    private List<String> selectedCategories = new ArrayList<>();
    private List<GrokIngredient> allIngredients = new ArrayList<>(); // All available ingredients
    private List<GrokIngredient> selectedIngredients = new ArrayList<>(); // Ingredients with prices set
    private List<Contact> selectedContacts = new ArrayList<>();
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grok_delegate);

        dbHelper = new GroceryDatabaseHelper(this);
        mealRecyclerView = findViewById(R.id.mealRecyclerView);
        ingredientRecyclerView = findViewById(R.id.ingredientRecyclerView);
        contactChipGroup = findViewById(R.id.contactChipGroup);

        mealRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ingredientRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.selectContactsButton).setOnClickListener(v -> showContactDialog());
        findViewById(R.id.setLocationButton).setOnClickListener(v -> showLocationDialog());
        findViewById(R.id.sendRequestButton).setOnClickListener(v -> sendSMS());

        loadMeals();
    }

    private void loadMeals() {
        Map<String, Map<String, List<String>>> weeklyItems = dbHelper.getGroceryItemsForWeek();
        List<String> categories = new ArrayList<>(weeklyItems.keySet());
        mealAdapter = new GrokMealAdapter(categories, this::updateIngredients);
        mealRecyclerView.setAdapter(mealAdapter);

        // Preload all ingredients for reference
        allIngredients.clear();
        for (String category : categories) {
            Map<String, List<String>> dateMap = weeklyItems.get(category);
            if (dateMap != null) {
                for (String date : dateMap.keySet()) {
                    for (String itemName : dateMap.get(date)) {
                        boolean isPurchased = dbHelper.isItemPurchased(itemName, date);
                        allIngredients.add(new GrokIngredient(itemName, date, category, isPurchased, 0.0f));
                    }
                }
            }
        }
    }

    private void updateIngredients() {
        selectedCategories.clear();
        for (int i = 0; i < mealAdapter.categories.size(); i++) {
            if (mealAdapter.selectedCategories.contains(mealAdapter.categories.get(i))) {
                selectedCategories.add(mealAdapter.categories.get(i));
            }
        }

        List<GrokIngredient> ingredients = new ArrayList<>();
        for (GrokIngredient ingredient : allIngredients) {
            if (selectedCategories.contains(ingredient.category)) {
                float price = 0.0f;
                for (GrokIngredient selected : selectedIngredients) {
                    if (selected.name.equals(ingredient.name) && selected.date.equals(ingredient.date)) {
                        price = selected.price;
                        break;
                    }
                }
                ingredients.add(new GrokIngredient(ingredient.name, ingredient.date, ingredient.category, ingredient.isPurchased, price));
            }
        }
        ingredientAdapter = new GrokIngredientAdapter(ingredients, selectedIngredients); // Pass selectedIngredients
        ingredientRecyclerView.setAdapter(ingredientAdapter);
    }

    private void showContactDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.grok_dialog_contacts, null);
        RecyclerView contactRecyclerView = view.findViewById(R.id.contactRecyclerView);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Contact> contacts = fetchContacts();
        GrokContactAdapter adapter = new GrokContactAdapter(contacts);
        contactRecyclerView.setAdapter(adapter);
        view.findViewById(R.id.doneButton).setOnClickListener(v -> {
            selectedContacts.clear();
            selectedContacts.addAll(adapter.getSelectedContacts());
            updateContactChips();
            dialog.dismiss();
        });
        dialog.setContentView(view);
        dialog.show();
    }

    private List<Contact> fetchContacts() {
        List<Contact> contacts = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            // Query for phone numbers
            Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);
            if (phoneCursor != null) {
                while (phoneCursor.moveToNext()) {
                    String name = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String number = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String email = null;

                    // Query for email
                    Cursor emailCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                            new String[]{phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))},
                            null);

                    if (emailCursor != null && emailCursor.moveToFirst()) {
                        email = emailCursor.getString(emailCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS));
                        emailCursor.close();
                    }

                    contacts.add(new Contact(name, number, email));
                }
                phoneCursor.close();
            }
        }
        return contacts;
    }

    private void updateContactChips() {
        contactChipGroup.removeAllViews();
        for (Contact contact : selectedContacts) {
            Chip chip = new Chip(this);
            chip.setText(contact.name + "\n" + contact.number);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                selectedContacts.remove(contact);
                contactChipGroup.removeView(chip);
            });
            // Correct method and style
            chip.setChipBackgroundColorResource(com.google.android.material.R.color.m3_chip_background_color);
            chip.setMaxLines(2);
            contactChipGroup.addView(chip);
        }
    }

    private void showLocationDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.grok_dialog_location, null);
        TextInputEditText storeEditText = view.findViewById(R.id.storeEditText);
        TextInputEditText addressEditText = view.findViewById(R.id.addressEditText);
        builder.setView(view)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String store = storeEditText.getText().toString();
                    String address = addressEditText.getText().toString();
                    if (!store.isEmpty() && !address.isEmpty()) {
                        location = store + ", " + address;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendSMS() {
        if (selectedIngredients.isEmpty() || selectedContacts.isEmpty() || location == null) {
            Toast.makeText(this, "Please complete all selections", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder message = new StringBuilder("Will you shop for me? Here's the list: [");
        float totalPrice = 0;
        for (GrokIngredient ingredient : selectedIngredients) {
            message.append(ingredient.name).append(" (").append(ingredient.category).append("): $")
                    .append(String.format("%.2f", ingredient.price)).append(", ");
            totalPrice += ingredient.price;
        }
        message.setLength(message.length() - 2); // Remove last ", "
        message.append("], Total: NPR").append(String.format("%.2f", totalPrice)).append(", Place: ").append(location);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            for (Contact contact : selectedContacts) {
                smsManager.sendTextMessage(contact.number, null, message.toString(), null, null);
            }
            Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }
    }
}