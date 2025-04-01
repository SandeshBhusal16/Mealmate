package edu.ismt.mealmate;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.ismt.sandeshbhusal.mealmate.utils.GroceryDatabaseHelper;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GroceryDatabaseHelperTest {

    private GroceryDatabaseHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = new GroceryDatabaseHelper(context);
        dbHelper.clearGroceryData(); // Clear DB before each test
    }

    @After
    public void tearDown() {
        dbHelper.clearGroceryData(); // Clean up after each test
    }

    @Test
    public void testAddAndCheckGroceryItem() {
        String itemName = "Tomato";
        String date = dbHelper.getTodayDate();

        // Add item
        dbHelper.addGroceryItem(itemName, date);

        // Check if item exists
        boolean exists = dbHelper.isItemExistsForDate(itemName, date);
        assertTrue(exists);

        // Remove item
        dbHelper.removeGroceryItem(itemName, date);

        // Check again
        boolean existsAfterRemove = dbHelper.isItemExistsForDate(itemName, date);
        assertFalse(existsAfterRemove);
    }

    @Test
    public void testUpdateAndCheckPurchasedStatus() {
        String itemName = "Milk";
        String date = dbHelper.getTodayDate();

        dbHelper.addGroceryItem(itemName, date);

        // Initially should be not purchased
        boolean isPurchasedInitial = dbHelper.isItemPurchased(itemName, date);
        assertFalse(isPurchasedInitial);

        // Update to purchased
        dbHelper.updateItemPurchasedStatus(itemName, date, true);

        // Check again
        boolean isPurchasedNow = dbHelper.isItemPurchased(itemName, date);
        assertTrue(isPurchasedNow);
    }

    @Test
    public void testWeeklyGroceryCount() {
        String date = dbHelper.getTodayDate();
        dbHelper.addGroceryItem("Bread", date);
        dbHelper.addGroceryItem("Eggs", date);

        int count = dbHelper.getWeeklyGroceryItemCount();
        assertEquals(2, count);
    }
}
