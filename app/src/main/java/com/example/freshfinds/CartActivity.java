package com.example.freshfinds;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class CartActivity extends BaseActivity {

    private static final String TAG = "CartActivity";
    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private TextView placeholderText;
    private LinearLayout cartSummaryLayout;
    private TextView subtotalView, totalView;
    private Button checkoutButton;
    private static final double SHIPPING_FEE = 4.99;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        
        recyclerView = findViewById(R.id.recyclerViewCart);
        placeholderText = findViewById(R.id.textViewPlaceholder);
        cartSummaryLayout = findViewById(R.id.cartSummaryLayout);
        subtotalView = findViewById(R.id.textViewSubtotal);
        totalView = findViewById(R.id.textViewTotal);
        checkoutButton = findViewById(R.id.buttonCheckout);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        
        // Get cart items
        List<CartManager.CartItem> cartItems = CartManager.getInstance().getAllCartItems();
        
        // Update UI based on cart contents
        if (cartItems.isEmpty()) {
            placeholderText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            cartSummaryLayout.setVisibility(View.GONE);
        } else {
            placeholderText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            cartSummaryLayout.setVisibility(View.VISIBLE);
            
            // Setup recycler view
            adapter = new CartAdapter(cartItems);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            
            // Update totals
            updateTotals();
        }
        
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CartManager.getInstance().isEmpty()) {
                    Toast.makeText(CartActivity.this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                } else {
                    // Start checkout activity
                    Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                    startActivityForResult(intent, 100);
                }
            }
        });
        
        // Setup bottom navigation
        setupBottomNavigation();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Order was placed successfully, finish the CartActivity
            finish();
        }
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.cart); // Set cart as selected
        
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();
                try {
                    if (itemId == R.id.home) {
                        startActivity(new Intent(CartActivity.this, HomePage.class));
                        overridePendingTransition(0, 0); // Disable animation
                        finish();
                        return true;
                    } else if (itemId == R.id.cart) {
                        // Already on cart screen
                        return true;
                    } else if (itemId == R.id.messages) {
                        startActivity(new Intent(CartActivity.this, MessagesActivity.class));
                        overridePendingTransition(0, 0); // Disable animation
                        return true;
                    } else if (itemId == R.id.profile) {
                        startActivity(new Intent(CartActivity.this, ProfileActivity.class));
                        overridePendingTransition(0, 0); // Disable animation
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error: " + e.getMessage());
                    Toast.makeText(CartActivity.this, "Navigation failed", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
    
    private void updateTotals() {
        double subtotal = CartManager.getInstance().getCartTotal();
        double total = subtotal + SHIPPING_FEE;
        
        subtotalView.setText(String.format("$%.2f", subtotal));
        totalView.setText(String.format("$%.2f", total));
    }
    
    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
        
        private List<CartManager.CartItem> cartItems;
        
        public CartAdapter(List<CartManager.CartItem> cartItems) {
            this.cartItems = cartItems;
        }
        
        @NonNull
        @Override
        public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
            return new CartViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
            CartManager.CartItem item = cartItems.get(position);
            Product product = item.getProduct();
            
            holder.productName.setText(product.getName());
            holder.productPrice.setText(product.getFormattedPrice());
            holder.quantity.setText(String.valueOf(item.getQuantity()));
            holder.subtotal.setText(item.getFormattedSubtotal());
            holder.productImage.setImageResource(product.getImageResourceId());
            
            holder.decreaseButton.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() - 1;
                if (newQuantity <= 0) {
                    // Remove item if quantity becomes 0
                    CartManager.getInstance().removeFromCart(product.getId());
                    cartItems.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cartItems.size());
                    
                    // Check if cart is now empty
                    if (cartItems.isEmpty()) {
                        placeholderText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        cartSummaryLayout.setVisibility(View.GONE);
                    }
                } else {
                    // Update quantity
                    CartManager.getInstance().updateQuantity(product.getId(), newQuantity);
                    item.setQuantity(newQuantity);
                    notifyItemChanged(position);
                }
                updateTotals();
            });
            
            holder.increaseButton.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                CartManager.getInstance().updateQuantity(product.getId(), newQuantity);
                item.setQuantity(newQuantity);
                notifyItemChanged(position);
                updateTotals();
            });
            
            holder.removeButton.setOnClickListener(v -> {
                CartManager.getInstance().removeFromCart(product.getId());
                cartItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartItems.size());
                updateTotals();
                
                // Check if cart is now empty
                if (cartItems.isEmpty()) {
                    placeholderText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    cartSummaryLayout.setVisibility(View.GONE);
                }
            });
            
            holder.productImage.setOnClickListener(v -> {
                Intent intent = new Intent(CartActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            });
        }
        
        @Override
        public int getItemCount() {
            return cartItems.size();
        }
        
        class CartViewHolder extends RecyclerView.ViewHolder {
            ImageView productImage;
            TextView productName, productPrice, quantity, subtotal;
            Button decreaseButton, increaseButton;
            ImageButton removeButton;
            
            public CartViewHolder(@NonNull View itemView) {
                super(itemView);
                productImage = itemView.findViewById(R.id.imageViewCartProduct);
                productName = itemView.findViewById(R.id.textViewCartProductName);
                productPrice = itemView.findViewById(R.id.textViewCartProductPrice);
                quantity = itemView.findViewById(R.id.textViewQuantity);
                subtotal = itemView.findViewById(R.id.textViewSubtotal);
                decreaseButton = itemView.findViewById(R.id.buttonDecrease);
                increaseButton = itemView.findViewById(R.id.buttonIncrease);
                removeButton = itemView.findViewById(R.id.buttonRemove);
            }
        }
    }
}
