package com.w3engineers.mesh.application.data.local.wallet;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;

import com.google.zxing.WriterException;
import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.dataplan.DataPlanManager;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.local.db.networkinfo.NetworkInfo;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManager;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerBuyer;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerSeller;
import com.w3engineers.mesh.application.ui.wallet.WalletActivity;
import com.w3engineers.mesh.util.Constant;
import com.w3engineers.mesh.util.MeshLog;
import com.w3engineers.mesh.util.Util;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import io.reactivex.Flowable;


public class WalletManager {
    private static WalletManager walletManager;
    private PreferencesHelperDataplan preferencesHelperDataplan;
    private DataPlanManager dataPlanManager;
    private WalletListener walletListener;
    private ProgressDialog dialog;


    public static void openActivity(Context context, byte[] picture){
        Intent intent = new Intent(context, WalletActivity.class);
        if(picture != null) {
            intent.putExtra("picture", picture);
        }
        context.startActivity(intent);
    }




    public static WalletManager getInstance(){
        if (walletManager == null){
            walletManager = new WalletManager();
        }
        return walletManager;
    }

    public void setWalletListener(WalletListener walletListener) {
        this.walletListener = walletListener;

        if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER) {
            PurchaseManagerBuyer.getInstance().setWalletListener(walletListener);
        } else if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER) {
            PurchaseManagerSeller.getInstance().setWalletListener(walletListener);
        }
    }

    private WalletManager(){
        preferencesHelperDataplan = PreferencesHelperDataplan.on();
        dataPlanManager = DataPlanManager.getInstance();
    }


    public boolean giftEther() {

        if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER) {

            return PurchaseManagerBuyer.getInstance().giftEtherForOtherNetwork();

        } else if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER){
            return PurchaseManagerSeller.getInstance().requestForGiftForSeller();
        }
        return false;
    }

    public interface WalletListener {

        void onGiftResponse(boolean success, boolean isGifted, String message);

        void onBalanceInfo(boolean success, String msg);

        void onEtherRequestResponse(boolean success, String msg);

        void onTokenRequestResponse(boolean success, String msg);

        void onRequestSubmitted(boolean success, String msg);

        void onRequestCompleted(boolean success, String msg);
    }

    public LiveData<Double> getTotalEarn(String myAddress, int endPoint) {
        return PurchaseManagerSeller.getInstance().getTotalEarn(myAddress, endPoint);
    }
    public LiveData<Double> getTotalSpent(String myAddress, int endPoint) {
        return PurchaseManagerBuyer.getInstance().getTotalSpent(myAddress, endPoint);
    }

    public LiveData<Double> getTotalPendingEarning(String myAddress, int endPoint) {
        return PurchaseManagerSeller.getInstance().getTotalPendingEarning(myAddress, endPoint);
    }

    public String getMyAddress(){
        return PurchaseManager.getInstance().getEthService().getAddress();
    }

    public int getMyEndpoint(){
        return PurchaseManager.getInstance().getEndpoint();
    }
    public void setEndpoint(int endpoint){
        PurchaseManager.getInstance().setEndpoint(endpoint);
    }

    public void refreshMyBalance() {

        if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.MESH_USER) {

            if (walletListener != null) {
                walletListener.onBalanceInfo(false, "This feature is available only for data seller and data buyer and internet user.");
            }

        } else if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER || dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.INTERNET_USER) {

            PurchaseManagerSeller.getInstance().getMyBalanceInfo();

        } else if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER){
            PurchaseManagerBuyer.getInstance().getMyBalanceInfo();
        } else {

            if (walletListener != null) {
                walletListener.onBalanceInfo(false, "This feature is not available for you.");
            }
        }
    }

    public void sendEtherRequest() {
        PreferencesHelperDataplan preferencesHelperDataplan = PreferencesHelperDataplan.on();

        if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER) {

            PurchaseManagerSeller.getInstance().sendEtherRequest();
        } else if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER){

            PurchaseManagerBuyer.getInstance().sendEtherRequest();
        }else {

            if (walletListener != null) {
                walletListener.onEtherRequestResponse(false, "This feature is available only for data seller and data buyer.");
            }
        }
    }

    public void sendTokenRequest() {

        if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER) {

            PurchaseManagerSeller.getInstance().sendTokenRequest();

        } else if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER){

            PurchaseManagerBuyer.getInstance().sendTokenRequest();
        }else {

            if (walletListener != null) {
                walletListener.onTokenRequestResponse(false, "This feature is available only for data seller and data buyer.");
            }
        }
    }


    public static String getCurrencyTypeMessage(String message) {
        return Util.getCurrencyTypeMessage(message);
    }

    public LiveData<Integer> getDifferentNetworkData(String myAddress, int endpoint) {
        if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER) {
            return PurchaseManagerSeller.getInstance().getDifferentNetworkData(myAddress, endpoint);
        } else if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER) {
            return PurchaseManagerBuyer.getInstance().getDifferentNetworkData(myAddress, endpoint);
        }
        return null;
    }

    public void getAllOpenDrawableBlock() {
        PurchaseManagerSeller.getInstance().getAllOpenDrawableBlock();
    }

    public Flowable<List<NetworkInfo>> getNetworkInfoByNetworkType() {
        return PurchaseManager.getInstance().getNetworkInfoByNetworkType();
    }

    public interface WaletListener {
        void onWalletLoaded(String walletAddress, String publicKey);
        void onErrorOccurred(String message);
    }

    public interface WalletLoadListener {
        void onWalletLoaded(String walletAddress, String publicKey);
        void onError(String message);
    }

    public interface WalletCreateListener {
        void onWalletCreated(String walletAddress, String publicKey);
        void onError(String message);
    }

    public interface WalletImportListener {
        void onWalletImported(String walletAddress, String publicKey);
        void onError(String message);
    }

    public void readWallet(Context context, String password, WaletListener listener) {
        WalletService mWalletService =  WalletService.getInstance(context);

        mWalletService.createOrLoadWallet(password, new WalletService.WalletLoadListener() {
            @Override
            public void onWalletLoaded(String walletAddress, String publicKey) {
                MeshLog.i(" WalletManager loaded succesful");

                if (!walletAddress.equalsIgnoreCase(SharedPref.read(Constant.PreferenceKeys.ADDRESS))){

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            SharedPref.write(Constant.PreferenceKeys.ADDRESS, walletAddress);
                            QRGEncoder qrgEncoder = new QRGEncoder(walletAddress, null, QRGContents.Type.TEXT, 300);
                            try {
                                // Getting QR-Code as Bitmap
                                Bitmap bitmap = qrgEncoder.encodeAsBitmap();
                                String bitmapAddress = bitMapToString(bitmap);
                                SharedPref.write(Constant.PreferenceKeys.ADDRESS_BITMAP, bitmapAddress);

                            } catch (WriterException e) {

                            }
                        }
                    });
                }

                listener.onWalletLoaded(walletAddress, publicKey);
            }

            @Override
            public void onErrorOccurred(String message) {
                listener.onErrorOccurred(message);
                MeshLog.v("walletManager loading failed");
            }
        });
    }

    /**
     * This api is used to create wallet
     * @param context
     * @param password
     * @param listener
     */
    public void createWallet(Context context, String password, WalletCreateListener listener){
        if (TextUtils.isEmpty(password)){
            listener.onError("Password cant be empty");
        }else if (password.length() < 8){
            listener.onError("Password can't be smaller then 8 character");
        }else {
            WalletService mWalletService =  WalletService.getInstance(context);
            mWalletService.createWallet(password, new WalletService.WalletCreateListener() {
                @Override
                public void onWalletCreated(String walletAddress, String publicKey) {

                    if (!walletAddress.equalsIgnoreCase(SharedPref.read(Constant.PreferenceKeys.ADDRESS))){

                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                SharedPref.write(Constant.PreferenceKeys.ADDRESS, walletAddress);
                                QRGEncoder qrgEncoder = new QRGEncoder(walletAddress, null, QRGContents.Type.TEXT, 300);
                                try {
                                    // Getting QR-Code as Bitmap
                                    Bitmap bitmap = qrgEncoder.encodeAsBitmap();
                                    String bitmapAddress = bitMapToString(bitmap);
                                    SharedPref.write(Constant.PreferenceKeys.ADDRESS_BITMAP, bitmapAddress);

                                } catch (WriterException e) {

                                }
                            }
                        });
                    }

                    listener.onWalletCreated(walletAddress, publicKey);
                }

                @Override
                public void onError(String message) {
                  listener.onError(message);
                }
            });
        }
    }

    /**
     * This api is used to load wallet
     * @param context
     * @param password
     * @param listener
     */

    public void loadWallet(Context context, String password, WalletLoadListener listener){
        if (TextUtils.isEmpty(password)){
            listener.onError("Password cant be empty");
        }else if (password.length() < 8){
            listener.onError("Password can't be smaller then 8 character");
        }else {
            WalletService mWalletService =  WalletService.getInstance(context);

            mWalletService.loadWallet(password, new WalletService.WalletLoadListener() {
                @Override
                public void onWalletLoaded(String walletAddress, String publicKey) {

                    if (!walletAddress.equalsIgnoreCase(SharedPref.read(Constant.PreferenceKeys.ADDRESS))){

                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                SharedPref.write(Constant.PreferenceKeys.ADDRESS, walletAddress);
                                QRGEncoder qrgEncoder = new QRGEncoder(walletAddress, null, QRGContents.Type.TEXT, 300);
                                try {
                                    // Getting QR-Code as Bitmap
                                    Bitmap bitmap = qrgEncoder.encodeAsBitmap();
                                    String bitmapAddress = bitMapToString(bitmap);
                                    SharedPref.write(Constant.PreferenceKeys.ADDRESS_BITMAP, bitmapAddress);

                                } catch (WriterException e) {

                                }
                            }
                        });
                    }

                    listener.onWalletLoaded(walletAddress, publicKey);
                }
                @Override
                public void onErrorOccurred(String message) {
                    listener.onError(message);
                }
            });
        }
    }

    /**
     * This api is used to import wallet
     * @param context
     * @param password
     * @param fileUri
     * @param listener
     */
    public void importWallet(Context context, String password, Uri fileUri, WalletImportListener listener){
        if (TextUtils.isEmpty(password)){
            listener.onError("Password cant be empty");
        }else if (password.length() < 8){
            listener.onError("Password can't be smaller then 8 character");
        }else {
            WalletService mWalletService =  WalletService.getInstance(context);

            mWalletService.importWallet(password, fileUri, new WalletService.WalletImportListener() {
                @Override
                public void onWalletImported(String walletAddress, String publicKey) {
                    listener.onWalletImported(walletAddress, publicKey);

                    if (!walletAddress.equalsIgnoreCase(SharedPref.read(Constant.PreferenceKeys.ADDRESS))){

                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                SharedPref.write(Constant.PreferenceKeys.ADDRESS, walletAddress);
                                QRGEncoder qrgEncoder = new QRGEncoder(walletAddress, null, QRGContents.Type.TEXT, 300);
                                try {
                                    // Getting QR-Code as Bitmap
                                    Bitmap bitmap = qrgEncoder.encodeAsBitmap();
                                    String bitmapAddress = bitMapToString(bitmap);
                                    SharedPref.write(Constant.PreferenceKeys.ADDRESS_BITMAP, bitmapAddress);

                                } catch (WriterException e) {

                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(String message) {
                    listener.onError(message);
                }
            });
        }
    }



    private void showProgress(Context activity, boolean isNeeded) {
        if (isNeeded) {
            dialog = new ProgressDialog(activity);
            dialog.setMessage("Copying please wait...");
            dialog.show();
        } else {
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }



    /**
     * @param bitmap
     * @return converting bitmap and return a string
     */
    public String bitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
}
