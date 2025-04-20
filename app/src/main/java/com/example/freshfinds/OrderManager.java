package com.example.freshfinds;

import android.util.Log;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderManager {
    private static final String TAG = "OrderManager";
    private static final String DATA_TYPE = "orders";
    
    private static OrderManager instance;
    private List<Order> pastOrders;
    
    private OrderManager() {
        loadOrderData();
    }
    
    public static synchronized OrderManager getInstance() {
        if (instance == null) {
            instance = new OrderManager();
        }
        return instance;
    }
    
    public void addOrder(Order order) {
        pastOrders.add(0, order); // Add to beginning of list (most recent first)
        saveOrderData();
    }
    
    public List<Order> getAllOrders() {
        return pastOrders;
    }
    
    // Save orders to SharedPreferences
    private void saveOrderData() {
        try {
            DataPersistenceManager.saveData(DATA_TYPE, pastOrders);
            Log.d(TAG, "Orders data saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving orders data: " + e.getMessage());
        }
    }
    
    // Load orders from SharedPreferences
    @SuppressWarnings("unchecked")
    private void loadOrderData() {
        try {
            Type listType = new TypeToken<ArrayList<Order>>(){}.getType();
            List<Order> loadedOrders = DataPersistenceManager.loadData(DATA_TYPE, listType, new ArrayList<>());
            
            if (loadedOrders != null) {
                pastOrders = loadedOrders;
            } else {
                pastOrders = new ArrayList<>();
            }
            Log.d(TAG, "Orders data loaded with " + pastOrders.size() + " orders");
        } catch (Exception e) {
            Log.e(TAG, "Error loading orders data: " + e.getMessage());
            pastOrders = new ArrayList<>();
        }
    }
    
    // Reload orders data from storage
    public void reloadData() {
        loadOrderData();
    }
    
    public static class Order implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String orderId;
        private List<CartManager.CartItem> items;
        private double subtotal;
        private double tax;
        private double shipping;
        private double total;
        private boolean isDelivery;
        private Date orderDate;
        private String status;
        private String deliveryAddress;
        
        public Order() {
            // Empty constructor for serialization
        }
        
        public Order(String orderId, List<CartManager.CartItem> items, 
                    double subtotal, double tax, double shipping, 
                    boolean isDelivery) {
            this(orderId, items, subtotal, tax, shipping, isDelivery, null);
        }
        
        public Order(String orderId, List<CartManager.CartItem> items, 
                    double subtotal, double tax, double shipping, 
                    boolean isDelivery, String deliveryAddress) {
            this.orderId = orderId;
            this.items = new ArrayList<>(items);
            this.subtotal = subtotal;
            this.tax = tax;
            this.shipping = shipping;
            this.total = subtotal + tax + shipping;
            this.isDelivery = isDelivery;
            this.orderDate = new Date();
            this.status = "Processing";
            this.deliveryAddress = deliveryAddress;
        }
        
        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public List<CartManager.CartItem> getItems() { return items; }
        public void setItems(List<CartManager.CartItem> items) { this.items = items; }
        
        public double getSubtotal() { return subtotal; }
        public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
        
        public double getTax() { return tax; }
        public void setTax(double tax) { this.tax = tax; }
        
        public double getShipping() { return shipping; }
        public void setShipping(double shipping) { this.shipping = shipping; }
        
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
        
        public boolean isDelivery() { return isDelivery; }
        public void setDelivery(boolean delivery) { isDelivery = delivery; }
        
        public Date getOrderDate() { return orderDate; }
        public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getDeliveryAddress() { return deliveryAddress; }
        public void setDeliveryAddress(String address) { this.deliveryAddress = address; }
        
        // Helper methods
        public int getTotalItemCount() {
            int count = 0;
            for (CartManager.CartItem item : items) {
                count += item.getQuantity();
            }
            return count;
        }
        
        public String getFormattedTotal() {
            return String.format("$%.2f", total);
        }
        
        public String getFormattedSubtotal() {
            return String.format("$%.2f", subtotal);
        }
        
        public String getFormattedTax() {
            return String.format("$%.2f", tax);
        }
        
        public String getFormattedShipping() {
            return String.format("$%.2f", shipping);
        }
    }
}
