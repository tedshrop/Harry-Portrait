package com.example.fireseamlesslooper.usb

import android.content.Context
import android.content.SharedPreferences

class UsbUriStore(context: Context) {

    companion object {
        private const val PREFS_NAME = "usb_storage_prefs"
        private const val KEY_USB_URI = "usb_tree_uri"
        private const val TAG = "USB_SAF_DEBUG"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Save the persisted USB tree URI string
     */
    fun save(uriString: String) {
        prefs.edit().putString(KEY_USB_URI, uriString).apply()
        android.util.Log.d(TAG, "Saved USB URI: $uriString")
    }

    /**
     * Retrieve the persisted USB tree URI string
     * Returns null if no URI is stored
     */
    fun get(): String? {
        return prefs.getString(KEY_USB_URI, null).also { uri ->
            android.util.Log.d(TAG, "Retrieved USB URI: $uri")
        }
    }

    /**
     * Clear the stored USB URI
     */
    fun clear() {
        prefs.edit().remove(KEY_USB_URI).apply()
        android.util.Log.d(TAG, "Cleared USB URI from storage")
    }

    /**
     * Check if a USB URI is currently stored
     */
    fun hasStoredUri(): Boolean {
        return get() != null
    }
}
