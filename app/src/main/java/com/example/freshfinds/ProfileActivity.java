package com.example.freshfinds;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends BaseActivity {

    private static final String TAG = "ProfileActivity";
    private static final String PREFS_NAME = "UserProfile";
    
    private EditText nameEditText, phoneEditText, emailEditText;
    private EditText cardNumberEditText, cardExpiryEditText, cardCvvEditText;
    private EditText addressEditText, cityEditText, zipEditText;
    private Spinner provinceSpinner;
    private Button saveButton;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences userPrefs;
    
    // Province codes for Canada
    private static final String[] PROVINCES = {
            "Select Province", "AB", "BC", "MB", "NB", "NL", 
            "NS", "NT", "NU", "ON", "PE", "QC", "SK", "YT"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Initialize SharedPreferences for profile data
        userPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Initialize UI components
        nameEditText = findViewById(R.id.editTextName);
        phoneEditText = findViewById(R.id.editTextPhone);
        emailEditText = findViewById(R.id.editTextEmail);
        
        cardNumberEditText = findViewById(R.id.editTextCardNumber);
        cardExpiryEditText = findViewById(R.id.editTextCardExpiry);
        cardCvvEditText = findViewById(R.id.editTextCardCVV);
        
        addressEditText = findViewById(R.id.editTextAddress);
        cityEditText = findViewById(R.id.editTextCity);
        provinceSpinner = findViewById(R.id.spinnerProvince);
        zipEditText = findViewById(R.id.editTextZip);
        
        saveButton = findViewById(R.id.buttonSave);    
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        
        // Set up postal code formatting (convert to uppercase automatically)
        zipEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (!input.equals(input.toUpperCase())) {
                    // Set uppercase text without triggering the watcher
                    zipEditText.removeTextChangedListener(this);
                    
                    // Convert to uppercase
                    String uppercaseText = input.toUpperCase();
                    zipEditText.setText(uppercaseText);
                    
                    // Set cursor position at the end
                    zipEditText.setSelection(uppercaseText.length());
                    
                    zipEditText.addTextChangedListener(this);
                }
            }
        });
        
        // Set up province spinner
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, PROVINCES);
        provinceSpinner.setAdapter(provinceAdapter);
        
        // Initialize logout button
        Button logoutButton = findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out from Firebase
                FirebaseAuth.getInstance().signOut();
                
                // Redirect to login page
                Intent intent = new Intent(ProfileActivity.this, LoginPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        
        // Load user data
        loadUserData();
        
        // Set click listeners
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
                Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Setup bottom navigation
        setupBottomNavigation();
    }
    
    private void loadUserData() {
        // Load saved profile data
        String name = userPrefs.getString("name", "");
        String phone = userPrefs.getString("phone", "");
        String email = userPrefs.getString("email", "");
        
        // Get email from login session if not already saved
        if (email.isEmpty()) {
            email = getIntent().getStringExtra("user_email");
            // If we got an email from the intent, save it
            if (email != null && !email.isEmpty()) {
                userPrefs.edit().putString("email", email).apply();
            } else {
                // Try to get email from shared preferences used during login
                SharedPreferences loginPrefs = getSharedPreferences("LoginSession", MODE_PRIVATE);
                email = loginPrefs.getString("loggedInEmail", "");
            }
        }
        
        // Load payment details
        String cardNumber = userPrefs.getString("cardNumber", "");
        String cardExpiry = userPrefs.getString("cardExpiry", "");
        String cardCvv = userPrefs.getString("cardCvv", "");
        
        // Mask card number if it exists
        if (!cardNumber.isEmpty() && cardNumber.length() >= 4) {
            String lastFour = cardNumber.substring(cardNumber.length() - 4);
            cardNumber = "**** **** **** " + lastFour;
        }
        
        // Load address details
        String address = userPrefs.getString("address", "");
        String city = userPrefs.getString("city", "");
        String province = userPrefs.getString("province", "");
        String zip = userPrefs.getString("zip", "");
        
        // Set UI fields
        nameEditText.setText(name);
        phoneEditText.setText(phone);
        emailEditText.setText(email);
        
        cardNumberEditText.setText(cardNumber);
        cardExpiryEditText.setText(cardExpiry);
        cardCvvEditText.setText(cardCvv.isEmpty() ? "" : "***");
        
        addressEditText.setText(address);
        cityEditText.setText(city);
        
        // Set province spinner
        if (!province.isEmpty()) {
            for (int i = 0; i < PROVINCES.length; i++) {
                if (PROVINCES[i].equals(province)) {
                    provinceSpinner.setSelection(i);
                    break;
                }
            }
        }
        
        zipEditText.setText(zip);
    }
    
    private void saveUserData() {
        // Get values from form fields
        String name = nameEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String email = emailEditText.getText().toString();
        
        String cardNumber = cardNumberEditText.getText().toString();
        String cardExpiry = cardExpiryEditText.getText().toString();
        String cardCvv = cardCvvEditText.getText().toString();
        
        String address = addressEditText.getText().toString();
        String city = cityEditText.getText().toString();
        String province = provinceSpinner.getSelectedItemPosition() > 0 ?
                provinceSpinner.getSelectedItem().toString() : "";
        String zip = zipEditText.getText().toString();
        
        // Don't overwrite card number if it's already masked
        if (cardNumber.startsWith("*")) {
            cardNumber = userPrefs.getString("cardNumber", "");
        }
        
        // Don't overwrite CVV if the user entered asterisks
        if (cardCvv.contains("*")) {
            cardCvv = userPrefs.getString("cardCvv", "");
        }
        
        // Save to SharedPreferences
        SharedPreferences.Editor editor = userPrefs.edit();
        editor.putString("name", name);
        editor.putString("phone", phone);
        editor.putString("email", email);
        
        editor.putString("cardNumber", cardNumber);
        editor.putString("cardExpiry", cardExpiry);
        editor.putString("cardCvv", cardCvv);
        
        editor.putString("address", address);
        editor.putString("city", city);
        editor.putString("province", province);
        editor.putString("zip", zip);
        
        editor.apply();
        
        // Update UI with masked card number
        if (!cardNumber.isEmpty() && cardNumber.length() >= 4) {
            String lastFour = cardNumber.substring(cardNumber.length() - 4);
            cardNumberEditText.setText("**** **** **** " + lastFour);
        }
        
        // Mask CVV field if it's not empty and not already masked
        if (!cardCvv.isEmpty() && !cardCvv.contains("*")) {
            cardCvvEditText.setText("***");
        }
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.profile); // Set profile as selected
        
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();
                try {
                    if (itemId == R.id.home) {
                        startActivity(new Intent(ProfileActivity.this, HomePage.class));
                        overridePendingTransition(0, 0); // Disable animation
                        finish();
                        return true;
                    } else if (itemId == R.id.cart) {
                        startActivity(new Intent(ProfileActivity.this, CartActivity.class));
                        overridePendingTransition(0, 0); // Disable animation
                        return true;
                    } else if (itemId == R.id.messages) {
                        startActivity(new Intent(ProfileActivity.this, MessagesActivity.class));
                        overridePendingTransition(0, 0); // Disable animation
                        return true;
                    } else if (itemId == R.id.profile) {
                        // Already on profile screen
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error: " + e.getMessage());
                    Toast.makeText(ProfileActivity.this, "Navigation failed", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
}
