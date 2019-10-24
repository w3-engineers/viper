package com.w3engineers.mesh.application.ui.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.w3engineers.mesh.util.MeshApp;

public class WalletPreference {
    private final static String PREFERENCE_NAME = "my_wallet";
    public static String LATEST_UPDATE = "latest_update";
    private static SharedPreferences preferences;
    private static WalletPreference walletPreference;

    private WalletPreference(){
        Context context = MeshApp.getContext();
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }

    public static WalletPreference on() {
        if (walletPreference == null) {
            walletPreference = new WalletPreference();
        }
        return walletPreference;
    }

    public boolean write(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }




    public String read(String key) {
        return preferences.getString(key, "");
    }

    public String read(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public boolean readBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }


}
