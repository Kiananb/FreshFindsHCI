package com.example.freshfinds;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomePage extends BaseActivity {

    private static final String TAG = "HomePage";
    private TextView shopAislesTextView, favouritesListTextView, pastPurchasesTextView;
    private BottomNavigationView bottomNavigationView;
    private EditText searchEditText;
    private ImageButton searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // This calls checkAuthentication() in BaseActivity
        setContentView(R.layout.activity_home_page);

        try {
            // Initialize components
            searchEditText = findViewById(R.id.searchEditText);
            searchButton = findViewById(R.id.searchButton);
            shopAislesTextView = findViewById(R.id.shopAislesTextView);
            favouritesListTextView = findViewById(R.id.favouritesListTextView);
            pastPurchasesTextView = findViewById(R.id.pastPurchasesTextView);
            bottomNavigationView = findViewById(R.id.bottomNavigationView);

            // Verify UI components are properly initialized
            if (shopAislesTextView == null || favouritesListTextView == null || 
                pastPurchasesTextView == null || bottomNavigationView == null || 
                searchButton == null || searchEditText == null) {
                Toast.makeText(this, "UI setup failed", Toast.LENGTH_LONG).show();
                return;
            }

            // Search button click listener
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performSearch();
                }
            });
            
            // Also trigger search when user presses Enter on keyboard
            searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        performSearch();
                        return true;
                    }
                    return false;
                }
            });

            // Shop Aisles
            shopAislesTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(HomePage.this, AislesActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Shop Aisles error: " + e.getMessage());
                        Toast.makeText(HomePage.this, "Could not open Shop Aisles", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Favourites List
            favouritesListTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(HomePage.this, FavoritesActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Favorites error: " + e.getMessage());
                        Toast.makeText(HomePage.this, "Could not open Favorites", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Past Orders (renamed from Past Purchases)
            pastPurchasesTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(HomePage.this, PastPurchasesActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Past Orders error: " + e.getMessage());
                        Toast.makeText(HomePage.this, "Could not open Past Orders", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Set up the Bottom Navigation Bar
            setupBottomNavigation();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "An error occurred initializing the app", Toast.LENGTH_LONG).show();
        }
    }
    
    private void performSearch() {
        try {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                Intent intent = new Intent(HomePage.this, SearchResultsActivity.class);
                intent.putExtra("search_query", query);
                startActivity(intent);
            } else {
                Toast.makeText(HomePage.this, "Please enter a search term", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Search error: " + e.getMessage());
            Toast.makeText(HomePage.this, "Search failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        // Set the Home item as the default selected
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();
                try {
                    if (itemId == R.id.home) {
                        // Already on home screen
                        return true;
                    } else if (itemId == R.id.cart) {
                        // Navigate to Cart Activity
                        Intent intent = new Intent(HomePage.this, CartActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0); // Disable animation
                        return true;
                    } else if (itemId == R.id.messages) {
                        // Navigate to Messages Activity
                        Intent intent = new Intent(HomePage.this, MessagesActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0); // Disable animation
                        return true;
                    } else if (itemId == R.id.profile) {
                        Intent profileIntent = new Intent(HomePage.this, ProfileActivity.class);
                        
                        // Forward the email if it was passed to HomePage
                        String email = getIntent().getStringExtra("user_email");
                        if (email != null && !email.isEmpty()) {
                            profileIntent.putExtra("user_email", email);
                        }
                        startActivity(profileIntent);
                        overridePendingTransition(0, 0);
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error: " + e.getMessage());
                    Toast.makeText(HomePage.this, "Navigation failed", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
}