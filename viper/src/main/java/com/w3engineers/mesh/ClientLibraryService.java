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

import com.w3engineers.mesh.application.data.remote.service.BaseTmServiceNotificationHelper;
import com.w3engineers.mesh.util.lib.mesh.DataManager;



public class ClientLibraryService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
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
           DataManager.getInstance().onPeerAdd(peerId);
        }

        @Override
        public void onPeerRemoved(String nodeId) throws RemoteException {
          DataManager.getInstance().onPeerRemoved(nodeId);
        }

        @Override
        public void onRemotePeerAdd(String peerId) throws RemoteException {
         DataManager.getInstance().onRemotePeerAdd(peerId);
        }

        @Override
        public void onDataReceived(String senderId, byte[] frameData) throws RemoteException {
            DataManager.getInstance().onDataReceived(senderId, frameData);
        }

        @Override
        public void onAckReceived(String messageId, int status) throws RemoteException {
          DataManager.getInstance().onAckReceived(messageId, status);
        }

        @Override
        public void setServiceForeground(boolean isForeGround) throws RemoteException {
            if(isForeGround){
                startInForeground();
            }else {
                stopInForeground();
            }
        }
    };

    private void startInForeground(){
        new BaseTmServiceNotificationHelper(this).startForegroundService();
    }

    private void stopInForeground() {
        new BaseTmServiceNotificationHelper(this).stopForegroundService();
    }

}
