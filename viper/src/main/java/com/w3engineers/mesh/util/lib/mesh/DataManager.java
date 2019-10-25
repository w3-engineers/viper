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

import com.w3engineers.mesh.ClientLibraryService;
import com.w3engineers.mesh.application.data.model.PayMessage;
import com.w3engineers.mesh.application.data.model.PayMessageAck;
import com.w3engineers.mesh.application.data.model.TransportInit;
import com.w3engineers.mesh.application.data.remote.model.BuyerPendingMessage;
import com.w3engineers.mesh.util.MeshLog;
import com.w3engineers.meshrnd.ITmCommunicator;
import com.w3engineers.mesh.ViperCommunicator;
import com.w3engineers.mesh.application.data.AppDataObserver;
import com.w3engineers.mesh.application.data.model.DataAckEvent;
import com.w3engineers.mesh.application.data.model.DataEvent;
import com.w3engineers.mesh.application.data.model.PeerAdd;
import com.w3engineers.mesh.application.data.model.PeerRemoved;

import java.util.List;

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


    /**
     * Bind to the remote service
     */
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

    public void sendPayMessage(String receiverId, String message, String messageId) throws RemoteException {
        mTmCommunicator.sendPayMessage(receiverId, message, messageId);
    }

    public void onPaymentGotForIncomingMessage( boolean success,  String receiver,  String sender,  String messageId,  String msgData) throws RemoteException {
        mTmCommunicator.onPaymentGotForIncomingMessage(success,receiver, sender, messageId,msgData);
    }
    public void onPaymentGotForOutgoingMessage( boolean success,  String receiver,  String sender,  String messageId,  String msgData) throws RemoteException {
        mTmCommunicator.onPaymentGotForOutgoingMessage(success, receiver, sender, messageId, msgData);
    }
    public List<String> getInternetSellers() throws RemoteException {
        return mTmCommunicator.getInternetSellers();
    }
    public boolean isInternetSeller( String address) throws RemoteException {
        return mTmCommunicator.isInternetSeller(address);
    }
    public boolean isUserConnected( String address) throws RemoteException {
        return mTmCommunicator.isUserConnected(address);
    }
    public void onBuyerConnected( String address) throws RemoteException {
        mTmCommunicator.onBuyerConnected(address);
    }
    public void onBuyerDisconnected( String address) throws RemoteException {
        mTmCommunicator.onBuyerDisconnected(address);
    }
    public void restartMesh(int newRole) throws RemoteException{
        mTmCommunicator.restartMesh(newRole);
    }


    public void onMessagePayReceived( String sender, byte[] paymentData){
        PayMessage payMessage = new PayMessage();
        payMessage.sender = sender;
        payMessage.paymentData = paymentData;
        AppDataObserver.on().sendObserverData(payMessage);
    }
    public void onPayMessageAckReceived( String sender,  String receiver,  String messageId){
        PayMessageAck payMessageAck = new PayMessageAck();
        payMessageAck.sender = sender;
        payMessageAck.receiver = receiver;
        payMessageAck.messageId = messageId;
        AppDataObserver.on().sendObserverData(payMessageAck);
    }

    public void buyerInternetMessageReceived(String sender, String receiver, String messageId, String messageData, long dataLength, boolean isIncoming) {
        BuyerPendingMessage buyerPendingMessage = new BuyerPendingMessage();
        buyerPendingMessage.sender = sender;
        buyerPendingMessage.receiver = receiver;
        buyerPendingMessage.messageId = messageId;
        buyerPendingMessage.messageData = messageData;
        buyerPendingMessage.dataLength = dataLength;
        buyerPendingMessage.isIncoming = isIncoming;

        AppDataObserver.on().sendObserverData(buyerPendingMessage);


    }

    public void onTransportInit( String nodeId,  String publicKey,  boolean success, String msg){
        TransportInit transportInit = new TransportInit();
        transportInit.nodeId = nodeId;
        transportInit.publicKey = publicKey;
        transportInit.success = success;
        transportInit.msg = msg;

        AppDataObserver.on().sendObserverData(transportInit);
    }

}
