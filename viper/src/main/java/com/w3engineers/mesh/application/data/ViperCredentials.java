package com.w3engineers.mesh.application.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ViperCredentials {
    @Nullable
    private static ViperCredentials viperCredentials;

    private ViperCredentials() {
    }

    @NonNull
    public static ViperCredentials getInstance() {
        if (viperCredentials == null) {
            viperCredentials = new ViperCredentials();
        }
        return viperCredentials;
    }


    public native String getConfiguration();

    static {
        System.loadLibrary("config-lib");
    }
}
