package com.w3engineers.mesh.application.data.local.wallet;

import android.content.Context;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Credentials;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.Provider;
import java.security.Security;

/**
 * Created by USER09 on 10/30/2017.
 */

public class Web3jWalletHelper extends Web3jWallet {

    public Web3jWalletHelper(Context context) {
        super(context);
    }

    private static Web3jWalletHelper sInstance;

    synchronized public static Web3jWalletHelper onInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Web3jWalletHelper(context);
        }
        return sInstance;
    }

    private void setupBouncyCastle() {
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

    public String createWallet(String password, String keystoreFolderName) {
        setupBouncyCastle();
        String keyStoreFileName = createKeystoreFile(password, keystoreFolderName);
        return keyStoreFileName;
    }


    public Credentials getWallet(String password, String keyStoreFolderName, String keyStoreFileName) {
        Credentials credentials = loadKeystoreFile(password, keyStoreFolderName, keyStoreFileName);
        return credentials;
    }

    public String getWalletDir(String dir) {
        String walletDir = getKeystoreDir(dir);
        return walletDir;
    }
/*
    public String getWalletFullPath(String keyStoreFolderName, String keyStoreFileName) {
        String walletFullPath = getKeystorePath(keyStoreFolderName, keyStoreFileName);
        return walletFullPath;
    }*/

    public String readWalletFile(String filePath) {
        String jsonStr = null;
        try {
            File yourFile = new File(filePath);
            FileInputStream stream = new FileInputStream(yourFile);

            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                jsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonStr;
    }

    /*public String getKeyStoreFilePath(String keyStoreFolderName, String keyStoreFileName) {
       // String filePath = getKeystoreDir(keyStoreFolderName);
        String keyStorePath = getKeystorePath(keyStoreFolderName, keyStoreFileName);

        return keyStorePath;
    }*/

}
