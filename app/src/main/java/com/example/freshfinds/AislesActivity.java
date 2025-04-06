package com.example.freshfinds;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AislesActivity extends BaseActivity {
    
    private static final String TAG = "AislesActivity";
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aisles);
        
        try {
            ImageButton backButton = findViewById(R.id.buttonBack);
            RecyclerView recyclerView = findViewById(R.id.recyclerViewAisles);
            
            // Get unique categories safely
            List<Product> allProducts;
            try {
                allProducts = ProductDataProvider.getProductList();
            } catch (Exception e) {
                Log.e(TAG, "Error getting products: " + e.getMessage());
                allProducts = new ArrayList<>();
            }
            
            Set<String> categoriesSet = new HashSet<>();
            for (Product product : allProducts) {
                categoriesSet.add(product.getCategory());
            }
            
            List<String> categories = new ArrayList<>(categoriesSet);
            
            // Setup RecyclerView
            AisleAdapter adapter = new AisleAdapter(this, categories);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            bottomNavigationView = findViewById(R.id.bottomNavigationView);
            
            // Setup bottom navigation
            setupBottomNavigation();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "An error occurred loading aisles.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.home); // Set home as selected since aisles is part of home
        
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();
                try {
                    if (itemId == R.id.home) {
                        startActivity(new Intent(AislesActivity.this, HomePage.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.profile) {
                        startActivity(new Intent(AislesActivity.this, ProfileActivity.class));
                        return true;
                    } else if (itemId == R.id.cart) {
                        startActivity(new Intent(AislesActivity.this, CartActivity.class));
                        return true;
                    } else if (itemId == R.id.messages) {
                        startActivity(new Intent(AislesActivity.this, MessagesActivity.class));
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error: " + e.getMessage());
                    Toast.makeText(AislesActivity.this, "Navigation failed", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
    
    // Simple adapter for aisles
    private class AisleAdapter extends RecyclerView.Adapter<AisleAdapter.AisleViewHolder> {
        
        private Context context;
        private List<String> aisles;
        
        public AisleAdapter(Context context, List<String> aisles) {
            this.context = context;
            this.aisles = aisles;
        }
        
        @NonNull
        @Override
        public AisleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.aisle_item, parent, false);
            return new AisleViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull AisleViewHolder holder, int position) {
            String aisle = aisles.get(position);
            holder.aisleName.setText(aisle);
        }
        
        @Override
        public int getItemCount() {
            return aisles.size();
        }
        
        class AisleViewHolder extends RecyclerView.ViewHolder {
            TextView aisleName;
            
            public AisleViewHolder(@NonNull View itemView) {
                super(itemView);
                aisleName = itemView.findViewById(R.id.textViewAisleName);
                
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                String category = aisles.get(position);
                                Intent intent = new Intent(context, SearchResultsActivity.class);
                                intent.putExtra("search_query", category);
                                intent.putExtra("from_aisles", true); // Add this flag to indicate we're coming from aisles
                                context.startActivity(intent);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error on aisle click: " + e.getMessage());
                            Toast.makeText(context, "Error opening aisle", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}
