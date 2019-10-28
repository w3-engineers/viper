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
    private ViperCommunicator mViperCommunicator;
    private String mSsid;
    private Context mContext;

    private static DataManager mDataManager;
    private boolean isFirstAttempt = true;
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

        checkAndBindService();
    }



    ServiceConnection clientServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mViperCommunicator = ViperCommunicator.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public void stopService() {
        mContext.unbindService(serviceConnection);
    }


    /**
     * <h1>Note:</h1>
     *
     * <h1>Author: Azizul Islam</h1>
     *
     * <p>Purpose to check service connection with a time interval.
     * If TeleMeshService is not installed then notify user to install
     * the service app. In future we will show an alert dialog with play store
     * link to install the service app</p>
     *
     */
    private void checkAndBindService(){
       HandlerUtil.postBackground(new Runnable() {
           @Override
           public void run() {
               if(mTmCommunicator == null) {
                   boolean isSuccess =initServiceConnection();

                   if(isSuccess){
                       Toast.makeText(mContext,"Bind service successful", Toast.LENGTH_LONG).show();
                       return;
                   }
                   HandlerUtil.postBackground(this, 5000);

                   if(!isFirstAttempt){
                       Toast.makeText(mContext,"Please install TeleMeshService app", Toast.LENGTH_LONG).show();
                   }
                   isFirstAttempt = false;
               }
           }
       });
    }


    /**
     * Bind to the remote service
     */
    public boolean initServiceConnection() {
        if (mTmCommunicator == null) {
            Intent intent = new Intent(ITmCommunicator.class.getName());
            /*this is service name that is associated with server end*/
            intent.setAction("service.viper_server");

            /*From 5.0 annonymous intent calls are suspended so replacing with server app's package name*/
            intent.setPackage("com.w3engineers.meshrnd");
            // binding to remote service
            return mContext.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        }else {
            return false;
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
                //mTmCommunicator.startMesh(mContext.getPackageName());
                mTmCommunicator.setViperCommunicator(viperCommunicator);
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


    /**
     * <h1>Note:
     * <p>This is client side IPC callback
     * pass this callback when client lib bind with TelemeshService
     * We don't need to bind both service each other. this causes some unnecessary
     * Exception like DeadObject exception. Sometime Server pass data through
     * IPC but Client side does not receive the message.
     * So client side Service and client app will perform its own functionality.
     * Service will keep app data manager alive to receive message </p>
     * </h1>
     *
     */
    private ViperCommunicator.Stub viperCommunicator = new ViperCommunicator.Stub() {
        @Override
        public void onPeerAdd(String peerId) throws RemoteException {
            DataManager.this.onPeerAdd(peerId);
        }

        @Override
        public void onPeerRemoved(String nodeId) throws RemoteException {
            DataManager.this.onPeerRemoved(nodeId);
        }

        @Override
        public void onRemotePeerAdd(String peerId) throws RemoteException {
            DataManager.this.onPeerAdd(peerId);
        }

        @Override
        public void onDataReceived(String senderId, byte[] frameData) throws RemoteException {
            DataManager.this.onDataReceived(senderId, frameData);
        }

        @Override
        public void onAckReceived(String messageId, int status) throws RemoteException {
             DataManager.this.onAckReceived(messageId, status);
        }

        @Override
        public void onServiceAvailable(int status) throws RemoteException {

        }

        @Override
        public void setServiceForeground(boolean isForeGround) throws RemoteException {

        }

        @Override
        public void onMessagePayReceived(String sender, byte[] paymentData) throws RemoteException {
            DataManager.this.onMessagePayReceived(sender, paymentData);
        }

        @Override
        public void onPayMessageAckReceived(String sender, String receiver, String messageId) throws RemoteException {
            DataManager.this.onPayMessageAckReceived(sender, receiver, messageId);
        }

        @Override
        public void buyerInternetMessageReceived(String sender, String receiver, String messageId, String messageData, long dataLength, boolean isIncoming) throws RemoteException {
            DataManager.this.buyerInternetMessageReceived(sender, receiver, messageId, messageData, dataLength, isIncoming);
        }

        @Override
        public void onTransportInit(String nodeId, String publicKey, boolean success, String msg) throws RemoteException {
            DataManager.this.onTransportInit(nodeId, publicKey, success, msg);
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
        MeshLog.v("sendPayMessage dtm");
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
        MeshLog.v("onMessagePayReceived dtm " + sender);
        PayMessage payMessage = new PayMessage();
        payMessage.sender = sender;
        payMessage.paymentData = paymentData;
        AppDataObserver.on().sendObserverData(payMessage);
    }
    public void onPayMessageAckReceived( String sender,  String receiver,  String messageId){
        MeshLog.v("onPayMessageAckReceived dtm " + sender);
        PayMessageAck payMessageAck = new PayMessageAck();
        payMessageAck.sender = sender;
        payMessageAck.receiver = receiver;
        payMessageAck.messageId = messageId;
        AppDataObserver.on().sendObserverData(payMessageAck);
    }

    public void buyerInternetMessageReceived(String sender, String receiver, String messageId, String messageData, long dataLength, boolean isIncoming) {
        MeshLog.v("buyerInternetMessageReceived dtm " + sender);
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
        MeshLog.v("onTransportInit dtm " + nodeId);
        TransportInit transportInit = new TransportInit();
        transportInit.nodeId = nodeId;
        transportInit.publicKey = publicKey;
        transportInit.success = success;
        transportInit.msg = msg;

        AppDataObserver.on().sendObserverData(transportInit);
    }

}
