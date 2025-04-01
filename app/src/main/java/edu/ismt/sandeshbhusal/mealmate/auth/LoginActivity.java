package edu.ismt.sandeshbhusal.mealmate.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.ismt.sandeshbhusal.mealmate.CustomProgressDialog;
import edu.ismt.sandeshbhusal.mealmate.home.DashboardActivity;
import edu.ismt.sandeshbhusal.mealmate.home.ProfileActivity;
import edu.ismt.sandeshbhusal.mealmate.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnSignIn;
    private MaterialTextView forgotPassword;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth & Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        forgotPassword = findViewById(R.id.tvForgotPassword);

        // Sign In Button Click
        btnSignIn.setOnClickListener(v -> loginUser(v));
        forgotPassword.setOnClickListener(v->redirectToForgetPassword(v));
    }


    private void redirectToForgetPassword(View view){
        goToActivity(ForgotPasswordActivity.class);
    }

    private void loginUser(View view) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate Fields
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            showSnackbar(view, "Please enter your email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter your password");
            showSnackbar(view, "Please enter your password");
            return;
        }

        // Sign In with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Get User ID
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            saveUserId(userId); // Save USER_ID to SharedPreferences
                            fetchUserData(view, userId); // Fetch user profile data from Firestore
                        }
                    } else {
                        // Show error message
                        showSnackbar(view, "Login Failed: " + task.getException().getMessage());
                    }
                });
    }

    private void fetchUserData(View view, String userId) {
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Extract user details
                        String name = documentSnapshot.getString("name");
                        String mobile = documentSnapshot.getString("mobile");
                        String dob = documentSnapshot.getString("dob");
                        String gender = documentSnapshot.getString("gender");
                        String photoUrl = documentSnapshot.getString("photoUrl");

                        // Save user details in SharedPreferences
                        saveUserDetails(name, mobile, dob, gender, photoUrl);

                        // Redirect to Dashboard if all details exist
                        if (name != null && mobile != null && dob != null && gender != null && photoUrl != null) {
                            goToActivity(DashboardActivity.class);
                        } else {
                            // Redirect to ProfileActivity if any detail is missing
                            goToActivity(ProfileActivity.class);
                        }
                    } else {
                        // If no user data found, go to ProfileActivity
                        showSnackbar(view, "Profile data not found. Please complete your profile.");
                        goToActivity(ProfileActivity.class);
                    }
                })
                .addOnFailureListener(e -> showSnackbar(view, "Error fetching data: " + e.getMessage()));
    }

    private void saveUserId(String userId) {
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("USER_ID", userId);
        editor.apply();
    }

    private void saveUserDetails(String name, String mobile, String dob, String gender, String photoUrl) {
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("USER_NAME", name);
        editor.putString("USER_MOBILE", mobile);
        editor.putString("USER_DOB", dob);
        editor.putString("USER_GENDER", gender);
        editor.putString("USER_PHOTO", photoUrl);

        editor.apply();
    }

    private void goToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(LoginActivity.this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
}
