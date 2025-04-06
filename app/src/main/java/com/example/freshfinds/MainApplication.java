package com.example.freshfinds;

import android.app.Application;
import android.util.Log;

public class MainApplication extends Application {
    private static final String TAG = "MainApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize data persistence
        DataPersistenceManager.init(this);
        Log.d(TAG, "DataPersistenceManager initialized");
    }
}
