package com.w3engineers.eth.util.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.text.TextUtils;

import org.w3c.dom.Text;

public class WiFiDataNetworkUtil {
    public static Network getConnectedWiFiNetwork(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo;
        Network[] networks = connectivityManager.getAllNetworks();
        for (Network network : networks) {
            networkInfo = connectivityManager.getNetworkInfo(network);

            NetworkInfo.DetailedState detailedState = networkInfo.getDetailedState();
            String typeName = networkInfo.getTypeName();


            if (detailedState.equals(NetworkInfo.DetailedState.CONNECTED)
                    && !TextUtils.isEmpty(typeName) && typeName.toLowerCase().contains("wifi")) {
                return network;
            }
        }
        return null;
    }

}
