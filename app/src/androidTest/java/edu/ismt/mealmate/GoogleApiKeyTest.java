package edu.ismt.mealmate;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.ismt.sandeshbhusal.mealmate.utils.APIKey;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GoogleApiKeyTest {

    @Test
    public void testGoogleApiKeyValidity() {
        try {
            String address = "Kathmandu";
            String urlString = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                    address + "&key=" + APIKey.GOOGLE_API_KEY;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            Log.d("API_TEST", "Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Log.d("API_TEST", "Response Body: " + response.toString());

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());
            String status = jsonResponse.getString("status");

            Log.d("API_TEST", "API Status: " + status);

            // ✅ Pass test if status is OK
            assertEquals("OK", status);

        } catch (Exception e) {
            e.printStackTrace();
            fail("API call failed: " + e.getMessage());
        }
    }
}
