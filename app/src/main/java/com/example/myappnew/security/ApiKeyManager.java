package com.example.myappnew.security;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class ApiKeyManager {

    private static final String PREFERENCE_FILE_NAME = "api_key_prefs";
    private static final String KEY_GEMINI_API_KEY = "gemini_api_key"; // Example key name

    private SharedPreferences sharedPreferences;

    public ApiKeyManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFERENCE_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Handle exceptions appropriately in a real app
            // For example, log the error, disable API key functionality, or inform the user
            System.err.println("Error initializing EncryptedSharedPreferences: " + e.getMessage());
            e.printStackTrace();
            // Fallback to regular SharedPreferences or disable functionality if security is paramount
            // For this example, we'll let it proceed, but 'sharedPreferences' might be null
        }
    }

    /**
     * Saves the API key securely.
     * For now, it saves a generic Gemini API key. This can be expanded for multiple providers.
     *
     * @param apiKey The API key to save.
     */
    public void saveApiKey(String apiKey) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_GEMINI_API_KEY, apiKey).apply();
        } else {
            System.err.println("ApiKeyManager: SharedPreferences not initialized. Cannot save API key.");
        }
    }

    /**
     * Retrieves the saved API key.
     * For now, it retrieves a generic Gemini API key.
     *
     * @return The saved API key, or null if not found or if an error occurred.
     */
    public String getApiKey() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_GEMINI_API_KEY, null);
        }
        System.err.println("ApiKeyManager: SharedPreferences not initialized. Cannot get API key.");
        return null;
    }

    /**
     * Clears the saved API key.
     */
    public void clearApiKey() {
        if (sharedPreferences != null) {
            sharedPreferences.edit().remove(KEY_GEMINI_API_KEY).apply();
        } else {
            System.err.println("ApiKeyManager: SharedPreferences not initialized. Cannot clear API key.");
        }
    }
}
