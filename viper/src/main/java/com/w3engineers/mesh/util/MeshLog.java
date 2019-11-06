package com.w3engineers.mesh.util;

import android.content.Intent;
import android.util.Log;

import com.w3engineers.mesh.util.lib.mesh.DataManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MeshLog {

    private static String TAG = "MeshLog";

    public static final String INFO = "(I)";
    public static final String WARNING = "(W)";
    public static final String ERROR = "(E)";
    public static final String SPECIAL = "(S)";
    public static final String PAYMENT = "(P)";

    private static String addTimeWithType(String type, String msg) {
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        return type.concat(" ").concat(currentTime).concat(": ").concat(msg);
    }

    public static void clearLog() {
        writeText("", false);
    }


    public static void p(String msg) {
        String m = addTimeWithType(PAYMENT, msg);
        e(TAG, m);
        writeText(m, true);
    }

    public static void o(String msg) {
        p(msg);
//        e(TAG, msg);
//        writeText(msg, true);
    }

    public static void k(String msg) {
        String m = addTimeWithType(SPECIAL, msg);
        e(TAG, m);
        writeText(m, true);
    }

    public static void v(String msg) {
        String m = addTimeWithType(SPECIAL, msg);
        v(TAG, m);
        writeText(m, true);
    }

    public static void mm(String msg) {
        String m = addTimeWithType(SPECIAL, msg);

        e(TAG, m);
        writeText(m, true);
    }

    public static void i(String msg) {
        String m = addTimeWithType(INFO, msg);
        i(TAG, m);
        writeText(m, true);
    }


    public static void e(String msg) {
        String m = addTimeWithType(ERROR, msg);
        e(TAG, m);
        writeText(m, true);
    }

    public static void w(String msg) {
        String m = addTimeWithType(PAYMENT, msg);
        w(TAG, m);
        writeText(m, true);
    }


    private static void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    private static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    private static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }


    private static void writeText(String text, boolean isAppend) {

        Intent intent = new Intent("com.w3engineers.meshrnd.DEBUG_MESSAGE");
        intent.putExtra("value", text);
        MeshApp.getContext().sendBroadcast(intent);

        DataManager.on().writeLogIntoTxtFile(text, isAppend);
    }
}
