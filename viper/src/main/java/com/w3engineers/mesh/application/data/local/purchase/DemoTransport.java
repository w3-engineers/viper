package com.w3engineers.mesh.application.data.local.purchase;

import com.w3engineers.mesh.application.data.local.wallet.WalletService;
import com.w3engineers.mesh.util.MeshApp;

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
        return WalletService.getInstance(MeshApp.getContext());
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
