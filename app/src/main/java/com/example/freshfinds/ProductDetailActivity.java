package com.example.freshfinds;

import android.app.Dialog;
import android.content.Intent; 
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

public class ProductDetailActivity extends BaseActivity {

    private static final String TAG = "ProductDetailActivity";
    private Product product;
    private MessageStorage messageStorage;
    private Button addToCartButton;
    private View quantityEditorLayout;
    private Button decreaseButton, increaseButton;
    private TextView quantityTextView;
    private int currentQuantity = 1;
    private CartManager cartManager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        
        messageStorage = MessageStorage.getInstance();
        cartManager = CartManager.getInstance();
        
        try {
            // Get product ID from intent
            String productId = getIntent().getStringExtra("product_id");
            if (productId == null) {
                Toast.makeText(this, "Error: Product ID missing", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Get product from data provider
            product = ProductDataProvider.getProductById(productId);
            if (product == null) {
                Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Initialize UI components
            ImageView productImage = findViewById(R.id.imageViewProductDetail);
            TextView productName = findViewById(R.id.textViewProductName);
            TextView productPrice = findViewById(R.id.textViewProductPrice);
            TextView productFarm = findViewById(R.id.textViewFarmName);
            TextView productDescription = findViewById(R.id.textViewProductDescription);
            ImageView organicBadge = findViewById(R.id.imageViewOrganic);
            addToCartButton = findViewById(R.id.buttonAddToCart);
            ImageButton favoriteButton = findViewById(R.id.buttonFavorite);
            ImageButton backButton = findViewById(R.id.buttonBack);
            
            // New components for enhanced details
            TextView farmLocationText = findViewById(R.id.textViewFarmLocation);
            Button messageFarmerButton = findViewById(R.id.buttonMessageFarmer);
            TextView ratingText = findViewById(R.id.textViewRating);
            Button viewReviewsButton = findViewById(R.id.buttonViewReviews);
            quantityEditorLayout = findViewById(R.id.quantityEditorLayout);
            decreaseButton = findViewById(R.id.buttonDecrease);
            increaseButton = findViewById(R.id.buttonIncrease);
            quantityTextView = findViewById(R.id.textViewQuantity);
            bottomNavigationView = findViewById(R.id.bottomNavigationView);
            
            // Set product details
            productName.setText(product.getName());
            productPrice.setText(product.getFormattedPrice());
            productFarm.setText(product.getFarm());
            productDescription.setText(product.getDescription());
            
            // Set product image
            productImage.setImageResource(product.getImageResourceId());
            
            // Show/hide organic badge
            organicBadge.setVisibility(product.isOrganic() ? View.VISIBLE : View.GONE);
            
            // Set additional farm details
            farmLocationText.setText("Location: " + getFarmLocation(product.getFarm()));
            
            // Set rating information
            float avgRating = messageStorage.getAverageRating(product.getId());
            int reviewCount = messageStorage.getReviewCount(product.getId());
            
            ratingText.setText(String.format("%.1f ★ (%d reviews)", avgRating, reviewCount));
            
            // Update favorite button based on product status
            updateFavoriteButton(favoriteButton);
            
            // Check if product is already in cart
            checkIfProductInCart();
            
            // Set click listeners
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            
            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    product.setFavorite(!product.isFavorite());
                    updateFavoriteButton(favoriteButton);
                    
                    String message = product.isFavorite() 
                        ? "Added to favorites" 
                        : "Removed from favorites";
                    Toast.makeText(ProductDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
            
            addToCartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Add product to cart
                    cartManager.addToCart(product, 1);
                    
                    // Show quantity editor instead of Add to Cart button
                    addToCartButton.setVisibility(View.GONE);
                    quantityEditorLayout.setVisibility(View.VISIBLE);
                    currentQuantity = 1;
                    updateQuantityText();
                    
                    Toast.makeText(ProductDetailActivity.this, 
                                  product.getName() + " added to cart", 
                                  Toast.LENGTH_SHORT).show();
                }
            });
            
            decreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentQuantity > 1) {
                        currentQuantity--;
                        cartManager.updateQuantity(product.getId(), currentQuantity);
                        updateQuantityText();
                    } else {
                        // Remove from cart
                        cartManager.removeFromCart(product.getId());
                        addToCartButton.setVisibility(View.VISIBLE);
                        quantityEditorLayout.setVisibility(View.GONE);
                        Toast.makeText(ProductDetailActivity.this, 
                                    product.getName() + " removed from cart", 
                                    Toast.LENGTH_SHORT).show();
                    }
                }
            });
            
            increaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentQuantity++;
                    cartManager.updateQuantity(product.getId(), currentQuantity);
                    updateQuantityText();
                }
            });
            
            // Message farmer button
            messageFarmerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMessageFarmerDialog();
                }
            });
            
            // View reviews button
            viewReviewsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showReviewsDialog();
                }
            });
            
            // Setup bottom navigation
            setupBottomNavigation();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "An error occurred loading product details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private String getFarmLocation(String farmName) {
        // Mock farm locations
        switch (farmName) {
            case "Green Valley Farm": return "Boulder, CO";
            case "Riverside Organics": return "Sacramento, CA";
            case "Sunny Fields Farm": return "Austin, TX";
            case "Nature's Bounty Farm": return "Portland, OR";
            case "Meadow Farm": return "Madison, WI";
            case "Highland Orchards": return "Ithaca, NY";
            case "Berry Good Farm": return "Burlington, VT";
            case "Blue Ridge Farm": return "Asheville, NC";
            case "Tropical Harvest": return "Miami, FL";
            case "Sunshine Orchards": return "San Diego, CA";
            case "Happy Hen Farm": return "Eugene, OR";
            case "Green Pastures Dairy": return "Amish County, PA";
            case "Bee Happy Farm": return "Savannah, GA";
            case "Homegrown Preserves": return "Nashville, TN";
            case "Herb Haven Farm": return "Santa Fe, NM";
            case "Garden Delights": return "Seattle, WA";
            default: return "Local Farm";
        }
    }
    
    private void showReviewsDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_reviews);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        TextView titleText = dialog.findViewById(R.id.textViewReviewsTitle);
        RecyclerView reviewsRecyclerView = dialog.findViewById(R.id.recyclerViewReviews);
        Button closeButton = dialog.findViewById(R.id.buttonCloseReviews);
        Button addReviewButton = dialog.findViewById(R.id.buttonAddReview);
        
        titleText.setText("Reviews for " + product.getName());
        
        // Set up recycler view with reviews
        List<MessageStorage.ReviewItem> reviews = messageStorage.getReviewsForProduct(product.getId());
        ReviewsAdapter adapter = new ReviewsAdapter(reviews);
        reviewsRecyclerView.setAdapter(adapter);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        
        // Add review button
        addReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProductDetailActivity.this, "Review feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }
    
    private void showMessageFarmerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_message_farmer);
        dialog.setCancelable(true);
        
        // Get dialog views
        TextView farmerNameView = dialog.findViewById(R.id.textViewFarmerName);
        EditText messageEdit = dialog.findViewById(R.id.editTextMessage);
        Button sendButton = dialog.findViewById(R.id.buttonSendMessage);
        Button cancelButton = dialog.findViewById(R.id.buttonCancelMessage);
        
        // Set farm name
        farmerNameView.setText("Message to " + product.getFarmName());
        
        // Set up cancel button
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        // Set up send button
        sendButton.setOnClickListener(v -> {
            String message = messageEdit.getText().toString().trim();
            if (!message.isEmpty()) {
                // Add message to storage using the new conversation system
                MessageStorage.getInstance().addMessage(product.getFarmName(), message, false);
                Toast.makeText(this, "Message sent to " + product.getFarmName(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                messageEdit.setError("Please enter a message");
            }
        });
        
        dialog.show();
    }
    
    private void updateFavoriteButton(ImageButton favoriteButton) {
        if (product.isFavorite()) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border);
        }
    }
    
    private void checkIfProductInCart() {
        // Check if the product is already in the cart
        List<CartManager.CartItem> cartItems = cartManager.getAllCartItems();
        for (CartManager.CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                // Product is in cart, show quantity editor
                addToCartButton.setVisibility(View.GONE);
                quantityEditorLayout.setVisibility(View.VISIBLE);
                currentQuantity = item.getQuantity();
                updateQuantityText();
                return;
            }
        }
        
        // Product not in cart, show Add to Cart button
        addToCartButton.setVisibility(View.VISIBLE);
        quantityEditorLayout.setVisibility(View.GONE);
    }
    
    private void updateQuantityText() {
        quantityTextView.setText(String.valueOf(currentQuantity));
    }
    
    private void setupBottomNavigation() {
        // No item selected by default as we're on a product page
        
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();
                try {
                    if (itemId == R.id.home) {
                        startActivity(new Intent(ProductDetailActivity.this, HomePage.class));
                        overridePendingTransition(0, 0); // Disable animation
                        finish();
                        return true;
                    } else if (itemId == R.id.cart) {
                        startActivity(new Intent(ProductDetailActivity.this, CartActivity.class));
                        overridePendingTransition(0, 0); // Disable animation
                        return true;
                    } else if (itemId == R.id.messages) {
                        startActivity(new Intent(ProductDetailActivity.this, MessagesActivity.class));
                        overridePendingTransition(0, 0); // Disable animation
                        return true;
                    } else if (itemId == R.id.profile) {
                        startActivity(new Intent(ProductDetailActivity.this, ProfileActivity.class));
                        overridePendingTransition(0, 0); // Disable animation
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error: " + e.getMessage());
                    Toast.makeText(ProductDetailActivity.this, "Navigation failed", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
    
    // Adapter for reviews
    private class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
        
        private List<MessageStorage.ReviewItem> reviews;
        
        public ReviewsAdapter(List<MessageStorage.ReviewItem> reviews) {
            this.reviews = reviews;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_review, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MessageStorage.ReviewItem review = reviews.get(position);
            holder.nameText.setText(review.getUserName());
            holder.ratingBar.setRating(review.getRating());
            holder.commentText.setText(review.getComment());
        }
        
        @Override
        public int getItemCount() {
            return reviews.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText;
            RatingBar ratingBar;
            TextView commentText;
            
            public ViewHolder(View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.textViewReviewerName);
                ratingBar = itemView.findViewById(R.id.ratingBarReview);
                commentText = itemView.findViewById(R.id.textViewReviewComment);
            }
        }
    }
}
