package com.example.freshfinds;

import android.util.Log;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Product implements Serializable {
    private static final String TAG = "Product";
    private static final String FAVORITES_DATA_TYPE = "favorites";
    private static Set<String> favorites = new HashSet<>();
    private static boolean favoritesLoaded = false;
    
    private String id;
    private String name;
    private String description;
    private double price;
    private String farm;
    private int ratingCount;
    private float ratingAverage;
    private int imageResourceId;
    private String category;
    private boolean organic;
    private boolean favorite;
    
    // Static initializer for favorites
    static {
        loadFavorites();
    }
    
    public Product() {
        // Empty constructor for serialization
    }
    
    public Product(String id, String name, String description, double price, String farm, 
                  int imageResourceId, String category, boolean organic) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.farm = farm;
        this.imageResourceId = imageResourceId;
        this.category = category;
        this.organic = organic;
        this.ratingCount = 0;
        this.ratingAverage = 0f;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public String getFarm() { return farm; }
    public void setFarm(String farm) { this.farm = farm; }
    
    public String getFarmName() { return farm; } 
    
    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    
    public float getRatingAverage() { return ratingAverage; }
    public void setRatingAverage(float ratingAverage) { this.ratingAverage = ratingAverage; }
    
    public int getImageResourceId() { return imageResourceId; }
    public void setImageResourceId(int imageResourceId) { this.imageResourceId = imageResourceId; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public boolean isOrganic() { return organic; }
    public void setOrganic(boolean organic) { this.organic = organic; }
    
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
        
        if (favorite && !favorites.contains(id)) {
            favorites.add(id);
            saveFavorites();
        } else if (!favorite && favorites.contains(id)) {
            favorites.remove(id);
            saveFavorites();
        }
    }
    
    // Format price as currency string
    public String getFormattedPrice() {
        return String.format("$%.2f", price);
    }
    
    // Favorites functionality
    public boolean isFavorite() {
        if (!favoritesLoaded) {
            loadFavorites();
        }
        // Update the instance variable to be consistent with the static collection
        this.favorite = favorites.contains(id);
        return this.favorite;
    }
    
    public void toggleFavorite() {
        boolean newFavoriteStatus = !isFavorite();
        if (newFavoriteStatus) {
            favorites.add(id);
        } else {
            favorites.remove(id);
        }
        this.favorite = newFavoriteStatus;
        saveFavorites();
    }
    
    // Get list of favorite products
    public static List<Product> getFavorites(List<Product> allProducts) {
        if (!favoritesLoaded) {
            loadFavorites();
        }
        
        List<Product> favoriteProducts = new ArrayList<>();
        for (Product product : allProducts) {
            if (favorites.contains(product.getId())) {
                favoriteProducts.add(product);
            }
        }
        return favoriteProducts;
    }
    
    // Get list of favorite product IDs only
    public static Set<String> getFavoriteProductIds() {
        if (!favoritesLoaded) {
            loadFavorites();
        }
        return new HashSet<>(favorites);
    }
    
    // Save favorites to SharedPreferences
    private static void saveFavorites() {
        try {
            DataPersistenceManager.saveData(FAVORITES_DATA_TYPE, new ArrayList<>(favorites));
            Log.d(TAG, "Favorites saved successfully with " + favorites.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error saving favorites: " + e.getMessage());
        }
    }
    
    // Load favorites from SharedPreferences
    @SuppressWarnings("unchecked")
    private static void loadFavorites() {
        try {
            Type listType = new TypeToken<ArrayList<String>>(){}.getType();
            List<String> favoritesList = DataPersistenceManager.loadData(
                    FAVORITES_DATA_TYPE, listType, new ArrayList<>());
            
            favorites = new HashSet<>(favoritesList != null ? favoritesList : new ArrayList<>());
            favoritesLoaded = true;
            Log.d(TAG, "Favorites loaded successfully with " + favorites.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error loading favorites: " + e.getMessage());
            favorites = new HashSet<>();
            favoritesLoaded = true;
        }
    }
    
    // Force reload of favorites from storage
    public static void reloadFavorites() {
        favoritesLoaded = false;
        loadFavorites();
    }
}
