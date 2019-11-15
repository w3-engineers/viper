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
