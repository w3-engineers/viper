package com.w3engineers.mesh.application.data.local.helper.crypto;


import android.util.Base64;

import com.w3engineers.mesh.util.MeshLog;

//import org.apache.commons.net.util.Base64;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtil {



    private static SecretKeySpec getKey(String secretKey){
        MessageDigest md = null;
        String base64 = "";
        try {
            md = MessageDigest.getInstance("SHA-512");
            md.update(secretKey.getBytes());
            byte byteData[] = md.digest();
            base64 = Base64.encodeToString(byteData, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {

            return null;
        }

        byte[] key = base64.getBytes();;
        key = Arrays.copyOf(key, 16);

        MeshLog.v("key " + key);
        SecretKeySpec eSpec = new SecretKeySpec(key, "AES");
        return eSpec;
    }


    public static String encrypt(String secretKey, byte[] value) {

        Cipher ecipher;
        try {
            ecipher = Cipher.getInstance("AES");
            SecretKeySpec eSpec = getKey(secretKey);
            ecipher.init(Cipher.ENCRYPT_MODE, eSpec);
            byte[] encryptedValue;

            encryptedValue = ecipher.doFinal(value);
            return bytesToHex(encryptedValue);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    public static String decrypt(String secretKey, String encryptedValue) {
        Cipher dcipher;
        try {
            dcipher = Cipher.getInstance("AES");
            SecretKeySpec dSpec = getKey(secretKey);
            dcipher.init(Cipher.DECRYPT_MODE, dSpec);
            byte[] decryptedValue = hexToBytes(encryptedValue);
            byte[] decValue;

            decValue = dcipher.doFinal(decryptedValue);
            return new String(decValue);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String bytesToHex(byte[] data, int length) {
        String digits = "0123456789ABCDEF";
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i != length; i++) {
            int v = data[i] & 0xff;

            buffer.append(digits.charAt(v >> 4));
            buffer.append(digits.charAt(v & 0xf));
        }

        return buffer.toString();
    }

    public static String bytesToHex(byte[] data) {
        return bytesToHex(data, data.length);
    }

    public static byte[] hexToBytes(String string) {
        int length = string.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4) + Character
                    .digit(string.charAt(i + 1), 16));
        }
        return data;
    }
}
