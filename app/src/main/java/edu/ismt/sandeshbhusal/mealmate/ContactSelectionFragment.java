package edu.ismt.sandeshbhusal.mealmate;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.adapters.ContactAdapter;

public class ContactSelectionFragment extends Fragment {

    private static final int REQUEST_CONTACTS_PERMISSION = 100;

    private RecyclerView recyclerViewContacts;
    private ContactAdapter contactAdapter;
    private List<Pair<String, String>> contactList = new ArrayList<>();
    private List<Pair<String, String>> selectedContacts = new ArrayList<>();
    private TextInputEditText editTextSearchContacts;
    private ChipGroup chipGroupSelectedContacts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_selection, container, false);

        // Initialize Views
        recyclerViewContacts = view.findViewById(R.id.recyclerview_contacts);
        editTextSearchContacts = view.findViewById(R.id.edittext_search_contacts);
        chipGroupSelectedContacts = view.findViewById(R.id.chipgroup_selected_contacts);

        // Setup RecyclerView
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
        contactAdapter = new ContactAdapter(requireContext(), contactList, this::updateSelectedContacts);
        recyclerViewContacts.setAdapter(contactAdapter);

        // Request Contacts Permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACTS_PERMISSION);
        } else {
            loadContacts();
        }

        // Search Functionality
        editTextSearchContacts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadContacts() {
        contactList.clear();
        ContentResolver contentResolver = requireContext().getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contactList.add(new Pair<>(name, number));
            }
            cursor.close();
        } else {
            Log.e("ContactSelection", "Cursor is null or no contacts found.");
        }

        contactAdapter.notifyDataSetChanged();
    }

    private void filterContacts(String query) {
        List<Pair<String, String>> filteredList = new ArrayList<>();
        for (Pair<String, String> contact : contactList) {
            if (contact.first.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(contact);
            }
        }
        contactAdapter.updateList(filteredList);
    }

    public void updateSelectedContacts(List<Pair<String, String>> selected) {
        selectedContacts = selected;
        chipGroupSelectedContacts.removeAllViews();

        for (Pair<String, String> contact : selectedContacts) {
            Chip chip = new Chip(requireContext());
            chip.setText(contact.first + "\n" + contact.second);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(view -> {
                selectedContacts.remove(contact);
                chipGroupSelectedContacts.removeView(chip);
                contactAdapter.updateSelectedContacts(selectedContacts);
            });
            chipGroupSelectedContacts.addView(chip);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACTS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(requireContext(), "Permission Denied. Cannot access contacts.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
