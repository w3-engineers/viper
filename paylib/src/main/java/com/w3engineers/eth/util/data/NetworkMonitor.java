package com.w3engineers.eth.util.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class NetworkMonitor {
    private static volatile boolean isOnline = false;
    private static volatile NetworkInterfaceListener networkInterfaceListener;
    private static volatile String socketUrl;
    private static volatile Context context;
    private static volatile Socket socket;
    private static volatile String TAG = "NetworkMonitor";
    private static boolean usingWifiNetwork;

    private static volatile Network cellularNetwork;
    private static volatile Network usableNetwork;
    private static volatile boolean isConnecting;
    private static volatile boolean usingCellularNetwork;
//    private static volatile ConnectivityManager connectivityManager;
//    private static Handler handler;
//    private static HandlerThread handlerThread;


    public interface NetworkInterfaceListener{
        void onNetworkAvailable(boolean isOnline, Network network, boolean isWiFi);
    }

    public static void start(Context context_, String socket_Url, NetworkInterfaceListener network_Interface_Listener){
        networkInterfaceListener = network_Interface_Listener;
        socketUrl = socket_Url;
        context = context_.getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                setCellularObserver();
                setWifiObserver();
            }
        }).start();
    }

    private static void setCellularObserver(){
        Log.v("cellular","observe");

        NetworkRequest.Builder request = new NetworkRequest.Builder();

        request.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        request.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        connectivityManager.requestNetwork(request.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Log.v("cellular","network connected " + network.toString());
                cellularNetwork = network;
                if (!usingWifiNetwork){
                    initSocket(network);
                }
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.v("cellular","network not found");
                cellularNetwork = null;

                if (usingCellularNetwork){
                    makeOffLine();
                    closeSocket();
                }
                usingCellularNetwork = false;
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                Log.v("cellular","network lost");
                cellularNetwork = null;
                if (usingCellularNetwork){
                    makeOffLine();
                    closeSocket();
                }
                usingCellularNetwork = false;
            }


            @Override
            public void onLosing(Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                Log.v(TAG, network.toString() + "--" + maxMsToLive);
            }


            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                Log.v(TAG, network.toString() + "--" + networkCapabilities.toString());
            }

            @Override
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
                Log.v(TAG, network.toString());
            }


        });
    }

    private static void setWifiObserver(){
        Log.v("wifi","observe");

        NetworkRequest.Builder request = new NetworkRequest.Builder();

        request.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        request.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        connectivityManager.requestNetwork(request.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Log.v("wifi","network connected " + network);
                initSocket(network);
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.v("wifi","network not found");
                if (usingWifiNetwork){
                    makeOffLine();
                    closeSocket();
                }
                usingWifiNetwork = false;
                if (cellularNetwork != null && !usingCellularNetwork){
//                    initSocket(cellularNetwork);
                    setCellularObserver();
                }
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                Log.v("wifi","network lost");

                if (usingWifiNetwork){
                    makeOffLine();
                    closeSocket();
                }
                usingWifiNetwork = false;
                if (cellularNetwork != null && !usingCellularNetwork){
                   // initSocket(cellularNetwork);
                    setCellularObserver();
                }
            }

            @Override
            public void onLosing(Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                Log.v(TAG, network.toString() + "--" + maxMsToLive);
            }


            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                Log.v(TAG, network.toString() + "--" + networkCapabilities.toString());
            }

            @Override
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
                Log.v(TAG, network.toString());
            }
        });
    }

    private static void closeSocket(){
        if (socket != null){
            if (socket.connected()){
                socket.disconnect();
            }
            socket.close();
            socket = null;
        }
    }

    private static void makeOffLine(){
        if (isOnline){
            isOnline = false;
            isConnecting = false;
            usingCellularNetwork = false;
            usingWifiNetwork = false;
            cellularNetwork = null;
            networkInterfaceListener.onNetworkAvailable(isOnline, null, false);
        }
    }

    private synchronized static void initSocket(final Network network) {


        if (isConnecting) return;

        try {

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
            String typeName = networkInfo.getTypeName();
            Log.v(TAG, typeName);

            final boolean isWiFi = typeName.equalsIgnoreCase("wifi");

            OkHttpClient okHttpClient = new OkHttpClient.Builder().socketFactory(network.getSocketFactory()).build();

            // set as an option
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            opts.callFactory = okHttpClient;
            opts.webSocketFactory = okHttpClient;
            opts.reconnectionAttempts = 3;
            opts.timeout = 5000;
            socket = IO.socket(socketUrl, opts);



            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Socket EVENT_CONNECT: ");

                    usingWifiNetwork = isWiFi;
                    isOnline = true;
                    networkInterfaceListener.onNetworkAvailable(isOnline, network, isWiFi);
                    usableNetwork = network;
                    usingCellularNetwork = !isWiFi;
                    closeSocket();
                    isConnecting = false;
                }
            });

            socket.on(Socket.EVENT_CONNECTING, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Socket EVENT_CONNECTING: ");
                    isConnecting = true;
                }
            });

            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Socket EVENT_DISCONNECT: ");
                    isConnecting = false;
                }
            });

            socket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Socket EVENT_ERROR: ");
                    isConnecting = false;
                }
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Socket EVENT_CONNECT_ERROR: " );
                    isConnecting = false;
                }
            });

            socket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Socket EVENT_CONNECT_TIMEOUT: " );
                    isConnecting = false;
                  if (socket != null){
                      socket.connect();
                  }
                }
            });

            socket.on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Socket EVENT_RECONNECT: ");
                    usingWifiNetwork = isWiFi;
                    isOnline = true;
                    networkInterfaceListener.onNetworkAvailable(isOnline, network, isWiFi);
                    usableNetwork = network;
                    usingCellularNetwork = !isWiFi;
                    closeSocket();
                    isConnecting = false;
                }
            });

            socket.on(Socket.EVENT_RECONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Socket EVENT_RECONNECT_ERROR: ");
                    isConnecting = false;
                }
            });

            socket.on(Socket.EVENT_RECONNECT_FAILED, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Socket EVENT_RECONNECT_FAILED: ");
                    isConnecting = false;
                }
            });
            socket.on(Socket.EVENT_RECONNECT_ATTEMPT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Socket EVENT_RECONNECT_ATTEMPT: ");
                    isConnecting = true;
                }
            });
            socket.on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Socket EVENT_RECONNECTING: ");
                    isConnecting = true;
                }
            });

            socket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static boolean isOnline(){
        return isOnline;
    }

    public static Network getNetwork(){
        return usableNetwork;
    }



}
