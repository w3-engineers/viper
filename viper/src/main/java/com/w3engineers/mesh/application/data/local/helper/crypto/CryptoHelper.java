package com.w3engineers.mesh.application.data.local.helper.crypto;



import android.content.Context;

import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.util.MeshLog;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.web3j.crypto.ECKeyPair;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Provider;
import java.security.Security;

import javax.crypto.SecretKey;

public class CryptoHelper {


   /* public static void setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            // Web3j will set up the provider lazily when it's first used.
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return;
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    public static String encrypt(String privateKey, String otherPartyPoint, byte[] plainText) {
        BigInteger sharedSecret = ECDSA.generateBasicSharedSecret(privateKey, otherPartyPoint);
        String secretKey = Numeric.toHexStringNoPrefix(sharedSecret);
        MeshLog.v("sendPayMessage: secretKey: " + secretKey);
        return AES.encryptString(secretKey, plainText);
    }

    public static String decrypt(String privateKey, String otherPartyPoint, String cipherText) {
        BigInteger sharedSecret = ECDSA.generateBasicSharedSecret(privateKey, otherPartyPoint);
        String secretKey = Numeric.toHexStringNoPrefix(sharedSecret);
        MeshLog.v("onMessageReceived: secretKey: " + secretKey);
        MeshLog.v("onMessageReceived: cipherText: " + cipherText);
        return AES.decryptString(secretKey, cipherText);
    }

    public static void testECDSA() {
        String plainText = "Look mah, I'm a message!";
        System.out.println("Original plaintext message: " + plainText);

        // Initialize two key pairs
        KeyPair keyPairA = ECDSA.generateECKeys();
        KeyPair keyPairB = ECDSA.generateECKeys();

        // Create two AES secret keys to encrypt/decrypt the message
        SecretKey secretKeyA = ECDSA.generateSharedSecret(keyPairA.getPrivate(),
                keyPairB.getPublic());
        SecretKey secretKeyB = ECDSA.generateSharedSecret(keyPairB.getPrivate(),
                keyPairA.getPublic());

        // Encrypt the message using 'secretKeyA'
        // String cipherText = AES.encryptString(secretKeyA, plainText);
        String cipherText = AES.encryptString(secretKeyA, plainText.getBytes());
        System.out.println("Encrypted cipher text: " + cipherText);

        // Decrypt the message using 'secretKeyB'
        String decryptedPlainText = AES.decryptString(secretKeyB, cipherText);
        System.out.println("Decrypted cipher text: " + decryptedPlainText);
    }

    public static void testBCEC() {
        String plainText = "Look mah, I'm a message!";
        System.out.println("Original plaintext message: " + plainText);

        KeyPair keyPair = ECDSA.generateECKeys();
        ECKeyPair ecKeyPair = ECKeyPair.create(keyPair);

        String hexPrivateKeyA = Numeric.toHexStringWithPrefix(Numeric.toBigInt(SharedPref.read("private_key")));
        String hexPrivateKeyB = Numeric.toHexStringWithPrefix(ecKeyPair.getPrivateKey());

        ECPoint pointA = ECDSA.getECPointFromPrivateKey(hexPrivateKeyA);
        ECPoint pointB = ECDSA.getECPointFromPrivateKey(hexPrivateKeyB);

        String hexPointA = ECDSA.getHexEncodedPoint(pointA);
        String hexPointB = ECDSA.getHexEncodedPoint(pointB);
        System.out.println("hexPointA=" + hexPointA);
        System.out.println("hexPointB=" + hexPointB);

        BigInteger sharedSecretA = ECDSA.generateBasicSharedSecret(hexPrivateKeyA, hexPointB);
        System.out.println("sharedSecretE=" + Numeric.toHexStringWithPrefix(sharedSecretA));

        BigInteger sharedSecretB = ECDSA.generateBasicSharedSecret(hexPrivateKeyB, hexPointA);
        System.out.println("sharedSecretD=" + Numeric.toHexStringWithPrefix(sharedSecretB));

        // Encrypt the message using 'secretKeyA'
        // String cipherText = AES.encryptString(Numeric.toHexStringNoPrefix(sharedSecretA), plainText);
        String cipherText = AES.encryptString(Numeric.toHexStringNoPrefix(sharedSecretA), plainText.getBytes());
        System.out.println("Encrypted cipher text: " + cipherText);

        // Decrypt the message using 'secretKeyB'
        String decryptedPlainText = AES.decryptString(Numeric.toHexStringNoPrefix(sharedSecretB), cipherText);
        System.out.println("Decrypted cipher text: " + decryptedPlainText);
    }*/

    public static String encrypt(String privateKey, String otherPartyPoint, byte[] plainText) {
        BigInteger sharedSecret = ECDSA.generateBasicSharedSecret(privateKey, otherPartyPoint);
        String secretKey = sharedSecret.toString(16);
        MeshLog.v("sendPayMessage: secretKey: " + secretKey);

        String encoded = EncryptUtil.encrypt(secretKey, plainText);
        return encoded;
    }

    public static String decrypt(String privateKey, String otherPartyPoint, String cipherText) {
        BigInteger sharedSecret = ECDSA.generateBasicSharedSecret(privateKey, otherPartyPoint);

        String secretKey = sharedSecret.toString(16);
        MeshLog.v("onMessageReceived: secretKey: " + secretKey);

        String decrypted = EncryptUtil.decrypt(secretKey, cipherText);
        return decrypted;
    }

    public static String decryptMessage(String myPrivateKey, String othersPublicKey, String message) {
        return CryptoHelper.decrypt(myPrivateKey, othersPublicKey, message);
    }

    public static String encryptMessage(String myPrivateKey, String othersPublicKey, String message) {
        return CryptoHelper.encrypt(myPrivateKey, othersPublicKey, message.getBytes());
    }
}
