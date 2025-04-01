package edu.ismt.sandeshbhusal.mealmate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import edu.ismt.sandeshbhusal.mealmate.home.DashboardActivity;
import edu.ismt.sandeshbhusal.mealmate.home.MainActivity;
import edu.ismt.sandeshbhusal.mealmate.home.ProfileActivity;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1000; // 3 seconds delay

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Handler to delay the transition
        new Handler().postDelayed(() -> {
            // Check if user is already logged in
            SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String userId = sharedPref.getString("USER_ID", null);


            if (userId != null) {
                // Redirect to Dashboard if logged in
                String name = sharedPref.getString("USER_NAME", null);
                String mobile = sharedPref.getString("USER_MOBILE", null);
                String dob = sharedPref.getString("USER_DOB", null);
                String gender = sharedPref.getString("USER_GENDER", null);
                String photo = sharedPref.getString("USER_PHOTO", null);

                Intent intent;
                if (name != null && mobile != null && dob != null && gender != null && photo != null) {
                    // All details exist, go to Dashboard
                    intent = new Intent(SplashScreen.this, DashboardActivity.class);
                } else {
                    // Some details missing, go to ProfileActivity
                    intent = new Intent(SplashScreen.this, ProfileActivity.class);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                //intent = new Intent(SplashScreen.this, DashboardActivity.class);
            } else {
                // Redirect to MainActivity (Login/Signup screen)
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);;
            }

            // Clear back stack so user cannot go back to SplashActivity
            finish();
        }, SPLASH_DELAY);
    }
}