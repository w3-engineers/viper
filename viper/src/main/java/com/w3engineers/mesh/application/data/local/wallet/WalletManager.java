package com.w3engineers.mesh.application.data.local.wallet;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;

import com.google.zxing.WriterException;
import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.local.dataplan.DataPlanManager;
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


    public static void openActivity(Context context){
        Intent intent = new Intent(context, WalletActivity.class);
        context.startActivity(intent);
    }

    public static WalletManager getInstance(){
        if (walletManager == null){
            walletManager = new WalletManager();
        }
        return walletManager;
    }

    private WalletManager(){
        preferencesHelperDataplan = PreferencesHelperDataplan.on();
        dataPlanManager = DataPlanManager.getInstance();
    }


    public boolean giftEther(){
        if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER) {
            return PurchaseManagerBuyer.getInstance().giftEtherForOtherNetwork();
        } else if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER){
            return PurchaseManagerSeller.getInstance().requestForGiftForSeller();
        }
        return false;
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

    public void refreshMyBalance(BalanceInfoListener listener) {

        if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.MESH_USER) {
            listener.onBalanceInfo(false, "This feature is available only for data seller and data buyer and internet user.");
        } else if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER || dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.INTERNET_USER) {

            PurchaseManagerSeller.getInstance().getMyBalanceInfo(new PurchaseManagerSeller.MyBalanceInfoListener() {
                @Override
                public void onBalanceInfoReceived(double ethBalance, double tknBalance) {
                    listener.onBalanceInfo(true, "Balance will be update soon.");
                }

                @Override
                public void onBalanceErrorReceived(String msg) {
                    listener.onBalanceInfo(false, msg);
                }
            });
        } else if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER){
            PurchaseManagerBuyer.getInstance().getMyBalanceInfo(new PurchaseManagerBuyer.MyBalanceInfoListener() {
                @Override
                public void onBalanceInfoReceived(double ethBalance, double tknBalance) {
                    listener.onBalanceInfo(true, "Balance will be update soon.");
                }

                @Override
                public void onBalanceErrorReceived(String msg) {
                    listener.onBalanceInfo(false, msg);
                }
            });
        } else {
            listener.onBalanceInfo(false, "This feature is not available for you.");
        }
    }

    public interface BalanceInfoListener{
        void onBalanceInfo(boolean success, String msg);
    }

    public void sendEtherRequest(EtherRequestListener etherRequestListener) {
        PreferencesHelperDataplan preferencesHelperDataplan = PreferencesHelperDataplan.on();

        if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER) {

            PurchaseManagerSeller.getInstance().sendEtherRequest((success, msg) ->{
              etherRequestListener.onEtherRequestResponse(success, msg);
            });
        } else if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER){

            PurchaseManagerBuyer.getInstance().sendEtherRequest((success, from, msg) -> {
                etherRequestListener.onEtherRequestResponse(success, msg);
            });
        }else {
            etherRequestListener.onEtherRequestResponse(false, "This feature is available only for data seller and data buyer.");
        }
    }
    public interface EtherRequestListener{
        void onEtherRequestResponse(boolean success, String msg);
    }

    public void sendTokenRequest(TokenRequestListener tokenRequestListener) {

        if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER) {
            PurchaseManagerSeller.getInstance().sendTokenRequest((success, msg, tokenValue, etherValue) ->{
                tokenRequestListener.onTokenRequestResponse(success, msg);
            });
        } else if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER){
            PurchaseManagerBuyer.getInstance().sendTokenRequest(new PurchaseManagerBuyer.TokenRequestListener() {
                @Override
                public void onTokenRequestResponseReceived(boolean success, String from, String msg, double tokenValue, double ethValue) {
                    tokenRequestListener.onTokenRequestResponse(success, msg);
                }
            });
        }else {
            tokenRequestListener.onTokenRequestResponse(false, "This feature is available only for data seller and data buyer.");
        }
    }

    public interface TokenRequestListener{
        void onTokenRequestResponse(boolean success, String msg);
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

    public void getAllOpenDrawableBlock(BalanceWithdrawtListener balanceWithdrawtListener) throws ExecutionException, InterruptedException {
        //TODO there is a problem in the callback, for every request a callback is fired, but it should be at the end when all process are completed.

            PurchaseManagerSeller.getInstance().getAllOpenDrawableBlock(new PurchaseManagerSeller.BalanceWithdrawtListener() {
                @Override
                public void onRequestSubmitted(boolean success, String msg) {
                    balanceWithdrawtListener.onRequestSubmitted(success, msg);
                }

                @Override
                public void onRequestCompleted(boolean success, String msg) {
                    balanceWithdrawtListener.onRequestCompleted(success, msg);
                }
            });
    }
    public interface BalanceWithdrawtListener {
        void onRequestSubmitted(boolean success, String msg);
        void onRequestCompleted(boolean success, String msg);
    }

    public Flowable<List<NetworkInfo>> getNetworkInfoByNetworkType() {
        return PurchaseManager.getInstance().getNetworkInfoByNetworkType();
    }

    public interface WaletListener {
        void onWalletLoaded(String walletAddress, String publicKey);
        void onErrorOccurred(String message);
    }

    public void readWallet(Context context, WaletListener listener) {
        WalletService mWalletService =  WalletService.getInstance(context);

        mWalletService.createOrLoadWallet(WalletService.PASSWORD, new WalletService.Listener() {
            @Override
            public void onWalletLoaded(String walletAddress, String publicKey) {
                MeshLog.i(" WalletManager loaded succesful");

                listener.onWalletLoaded(walletAddress, publicKey);

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
            public void onErrorOccurred(String message) {
                listener.onErrorOccurred(message);
                MeshLog.v("walletManager loading failed");
            }
        });
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
