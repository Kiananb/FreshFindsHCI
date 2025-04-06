package com.example.freshfinds;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends BaseActivity {

    private static final String TAG = "CheckoutActivity";
    private static final String PREFS_NAME = "UserProfile";
    private static final double TAX_RATE = 0.08; // 8% sales tax
    private static final double SHIPPING_FEE = 4.99;

    private RadioGroup deliveryOptionsGroup, paymentMethodGroup;
    private RadioButton deliveryRadio, pickupRadio, cardRadio, cashRadio;
    private LinearLayout deliveryAddressLayout, pickupInfoLayout, cardDetailsLayout;
    private LinearLayout shippingLayout;
    private TextView subtotalView, taxView, shippingView, totalView;
    private Button placeOrderButton;
    private EditText nameField, phoneField, addressField, cityField, zipField;
    private Spinner provinceSpinner;
    private EditText cardNumberField, cardExpiryField, cardCVVField;
    private CheckBox saveDetailsCheckbox;
    private Button useProfileAddressBtn, useProfilePaymentBtn;
    private SharedPreferences userPrefs;
    
    // Province codes for Canada
    private static final String[] PROVINCES = {
            "Select Province", "AB", "BC", "MB", "NB", "NL", 
            "NS", "NT", "NU", "ON", "PE", "QC", "SK", "YT"
    };
    
    private double subtotal = 0.0;
    private double tax = 0.0;
    private double shipping = 0.0;
    private double total = 0.0;
    private boolean isDelivery = true;
    private boolean hasSavedAddress = false;
    private boolean hasSavedPayment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        
        // Get user preferences with saved profile data
        userPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize UI components
        deliveryOptionsGroup = findViewById(R.id.radioGroupDelivery);
        paymentMethodGroup = findViewById(R.id.radioGroupPayment);
        deliveryRadio = findViewById(R.id.radioDelivery);
        pickupRadio = findViewById(R.id.radioPickup);
        cardRadio = findViewById(R.id.radioCard);
        cashRadio = findViewById(R.id.radioCash);
        deliveryAddressLayout = findViewById(R.id.deliveryAddressLayout);
        pickupInfoLayout = findViewById(R.id.pickupInfoLayout);
        cardDetailsLayout = findViewById(R.id.cardDetailsLayout);
        shippingLayout = findViewById(R.id.shippingLayout);
        subtotalView = findViewById(R.id.textViewSubtotal);
        taxView = findViewById(R.id.textViewTax);
        shippingView = findViewById(R.id.textViewShipping);
        totalView = findViewById(R.id.textViewTotal);
        placeOrderButton = findViewById(R.id.buttonPlaceOrder);
        saveDetailsCheckbox = findViewById(R.id.checkboxSaveDetails);
        
        nameField = findViewById(R.id.editTextName);
        phoneField = findViewById(R.id.editTextPhone);
        addressField = findViewById(R.id.editTextAddress);
        cityField = findViewById(R.id.editTextCity);
        provinceSpinner = findViewById(R.id.spinnerProvince);
        zipField = findViewById(R.id.editTextZip);
        cardNumberField = findViewById(R.id.editTextCardNumber);
        cardExpiryField = findViewById(R.id.editTextCardExpiry);
        cardCVVField = findViewById(R.id.editTextCardCVV);
        
        useProfileAddressBtn = findViewById(R.id.buttonUseProfileAddress);
        useProfilePaymentBtn = findViewById(R.id.buttonUseProfilePayment);
        
        ImageButton backButton = findViewById(R.id.buttonBack);
        
        // Set up province spinner
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, PROVINCES);
        provinceSpinner.setAdapter(provinceAdapter);
        
        // Auto-populate name and phone from profile if available (no button required)
        String savedName = userPrefs.getString("name", "");
        String savedPhone = userPrefs.getString("phone", "");
        
        if (!savedName.isEmpty()) {
            nameField.setText(savedName);
        }
        
        if (!savedPhone.isEmpty()) {
            phoneField.setText(savedPhone);
        }
        
        // Set up postal code formatting (convert to uppercase automatically)
        zipField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (!input.equals(input.toUpperCase())) {
                    // Set uppercase text without triggering the watcher
                    zipField.removeTextChangedListener(this);
                    
                    // Convert to uppercase
                    String uppercaseText = input.toUpperCase();
                    zipField.setText(uppercaseText);
                    
                    // Set cursor position at the end
                    zipField.setSelection(uppercaseText.length());
                    
                    zipField.addTextChangedListener(this);
                }
            }
        });
        
        // Check if user has saved details
        checkForSavedDetails();

        // Get cart subtotal
        subtotal = CartManager.getInstance().getCartTotal();

        // Setup initial order calculations
        calculateOrderSummary();
        updateOrderSummaryViews();

        // Set up delivery option radio group listener
        deliveryOptionsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioDelivery) {
                    // Show delivery address fields, hide pickup info
                    deliveryAddressLayout.setVisibility(View.VISIBLE);
                    pickupInfoLayout.setVisibility(View.GONE);
                    
                    // Enable card payments only for delivery
                    cardRadio.setChecked(true);
                    cashRadio.setEnabled(false);
                    cardDetailsLayout.setVisibility(View.VISIBLE);
                    
                    // Add shipping fee for delivery
                    isDelivery = true;
                    calculateOrderSummary();
                    updateOrderSummaryViews();
                    
                    // Show "use profile address" button if available
                    useProfileAddressBtn.setVisibility(hasSavedAddress ? View.VISIBLE : View.GONE);
                } else if (checkedId == R.id.radioPickup) {
                    // Hide delivery address fields, show pickup info
                    deliveryAddressLayout.setVisibility(View.GONE);
                    pickupInfoLayout.setVisibility(View.VISIBLE);
                    
                    // Enable both card and cash payment options for pickup
                    cashRadio.setEnabled(true);
                    
                    // No shipping fee for pickup
                    isDelivery = false;
                    calculateOrderSummary();
                    updateOrderSummaryViews();
                    
                    // Hide "use profile address" button for pickup
                    useProfileAddressBtn.setVisibility(View.GONE);
                }
            }
        });

        // Set up payment method radio group listener
        paymentMethodGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioCard) {
                    // Show card details fields
                    cardDetailsLayout.setVisibility(View.VISIBLE);
                    // Show "use profile payment" button if available
                    useProfilePaymentBtn.setVisibility(hasSavedPayment ? View.VISIBLE : View.GONE);
                } else if (checkedId == R.id.radioCash) {
                    // Hide card details fields
                    cardDetailsLayout.setVisibility(View.GONE);
                    useProfilePaymentBtn.setVisibility(View.GONE);
                }
            }
        });
        
        // Set up "use profile address" button
        useProfileAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillAddressFromProfile();
            }
        });
        
        // Set up "use profile payment" button
        useProfilePaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillPaymentFromProfile();
            }
        });

        // Set up back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Place order button
        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateOrderForm()) {
                    // Save details if checkbox is checked
                    if (saveDetailsCheckbox.isChecked()) {
                        saveDetailsToProfile();
                    }
                    processOrder();
                }
            }
        });
    }
    
    private void checkForSavedDetails() {
        // Check if user has saved address
        String address = userPrefs.getString("address", "");
        String city = userPrefs.getString("city", "");
        String province = userPrefs.getString("province", "");
        String zip = userPrefs.getString("zip", "");
        
        hasSavedAddress = !address.isEmpty() && !city.isEmpty() && !province.isEmpty() && !zip.isEmpty();
        
        // Check if user has saved payment details
        String cardNumber = userPrefs.getString("cardNumber", "");
        String cardExpiry = userPrefs.getString("cardExpiry", "");
        String cardCvv = userPrefs.getString("cardCvv", "");
        
        hasSavedPayment = !cardNumber.isEmpty() && !cardExpiry.isEmpty() && !cardCvv.isEmpty();
        
        // Show the "use profile address" button if there is a saved address and delivery is selected
        // The visibility for address is ONLY dependent on having an address saved
        useProfileAddressBtn.setVisibility(hasSavedAddress && isDelivery ? View.VISIBLE : View.GONE);
        
        // Show the "use profile payment" button if there is saved payment info and card payment is selected
        useProfilePaymentBtn.setVisibility(hasSavedPayment && cardRadio.isChecked() ? View.VISIBLE : View.GONE);
    }
    
    private void fillAddressFromProfile() {
        String address = userPrefs.getString("address", "");
        String city = userPrefs.getString("city", "");
        String province = userPrefs.getString("province", "");
        String zip = userPrefs.getString("zip", "");
        
        addressField.setText(address);
        cityField.setText(city);
        zipField.setText(zip);
        
        // Set province in spinner
        if (!province.isEmpty()) {
            for (int i = 0; i < PROVINCES.length; i++) {
                if (PROVINCES[i].equals(province)) {
                    provinceSpinner.setSelection(i);
                    break;
                }
            }
        }
    }
    
    private void fillPaymentFromProfile() {
        String cardNumber = userPrefs.getString("cardNumber", "");
        String cardExpiry = userPrefs.getString("cardExpiry", "");
        
        // Mask the card number except last 4 digits
        if (cardNumber.length() > 4) {
            String lastFour = cardNumber.substring(cardNumber.length() - 4);
            cardNumberField.setText("**** **** **** " + lastFour);
        } else {
            cardNumberField.setText(cardNumber);
        }
        
        cardExpiryField.setText(cardExpiry);
        cardCVVField.setText("***");
    }
    
    private void saveDetailsToProfile() {
        SharedPreferences.Editor editor = userPrefs.edit();
        
        // Always save name and phone
        editor.putString("name", nameField.getText().toString());
        editor.putString("phone", phoneField.getText().toString());
        
        // Save address if in delivery mode
        if (isDelivery) {
            editor.putString("address", addressField.getText().toString());
            editor.putString("city", cityField.getText().toString());
            editor.putString("province", provinceSpinner.getSelectedItemPosition() > 0 ?
                    provinceSpinner.getSelectedItem().toString() : "");
            editor.putString("zip", zipField.getText().toString());
        }
        
        // Save payment info if using card
        if (cardRadio.isChecked()) {
            String cardNumber = cardNumberField.getText().toString();
            
            // Only save if it's not already masked
            if (!cardNumber.startsWith("*")) {
                editor.putString("cardNumber", cardNumber);
            }
            
            editor.putString("cardExpiry", cardExpiryField.getText().toString());
            
            // Only save CVV if not masked
            String cardCvv = cardCVVField.getText().toString();
            if (!cardCvv.contains("*")) {
                editor.putString("cardCvv", cardCvv);
            }
        }
        
        editor.apply();
    }

    private void calculateOrderSummary() {
        // Calculate tax
        tax = subtotal * TAX_RATE;
        
        // Apply shipping only for delivery orders
        shipping = isDelivery ? SHIPPING_FEE : 0;
        
        // Calculate total
        total = subtotal + tax + shipping;
    }

    private void updateOrderSummaryViews() {
        DecimalFormat df = new DecimalFormat("$#,##0.00");
        
        subtotalView.setText(df.format(subtotal));
        taxView.setText(df.format(tax));
        shippingView.setText(df.format(shipping));
        totalView.setText(df.format(total));
        
        // Show/hide shipping line
        shippingLayout.setVisibility(isDelivery ? View.VISIBLE : View.GONE);
    }

    private boolean validateOrderForm() {
        boolean valid = true;
        
        // Validate name and phone fields
        if (nameField.getText().toString().trim().isEmpty()) {
            nameField.setError("Name is required");
            valid = false;
        }
        
        if (phoneField.getText().toString().trim().isEmpty()) {
            phoneField.setError("Phone number is required");
            valid = false;
        }

        // Validate delivery address if delivery is selected
        if (isDelivery) {
            if (addressField.getText().toString().trim().isEmpty()) {
                addressField.setError("Address is required");
                valid = false;
            }
            if (cityField.getText().toString().trim().isEmpty()) {
                cityField.setError("City is required");
                valid = false;
            }
            if (provinceSpinner.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select a province", Toast.LENGTH_SHORT).show();
                valid = false;
            }
            if (zipField.getText().toString().trim().isEmpty()) {
                zipField.setError("Postal code is required");
                valid = false;
            }
        }

        // Validate card details if card payment is selected
        if (cardRadio.isChecked()) {
            String cardNumber = cardNumberField.getText().toString().trim();
            if (cardNumber.isEmpty()) {
                cardNumberField.setError("Card number is required");
                valid = false;
            } else if (!cardNumber.startsWith("*") && cardNumber.length() < 15) {
                cardNumberField.setError("Invalid card number");
                valid = false;
            }
            
            if (cardExpiryField.getText().toString().trim().isEmpty()) {
                cardExpiryField.setError("Expiry date is required");
                valid = false;
            }
            
            if (cardCVVField.getText().toString().trim().isEmpty()) {
                cardCVVField.setError("CVV is required");
                valid = false;
            }
        }

        return valid;
    }

    private void processOrder() {
        try {
            // Save a copy of the cart items before clearing the cart
            List<CartManager.CartItem> orderItems = new ArrayList<>(CartManager.getInstance().getAllCartItems());
            
            // Get delivery address if applicable
            String deliveryAddress = null;
            if (isDelivery) {
                // Construct full address string
                String address = addressField.getText().toString();
                String city = cityField.getText().toString();
                String province = provinceSpinner.getSelectedItemPosition() > 0 ?
                        provinceSpinner.getSelectedItem().toString() : "";
                String zip = zipField.getText().toString();
                
                deliveryAddress = address + ", " + city + ", " + province + " " + zip;
            }
            
            // Clear the cart
            CartManager.getInstance().clearCart();
            
            // Show success message
            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
            
            // Navigate to order confirmation
            Intent intent = new Intent(this, OrderConfirmationActivity.class);
            intent.putExtra("order_total", total);
            intent.putExtra("subtotal", subtotal);
            intent.putExtra("tax", tax);
            intent.putExtra("shipping", shipping);
            intent.putExtra("is_delivery", isDelivery);
            intent.putExtra("delivery_address", deliveryAddress);
            startActivity(intent);
            
            // Close checkout and cart activities
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error processing order: " + e.getMessage());
            Toast.makeText(this, "There was a problem processing your order. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
