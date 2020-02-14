package com.w3engineers.mesh.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class AppCredentials {
    @Nullable
    private static AppCredentials appCredentials;

    private AppCredentials() {
    }

    @NonNull
    public static AppCredentials getInstance() {
        if (appCredentials == null) {
            appCredentials = new AppCredentials();
        }
        return appCredentials;
    }

    public native String getBroadCastToken();

    public native String getBroadCastUrl();

    public native String getParseUrl();

    public native String getParseAppId();

    public native String getAuthUserName();

    public native String getAuthPassword();

    public native String getFileRepoLink();

    public native String getSignalServerUrl();

    public native String getConfiguration();


    static {
        System.loadLibrary("staging-native-lib");
    }

}
