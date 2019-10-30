package com.w3engineers.mesh.application.data.local.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/*
 * ****************************************************************************
 * * Copyright © 2018 W3 Engineers Ltd., All rights reserved.
 * *
 * * Created by:
 * * Name : SUDIPTA KUMAR PAIK
 * * Date : 2/1/18
 * * Email : sudipta@w3engineers.com
 * *
 * * Purpose : SharedPref for all type of SharedPreferences functionality
 * *
 * * Last Edited by :  Md. Nomanur Rashid on 5 May 2018.
 * *
 * * Last Reviewed by :
 * ****************************************************************************
 */

public class SharedPref {

    private final static String PREFERENCE_NAME = "mesh_rnd";
    private static SharedPreferences preferences;
    private static SharedPref sharedPref;

    private SharedPref() {
    }

    public SharedPref(Context mContext) {
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public static SharedPref on(Context context) {
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        if (sharedPref == null) {
            sharedPref = new SharedPref();
        }
        return sharedPref;
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