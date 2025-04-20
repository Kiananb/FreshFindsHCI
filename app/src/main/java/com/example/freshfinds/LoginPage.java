package com.example.freshfinds;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {

    private static final String TAG = "LoginPage";
    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerNowText;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth; // Firebase Authentication

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Save the email in shared preferences to maintain state after logout
            SharedPreferences loginPrefs = getSharedPreferences("LoginSession", MODE_PRIVATE);
            loginPrefs.edit().putString("loggedInEmail", currentUser.getEmail()).apply();
            
            // Navigate to HomePage
            Intent intent = new Intent(getApplicationContext(), HomePage.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        Log.d(TAG, "onCreate started");

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();


        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        registerNowText = findViewById(R.id.registerNow);
        progressBar = findViewById(R.id.progressBar);

        // Login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Login button clicked");
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Input validation
                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("Email is required");
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Enter a valid email");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("Password is required");
                    return;
                }

                Log.d(TAG, "Validation passed: email=" + email + ", password=" + password);
                progressBar.setVisibility(View.VISIBLE);
                loginButton.setEnabled(false);

                // Authenticate user with Firebase
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            loginButton.setEnabled(true);

                            if (task.isSuccessful()) {
                                onLoginSuccess(email);
                            } else {
                                Toast.makeText(LoginPage.this, "Authentication failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Login failed: " + task.getException().getMessage());
                            }
                        });
            }
        });

        // Navigate to RegisterPage
        registerNowText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Navigating to RegisterPage");
                Intent intent = new Intent(LoginPage.this, RegisterPage.class);
                startActivity(intent);
            }
        });
    }

    private void onLoginSuccess(String email) {
        // Save user session
        SharedPreferences loginPrefs = getSharedPreferences("LoginSession", MODE_PRIVATE);
        loginPrefs.edit().putString("loggedInEmail", email).apply();
        
        // Reload all user data from storage
        reloadUserData();
        
        // Navigate to home page
        Toast.makeText(LoginPage.this, "Login successful!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Login success: " + email);
        
        Intent intent = new Intent(LoginPage.this, HomePage.class);
        intent.putExtra("user_email", email);
        startActivity(intent);
        finish();
    }

    private void reloadUserData() {
        // Load all user data
        CartManager.getInstance().reloadData();
        OrderManager.getInstance().reloadData();
        MessageStorage.getInstance().reloadData();
        Product.reloadFavorites();
        Log.d(TAG, "User data reloaded after login");
    }

    private boolean isValidCredentials(String email, String password) {
        // For demo purposes, any non-empty email with '@' and any password of at least 6 chars
        return email.contains("@") && password.length() >= 6;
    }
}
