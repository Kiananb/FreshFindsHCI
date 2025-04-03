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



        filterButton = findViewById(R.id.filterButton);
        priceButton = findViewById(R.id.priceButton);
        shopAislesTextView = findViewById(R.id.shopAislesTextView);
        favouritesListTextView = findViewById(R.id.favouritesListTextView);
        pastPurchasesTextView = findViewById(R.id.pastPurchasesTextView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (filterButton == null || priceButton == null || shopAislesTextView == null ||
                favouritesListTextView == null || pastPurchasesTextView == null || bottomNavigationView == null) {

            Toast.makeText(this, "UI setup failed", Toast.LENGTH_LONG).show();
            return;
        }


        // Filter button
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Filter clicked (placeholder)", Toast.LENGTH_SHORT).show();

            }
        });

        // Price button
        priceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Price sort clicked (placeholder)", Toast.LENGTH_SHORT).show();

            }
        });

        // Shop Aisles
        shopAislesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Shop Aisles clicked (placeholder)", Toast.LENGTH_SHORT).show();

            }
        });

        // Favourites List
        favouritesListTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Favourites List clicked (placeholder)", Toast.LENGTH_SHORT).show();

            }
        });

        // Past Purchases
        pastPurchasesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Past Purchases clicked (placeholder)", Toast.LENGTH_SHORT).show();

            }
        });

        // Set up the Bottom Navigation Bar to respond to user clicks
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {

                int itemId = item.getItemId();


                if (itemId == R.id.home) {

                    // Return true to tell the BottomNavigationView to highlight this item
                    return true;
                } else if (itemId == R.id.profile) {
                    // Profile icon was tapped
                    Toast.makeText(HomePage.this, "Profile tapped (placeholder)", Toast.LENGTH_SHORT).show();

                    return true;
                } else if (itemId == R.id.cart) {
                    // Cart icon was tapped
                    Toast.makeText(HomePage.this, "Cart tapped (placeholder)", Toast.LENGTH_SHORT).show();

                    return true;
                } else if (itemId == R.id.notifications) {
                    // Notifications icon was tapped
                    Toast.makeText(HomePage.this, "Notifications tapped (placeholder)", Toast.LENGTH_SHORT).show();

                    return true;
                } else {
                    // If an unknown item is tapped, return false (no action taken)
                    return false;
                }
            }
        });

        // Set the Home item as the default selected item when the page loads

        bottomNavigationView.setSelectedItemId(R.id.home);

    }
}