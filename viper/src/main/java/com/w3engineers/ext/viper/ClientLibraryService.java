package com.w3engineers.ext.viper;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.w3engineers.ext.viper.util.Constant;

public class ClientLibraryService extends Service {

    private ITmCommunicator iTmCommunicator;

    @Override
    public void onCreate() {
        super.onCreate();

        initServiceConnection();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent !=null){
            String ssid = intent.getExtras().getString(Constant.IntentKey.SSID);
            try {
                iTmCommunicator.startMesh(ssid);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return super.onStartCommand(intent, flags, startId);
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

    private void initServiceConnection() {
        if (iTmCommunicator == null) {
            Intent intent = new Intent(ITmCommunicator.class.getName());

            /*this is service name that is associated with server end*/
            intent.setAction("service.aes_security");

            /*From 5.0 annonymous intent calls are suspended so replacing with server app's package name*/
            intent.setPackage("com.demo.AESSecurity");

            // binding to remote service
            bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        }
    }


    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            iTmCommunicator = ITmCommunicator.Stub.asInterface(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iTmCommunicator = null;
        }
    };



    ViperCommunicator.Stub viperCommunicator = new ViperCommunicator.Stub() {

        @Override
        public void onPeerAdd(String peerId) throws RemoteException {

        }

        @Override
        public void onPeerRemoved(String nodeId) throws RemoteException {

        }

        @Override
        public void onRemotePeerAdd(String peerId) throws RemoteException {

        }

        @Override
        public void onDataReceived(String senderId, byte[] frameData) throws RemoteException {

        }

        @Override
        public void onAckReceived(String messageId, int status) throws RemoteException {

        }
    };

}
