package com.example.freshfinds;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MessageActivity extends BaseActivity {

    private static final String TAG = "MessageActivity";
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        
        ImageButton backButton = findViewById(R.id.buttonBack);
        TextView placeholderText = findViewById(R.id.textViewPlaceholder);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        
        placeholderText.setText("You have no new messages");
        
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // Setup bottom navigation
        setupBottomNavigation();
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.messages); // Set messages as selected
        
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();
                try {
                    if (itemId == R.id.home) {
                        startActivity(new Intent(MessageActivity.this, HomePage.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.profile) {
                        startActivity(new Intent(MessageActivity.this, ProfileActivity.class));
                        return true;
                    } else if (itemId == R.id.cart) {
                        startActivity(new Intent(MessageActivity.this, CartActivity.class));
                        return true;
                    } else if (itemId == R.id.messages) {
                        // Already on messages screen
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error: " + e.getMessage());
                    Toast.makeText(MessageActivity.this, "Navigation failed", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
}
