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
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterPage extends AppCompatActivity {

    private static final String TAG = "RegisterPage";

    private TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginNowText;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth; // Firebase Authentication

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(getApplicationContext(), HomePage.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);
        Log.d(TAG, "onCreate started");

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();


        emailEditText = findViewById(R.id.registerEmail);
        passwordEditText = findViewById(R.id.registerPassword);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        registerButton = findViewById(R.id.registerButton);
        loginNowText = findViewById(R.id.loginNow);
        progressBar = findViewById(R.id.registerProgressBar);



        // Register button
        registerButton.setOnClickListener(v -> {
            Log.d(TAG, "Register button clicked");

            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

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
            if (password.length() < 6) {
                passwordEditText.setError("Password must be at least 6 characters");
                return;
            }
            if (!password.equals(confirmPassword)) {
                confirmPasswordEditText.setError("Passwords do not match");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            registerButton.setEnabled(false);

            // Register user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        registerButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterPage.this, "Registration successful!", Toast.LENGTH_SHORT).show();


                            // Navigate to LoginPage or HomeActivity
                            startActivity(new Intent(RegisterPage.this, LoginPage.class));
                            finish();
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(RegisterPage.this, "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();

                        }
                    });
        });

        // Navigate to LoginPage
        loginNowText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterPage.this, LoginPage.class));
        });
    }
}
