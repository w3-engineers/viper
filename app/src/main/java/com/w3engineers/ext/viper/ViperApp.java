package com.w3engineers.ext.viper;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.w3engineers.mesh.util.MeshApp;
import com.w3engineers.mesh.util.ObjectBox;

public class ViperApp extends MeshApp {
    private static ViperApp sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        ObjectBox.init(this);
    }
    public static Context getContext() {

        if (sContext != null) {
            return sContext;
        }
        return null;
    }
}