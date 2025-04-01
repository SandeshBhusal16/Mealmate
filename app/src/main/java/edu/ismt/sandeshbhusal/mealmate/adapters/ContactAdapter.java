package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.R;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private final Context context;
    private List<Pair<String, String>> contactList;
    private final List<Pair<String, String>> selectedContacts = new ArrayList<>();
    private final OnContactSelectedListener onContactSelectedListener;

    // Interface for Contact Selection Listener
    public interface OnContactSelectedListener {
        void onContactSelected(List<Pair<String, String>> selectedContacts);
    }

    public ContactAdapter(Context context, List<Pair<String, String>> contactList,
                          OnContactSelectedListener onContactSelectedListener) {
        this.context = context;
        this.contactList = contactList;
        this.onContactSelectedListener = onContactSelectedListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Pair<String, String> contact = contactList.get(position);

        holder.textViewName.setText(contact.first);
        holder.textViewNumber.setText(contact.second);

        // Check if the contact is already selected
        holder.checkBoxSelect.setOnCheckedChangeListener(null);  // Reset listener
        holder.checkBoxSelect.setChecked(selectedContacts.contains(contact));

        holder.checkBoxSelect.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                if (!selectedContacts.contains(contact)) {
                    selectedContacts.add(contact);
                }
            } else {
                selectedContacts.remove(contact);
            }
            onContactSelectedListener.onContactSelected(selectedContacts);
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    // Update List for Filtering
    public void updateList(List<Pair<String, String>> filteredList) {
        contactList = filteredList;
        notifyDataSetChanged();
    }

    // Update Selected Contacts
    public void updateSelectedContacts(List<Pair<String, String>> updatedSelectedContacts) {
        selectedContacts.clear();
        selectedContacts.addAll(updatedSelectedContacts);
        notifyDataSetChanged();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewNumber;
        MaterialCheckBox checkBoxSelect;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textview_contact_name);
            textViewNumber = itemView.findViewById(R.id.textview_contact_number);
            checkBoxSelect = itemView.findViewById(R.id.checkbox_select_contact);
        }
    }
}
