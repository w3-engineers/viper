package com.w3engineers.eth.util.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Uses: For all type of toast showing
 * Created by : Monir Zzaman.
 */
public class ToastUtil {
    public static void showLong(Context context, String txt) {
        HandlerUtil.postForeground(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, txt, Toast.LENGTH_LONG).show();
            }
        });

    }

    public static void showShort(Context context, String txt) {
        HandlerUtil.postForeground(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, txt, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static void showLog(String tag, String message){
        Log.e(tag, " :: "+message);
    }
}