package com.w3engineers.mesh.util.lib;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.lang.reflect.Method;

import java8.util.function.BiConsumer;

public class ConnectivityUtil {

    public static void isMobileDataEnable(Context context, BiConsumer<String, Boolean> listener) {
        new Thread(() -> {
            try {
                TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");
                if (null != getMobileDataEnabledMethod) {
                    boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);

                    if (mobileDataEnabled) {
                        final String command = "ping -c 1 google.com";
                        boolean isSuccess = Runtime.getRuntime().exec(command).waitFor() == 0;
                        listener.accept("Internet is available", isSuccess);
                    } else {
                        listener.accept("Mobile data is not enable", mobileDataEnabled);
                    }
                    return;
                }
            } catch (Exception ex) {
                listener.accept("Exception occurred ", false);
                return;
            }
            listener.accept("Not connected", false);
        }).start();


    }

    public static void isInternetAvailable(BiConsumer<String, Boolean> listener) {
        new Thread(() -> {
            try {
                final String command = "ping -c 1 google.com";
                boolean isSuccess = Runtime.getRuntime().exec(command).waitFor() == 0;
                listener.accept(isSuccess ? "Internet available" : "Internet not available", isSuccess);
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            listener.accept("Internet not available", false);
        }).start();
    }
}