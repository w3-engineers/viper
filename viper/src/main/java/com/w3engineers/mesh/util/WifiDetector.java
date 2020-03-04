/*
 * Copyright (c) 2016 Vladimir L. Shabanov <virlof@gmail.com>
 *
 * Licensed under the Underdark License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://underdark.io/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.w3engineers.mesh.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;




public class WifiDetector {
    public interface Listener {
        void onWifiConnected();
        void onWifiDisconnected();
    }

    /**
     * We have observed, often first time call of AP state broadcast (If AP turned on) destroy the
     * manual tracking of AP forceful turn off so that we can turn it back on. Hence we are ignoring
     * first time default call.
     */
    private boolean mIsToIgnoreAPBroadcast = true;
    private boolean running;
    private Listener listener;
    private Context context;

    private boolean connected;
    private BroadcastReceiver receiver;
    private final String LOCAL_IP_FIRST_PORTION = "/192";
    private final String TAG = "Jmdnslog";

    public WifiDetector(Listener listener, Context context) {
        this.listener = listener;
        this.context = context.getApplicationContext();
    }

    public void start() {
        if (running)
            return;

        running = true;
        Log.e(TAG, "WifiDetector started");

        /*if (isConnectedViaWifi() || isWifiConnected()) {

            final InetAddress address = determineAddress(context);

            if (address != null) {
                connected = true;
                queue.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAdhocEnabled(address);
                    }
                });
            }
        }*/

        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiDetector.this.onReceive(context, intent);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        context.registerReceiver(receiver, intentFilter);
    } // start()

    public void stop() {
        if (!running)
            return;

        running = false;

        context.unregisterReceiver(receiver);
        receiver = null;
    } // stop

    private void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            onReceive_NETWORK_STATE_CHANGED_ACTION(context, intent);
        } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
            // onReceive_NETWORK_AP_STATE_CHANGED_ACTION(context, intent);
        }

    }

    private void onReceive_NETWORK_STATE_CHANGED_ACTION(Context context, Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        if (NetworkInfo.State.CONNECTED.equals(info.getState())) {
            if (connected)
                return;

            this.connected = true;
            listener.onWifiConnected();
            return;
        }

        if (NetworkInfo.State.DISCONNECTED.equals(info.getState())) {
            if (!connected)
                return;

            this.connected = false;
            listener.onWifiDisconnected();
        }
    }
}
