package com.example.freshfinds;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Base class for all activities that require authentication
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAuthentication();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthentication();
    }

    /**
     * Check if the user is authenticated, redirect to login if not
     */
    private void checkAuthentication() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        
        if (currentUser == null) {
            // User is not logged in, redirect to LoginPage
            Intent intent = new Intent(this, LoginPage.class);
            startActivity(intent);
            finish(); // Close this activity
        }
    }
}