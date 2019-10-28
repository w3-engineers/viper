package com.w3engineers.mesh.util.lib.mesh;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.w3engineers.mesh.ClientLibraryService;
import com.w3engineers.mesh.util.MeshLog;
import com.w3engineers.meshrnd.ITmCommunicator;
import com.w3engineers.mesh.ViperCommunicator;
import com.w3engineers.mesh.application.data.AppDataObserver;
import com.w3engineers.mesh.application.data.model.DataAckEvent;
import com.w3engineers.mesh.application.data.model.DataEvent;
import com.w3engineers.mesh.application.data.model.PeerAdd;
import com.w3engineers.mesh.application.data.model.PeerRemoved;

public class DataManager {

    private ITmCommunicator mTmCommunicator;
    private ViperCommunicator viperCommunicator;
    private String mSsid;
    private Context mContext;

    private static DataManager mDataManager;
    private DataManager() {
        //Prevent form the reflection api.
        if (mDataManager != null) {
            throw new RuntimeException("Use on() method to get the single instance of this class.");
        }
    }

    public static DataManager on() {
        if (mDataManager == null) {
            synchronized (DataManager.class) {
                if (mDataManager == null) mDataManager = new DataManager();
            }
        }
        return mDataManager;
    }

    /**
     * Start the ClientLibraryService class
     *
     * @param context
     * @param networkPrefix
     */
    public void doBindService(Context context, String networkPrefix) {
        this.mContext = context;
        this.mSsid = networkPrefix;

        Intent mIntent = new Intent(context, ClientLibraryService.class);
        context.startService(mIntent);

        context.bindService(mIntent, clientServiceConnection, Service.BIND_AUTO_CREATE);

        initServiceConnection();
    }

    ServiceConnection clientServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            viperCommunicator = ViperCommunicator.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public void stopService() {
        mContext.unbindService(serviceConnection);
    }

    public void initServiceConnection() {
        if (mTmCommunicator == null) {
            Intent intent = new Intent(ITmCommunicator.class.getName());
            /*this is service name that is associated with server end*/
            intent.setAction("service.viper_server");

            /*From 5.0 annonymous intent calls are suspended so replacing with server app's package name*/
            intent.setPackage("com.w3engineers.meshrnd");
            // binding to remote service
            mContext.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        }
    }


    /**
     * Initializing with remote connection
     */
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.e("service_status", "onServiceConnected");
            mTmCommunicator = ITmCommunicator.Stub.asInterface(binder);

            try {
                mTmCommunicator.startMesh(mContext.getPackageName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTmCommunicator = null;
            Log.e("service_status", "onServiceDisconnected");
        }
    };

/*    public void stopService() {
        mContext.unbindService(serviceConnection);
    }*/

    /**
     * To send any type of data
     *
     * @param senderId
     * @param receiverId
     * @param messageId
     * @param data
     */
    public void sendData(String senderId, String receiverId, String messageId, byte[] data) throws RemoteException {

        mTmCommunicator.sendData(senderId, receiverId, messageId, data);

    }

    /**
     * To get the int value for connection type
     *
     * @param nodeID
     * @return
     */
    public int getLinkTypeById(String nodeID) throws RemoteException {
        return mTmCommunicator.getLinkTypeById(nodeID);
    }

    public String getUserId() throws RemoteException {
        return mTmCommunicator.getUserId();
    }


    /**
     * called when new peer is added
     *
     * @param peerId
     */
    public void onPeerAdd(String peerId) {

        MeshLog.e("discover peer id: " + peerId);

        PeerAdd peerAdd = new PeerAdd();
        peerAdd.peerId = peerId;

        AppDataObserver.on().sendObserverData(peerAdd);
    }

    /**
     * called when a peer leave or removed
     *
     * @param nodeId
     */
    public void onPeerRemoved(String nodeId) {
        PeerRemoved peerRemoved = new PeerRemoved();
        peerRemoved.peerId = nodeId;

        AppDataObserver.on().sendObserverData(peerRemoved);
    }

    /**
     * called when remote/internet user found
     *
     * @param peerId
     */
    public void onRemotePeerAdd(String peerId) {

        PeerAdd peerAdd = new PeerAdd();
        peerAdd.peerId = peerId;

        AppDataObserver.on().sendObserverData(peerAdd);
    }

    /**
     * called when any kind of data receive
     *
     * @param senderId
     * @param frameData
     */
    public void onDataReceived(String senderId, byte[] frameData) {
        DataEvent dataEvent = new DataEvent();

        dataEvent.peerId = senderId;
        dataEvent.data = frameData;

        AppDataObserver.on().sendObserverData(dataEvent);
    }


    /**
     * called when any kind of ack is received
     *
     * @param messageId
     * @param status
     */
    public void onAckReceived(String messageId, int status) {

        DataAckEvent dataAckEvent = new DataAckEvent();
        dataAckEvent.dataId = messageId;
        dataAckEvent.status = status;

        AppDataObserver.on().sendObserverData(dataAckEvent);
    }

    public void setServiceForeground(boolean isForeground) {
        try {
            if (viperCommunicator != null) {
                viperCommunicator.setServiceForeground(isForeground);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
