package com.w3engineers.mesh.util;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.w3engineers.eth.data.helper.model.PayLibNetworkInfo;
import com.w3engineers.eth.data.remote.EthereumService;
import com.w3engineers.eth.util.data.NetworkMonitor;
import com.w3engineers.ext.strom.util.Text;
import com.w3engineers.mesh.BuildConfig;
import com.w3engineers.mesh.application.data.local.db.DatabaseService;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.local.db.networkinfo.NetworkInfo;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import io.reactivex.functions.BiConsumer;

public class EthereumServiceUtil implements EthereumService.NetworkInfoCallback, WifiDetector.Listener {

    private static EthereumServiceUtil ethereumServiceUtil = null;
    private DatabaseService databaseService;
    private EthereumService ethereumService;
    private static final String GO_PREFIX = "DIRECT-";
//    private boolean isInternetConnected;
    private boolean usingAdhocInternet;
    private Context context;
    private WifiDetector wifiDetector;
    private String SOCKET_URL = "https://dev-signal.telemesh.net";

    private EthereumServiceUtil(Context context) {
        databaseService = DatabaseService.getInstance(context);
        this.context = context;
        ethereumService = EthereumService.getInstance(this.context, EthereumServiceUtil.this,
                SharedPref.read(Constant.PreferenceKeys.GIFT_DONATE_LINK), false);
        checkandSetAdhocInternetConnected(context);
        wifiDetector = new WifiDetector(this, context);
        wifiDetector.start();
    }

    public void checkandSetAdhocInternetConnected(Context mContext){
        if (isWifiConnected(mContext)) {
            if (!isPotentialGO(mContext)) {
                isInternetAvailable(new BiConsumer<String, Boolean>() {
                    @Override
                    public void accept(String s, Boolean isConnected) throws Exception {
                        changeNetworkInterface(isConnected);
                    }
                });
            } else {
                changeNetworkInterface(false);
            }
        } else {
            changeNetworkInterface(false);
        }
    }

    private void changeNetworkInterface(boolean isAdhocConnected){
        usingAdhocInternet = isAdhocConnected;
        ethereumService.changeNetworkInterface(isAdhocConnected);

    }

    public static EthereumServiceUtil getInstance(Context context) {
        if (ethereumServiceUtil == null) {
            ethereumServiceUtil = new EthereumServiceUtil(context);
        }
        return ethereumServiceUtil;
    }

    public EthereumService getEthereumService() {
        return ethereumService;
    }

    @Override
    public List<PayLibNetworkInfo> getNetworkInfo() {
        try {

            List<NetworkInfo> networkInfos = databaseService.getAllNetworkInfo();
            return new NetworkInfo().toPayLibNetworkInfos(networkInfos);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateCurrencyAndToken(int networkType, double currency, double token) {
        databaseService.updateCurrencyAndToken(networkType, currency, token);
    }

    public void updateCurrency(int networkType, double currency) {
        databaseService.updateCurrency(networkType, currency);
    }

    public void updateToken(int networkType, double token) {
        databaseService.updateToken(networkType, token);
    }

    public double getCurrency(int networkType) {
        try {
            return databaseService.getCurrencyByType(networkType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0D;
    }

    public double getToken(int networkType) {
        try {
            return databaseService.getTokenByType(networkType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0D;
    }

    public void insertNetworkInfo(NetworkInfo networkInfo) {
        try {
            databaseService.insertNetworkInfo(networkInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    public static boolean isPotentialGO(Context context) {
        if (context != null) {
            String connectedSSID = getConnectedSSID(context);
            if (Text.isNotEmpty(connectedSSID)) {
                connectedSSID = connectedSSID.replaceAll("\"", "");
                return Text.isNotEmpty(connectedSSID) && connectedSSID.startsWith(GO_PREFIX);
            }
        }
        return false;
    }

    public static String getConnectedSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (connectionInfo == null || TextUtils.isEmpty(connectionInfo.getSSID()))
            return null;
        return connectionInfo.getSSID();
    }

    public static void isInternetAvailable(BiConsumer<String, Boolean> consumer) {
        new Thread(() -> {
            try {
                final String command = "ping -c 1 google.com";
                boolean isSuccess = Runtime.getRuntime().exec(command).waitFor() == 0;
                consumer.accept("Internet is available", isSuccess);
            } catch (Exception e) {
                try {
                    consumer.accept("Internet is not available", false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void onWifiConnected() {
        Log.e("Internettransport", "Adhoc connected detect in internet transport");
        if (!isPotentialGO(this.context)) {
            isInternetAvailable((message, isConnected) -> {
                if (isConnected) {
                    if (usingAdhocInternet) return;

                    usingAdhocInternet = isConnected;
                    ethereumService.changeNetworkInterface(true);
                }
            });
        }
    }

    @Override
    public void onWifiDisconnected() {
        if (usingAdhocInternet){
            usingAdhocInternet = false;
            ethereumService.changeNetworkInterface(false);
        }
    }


    public void startNetworkMonitor(){
        NetworkMonitor.start(context, SOCKET_URL, new NetworkMonitor.NetworkInterfaceListener() {
            @Override
            public void onNetworkAvailable(boolean isOnline, Network network, boolean isWiFi) {

            }
        });
    }
}
