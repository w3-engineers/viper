package com.w3engineers.eth.util.lib.shared_preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Monir Zzaman on 10/30/2017.
 * Purpose : To store data into shared preference in private mode :
 * like read/write data only from inside of this application
 */

public class SharedPreferencePrivateMode {
    private final static String PREFERENCE_NAME = "pay_lib";
    private SharedPreferences mPrivatePreference;

    public SharedPreferencePrivateMode(Context mContext) {
        mPrivatePreference = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }

    synchronized protected boolean writeString(String key, String value) {
        SharedPreferences.Editor editor = mPrivatePreference.edit();
        editor.putString(key, value);
        boolean isWritten = editor.commit();
        return isWritten;
    }

    synchronized protected String readString(String key, String defaultValue) {
        String value = mPrivatePreference.getString(key, defaultValue);
        return value;
    }

    synchronized protected void writeLong(String key, long value) {
        SharedPreferences.Editor editor = mPrivatePreference.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    synchronized protected long readLong(String key, long defaultValue) {
        long value = mPrivatePreference.getLong(key, defaultValue);
        return value;
    }

    synchronized protected void writeInt(String key, int value) {
        SharedPreferences.Editor editor = mPrivatePreference.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    synchronized protected int readInt(String key, int defaultValue) {
        int value = mPrivatePreference.getInt(key, defaultValue);
        return value;
    }

    synchronized protected void writeFloat(String key, float value) {
        SharedPreferences.Editor editor = mPrivatePreference.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    synchronized protected float readFloat(String key, float defaultValue) {
        float value = mPrivatePreference.getFloat(key, defaultValue);
        return value;
    }

    synchronized protected void writeBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = mPrivatePreference.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    synchronized protected boolean readBoolean(String key, boolean defaultValue) {
        boolean value = mPrivatePreference.getBoolean(key, defaultValue);
        return value;
    }

    synchronized protected void remove(String key) {
        SharedPreferences.Editor editor = mPrivatePreference.edit();
        editor.remove(key);
        editor.commit();
    }


}
