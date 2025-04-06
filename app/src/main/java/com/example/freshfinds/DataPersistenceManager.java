package com.example.freshfinds;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import java.lang.reflect.Type;

/**
 * Helper class to manage data persistence across app sessions
 */
public class DataPersistenceManager {
    private static final String TAG = "DataPersistenceManager";
    private static final String PREF_NAME = "UserDataStore";
    
    private static Context appContext;
    private static Gson gson;
    
    // Initialize with application context
    public static void init(Context context) {
        appContext = context.getApplicationContext();
        gson = new Gson();
    }
    
    /**
     * Get the current user's ID for data separation
     */
    public static String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        
        // Fallback to locally stored email
        SharedPreferences loginPrefs = appContext.getSharedPreferences("LoginSession", Context.MODE_PRIVATE);
        String email = loginPrefs.getString("loggedInEmail", "");
        if (!email.isEmpty()) {
            // Use a simple hash of the email as ID
            return String.valueOf(email.hashCode());
        }
        
        return "anonymous";
    }
    
    /**
     * Save data for the current user
     */
    public static void saveData(String dataType, Object data) {
        try {
            SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            String key = getUserId() + "_" + dataType;
            String json = gson.toJson(data);
            
            editor.putString(key, json);
            editor.apply();
            
            Log.d(TAG, "Saved " + dataType + " data for user " + getUserId());
        } catch (Exception e) {
            Log.e(TAG, "Error saving " + dataType + " data: " + e.getMessage());
        }
    }
    
    /**
     * Load data for the current user (with Class parameter)
     */
    public static <T> T loadData(String dataType, Class<T> classOfT, T defaultValue) {
        try {
            SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            
            String key = getUserId() + "_" + dataType;
            String json = prefs.getString(key, "");
            
            if (json.isEmpty()) {
                return defaultValue;
            }
            
            T data = gson.fromJson(json, classOfT);
            Log.d(TAG, "Loaded " + dataType + " data for user " + getUserId());
            return data;
        } catch (Exception e) {
            Log.e(TAG, "Error loading " + dataType + " data: " + e.getMessage());
            return defaultValue;
        }
    }
    
    /**
     * Load data for the current user (with Type parameter for generic collections)
     */
    public static <T> T loadData(String dataType, Type typeOfT, T defaultValue) {
        try {
            SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            
            String key = getUserId() + "_" + dataType;
            String json = prefs.getString(key, "");
            
            if (json.isEmpty()) {
                return defaultValue;
            }
            
            T data = gson.fromJson(json, typeOfT);
            Log.d(TAG, "Loaded " + dataType + " data for user " + getUserId());
            return data;
        } catch (Exception e) {
            Log.e(TAG, "Error loading " + dataType + " data: " + e.getMessage());
            return defaultValue;
        }
    }
    
    /**
     * Clear all data for the current user
     */
    public static void clearAllUserData() {
        try {
            SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            String userId = getUserId();
            for (String key : prefs.getAll().keySet()) {
                if (key.startsWith(userId + "_")) {
                    editor.remove(key);
                }
            }
            
            editor.apply();
            Log.d(TAG, "Cleared all data for user " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error clearing user data: " + e.getMessage());
        }
    }
}
