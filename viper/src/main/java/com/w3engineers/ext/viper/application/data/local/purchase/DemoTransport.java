package com.w3engineers.ext.viper.application.data.local.purchase;

import android.text.TextUtils;

import com.w3engineers.ext.viper.application.data.local.wallet.WalletService;

import java.util.List;

public class DemoTransport {
    private static DemoTransport demoTransport;
    private WalletService mWalletService;


    public static DemoTransport getInstance(){
        if (demoTransport == null){
            demoTransport = new DemoTransport();
        }
        return demoTransport;
    }
    public WalletService getWalletService() {
        return this.mWalletService;
    }
    public void initPayListener(PayEventListener payEventListener) {

    }
    public void sendPayMessage(String receiverId, String message, String messageId) {


    }
    public boolean isInternetSeller(String nodeId) {
        return false;
    }
    public boolean isUserConnected(String id) {

        return false;
    }

    public List<String> getInternetSellers() {
        return null;
    }
    public InternetTransport getInternetTransport() {
        return null;
    }

}
