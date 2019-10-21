package com.w3engineers.eth.util.lib.qrcode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;


/**
 * Created by HERA on 11/8/17.
 * Purpose: QR code encode decode.
 * Copyright © W3 Engineers Ltd.
 */

public class QRFactory {

    /**
     * Extract string from a QR code bitmap
     * @param bitmap
     * @return string value
     */
    public static String bitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    /**
     * Encode a QR code image from plain text
     * @param encodedString
     * @return bitmap
     */
    public static Bitmap stringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
}
