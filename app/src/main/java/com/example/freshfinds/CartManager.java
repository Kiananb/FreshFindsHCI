package com.example.freshfinds;

import android.util.Log;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {
    private static final String TAG = "CartManager";
    private static final String DATA_TYPE = "cart_items";
    
    private static CartManager instance;
    private Map<String, CartItem> cartItems;
    
    // Private constructor for singleton
    private CartManager() {
        loadCartData();
    }
    
    // Get singleton instance
    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }
    
    // Add item to cart
    public void addToCart(Product product, int quantity) {
        String productId = product.getId();
        
        if (cartItems.containsKey(productId)) {
            // Update quantity if item already in cart
            CartItem item = cartItems.get(productId);
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // Add new item to cart
            CartItem item = new CartItem(product, quantity);
            cartItems.put(productId, item);
        }
        
        // Save changes
        saveCartData();
    }
    
    // Remove item from cart
    public void removeFromCart(String productId) {
        cartItems.remove(productId);
        saveCartData();
    }
    
    // Update item quantity
    public void updateQuantity(String productId, int quantity) {
        if (cartItems.containsKey(productId)) {
            CartItem item = cartItems.get(productId);
            if (quantity <= 0) {
                cartItems.remove(productId);
            } else {
                item.setQuantity(quantity);
            }
            saveCartData();
        }
    }
    
    // Get all cart items as list
    public List<CartItem> getAllCartItems() {
        return new ArrayList<>(cartItems.values());
    }
    
    // Get cart total
    public double getCartTotal() {
        double total = 0;
        for (CartItem item : cartItems.values()) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        return total;
    }
    
    // Clear the entire cart
    public void clearCart() {
        cartItems.clear();
        saveCartData();
    }
    
    // Check if cart is empty
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }
    
    // Save cart data to SharedPreferences
    private void saveCartData() {
        try {
            DataPersistenceManager.saveData(DATA_TYPE, new ArrayList<>(cartItems.values()));
            Log.d(TAG, "Cart data saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving cart data: " + e.getMessage());
        }
    }
    
    // Load cart data from SharedPreferences
    @SuppressWarnings("unchecked")
    private void loadCartData() {
        try {
            Type listType = new TypeToken<ArrayList<CartItem>>(){}.getType();
            List<CartItem> items = DataPersistenceManager.loadData(DATA_TYPE, listType, new ArrayList<>());
            
            // Convert list to map for internal usage
            cartItems = new HashMap<>();
            if (items != null) {
                for (CartItem item : items) {
                    cartItems.put(item.getProduct().getId(), item);
                }
            }
            Log.d(TAG, "Cart data loaded successfully with " + cartItems.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error loading cart data: " + e.getMessage());
            cartItems = new HashMap<>();
        }
    }
    
    // Reload cart data from storage
    public void reloadData() {
        loadCartData();
    }
    
    // Cart item class
    public static class CartItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Product product;
        private int quantity;
        
        public CartItem() {
            // Empty constructor for serialization
        }
        
        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        
        public Product getProduct() {
            return product;
        }
        
        public void setProduct(Product product) {
            this.product = product;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
        
        public double getSubtotal() {
            return product.getPrice() * quantity;
        }
        
        public String getFormattedSubtotal() {
            return String.format("$%.2f", getSubtotal());
        }
    }
}
