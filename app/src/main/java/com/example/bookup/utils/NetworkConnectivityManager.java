package com.example.bookup.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * NetworkConnectivityManager - Monitors device network connectivity state
 * 
 * Provides real-time network status monitoring to detect when device goes
 * offline or comes back online. Allows UI to show offline indicator and
 * queue operations for sync when connection restored.
 * 
 * Usage:
 * NetworkConnectivityManager manager = new NetworkConnectivityManager(context);
 * manager.startMonitoring(new OnNetworkStateChanged() {
 *     public void onOnline() { showOnlineUI(); }
 *     public void onOffline() { showOfflineUI(); }
 * });
 * 
 * // Later:
 * manager.stopMonitoring();
 * 
 * @author Senior Developer
 * @version 1.0
 */
public class NetworkConnectivityManager {
    
    private static final String TAG = "NetworkConnectivity";
    
    private Context context;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private OnNetworkStateChanged stateChangeListener;
    private boolean isCurrentlyOnline = true;
    
    /**
     * Callback interface for network state changes
     */
    @FunctionalInterface
    public interface OnNetworkStateChanged {
        /**
         * Called when network state changes
         * 
         * @param isConnected true if online, false if offline
         * @param status "online", "offline", "wifi", "cellular", or "unknown"
         */
        void onStateChanged(boolean isConnected, String status);
    }
    
    /**
     * Constructor
     * 
     * @param context Application context for connectivity manager
     */
    public NetworkConnectivityManager(Context context) {
        this.context = context;
        this.connectivityManager = (ConnectivityManager) 
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        // Check initial connectivity state
        this.isCurrentlyOnline = isNetworkAvailable();
    }
    
    /**
     * Start monitoring network connectivity
     * 
     * Registers callback to detect network changes. Calls listener immediately
     * with current state.
     * 
     * @param listener Callback for state changes
     */
    public void startMonitoring(OnNetworkStateChanged listener) {
        this.stateChangeListener = listener;
        
        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager not available");
            return;
        }
        
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                Log.d(TAG, "Network available");
                if (!isCurrentlyOnline) {
                    isCurrentlyOnline = true;
                    if (stateChangeListener != null) {
                        stateChangeListener.onStateChanged(true, getNetworkStatus());
                    }
                }
            }
            
            @Override
            public void onLost(@NonNull Network network) {
                Log.d(TAG, "Network lost");
                if (isNetworkAvailable()) {
                    // Other networks still available
                    return;
                }
                
                if (isCurrentlyOnline) {
                    isCurrentlyOnline = false;
                    if (stateChangeListener != null) {
                        stateChangeListener.onStateChanged(false, getNetworkStatus());
                    }
                }
            }
            
            @Override
            public void onCapabilitiesChanged(@NonNull Network network, 
                                            @NonNull NetworkCapabilities capabilities) {
                // Network capabilities changed but we're still online
                Log.d(TAG, "Network capabilities changed");
            }
        };
        
        // Register the callback
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
        
        // Notify listener of current state
        if (stateChangeListener != null) {
            stateChangeListener.onStateChanged(isCurrentlyOnline, getNetworkStatus());
        }
    }
    
    /**
     * Stop monitoring network connectivity
     * 
     * Should be called in onStop() or onDestroy() to prevent memory leaks
     */
    public void stopMonitoring() {
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
                Log.d(TAG, "Unregistered network callback");
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Callback not registered or already unregistered", e);
            }
        }
        stateChangeListener = null;
    }
    
    /**
     * Check if network is currently available
     * 
     * @return true if device has network connectivity, false otherwise
     */
    public boolean isNetworkAvailable() {
        if (connectivityManager == null) {
            return false;
        }
        
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        }
        
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) {
            return false;
        }
        
        // Check for common network types
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
    }
    
    /**
     * Check if WiFi is available
     * 
     * @return true if WiFi connected, false otherwise
     */
    public boolean isWiFiAvailable() {
        if (connectivityManager == null) {
            return false;
        }
        
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        }
        
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) {
            return false;
        }
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }
    
    /**
     * Check if cellular data is available
     * 
     * @return true if cellular connected, false otherwise
     */
    public boolean isCellularAvailable() {
        if (connectivityManager == null) {
            return false;
        }
        
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        }
        
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) {
            return false;
        }
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
    }
    
    /**
     * Get current network status
     * 
     * @return "online", "offline", "wifi", "cellular", or "unknown"
     */
    public String getNetworkStatus() {
        if (!isNetworkAvailable()) {
            return "offline";
        }
        
        if (isWiFiAvailable()) {
            return "wifi";
        }
        
        if (isCellularAvailable()) {
            return "cellular";
        }
        
        return "online";
    }
    
    /**
     * Check if currently in offline mode
     * 
     * @return true if offline, false if online
     */
    public boolean isOffline() {
        return !isCurrentlyOnline;
    }
    
    /**
     * Check if currently in online mode
     * 
     * @return true if online, false if offline
     */
    public boolean isOnline() {
        return isCurrentlyOnline;
    }
}
