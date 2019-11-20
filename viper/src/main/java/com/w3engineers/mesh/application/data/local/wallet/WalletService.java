package com.w3engineers.mesh.application.data.local.wallet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;


import com.w3engineers.eth.util.helper.HandlerUtil;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.local.helper.crypto.ECDSA;
import com.w3engineers.mesh.util.Constant;
import com.w3engineers.mesh.util.MeshLog;

import org.web3j.crypto.Credentials;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class WalletService {
    private Context mContext;
    private final String walletSuffixDir = "wallet/";
//    public static final String PASSWORD = "123456789";
    private volatile Credentials mCredentials;
    private final String WALLET_ADDRESS = "wallet_address";
    private final String WALLET_FILE_NAME = "wallet_name";
    private final String PUBLIC_KEY = "public_key";
    private final String PRIVATE_KEY = "private_key";
    private final String WALLET_PASSWORD_KEY = "wallet_pass_key";
    private static WalletService walletService;


    public interface WalletLoadListener {
        void onWalletLoaded(String walletAddress, String publicKey);

        void onErrorOccurred(String message);
    }

    public interface WalletCreateListener {
        void onWalletCreated(String walletAddress, String publicKey);

        void onError(String message);
    }

    public interface WalletImportListener {
        void onWalletImported(String walletAddress, String publicKey);

        void onError(String message);
    }


    private WalletService(Context context) {
        this.mContext = context;
    }

    public static WalletService getInstance(Context context) {
        if (walletService == null) {
            walletService = new WalletService(context);
        }

        return walletService;
    }

    public boolean isWalletExists() {
        boolean isWalletExists = false;

        String filePath = Web3jWalletHelper.onInstance(mContext).getWalletDir(walletSuffixDir);
        File directory = new File(filePath);

        File[] list = directory.listFiles();
        if (list != null) {
            for (File f : list) {
                String name = f.getName();
                if (name.endsWith(".json")) {
                    SharedPref.write(WALLET_FILE_NAME, name);
                    isWalletExists = true;
                    break;
                }
            }
        }

        return isWalletExists;
    }

    public void createOrLoadWallet(final String password, final WalletLoadListener listener) {
        SharedPref.write(WALLET_PASSWORD_KEY, password);
        HandlerUtil.postBackground(() -> {
            String existAddress = SharedPref.read(WALLET_ADDRESS);
            if (!TextUtils.isEmpty(existAddress)) {
                MeshLog.i(" Ethereum address already exist");
                listener.onWalletLoaded(existAddress, SharedPref.read(PUBLIC_KEY));
                initCredential();
                return;
            }

            if (isWalletExists()) {
                String keyStoreFileName = SharedPref.read(WALLET_FILE_NAME);
                loadWalletFromKeystore(password, keyStoreFileName);

                if (mCredentials != null) {

                    listener.onWalletLoaded(mCredentials.getAddress(), SharedPref.read(PUBLIC_KEY));

                } else {
                    listener.onErrorOccurred("WalletManager credential load failed");
                }
            } else {
                String keyStoreFileName = Web3jWalletHelper.onInstance(mContext).createWallet(password, walletSuffixDir);

                if (keyStoreFileName != null) {

                    loadWalletFromKeystore(password, keyStoreFileName);

                    if (mCredentials != null) {

                        listener.onWalletLoaded(mCredentials.getAddress(), SharedPref.read(PUBLIC_KEY));


                    } else {

                        listener.onErrorOccurred("WalletManager credential load failed");
                    }
                } else {

                    listener.onErrorOccurred("WalletManager file generate failed");
                }
            }
        });
    }


    public void createWallet(String password, WalletCreateListener listener) {
        if (isWalletExists()) {
            deleteExistsWallet();
        }
        SharedPref.write(WALLET_PASSWORD_KEY, password);

        String keyStoreFileName = Web3jWalletHelper.onInstance(mContext).createWallet(password, walletSuffixDir);

        if (keyStoreFileName != null) {

            loadWalletFromKeystore(password, keyStoreFileName);

            if (mCredentials != null) {
                listener.onWalletCreated(mCredentials.getAddress(), SharedPref.read(PUBLIC_KEY));
            } else {
                listener.onError("Wallet creation failed");
            }
        } else {

            listener.onError("File creation failed");
        }
    }

    public void loadWallet(String password, WalletLoadListener listener) {
        if (isWalletExists()) {
            SharedPref.write(WALLET_PASSWORD_KEY, password);
            String keyStoreFileName = SharedPref.read(WALLET_FILE_NAME);
            loadWalletFromKeystore(password, keyStoreFileName);

            if (mCredentials != null) {
                listener.onWalletLoaded(mCredentials.getAddress(), SharedPref.read(PUBLIC_KEY));
            } else {
                listener.onErrorOccurred("Wallet load failed");
            }
        } else {
            listener.onErrorOccurred("Wallet is not exist");
        }
    }

    public void importWallet(String password, Uri fileUri, WalletImportListener listener) {
        if (isWalletExists()) {
            // delete wallet file
            deleteExistsWallet();
        }

        HandlerUtil.postBackground(new Runnable() {
            @Override
            public void run() {
                String savePtah = WalletService.getInstance(mContext).getWalletDirectory();
                copyFile(mContext, fileUri, savePtah);

                if (isWalletExists()) {
                    SharedPref.write(WALLET_PASSWORD_KEY, password);
                    String keyStoreFileName = SharedPref.read(WALLET_FILE_NAME);
                    loadWalletFromKeystore(password, keyStoreFileName);

                    if (mCredentials != null) {
                        listener.onWalletImported(mCredentials.getAddress(), SharedPref.read(PUBLIC_KEY));
                    } else {
                        listener.onError("Wallet import failed");
                    }
                } else {
                    listener.onError("Wallet import failed");
                }
            }
        });

    }

    /**
     * This is used to copy the content of file
     *
     * @param mConText
     * @param source
     * @param destination
     */
    private void copyFile(Context mConText, Uri source, String destination) {
        try {
            InputStream inputStream = mConText.getContentResolver().openInputStream(source);
            if (inputStream != null) {
                String ret = "";
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();

                File mainFile = new File(source.getPath());

                File outputDir = new File(destination);
                if (!outputDir.exists()) {
                    outputDir.mkdir();
                }
                File outputFile = File.createTempFile(mainFile.getName(), ".json", outputDir);

                FileOutputStream fos = new FileOutputStream(outputFile);
                fos.write(ret.getBytes());
                fos.close();
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public boolean deleteExistsWallet() {
        boolean isWalletExists = false;

        String filePath = Web3jWalletHelper.onInstance(mContext).getWalletDir(walletSuffixDir);
        File directory = new File(filePath);

        File[] list = directory.listFiles();
        if (list != null) {
            for (File f : list) {
                f.delete();
            }
        }

        return isWalletExists;
    }


    private void loadWalletFromKeystore(String password, String keyStoreFileName) {
        mCredentials = Web3jWalletHelper.onInstance(mContext).getWallet(password, walletSuffixDir, keyStoreFileName);
        if (mCredentials != null){
            SharedPref.write(WALLET_ADDRESS, mCredentials.getAddress());
            SharedPref.write(Constant.KEY_USER_ID, mCredentials.getAddress());
            //SharedPref.write(PUBLIC_KEY, mCredentials.getEcKeyPair().getPublicKey().toString(16));
            SharedPref.write(Constant.KEY_USER_ID, mCredentials.getAddress());
            SharedPref.write(PRIVATE_KEY, mCredentials.getEcKeyPair().getPrivateKey().toString(16));
            SharedPref.write(PUBLIC_KEY, ECDSA.getHexEncodedPoint(SharedPref.read(PRIVATE_KEY)));

            MeshLog.e("publickey::" + ECDSA.getHexEncodedPoint(SharedPref.read(PRIVATE_KEY)) + "\n" + "Length " + ECDSA.getHexEncodedPoint(SharedPref.read(PRIVATE_KEY)).length());
        }
    }

    public Credentials getCredentials() {
        if (mCredentials == null) {
            String privateKey = SharedPref.read(PRIVATE_KEY);
            if (TextUtils.isEmpty(privateKey)) {
                String keyStoreFileNmae = SharedPref.read(WALLET_FILE_NAME);
                String password = SharedPref.read(WALLET_PASSWORD_KEY);

                loadWalletFromKeystore(password, keyStoreFileNmae);
            } else {
                mCredentials = Credentials.create(privateKey);
            }
        }
        return mCredentials;
    }

    private void initCredential() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getCredentials();
            }
        }).start();

    }

    public String getPrivateKey() {
        String privateKey = SharedPref.read(PRIVATE_KEY);
        return privateKey;
    }

    public String getWalletFilePath() {
        String filePath = Web3jWalletHelper.onInstance(mContext).getWalletDir(walletSuffixDir);
        File directory = new File(filePath);

        File[] list = directory.listFiles();
        if (list != null) {
            for (File f : list) {
                String name = f.getName();
                if (name.endsWith(".json")) {
                    return f.getAbsolutePath();
                }
            }
        }
        return null;
    }

    public String getWalletDirectory() {
        String filePath = Web3jWalletHelper.onInstance(mContext).getWalletDir(walletSuffixDir);
        return filePath;
    }

}
