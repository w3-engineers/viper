package com.w3engineers.mesh.application.data.local.wallet;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;

import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.db.networkinfo.NetworkInfo;
import com.w3engineers.mesh.application.data.local.db.networkinfo.WalletInfo;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManager;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerBuyer;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerSeller;
import com.w3engineers.mesh.application.ui.wallet.WalletActivity;
import com.w3engineers.mesh.util.Util;

import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Flowable;


public class Wallet {
    private static Wallet wallet;
    private PreferencesHelperDataplan preferencesHelperDataplan;

    public static void openActivity(Context context){
        Intent intent = new Intent(context, WalletActivity.class);
        context.startActivity(intent);
    }

    public static Wallet getInstance(){
        if (wallet == null){
            wallet = new Wallet();
        }
        return wallet;
    }

    private Wallet(){
        preferencesHelperDataplan = PreferencesHelperDataplan.on();
    }


    public boolean giftEther(){
        if (preferencesHelperDataplan.getDataShareMode() == DataPlanConstants.USER_TYPES.DATA_BUYER) {
            return PurchaseManagerBuyer.getInstance().giftEtherForOtherNetwork();
        } else if (preferencesHelperDataplan.getDataShareMode() == DataPlanConstants.USER_TYPES.DATA_SELLER){
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

        if (preferencesHelperDataplan.getDataShareMode() == DataPlanConstants.USER_TYPES.MESH_USER) {
            listener.onBalanceInfo(false, "This feature is available only for data seller and data buyer and internet user.");
        } else if (preferencesHelperDataplan.getDataShareMode() == DataPlanConstants.USER_TYPES.DATA_SELLER || preferencesHelperDataplan.getDataShareMode() == DataPlanConstants.USER_TYPES.INTERNET_USER) {

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
        } else if (preferencesHelperDataplan.getDataShareMode() == DataPlanConstants.USER_TYPES.DATA_BUYER){
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

        if (preferencesHelperDataplan.getDataShareMode() == DataPlanConstants.USER_TYPES.DATA_SELLER) {

            PurchaseManagerSeller.getInstance().sendEtherRequest((success, msg) ->{
              etherRequestListener.onEtherRequestResponse(success, msg);
            });
        } else if (preferencesHelperDataplan.getDataShareMode() == DataPlanConstants.USER_TYPES.DATA_BUYER){

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

        if (preferencesHelperDataplan.getDataShareMode() == DataPlanConstants.USER_TYPES.DATA_SELLER) {
            PurchaseManagerSeller.getInstance().sendTokenRequest((success, msg, tokenValue, etherValue) ->{
                tokenRequestListener.onTokenRequestResponse(success, msg);
            });
        } else if (preferencesHelperDataplan.getDataShareMode() == DataPlanConstants.USER_TYPES.DATA_BUYER){
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
        if (preferencesHelperDataplan.getDataShareMode() == DataPlanConstants.USER_TYPES.DATA_SELLER) {
            return PurchaseManagerSeller.getInstance().getDifferentNetworkData(myAddress, endpoint);
        } else if (PreferencesHelperDataplan.on().getDataShareMode() == DataPlanConstants.USER_TYPES.DATA_BUYER) {
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
}
