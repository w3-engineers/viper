package com.w3engineers.mesh;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.w3engineers.mesh.application.data.remote.service.BaseTmServiceNotificationHelper;
import com.w3engineers.mesh.util.MeshApp;
import com.w3engineers.mesh.util.MeshLog;
import com.w3engineers.mesh.util.lib.mesh.DataManager;


public class ClientLibraryService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return viperCommunicator;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    ViperCommunicator.Stub viperCommunicator = new ViperCommunicator.Stub() {

        @Override
        public void onPeerAdd(String peerId) throws RemoteException {
            Log.e("viper_log","Service Local peer connected ="+peerId);
            DataManager.on().onPeerAdd(peerId);
        }

        @Override
        public void onPeerRemoved(String nodeId) throws RemoteException {
            Log.e("viper_log","Service Local peer onPeerRemoved ="+nodeId);
            DataManager.on().onPeerRemoved(nodeId);
        }

        @Override
        public void onRemotePeerAdd(String peerId) throws RemoteException {
            Log.e("viper_log","Service Local onRemotePeerAdd ="+peerId);
            DataManager.on().onRemotePeerAdd(peerId);
        }

        @Override
        public void onDataReceived(String senderId, byte[] frameData) throws RemoteException {
            Log.e("viper_log","Service Local onDataReceived ="+senderId);
            DataManager.on().onDataReceived(senderId, frameData);
        }

        @Override
        public void onAckReceived(String messageId, int status) throws RemoteException {
            Log.e("viper_log","Service Local onAckReceived ="+messageId);
            DataManager.on().onAckReceived(messageId, status);
        }

        @Override
        public void onServiceAvailable(int status) throws RemoteException {
            Log.e("peerid", "from server: " + status);
            //  DataManager.on().initServiceConnection();
        }

        @Override
        public void onReceiveLog(String text) throws RemoteException {

            Intent intent = new Intent("com.w3engineers.meshrnd.DEBUG_MESSAGE");
            intent.putExtra("value", text);
            MeshApp.getContext().sendBroadcast(intent);
        }


        @Override
        public void setServiceForeground(boolean isForeGround) throws RemoteException {
            if (isForeGround) {
                startInForeground();
            } else {
                stopInForeground();
            }
        }

        @Override
        public void onMessagePayReceived(String sender, byte[] paymentData) throws RemoteException {
            DataManager.on().onMessagePayReceived(sender, paymentData);
        }

        @Override
        public void onPayMessageAckReceived(String sender, String receiver, String messageId) throws RemoteException {
            DataManager.on().onPayMessageAckReceived(sender, receiver, messageId);
        }

        @Override
        public void buyerInternetMessageReceived(String sender, String receiver, String messageId, String messageData, long dataLength, boolean isIncoming) throws RemoteException {
            DataManager.on().buyerInternetMessageReceived(sender, receiver, messageId, messageData, dataLength, isIncoming);
        }

        @Override
        public void onTransportInit(String nodeId, String publicKey, boolean success, String msg) throws RemoteException {
            MeshLog.v("onTransportInit cls");
            DataManager.on().onTransportInit(nodeId, publicKey, success, msg);
        }
    };

    private void startInForeground() {
        new BaseTmServiceNotificationHelper(this).startForegroundService();

    }

    private void stopInForeground() {
        new BaseTmServiceNotificationHelper(this).stopForegroundService();
    }

}
