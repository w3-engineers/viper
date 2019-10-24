package com.w3engineers.mesh.util;

import android.content.Context;

import com.w3engineers.mesh.model.MyObjectBox;

import io.objectbox.BoxStore;

/**
 * This class represents the entry for ObjectBox which is used for local data persistent management
 */
public class ObjectBox {
    private static BoxStore sBoxStore;

    public static void init(Context context) {
        sBoxStore = MyObjectBox.builder()
                .androidContext(context.getApplicationContext())
                .build();
   /*    if (BuildConfig.DEBUG) {
           boolean started = new AndroidObjectBrowser(sBoxStore).start(context);
       }*/
    }

    public static BoxStore get() {
        return sBoxStore;
    }
}
