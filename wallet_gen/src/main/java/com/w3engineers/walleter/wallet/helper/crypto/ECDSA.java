package com.w3engineers.walleter.wallet.helper.crypto;

import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class ECDSA {
    public static ECDomainParameters CURVE = new ECDomainParameters(
            Sign.CURVE_PARAMS.getCurve(), Sign.CURVE_PARAMS.getG(), Sign.CURVE_PARAMS.getN(), Sign.CURVE_PARAMS.getH());

    public static ECPoint getECPointFromPrivateKey(BigInteger privateKey) {
        return Sign.publicPointFromPrivate(privateKey);
    }

    public static ECPoint getECPointFromPrivateKey(String hexPrivateKey) {
        return Sign.publicPointFromPrivate(Numeric.toBigInt(hexPrivateKey));
    }

    public static ECPoint getECPointFromEncoded(ECCurve curve, String hexEncodedPoint) {
        byte[] arr = Numeric.hexStringToByteArray(hexEncodedPoint);
        ECPoint p = curve.decodePoint(arr);
        return p;
    }

    public static String getHexEncodedPoint(ECPoint ecPoint) {
        return Numeric.toHexString(ecPoint.getEncoded(false));
    }

    public static String getHexEncodedPoint(String hexPrivateKey) {
        ECPoint ecPoint = getECPointFromPrivateKey(Numeric.toBigInt(hexPrivateKey));
        return getHexEncodedPoint(ecPoint);
    }



    public static BigInteger generateBasicSharedSecret(String hexPrivateKey, String hexOtherPartyEncodedPoint) {
        ECPoint myPoint = getECPointFromPrivateKey(hexPrivateKey);
        ECPoint otherPoint = getECPointFromEncoded(myPoint.getCurve(), hexOtherPartyEncodedPoint);

        ECDHBasicAgreement agreement = new ECDHBasicAgreement();
        agreement.init(new ECPrivateKeyParameters(Numeric.toBigInt(hexPrivateKey), CURVE));
        BigInteger sharedSecret = agreement.calculateAgreement(new ECPublicKeyParameters(otherPoint, CURVE));

        return sharedSecret;
    }
}
