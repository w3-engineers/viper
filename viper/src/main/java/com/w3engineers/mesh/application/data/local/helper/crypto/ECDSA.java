package com.w3engineers.mesh.application.data.local.helper.crypto;

import android.util.Log;

import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;

public class ECDSA {
    public static ECDomainParameters CURVE = new ECDomainParameters(
            Sign.CURVE_PARAMS.getCurve(), Sign.CURVE_PARAMS.getG(), Sign.CURVE_PARAMS.getN(), Sign.CURVE_PARAMS.getH());

    public static KeyPair generateECKeys() {
        try {
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    "ECDH", "BC");

            keyPairGenerator.initialize(ecGenParameterSpec);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            return keyPair;
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | NoSuchProviderException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PublicKey getPublicKey(byte[] pk) throws NoSuchAlgorithmException, InvalidKeySpecException {
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pk);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PublicKey publicKey = kf.generatePublic(publicKeySpec);
        return publicKey;
    }

    public static PrivateKey getPrivateKey(byte[] privk) throws NoSuchAlgorithmException, InvalidKeySpecException {
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privk);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PrivateKey privateKey = kf.generatePrivate(privateKeySpec);
        return privateKey;
    }

    public static ECPoint getECPointFromPrivateKey(BigInteger privateKey) {
        return Sign.publicPointFromPrivate(privateKey);
    }

    public static ECPoint getECPointFromPrivateKey(String hexPrivateKey) {
        return Sign.publicPointFromPrivate(Numeric.toBigInt(hexPrivateKey));
    }

    public static ECPoint getECPointFromEncoded(ECCurve curve, String hexEncodedPoint) {
        Log.d("GAMIRUDDIN", "getECPointFromEncoded " + hexEncodedPoint);
        byte[] arr = Numeric.hexStringToByteArray(hexEncodedPoint);
        Log.d("GAMIRUDDIN", "arr " + arr.toString());

        ECPoint p = curve.decodePoint(arr);

        Log.d("GAMIRUDDIN", "p " + p);


        return p;
    }

    public static String getHexEncodedPoint(ECPoint ecPoint) {
        return Numeric.toHexString(ecPoint.getEncoded(false));
    }

    public static String getHexEncodedPoint(String hexPrivateKey) {
        ECPoint ecPoint = getECPointFromPrivateKey(Numeric.toBigInt(hexPrivateKey));
        return getHexEncodedPoint(ecPoint);
    }

    public static BigInteger getPublicKeyFromECPoint(ECPoint ecPoint) {
        return Sign.publicFromPoint(ecPoint.getEncoded(false));
    }

    public static BigInteger getPublicKeyFromHexEncodedPoint(ECCurve curve, String hexEncodedPoint) {
        ECPoint myPoint = getECPointFromEncoded(curve, hexEncodedPoint);
        return getPublicKeyFromECPoint(myPoint);
    }

    public static SecretKey generateSharedSecret(PrivateKey privateKey,
                                                 PublicKey publicKey) {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "BC");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);

            SecretKey key = keyAgreement.generateSecret("AES");
            return key;
        } catch (InvalidKeyException | NoSuchAlgorithmException
                | NoSuchProviderException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BigInteger generateBasicSharedSecret(String hexPrivateKey, String hexOtherPartyEncodedPoint) {
        ECPoint myPoint = getECPointFromPrivateKey(hexPrivateKey);
        ECPoint otherPoint = getECPointFromEncoded(myPoint.getCurve(), hexOtherPartyEncodedPoint);

        Log.d("GAMIRUDDIN", "BigInteger generateBasicSharedSecret otherpoint is generated ");

        ECDHBasicAgreement agreement = new ECDHBasicAgreement();
        agreement.init(new ECPrivateKeyParameters(Numeric.toBigInt(hexPrivateKey), CURVE));
        BigInteger sharedSecret = agreement.calculateAgreement(new ECPublicKeyParameters(otherPoint, CURVE));

        return sharedSecret;
    }
}
