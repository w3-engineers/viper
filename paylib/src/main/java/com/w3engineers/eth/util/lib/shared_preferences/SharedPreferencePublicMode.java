package com.w3engineers.eth.util.lib.shared_preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Monir Zzaman on 10/30/2017.
 * Purpose : To store data into shared preference in public mode :
 * like read data outside of this application
 */

public class SharedPreferencePublicMode {
    private static SharedPreferencePublicMode sInstance;
    private SharedPreferences mPublicPreference;

     public SharedPreferencePublicMode (Context mContext){
          mPublicPreference = getDefaultSharedPreferences(mContext);
  }

    /**
     * This is the custom implementation of  getDefaultSharedPreferences() API
     * to give public access mode
     * @param mContext
     * @return
     */
    public static SharedPreferences getDefaultSharedPreferences(Context mContext) {
        return mContext.getSharedPreferences(getDefaultSharedPreferencesName(mContext),
                getDefaultSharedPreferencesMode());
    }

    /**
     * This will return apps package name pending with "_preferences_public"
     * @param mContext
     * @return
     */
    private static String getDefaultSharedPreferencesName(Context mContext) {
        return mContext.getPackageName() + "_preferences_public";
    }

    /**
     * To access data from ouside of these application
     * @return
     */
    private static int getDefaultSharedPreferencesMode() {
        return Context.MODE_WORLD_READABLE;
    }

    synchronized protected void writeString(String key, String value){
        SharedPreferences.Editor editor = mPublicPreference.edit();
        editor.putString(key, value);
        editor.commit();
    }

    synchronized protected String readString(String key, String defaultValue){
        String value = mPublicPreference.getString(key, defaultValue);
        return value;
    }

    synchronized protected void writeLong(String key, String value){
        SharedPreferences.Editor editor = mPublicPreference.edit();
        editor.putString(key, value);
        editor.commit();
    }

    synchronized protected long readLong(String key, long defaultValue){
        long value = mPublicPreference.getLong(key, defaultValue);
        return value;
    }

    synchronized protected void writeInt(String key, String value){
        SharedPreferences.Editor editor = mPublicPreference.edit();
        editor.putString(key, value);
        editor.commit();
    }

    synchronized protected int readInt(String key, int defaultValue){
        int value = mPublicPreference.getInt(key, defaultValue);
        return value;
    }

    synchronized protected void writeFloat(String key, String value){
        SharedPreferences.Editor editor = mPublicPreference.edit();
        editor.putString(key, value);
        editor.commit();
    }

    synchronized protected float readFloat(String key, float defaultValue){
        float value = mPublicPreference.getFloat(key, defaultValue);
        return value;
    }

    synchronized protected void writeBoolean(String key, String value){
        SharedPreferences.Editor editor = mPublicPreference.edit();
        editor.putString(key, value);
        editor.commit();
    }

    synchronized protected boolean readBoolean(String key, boolean defaultValue){
        boolean value = mPublicPreference.getBoolean(key, defaultValue);
        return value;
    }
}
