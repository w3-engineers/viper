package com.w3engineers.eth.util.data;

import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-07-09 at 11:51 AM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: meshsdk.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-07-09 at 11:51 AM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-07-09 at 11:51 AM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class NetworkCallBackReceiver extends ConnectivityManager.NetworkCallback {
    private final String TAG = getClass().getSimpleName();

    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        Log.d(TAG, network.toString());
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
        Log.d(TAG, network.toString() + "--" + maxMsToLive);
    }

    @Override
    public void onLost(Network network) {
        Log.d(TAG, network.toString());
    }

    @Override
    public void onUnavailable() {
        Log.d(TAG, "Unavailable");}


    @Override
    public void onCapabilitiesChanged(Network network,
                                      NetworkCapabilities networkCapabilities) {
        Log.d(TAG, network.toString() + "--" + networkCapabilities.toString());
    }

    @Override
    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
        Log.d(TAG, network.toString());
    }


}
