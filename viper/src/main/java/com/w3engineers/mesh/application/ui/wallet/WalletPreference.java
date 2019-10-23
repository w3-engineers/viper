package com.w3engineers.mesh.application.ui.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class WalletPreference {
    private final static String PREFERENCE_NAME = "my_wallet";
    public static String LATEST_UPDATE = "latest_update";
    private static SharedPreferences preferences;
    private static WalletPreference walletPreference;

    private WalletPreference(Context context){
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }

    public static WalletPreference on(Context context) {
        if (walletPreference == null) {
            walletPreference = new WalletPreference(context);
        }
        return walletPreference;
    }

    public static boolean write(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static boolean write(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(key, value);

        return editor.commit();
    }

    public static boolean write(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public static boolean write(String key, long value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        return editor.commit();
    }


    public static String read(String key) {
        return preferences.getString(key, "");
    }

    public static String read(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public static long readLong(String key) {
        return preferences.getLong(key, 0l);
    }

    public static int readInt(String key) {
        return readInt(key, 0);
    }

    public static int readInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public static boolean readBoolean(String key) {
        return readBoolean(key, false);
    }

    public static boolean readBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public static boolean readBooleanWithDefaultValue(String key, boolean value) {
        return preferences.getBoolean(key, value);
    }

    public boolean readSettingsBoolean(String key) {
        return preferences.getBoolean(key, true);
    }

    public static boolean readBooleanDefaultTrue(String key) {
        return preferences.getBoolean(key, true);
    }

    public static boolean contains(String key) {
        return preferences.contains(key);
    }

    /**
     * Remove all saved shared Preference data of app.
     */
    public static void removeAllPreferenceData() {
        preferences.edit().clear().apply();
    }

    public static void removeSpecificItem(String key) {
        preferences.edit().remove(key).apply();
    }
}
