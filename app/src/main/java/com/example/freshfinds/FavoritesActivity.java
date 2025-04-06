package com.example.freshfinds;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FavoritesActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private RecyclerView recyclerView;
    private TextView placeholderText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        
        ImageButton backButton = findViewById(R.id.buttonBack);
        placeholderText = findViewById(R.id.textViewPlaceholder);
        recyclerView = findViewById(R.id.recyclerViewFavorites);
        
        loadFavorites();
        
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set up bottom navigation
        setupBottomNavigation();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh favorites when returning to this screen
        loadFavorites();
    }
    
    private void loadFavorites() {
        // Get favorite product IDs
        Set<String> favoriteIds = Product.getFavoriteProductIds();
        
        // Get the actual products
        List<Product> favoriteProducts = new ArrayList<>();
        List<Product> allProducts = ProductDataProvider.getProductList();
        
        for (Product product : allProducts) {
            if (favoriteIds.contains(product.getId())) {
                favoriteProducts.add(product);
            }
        }
        
        // Update UI based on whether we have favorites
        if (favoriteProducts.isEmpty()) {
            placeholderText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            placeholderText.setText("You haven't added any favorites yet");
        } else {
            placeholderText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            
            // Setup RecyclerView with favorites
            ProductAdapter adapter = new ProductAdapter(this, favoriteProducts, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        // No items are selected by default in favorites
        
        // Set up navigation item selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.home) {
                startActivity(new Intent(this, HomePage.class));
                overridePendingTransition(0, 0); // Disable animation
                finish();
                return true;
            } else if (itemId == R.id.cart) {
                startActivity(new Intent(this, CartActivity.class));
                overridePendingTransition(0, 0); // Disable animation
                return true;
            } else if (itemId == R.id.messages) {
                startActivity(new Intent(this, MessagesActivity.class));
                overridePendingTransition(0, 0); // Disable animation
                return true;
            } else if (itemId == R.id.profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0); // Disable animation
                return true;
            }
            
            return false;
        });
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(FavoritesActivity.this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }
}
