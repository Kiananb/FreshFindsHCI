package com.example.freshfinds;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//test comment
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {

    private static final String TAG = "LoginPage"; // Log tag for debugging
    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerNowText;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth; // Firebase Authentication instance

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
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

        // Initialize UI elements
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        registerNowText = findViewById(R.id.registerNow);
        progressBar = findViewById(R.id.progressBar);

        // Login button click listener
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

                // Authenticate user with Firebase Authentication
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            loginButton.setEnabled(true);

                            if (task.isSuccessful()) {
                                Toast.makeText(LoginPage.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Login success: " + email);
                                Intent intent = new Intent(LoginPage.this, HomePage.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginPage.this, "Authentication failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Login failed: " + task.getException().getMessage());
                            }
                        });
            }
        });

        // Navigate to RegisterPage when "Register Now" is clicked
        registerNowText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Navigating to RegisterPage");
                Intent intent = new Intent(LoginPage.this, RegisterPage.class);
                startActivity(intent);
            }
        });
    }
}
