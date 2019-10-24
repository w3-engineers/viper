package com.w3engineers.mesh.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/*
*  ****************************************************************************
*  * Created by : Md. Azizul Islam on 12/13/2017 at 6:37 PM.
*  * Email : azizul@w3engineers.com
*  * 
*  * Last edited by : Md. Azizul Islam on 12/13/2017.
*  * 
*  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>  
*  ****************************************************************************
*/
public class HandlerUtil {
    private static final long defaultDelay = 250L;
    private static Handler foreGroundHandler;
     private static Handler backGroundHandler;
    private static HandlerThread handlerThread;

    private HandlerUtil() {
    }

    private static Handler handler() {
        if (foreGroundHandler == null) {
            foreGroundHandler = new Handler(Looper.getMainLooper());
        }
        return foreGroundHandler;
    }

    public static void removeForeGround(Runnable runnable) {
        handler().removeCallbacks(runnable);
    }

    public static void postForeground(Runnable runnable, long timeDelay) {
       handler().postDelayed(runnable, timeDelay);
    }


    public static void postForeground(Runnable runnable) {
        //postForeground(runnable, defaultDelay);
        handler().post(runnable);
    }

    public static void postRunnable(Runnable runnable) {
        handler().post(runnable);
    }

    public static void postBackground(Runnable runnable) {
        removeBackground(runnable);
        backGroundHandler.post(runnable);
    }

    public static void postBackground(Runnable runnable, long delay) {
        removeBackground(runnable);
        backGroundHandler.postDelayed(runnable, delay);
    }

    public static void removeBackground(Runnable runnable) {
        resolveHandler();
        backGroundHandler.removeCallbacks(runnable);
    }

    private static void resolveHandler() {
        if (handlerThread == null) {
            handlerThread = new HandlerThread("Backend_thread", Thread.MAX_PRIORITY);
            handlerThread.start();
        }
        if (backGroundHandler == null) {
            backGroundHandler = new Handler(handlerThread.getLooper());
        }
    }
}

