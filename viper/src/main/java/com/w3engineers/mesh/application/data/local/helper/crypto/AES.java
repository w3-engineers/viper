package com.w3engineers.mesh.application.data.local.helper.crypto;

import android.os.Build;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    private static String iv = "0x9246EFD920020F731AD9CD8D6C7D7D7B";
    private static SecretKeySpec secretKey;
    private static byte[] key;

    public static byte[] getIV() {
        //return new SecureRandom().generateSeed(16);
        return hexToBytes(iv);
    }

    public static void setKey(String myKey) {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("EncryptionTest", "Error: in setKey  " + e.getMessage());
        }
    }

    public static String encryptString(SecretKey key, byte[] strToEncrypt) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(getIV());
            Cipher cipher;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                cipher = Cipher.getInstance("AES/GCM/NoPadding");
            } else {
                cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
            }

            // byte[] plainTextBytes = strToEncrypt.getBytes("UTF-8");
            byte[] plainTextBytes = strToEncrypt;
            byte[] cipherText;

            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            cipherText = new byte[cipher.getOutputSize(plainTextBytes.length)];
            int encryptLength = cipher.update(plainTextBytes, 0,
                    plainTextBytes.length, cipherText, 0);
            encryptLength += cipher.doFinal(cipherText, encryptLength);

            return bytesToHex(cipherText);
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException
                | ShortBufferException
                | IllegalBlockSizeException | BadPaddingException e) {
            Log.e("EncryptionTest", "Error in encryptString : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String encryptString(String secret, byte[] strToEncrypt) {
        try {
            setKey(secret);
            return encryptString(secretKey, strToEncrypt);
        } catch (Exception e) {
            Log.e("EncryptionTest", "Error: " + e.getMessage());
            // System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decryptString(SecretKey key, String strToDecrypt) {
        try {
            Key decryptionKey = new SecretKeySpec(key.getEncoded(),
                    key.getAlgorithm());
            IvParameterSpec ivSpec = new IvParameterSpec(getIV());

            Cipher cipher;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                cipher = Cipher.getInstance("AES/GCM/NoPadding");
            } else {
                cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
            }

            byte[] cipherTextBytes = hexToBytes(strToDecrypt);
            byte[] plainText;

            cipher.init(Cipher.DECRYPT_MODE, decryptionKey, ivSpec);
            plainText = new byte[cipher.getOutputSize(cipherTextBytes.length)];
            int decryptLength = cipher.update(cipherTextBytes, 0,
                    cipherTextBytes.length, plainText, 0);
            decryptLength += cipher.doFinal(plainText, decryptLength);

            return new String(plainText, "UTF-8");
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException
                | ShortBufferException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decryptString(String secret, String strToDecrypt) {
        try {
            setKey(secret);
            return decryptString(secretKey, strToDecrypt);
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
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
