package com.example.freshfinds;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderConfirmationActivity extends BaseActivity {

    private static final String TAG = "OrderConfirmActivity";
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        TextView orderNumberView = findViewById(R.id.textViewOrderNumber);
        TextView orderTotalView = findViewById(R.id.textViewOrderTotal);
        TextView orderMethodView = findViewById(R.id.textViewOrderMethod);
        TextView estimatedTimeView = findViewById(R.id.textViewEstimatedTime);
        Button continueShoppingButton = findViewById(R.id.buttonContinueShopping);

        // Get order details from intent
        double orderTotal = getIntent().getDoubleExtra("order_total", 0.0);
        double subtotal = getIntent().getDoubleExtra("subtotal", 0.0);
        double tax = getIntent().getDoubleExtra("tax", 0.0);
        double shipping = getIntent().getDoubleExtra("shipping", 0.0);
        boolean isDelivery = getIntent().getBooleanExtra("is_delivery", true);
        String deliveryAddress = getIntent().getStringExtra("delivery_address");

        // Generate random order number
        String orderNumber = generateOrderNumber();
        
        // Format total
        DecimalFormat df = new DecimalFormat("$#,##0.00");
        
        // Set order details
        orderNumberView.setText(orderNumber);
        orderTotalView.setText(df.format(orderTotal));
        orderMethodView.setText(isDelivery ? "Delivery" : "Pickup");
        
        // Set estimated time based on delivery method
        String estimatedTime = isDelivery ? "45-60 minutes" : "20-30 minutes";
        estimatedTimeView.setText(estimatedTime);
        
        // Create and save order with the actual address
        List<CartManager.CartItem> orderItems = getOrderItemsFromIntent();
        if (orderItems == null || orderItems.isEmpty()) {
            // Create a mock order item if needed
            orderItems = createMockOrderItems();
        }
        
        // Save the order with the actual address
        OrderManager.Order order = new OrderManager.Order(
            orderNumber,
            orderItems,
            subtotal,
            tax,
            shipping,
            isDelivery,
            deliveryAddress
        );
        OrderManager.getInstance().addOrder(order);
        
        // Continue shopping button
        continueShoppingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to home screen
                Intent intent = new Intent(OrderConfirmationActivity.this, HomePage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        
        // Setup bottom navigation
        setupBottomNavigation();
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.home); // Set home as selected
        
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();
                try {
                    if (itemId == R.id.home) {
                        startActivity(new Intent(OrderConfirmationActivity.this, HomePage.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.profile) {
                        startActivity(new Intent(OrderConfirmationActivity.this, ProfileActivity.class));
                        return true;
                    } else if (itemId == R.id.cart) {
                        startActivity(new Intent(OrderConfirmationActivity.this, CartActivity.class));
                        return true;
                    } else if (itemId == R.id.messages) {
                        startActivity(new Intent(OrderConfirmationActivity.this, MessagesActivity.class));
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error: " + e.getMessage());
                    Toast.makeText(OrderConfirmationActivity.this, "Navigation failed", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
    
    private String generateOrderNumber() {
        Random random = new Random();
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        
        // Add a letter prefix
        sb.append(letters.charAt(random.nextInt(letters.length())));
        
        // Add 4-digit number
        sb.append(String.format("%04d", random.nextInt(10000)));
        
        // Add hyphen and 2 more digits
        sb.append("-").append(String.format("%02d", random.nextInt(100)));
        
        return sb.toString();
    }
    
    // In a real app, we would pass the order items in the intent
    // Here we create mock items because the cart was cleared before this activity
    private List<CartManager.CartItem> getOrderItemsFromIntent() {
        return null; // In a real app, this would retrieve data from the intent
    }
    
    private List<CartManager.CartItem> createMockOrderItems() {
        List<CartManager.CartItem> mockItems = new ArrayList<>();
        
        // Create some mock products from our product catalog
        List<Product> allProducts = ProductDataProvider.getProductList();
        if (!allProducts.isEmpty()) {
            // Add random products to the mock order
            Random random = new Random();
            int numItems = random.nextInt(3) + 1; // 1-3 items
            
            for (int i = 0; i < numItems && i < allProducts.size(); i++) {
                Product product = allProducts.get(i);
                int quantity = random.nextInt(2) + 1; // 1-2 quantity
                mockItems.add(new CartManager.CartItem(product, quantity));
            }
        }
        
        return mockItems;
    }
}
