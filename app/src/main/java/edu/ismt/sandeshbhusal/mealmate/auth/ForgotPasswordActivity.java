package edu.ismt.sandeshbhusal.mealmate.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;

import edu.ismt.sandeshbhusal.mealmate.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText etEmail;
    private MaterialButton btnResetPassword;
    private MaterialTextView tvBackToSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToSignIn = findViewById(R.id.tvBackToSignIn);

        // Click listener for Reset Password Button
        btnResetPassword.setOnClickListener(v -> resetPassword(v));

        // Click listener for "Back to Sign In" text
        tvBackToSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void resetPassword(View view) {
        String email = etEmail.getText().toString().trim();

        // Validate email input
        if (TextUtils.isEmpty(email)) {
            showSnackbar(view, "Please enter your email");
            return;
        }

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Show success message & redirect to login screen
                        showSnackbar(view, "Password reset email sent! Check your inbox.");
                        view.postDelayed(() -> {
                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }, 3000); // 3-second delay before redirect
                    } else {
                        // Show error message
                        showSnackbar(view, "Failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    private void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
}
