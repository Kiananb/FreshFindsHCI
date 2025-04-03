package com.example.freshfinds;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomePage extends AppCompatActivity {

    private static final String TAG = "HomePage";
    private Button filterButton, priceButton;
    private TextView shopAislesTextView, favouritesListTextView, pastPurchasesTextView;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Log.d(TAG, "onCreate started");

        // Initialize UI elements
        filterButton = findViewById(R.id.filterButton);
        priceButton = findViewById(R.id.priceButton);
        shopAislesTextView = findViewById(R.id.shopAislesTextView);
        favouritesListTextView = findViewById(R.id.favouritesListTextView);
        pastPurchasesTextView = findViewById(R.id.pastPurchasesTextView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (filterButton == null || priceButton == null || shopAislesTextView == null ||
                favouritesListTextView == null || pastPurchasesTextView == null || bottomNavigationView == null) {
            Log.e(TAG, "UI element not found");
            Toast.makeText(this, "UI setup failed", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "UI initialized");

        // Filter button click
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Filter clicked (placeholder)", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Filter button clicked");
            }
        });

        // Price button click
        priceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Price sort clicked (placeholder)", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Price button clicked");
            }
        });

        // Shop Aisles click
        shopAislesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Shop Aisles clicked (placeholder)", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Shop Aisles clicked");
            }
        });

        // Favourites List click
        favouritesListTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Favourites List clicked (placeholder)", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Favourites List clicked");
            }
        });

        // Past Purchases click
        pastPurchasesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Past Purchases clicked (placeholder)", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Past Purchases clicked");
            }
        });

        // Set up the Bottom Navigation Bar to respond to user clicks
        // Use BottomNavigationView.OnNavigationItemSelectedListener instead of NavigationBarView.OnItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                // Get the ID of the item that was tapped (e.g., R.id.home, R.id.profile)
                int itemId = item.getItemId();

                // Check which item was tapped using an if-else statement
                if (itemId == R.id.home) {
                    // Home icon was tapped
                    Log.d(TAG, "Home icon was tapped");
                    // Return true to tell the BottomNavigationView to highlight this item
                    return true;
                } else if (itemId == R.id.profile) {
                    // Profile icon was tapped
                    Toast.makeText(HomePage.this, "Profile tapped (placeholder)", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Profile icon was tapped");
                    return true;
                } else if (itemId == R.id.cart) {
                    // Cart icon was tapped
                    Toast.makeText(HomePage.this, "Cart tapped (placeholder)", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Cart icon was tapped");
                    return true;
                } else if (itemId == R.id.notifications) {
                    // Notifications icon was tapped
                    Toast.makeText(HomePage.this, "Notifications tapped (placeholder)", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Notifications icon was tapped");
                    return true;
                } else {
                    // If an unknown item is tapped, return false (no action taken)
                    return false;
                }
            }
        });

        // Set the "Home" item as the default selected item when the page loads
        // This highlights the Home icon to show the user they are on the Home page
        bottomNavigationView.setSelectedItemId(R.id.home);
        Log.d(TAG, "Set Home as the default selected item");
    }
}