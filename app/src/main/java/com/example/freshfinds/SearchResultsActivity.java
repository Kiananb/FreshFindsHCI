package com.example.freshfinds;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SearchResultsActivity extends BaseActivity implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "SearchResultsActivity";
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private TextView noResultsText;
    private List<Product> products;
    private List<Product> filteredProducts;
    private String searchQuery;
    private EditText searchEditText, aisleSearchEditText;
    private ImageButton searchButton, aisleSearchButton, backButton;
    private BottomNavigationView bottomNavigationView;
    private LinearLayout searchBarLayout, aisleTitleLayout, aisleSearchBarLayout;
    private TextView aisleTitleText;
    private boolean isFromAisles = false;
    private String currentAisle = "";
    
    // Filter parameters
    private boolean organicOnly = false;
    private String priceRange = "all";
    private String farmLocation = "All Locations";
    
    // Sort parameters
    private boolean priceHighToLow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        
        try {
            // Initialize UI components
            recyclerView = findViewById(R.id.recyclerViewProducts);
            noResultsText = findViewById(R.id.textViewNoResults);
            searchEditText = findViewById(R.id.searchEditText);
            searchButton = findViewById(R.id.searchButton);
            aisleSearchEditText = findViewById(R.id.aisleSearchEditText);
            aisleSearchButton = findViewById(R.id.aisleSearchButton);
            backButton = findViewById(R.id.buttonBack);
            Button filterButton = findViewById(R.id.filterButton);
            Button priceButton = findViewById(R.id.priceButton);
            bottomNavigationView = findViewById(R.id.bottomNavigationView);
            searchBarLayout = findViewById(R.id.searchBarLayout);
            aisleTitleLayout = findViewById(R.id.aisleTitleLayout);
            aisleSearchBarLayout = findViewById(R.id.aisleSearchBarLayout);
            aisleTitleText = findViewById(R.id.textViewAisleTitle);
            
            // Get search query and handle null case
            searchQuery = getIntent().getStringExtra("search_query");
            if (searchQuery == null) searchQuery = "";
            
            // Check if coming from Aisles activity
            isFromAisles = getIntent().getBooleanExtra("from_aisles", false);
            
            if (isFromAisles) {
                // Save current aisle for filtering
                currentAisle = searchQuery;
                
                // Hide regular search bar, show aisle title and aisle search
                searchBarLayout.setVisibility(View.GONE);
                aisleTitleLayout.setVisibility(View.VISIBLE);
                aisleSearchBarLayout.setVisibility(View.VISIBLE);
                
                // Set title to aisle name
                aisleTitleText.setText(searchQuery);
                
                // Update constraint for filter layout to be below aisle search
                ConstraintLayout layout = findViewById(R.id.constraintLayout);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(layout);
                constraintSet.connect(R.id.filterPriceLayout, ConstraintSet.TOP, 
                        R.id.aisleSearchBarLayout, ConstraintSet.BOTTOM);
                constraintSet.applyTo(layout);
                
                // Back button goes to AislesActivity
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish(); // Go back to AislesActivity
                    }
                });
                
                // Setup aisle-specific search
                aisleSearchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        performAisleSearch();
                    }
                });
                
                aisleSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            performAisleSearch();
                            return true;
                        }
                        return false;
                    }
                });
                
                // Get products from this aisle
                products = getProductsByCategory(searchQuery);
                filteredProducts = new ArrayList<>(products);
            } else {
                // Regular search view
                searchBarLayout.setVisibility(View.VISIBLE);
                aisleTitleLayout.setVisibility(View.GONE);
                aisleSearchBarLayout.setVisibility(View.GONE);
                searchEditText.setText(searchQuery);
                
                // Get products by search query
                products = ProductDataProvider.searchProducts(searchQuery);
                filteredProducts = new ArrayList<>(products);
                
                // Setup regular search functionality
                searchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        performSearch();
                    }
                });
                
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
            }
            
            // Setup UI based on results
            updateProductsDisplay();
            
            // Filter button
            filterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFilterDialog();
                }
            });
            
            // Price sort button
            priceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    priceHighToLow = !priceHighToLow;
                    priceButton.setText(priceHighToLow ? "Price ↓" : "Price ↑");
                    sortProducts();
                    updateProductsDisplay();
                }
            });
            
            // Set up bottom navigation
            setupBottomNavigation();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private List<Product> getProductsByCategory(String category) {
        List<Product> allProducts = ProductDataProvider.getProductList();
        List<Product> categoryProducts = new ArrayList<>();
        
        for (Product product : allProducts) {
            if (product.getCategory().equalsIgnoreCase(category)) {
                categoryProducts.add(product);
            }
        }
        
        return categoryProducts;
    }
    
    private void performSearch() {
        try {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                searchQuery = query;
                products = ProductDataProvider.searchProducts(query);
                filteredProducts = new ArrayList<>(products);
                
                // Reset filters when searching
                organicOnly = false;
                priceRange = "all";
                farmLocation = "All Locations";
                
                // Update UI
                updateProductsDisplay();
            } else {
                Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Search error: " + e.getMessage());
            Toast.makeText(this, "Search failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void performAisleSearch() {
        try {
            String query = aisleSearchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                // First get all products from this aisle
                List<Product> aisleProducts = getProductsByCategory(currentAisle);
                
                // Then filter by search term
                products = new ArrayList<>();
                for (Product product : aisleProducts) {
                    if (product.getName().toLowerCase().contains(query.toLowerCase()) || 
                        product.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                        product.getFarm().toLowerCase().contains(query.toLowerCase())) {
                        products.add(product);
                    }
                }
                
                filteredProducts = new ArrayList<>(products);
                
                // Reset filters when searching
                organicOnly = false;
                priceRange = "all";
                farmLocation = "All Locations";
                
                // Update UI
                updateProductsDisplay();
            } else {
                // If search field is empty, show all products in this aisle
                products = getProductsByCategory(currentAisle);
                filteredProducts = new ArrayList<>(products);
                updateProductsDisplay();
            }
        } catch (Exception e) {
            Log.e(TAG, "Aisle search error: " + e.getMessage());
            Toast.makeText(this, "Search failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.home); // Set home as selected
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();
                try {
                    if (itemId == R.id.home) {
                        startActivity(new Intent(SearchResultsActivity.this, HomePage.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.profile) {
                        startActivity(new Intent(SearchResultsActivity.this, ProfileActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.cart) {
                        startActivity(new Intent(SearchResultsActivity.this, CartActivity.class));
                        return true;
                    } else if (itemId == R.id.messages) {
                        startActivity(new Intent(SearchResultsActivity.this, MessagesActivity.class));
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error: " + e.getMessage());
                    Toast.makeText(SearchResultsActivity.this, "Navigation failed", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
    
    private void showFilterDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_filter);
        dialog.setCancelable(true);
        
        // Get dialog elements
        final CheckBox organicCheckbox = dialog.findViewById(R.id.checkBoxOrganic);
        final RadioGroup priceGroup = dialog.findViewById(R.id.radioGroupPrice);
        final RadioButton allPricesRadio = dialog.findViewById(R.id.radioAllPrices);
        final RadioButton under5Radio = dialog.findViewById(R.id.radioUnder5);
        final RadioButton from5to10Radio = dialog.findViewById(R.id.radio5to10);
        final RadioButton over10Radio = dialog.findViewById(R.id.radioOver10);
        final Spinner locationSpinner = dialog.findViewById(R.id.spinnerLocation);
        Button applyButton = dialog.findViewById(R.id.buttonApplyFilters);
        Button cancelButton = dialog.findViewById(R.id.buttonCancelFilters);
        
        // Set current selection
        organicCheckbox.setChecked(organicOnly);
        
        switch(priceRange) {
            case "under5":
                under5Radio.setChecked(true);
                break;
            case "5to10":
                from5to10Radio.setChecked(true);
                break;
            case "over10":
                over10Radio.setChecked(true);
                break;
            default:
                allPricesRadio.setChecked(true);
                break;
        }
        
        // Populate the location spinner
        Set<String> locations = new HashSet<>();
        locations.add("All Locations");
        for (Product product : products) {
            String location = getFarmLocation(product.getFarm()).split(",")[0]; // Get city only
            locations.add(location);
        }
        List<String> locationList = new ArrayList<>(locations);
        Collections.sort(locationList);
        
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, locationList);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
        
        // Set current location if possible
        int locationPosition = locationList.indexOf(farmLocation);
        if (locationPosition >= 0) {
            locationSpinner.setSelection(locationPosition);
        }
        
        // Handle location selection
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Not saving yet, just making a selection
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        
        // Apply button
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save filter options
                organicOnly = organicCheckbox.isChecked();
                
                int selectedPriceId = priceGroup.getCheckedRadioButtonId();
                if (selectedPriceId == R.id.radioUnder5) {
                    priceRange = "under5";
                } else if (selectedPriceId == R.id.radio5to10) {
                    priceRange = "5to10";
                } else if (selectedPriceId == R.id.radioOver10) {
                    priceRange = "over10";
                } else {
                    priceRange = "all";
                }
                
                farmLocation = (String) locationSpinner.getSelectedItem();
                
                // Apply filters
                applyFilters();
                sortProducts(); // Ensure sorting is maintained
                updateProductsDisplay();
                
                dialog.dismiss();
            }
        });
        
        // Cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        
        dialog.show();
    }
    
    private void applyFilters() {
        filteredProducts = new ArrayList<>();
        
        for (Product product : products) {
            // Apply organic filter
            if (organicOnly && !product.isOrganic()) {
                continue;
            }
            
            // Apply price filter
            double price = product.getPrice();
            if (priceRange.equals("under5") && price >= 5.0) {
                continue;
            } else if (priceRange.equals("5to10") && (price < 5.0 || price > 10.0)) {
                continue;
            } else if (priceRange.equals("over10") && price <= 10.0) {
                continue;
            }
            
            // Apply location filter
            if (!farmLocation.equals("All Locations")) {
                String productLocation = getFarmLocation(product.getFarm()).split(",")[0]; // Get city only
                if (!productLocation.equals(farmLocation)) {
                    continue;
                }
            }
            
            // If we made it here, add the product
            filteredProducts.add(product);
        }
    }
    
    private void sortProducts() {
        Collections.sort(filteredProducts, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                if (priceHighToLow) {
                    return Double.compare(p2.getPrice(), p1.getPrice());
                } else {
                    return Double.compare(p1.getPrice(), p2.getPrice());
                }
            }
        });
    }
    
    private void updateProductsDisplay() {
        if (filteredProducts.isEmpty()) {
            noResultsText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noResultsText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            
            // Setup RecyclerView
            adapter = new ProductAdapter(this, filteredProducts, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }
    }
    
    @Override
    public void onProductClick(Product product) {
        try {
            Intent intent = new Intent(SearchResultsActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error in onProductClick: " + e.getMessage());
            Toast.makeText(this, "Error opening product details", Toast.LENGTH_SHORT).show();
        }
    }
    
    // Copy of the method from ProductDetailActivity to get farm location
    private String getFarmLocation(String farmName) {
        switch (farmName) {
            case "Green Valley Farm": return "Boulder, CO";
            case "Riverside Organics": return "Sacramento, CA";
            case "Sunny Fields Farm": return "Austin, TX";
            case "Nature's Bounty Farm": return "Portland, OR";
            case "Meadow Farm": return "Madison, WI";
            case "Berry Good Farm": return "Burlington, VT";
            case "Blue Ridge Farm": return "Asheville, NC";
            case "Garden Delights": return "Seattle, WA";
            case "Herb Haven Farm": return "Santa Fe, NM";
            case "Homegrown Preserves": return "Nashville, TN";
            case "Bee Happy Farm": return "Savannah, GA";
            case "Green Pastures Dairy": return "Amish County, PA";
            case "Happy Hen Farm": return "Eugene, OR";
            case "Sunshine Orchards": return "San Diego, CA";
            case "Tropical Harvest": return "Miami, FL";
            default: return "Local Farm";
        }
    }
}