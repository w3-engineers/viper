package com.w3engineers.eth.util.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-07-09 at 4:44 PM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: meshsdk.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-07-09 at 4:44 PM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-07-09 at 4:44 PM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class CellularDataNetworkUtil {

    private static final String TAG = CellularDataNetworkUtil.class.getSimpleName();
    /**
     * Few devices send this callback continuously. We restrict that using this flag.
     */
    private static boolean hasUpdatedTheListener;
    private static Network mynet;
    private Context mContext;
    private static CellularDataNetworkListenerForInternet cellularDataNetworkListenerForInternet;
    private static CellularDataNetworkListenerForPurchase cellularDataNetworkListenerForPurchase;


    public interface CellularDataNetworkListenerForInternet {
        void onAvailable(Network network);
        void onLost();
    }

    public interface CellularDataNetworkListenerForPurchase {
        void onAvailable(Network network);
        void onLost();
    }


    private static CellularDataNetworkUtil cellularDataNetworkUtil;

    private CellularDataNetworkUtil(Context context) {
        mContext = context;
    }

    public static <L> CellularDataNetworkUtil on(Context context, L listener) {
        if (cellularDataNetworkUtil == null) {
            cellularDataNetworkUtil = new CellularDataNetworkUtil(context);

        }

        initListener(listener);
        return cellularDataNetworkUtil;
    }

    private static <L> void initListener(L listener) {
        if (listener instanceof CellularDataNetworkListenerForInternet) {
            Log.i("chkethlog","instance of CellularDataNetworkListenerForInternet");
            cellularDataNetworkListenerForInternet = (CellularDataNetworkListenerForInternet) listener;
        } else if (listener instanceof CellularDataNetworkListenerForPurchase) {
            Log.i("chkethlog","instance of CellularDataNetworkListenerForPurchase");
            cellularDataNetworkListenerForPurchase = (CellularDataNetworkListenerForPurchase) listener;
        }
    }


    public void initMobileDataNetworkRequest() {

        if (mynet != null) {
            Log.i("chkethlog","mynet not null");
            if (cellularDataNetworkListenerForInternet != null) {
                cellularDataNetworkListenerForInternet.onAvailable(mynet);
                Log.i("chkethlog","cellularDataNetworkListenerForInternet not null");
            }
            if (cellularDataNetworkListenerForPurchase != null) {
                cellularDataNetworkListenerForPurchase.onAvailable(mynet);
                Log.i("chkethlog","cellularDataNetworkListenerForPurchase not null");
            }
        } else {
            Log.i("chkethlog","mynet is null");
            // Add any NetworkCapabilities.NET_CAPABILITY_...
            int[] capabilities = new int[]{NetworkCapabilities.NET_CAPABILITY_INTERNET};

            // Add any NetworkCapabilities.TRANSPORT_...
            int[] transportTypes = new int[]{NetworkCapabilities.TRANSPORT_CELLULAR};

            NetworkRequest.Builder request = new NetworkRequest.Builder();

            // add capabilities
            for (int cap : capabilities) {
                request.addCapability(cap);
            }

            // add transport types
            for (int trans : transportTypes) {
                request.addTransportType(trans);
            }

            final ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);

            connectivityManager.requestNetwork(request.build(), new NetworkCallBackReceiver() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    if (hasUpdatedTheListener) {
                        Log.i("chkethlog","network available");
                        return;
                    }

                    hasUpdatedTheListener = true;
                    mynet = network;
                    if (cellularDataNetworkListenerForInternet != null) {
                        cellularDataNetworkListenerForInternet.onAvailable(mynet);
                    }
                    if (cellularDataNetworkListenerForPurchase != null) {
                        cellularDataNetworkListenerForPurchase.onAvailable(mynet);
                    }

                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Log.i("chkethlog","network unavailable");
                    hasUpdatedTheListener = false;
                    mynet = null;
                    if (cellularDataNetworkListenerForInternet != null) {
                        cellularDataNetworkListenerForInternet.onLost();
                    }
                    if (cellularDataNetworkListenerForPurchase != null) {
                        cellularDataNetworkListenerForPurchase.onLost();
                    }
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    Log.i("chkethlog","network lost");
                    hasUpdatedTheListener = false;
                    mynet = null;
                    if (cellularDataNetworkListenerForInternet != null) {
                        cellularDataNetworkListenerForInternet.onLost();
                    }
                    if (cellularDataNetworkListenerForPurchase != null) {
                        cellularDataNetworkListenerForPurchase.onLost();
                    }
                }
            });
        }
    }
}
