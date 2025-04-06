package com.example.freshfinds;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PastPurchasesActivity extends BaseActivity {

    private static final String TAG = "PastPurchasesActivity";
    private RecyclerView recyclerView;
    private TextView placeholderText;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_purchases);
        
        recyclerView = findViewById(R.id.recyclerViewOrders);
        placeholderText = findViewById(R.id.textViewPlaceholder);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        
        // Add back button functionality
        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous screen
            }
        });
        
        // Get all orders
        List<OrderManager.Order> orders = OrderManager.getInstance().getAllOrders();
        
        // Show placeholder or order list
        if (orders.isEmpty()) {
            placeholderText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            // Update the placeholder text for past orders
            placeholderText.setText("You haven't placed any orders yet");
        } else {
            placeholderText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            
            // Setup RecyclerView
            OrderAdapter adapter = new OrderAdapter(orders);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        
        // Setup bottom navigation
        setupBottomNavigation();
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.home); // Home tab since it's part of the home section
        
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();
                try {
                    if (itemId == R.id.home) {
                        startActivity(new Intent(PastPurchasesActivity.this, HomePage.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.profile) {
                        startActivity(new Intent(PastPurchasesActivity.this, ProfileActivity.class));
                        return true;
                    } else if (itemId == R.id.cart) {
                        startActivity(new Intent(PastPurchasesActivity.this, CartActivity.class));
                        return true;
                    } else if (itemId == R.id.messages) {
                        startActivity(new Intent(PastPurchasesActivity.this, MessagesActivity.class));
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error: " + e.getMessage());
                    Toast.makeText(PastPurchasesActivity.this, "Navigation failed", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
    
    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
        
        private List<OrderManager.Order> orders;
        private SimpleDateFormat dateFormat;
        
        public OrderAdapter(List<OrderManager.Order> orders) {
            this.orders = orders;
            this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        }
        
        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(PastPurchasesActivity.this).inflate(
                    R.layout.item_order, parent, false);
            return new OrderViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            OrderManager.Order order = orders.get(position);
            holder.orderIdText.setText("Order #" + order.getOrderId());
            holder.orderDateText.setText(dateFormat.format(order.getOrderDate()));
            holder.itemCountText.setText(order.getTotalItemCount() + " items");
            holder.methodText.setText(order.isDelivery() ? "Delivery" : "Pickup");
            holder.orderTotalText.setText(order.getFormattedTotal());
            holder.statusText.setText(order.getStatus());
            
            // Set delivery address or hide the section
            if (order.isDelivery() && order.getDeliveryAddress() != null) {
                holder.deliveryAddressSection.setVisibility(View.VISIBLE);
                holder.deliveryAddressText.setText(order.getDeliveryAddress());
            } else {
                holder.deliveryAddressSection.setVisibility(View.GONE);
            }
            
            // Setup expandable details
            boolean isExpanded = holder.isExpanded;
            holder.detailsLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            
            // Setup delivery address section
            if (order.isDelivery() && order.getDeliveryAddress() != null) {
                holder.deliveryAddressSection.setVisibility(View.VISIBLE);
                holder.deliveryAddressText.setText(order.getDeliveryAddress());
            } else {
                holder.deliveryAddressSection.setVisibility(View.GONE);
            }
            
            // Setup shipping section
            if (order.isDelivery()) {
                holder.shippingSection.setVisibility(View.VISIBLE);
                holder.shippingText.setText(order.getFormattedShipping());
            } else {
                holder.shippingSection.setVisibility(View.GONE);
            }
            
            // Set price breakdown values
            holder.subtotalText.setText(order.getFormattedSubtotal());
            holder.taxText.setText(order.getFormattedTax());
            holder.totalDetailText.setText(order.getFormattedTotal());
            
            // Add order items to details section when expanded
            if (isExpanded && holder.detailsLayout.getChildCount() <= 3) { // Account for the existing views
                for (CartManager.CartItem item : order.getItems()) {
                    View itemView = LayoutInflater.from(PastPurchasesActivity.this)
                            .inflate(R.layout.item_order_detail, null);
                    
                    TextView nameText = itemView.findViewById(R.id.textViewItemName);
                    TextView quantityText = itemView.findViewById(R.id.textViewItemQuantity);
                    TextView priceText = itemView.findViewById(R.id.textViewItemPrice);
                    
                    nameText.setText(item.getProduct().getName());
                    quantityText.setText("Qty: " + item.getQuantity());
                    priceText.setText(item.getFormattedSubtotal());
                    
                    holder.detailsLayout.addView(itemView);
                }
            }
        }
        
        @Override
        public int getItemCount() {
            return orders.size();
        }
        
        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView orderIdText, orderDateText, itemCountText, orderTotalText, statusText, methodText;
            TextView deliveryAddressText, subtotalText, taxText, shippingText, totalDetailText;
            LinearLayout detailsLayout, deliveryAddressSection, shippingSection;
            boolean isExpanded = false;
            
            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                
                orderIdText = itemView.findViewById(R.id.textViewOrderId);
                orderDateText = itemView.findViewById(R.id.textViewOrderDate);
                itemCountText = itemView.findViewById(R.id.textViewItemCount);
                orderTotalText = itemView.findViewById(R.id.textViewOrderTotal);
                statusText = itemView.findViewById(R.id.textViewStatus);
                methodText = itemView.findViewById(R.id.textViewMethod);
                detailsLayout = itemView.findViewById(R.id.orderDetailsLayout);
                
                // Additional detail views
                deliveryAddressSection = itemView.findViewById(R.id.deliveryAddressSection);
                deliveryAddressText = itemView.findViewById(R.id.textViewDeliveryAddress);
                subtotalText = itemView.findViewById(R.id.textViewOrderSubtotal);
                taxText = itemView.findViewById(R.id.textViewOrderTax);
                shippingSection = itemView.findViewById(R.id.shippingSection);
                shippingText = itemView.findViewById(R.id.textViewOrderShipping);
                totalDetailText = itemView.findViewById(R.id.textViewOrderTotalDetail);
                
                // Toggle expand/collapse on click
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Toggle expanded state
                        isExpanded = !isExpanded;
                        detailsLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                        
                        // Notify adapter to ensure proper layout
                        notifyItemChanged(getAdapterPosition());
                    }
                });
            }
        }
    }
}
