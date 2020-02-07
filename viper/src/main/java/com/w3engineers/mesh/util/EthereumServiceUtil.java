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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.w3engineers.eth.data.helper.model.PayLibNetworkInfo;
import com.w3engineers.eth.data.remote.EthereumService;
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

public class EthereumServiceUtil implements EthereumService.NetworkInfoCallback {

    private static EthereumServiceUtil ethereumServiceUtil = null;
    private DatabaseService databaseService;
    private EthereumService ethereumService;
    private static final String GO_PREFIX = "DIRECT-";

    private EthereumServiceUtil(Context context) {
        databaseService = DatabaseService.getInstance(context);
        boolean isAdhocConnected = isAdhocConnected(context);
        ethereumService = EthereumService.getInstance(context, this,
                SharedPref.read(Constant.PreferenceKeys.GIFT_DONATE_LINK), isAdhocConnected);
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

  /*  private void populateDb(Context context){
        try {
            InputStream is = context.getAssets().open("blockchainnetworkinfo.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            Element element=doc.getDocumentElement();
            element.normalize();

            NodeList nList = doc.getElementsByTagName("network");

            for (int i=0; i<nList.getLength(); i++) {

                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element_ = (Element) node;
                    int network_type = Integer.parseInt(getValue("network_type", element_));
                    String network_url = getValue("network_url", element_);
                    String currency_symbol = getValue("currency_symbol", element_);
                    String token_symbol = getValue("token_symbol", element_);
                    String network_name = getValue("network_name", element_);
                    String token_address = getValue("token_address", element_);
                    String channel_address = getValue("channel_address", element_);
                    long gas_price = Long.parseLong(getValue("gas_price", element_));
                    long gas_limit = Long.parseLong(getValue("gas_limit", element_));
                    double token_amount = Double.parseDouble(getValue("token_amount", element_));
                    double currency_amount = Double.parseDouble(getValue("currency_amount", element_));


                    NetworkInfo ni = new NetworkInfo();
                    ni.channelAddress = channel_address;
                    ni.currencyAmount = currency_amount;
                    ni.currencySymbol = currency_symbol;
                    ni.gasLimit = gas_limit;
                    ni.gasPrice = gas_price;
                    ni.networkName = network_name;
                    ni.networkType = network_type;
                    ni.networkUrl = network_url;
                    ni.tokenAddress = token_address;
                    ni.tokenAmount = token_amount;
                    ni.tokenSymbol = token_symbol;

                    insertNetworkInfo(ni);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public void insertNetworkInfo(NetworkInfo networkInfo) {
        try {
            databaseService.insertNetworkInfo(networkInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*    private static String getValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }*/
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

    public boolean isAdhocConnected(Context mContext){
        if (isWifiConnected(mContext)) {
            if (!isPotentialGO(mContext)) {
                return true;
            }
        }
        return false;
    }
}
