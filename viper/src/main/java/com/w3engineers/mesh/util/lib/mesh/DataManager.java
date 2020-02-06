package com.w3engineers.mesh.util.lib.mesh;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.w3engineers.ext.strom.util.helper.Toaster;
import com.w3engineers.mesh.BuildConfig;
import com.w3engineers.mesh.R;
import com.w3engineers.mesh.ViperCommunicator;
import com.w3engineers.mesh.application.data.ApiEvent;
import com.w3engineers.mesh.application.data.AppDataObserver;
import com.w3engineers.mesh.application.data.local.dataplan.DataPlanManager;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.local.helper.crypto.CryptoHelper;
import com.w3engineers.mesh.application.data.model.ConfigSyncEvent;
import com.w3engineers.mesh.application.data.model.DataAckEvent;
import com.w3engineers.mesh.application.data.model.DataEvent;
import com.w3engineers.mesh.application.data.model.PayMessage;
import com.w3engineers.mesh.application.data.model.PayMessageAck;
import com.w3engineers.mesh.application.data.model.PeerAdd;
import com.w3engineers.mesh.application.data.model.PeerRemoved;
import com.w3engineers.mesh.application.data.model.PermissionInterruptionEvent;
import com.w3engineers.mesh.application.data.model.SellerRemoved;
import com.w3engineers.mesh.application.data.model.ServiceUpdate;
import com.w3engineers.mesh.application.data.model.TransportInit;
import com.w3engineers.mesh.application.data.model.UserInfoEvent;
import com.w3engineers.mesh.application.data.remote.model.BuyerPendingMessage;
import com.w3engineers.mesh.util.CommonUtil;
import com.w3engineers.mesh.util.ConfigSyncUtil;
import com.w3engineers.mesh.util.Constant;
import com.w3engineers.mesh.util.DialogUtil;
import com.w3engineers.mesh.util.MeshApp;
import com.w3engineers.mesh.util.MeshLog;
import com.w3engineers.mesh.util.TSAppInstaller;
import com.w3engineers.mesh.util.Util;
import com.w3engineers.meshrnd.ITmCommunicator;
import com.w3engineers.models.UserInfo;
import com.w3engineers.walleter.wallet.WalletService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataManager {

    private ITmCommunicator mTmCommunicator;
    private ViperCommunicator mViperCommunicator;
    private String mSsid;
    private Context mContext;
    private String appName;
    private UserInfo userInfo;
    private String signalServerUrl;

    private static DataManager mDataManager;
    private boolean isAlreadyToPlayStore = false;

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
     * @param appName
     * @param networkPrefix
     */
    public void doBindService(Context context, String appName, String networkPrefix, UserInfo userInfo, String signalServerUrl) {
        this.mContext = context;
        this.appName = appName;
        this.mSsid = networkPrefix;
        this.userInfo = userInfo;
        this.signalServerUrl = signalServerUrl;


        MeshLog.v("Data manager has been called");

/*        Intent mIntent = new Intent(context, ClientLibraryService.class);
        context.startService(mIntent);

        context.bindService(mIntent, clientServiceConnection, Service.BIND_AUTO_CREATE);*/
    }

    public void startMeshService() {
        AppDataObserver.on().startObserver(ApiEvent.CONFIG_SYNC, event -> {
            MeshLog.v("startMeshService  CONFIG_SYNC");
            ConfigSyncEvent configSyncEvent = (ConfigSyncEvent) event;

            if (configSyncEvent != null) {
                if (configSyncEvent.isMeshStartTime()) {
                    checkAndBindService();
                }
            }
        });


        Util.isConnected(new Util.ConnectionCheck() {
            @Override
            public void onConnectionCheck(boolean isConnected) {
                if (isConnected){
                    ConfigSyncUtil.getInstance().startConfigurationSync(mContext, true);
                } else {
                    checkAndBindService();
                }
            }
        });
    }


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
     */
    private void checkAndBindService() {
        HandlerUtil.postBackground(new Runnable() {
            @Override
            public void run() {
                if (mTmCommunicator == null) {
                    boolean isSuccess = initServiceConnection();

                    if (isSuccess) {
                        Toaster.showShort("Bind service successful");
                        return;
                    }

                    if (CommonUtil.isEmulator()) {
                        isAlreadyToPlayStore = true;
                    }

                    MeshLog.i("Bind Service failed 1 " + isAlreadyToPlayStore);
                    HandlerUtil.postBackground(this, 5000);

                    if (!isAlreadyToPlayStore) {
                        showConfirmationPopUp();
                    }
                    isAlreadyToPlayStore = true;
                }
            }
        });
    }

    private void showConfirmationPopUp() {

        MeshLog.i("Bind Service failed 2");

        DialogUtil.showConfirmationDialog(MeshApp.getCurrentActivity(),
                mContext.getResources().getString(R.string.install_ts),
                mContext.getResources().getString(R.string.need_ts),
                mContext.getString(R.string.cancel),
                mContext.getString(R.string.yes),
                new DialogUtil.DialogButtonListener() {
                    @Override
                    public void onClickPositive() {
//                        if()
                        checkConnectionAndStartDownload();
//                        gotoPlayStore();
                        isAlreadyToPlayStore = true;
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onClickNegative() {
                        isAlreadyToPlayStore = false;
                    }
                });
    }

    private void gotoPlayStore() {
        final String appPackageName = "com.w3engineers.meshservice";
        //final String appPackageName = "com.w3engineers.banglabrowser";
        try {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    // Please don't remove this method. It is needed in our release time
    private void checkConnectionAndStartDownload() {
        Util.isConnected(isConnected ->
                HandlerUtil.postForeground(() -> {
                    if (isConnected) {
                        TSAppInstaller.downloadApkFile(mContext, SharedPref.read(Constant.PreferenceKeys.APP_DOWNLOAD_LINK));
                    } else {
                        isAlreadyToPlayStore = false;
                        Toaster.showShort("Internet connection not available");
                    }
                })

        );
    }

    private void showPermissionPopUp() {
        Toaster.showLong("showpopup");
        MeshLog.v("mContext  " + mContext);
        if (mContext instanceof Activity){
            MeshLog.v("yes");
        }else {
            MeshLog.v("no");
        }




        DialogUtil.showConfirmationDialog(MeshApp.getCurrentActivity(),
                mContext.getResources().getString(R.string.permission),
                mContext.getResources().getString(R.string.permission_message),
                mContext.getString(R.string.later),
                mContext.getString(R.string.allow),
                new DialogUtil.DialogButtonListener() {
                    @Override
                    public void onClickPositive() {

                        /*try {
                            if (mTmCommunicator != null) {
                                mTmCommunicator.allowPermissions();
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }*/

                        launchServiceApp();

                        android.os.Process.killProcess(android.os.Process.myPid());
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onClickNegative() {

                    }
                });
    }

    private void launchServiceApp() {
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage("com.w3engineers.meshservice");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
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
            intent.setPackage("com.w3engineers.meshservice");
            // binding to remote service
            return mContext.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        } else {
            return false;
        }
    }

    public void allowMissingPermission(List<String> missingPermission) {
        try {
            if (mTmCommunicator != null) {
                mTmCommunicator.allowPermissions(missingPermission);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
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
                //mTmCommunicator.saveUserInfo(userInfo);
                Log.e("service_status", "onServiceConnected");
                int userRole = DataPlanManager.getInstance().getDataPlanRole();

                int configVersion = PreferencesHelperDataplan.on().getConfigVersion();
                userInfo.setConfigVersion(configVersion);

                boolean status;
                if (CommonUtil.isEmulator()) {
                    status = true;
                } else {
                    mTmCommunicator.setViperCommunicator(viperCommunicator);
                    status = mTmCommunicator.startMesh(appName, userRole, userInfo, mSsid, signalServerUrl);
                }
                MeshLog.v("status " + status);
                mTmCommunicator.startService();
//                boolean status = mTmCommunicator.startMesh(appName, userRole, userInfo, mSsid);
                if (!status) {
                    showPermissionPopUp();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTmCommunicator = null;
            Log.v("service_status", "onServiceDisconnected");
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
            DataManager.this.onRemotePeerAdd(peerId);
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
        public void onReceiveLog(String text) throws RemoteException {
            if (BuildConfig.DEBUG) {
                Intent intent = new Intent("com.w3engineers.meshservice.DEBUG_MESSAGE");
                intent.putExtra("value", text);
                MeshApp.getContext().sendBroadcast(intent);
            }

            DataManager.this.writeLogIntoTxtFile(text, true);
        }

        @Override
        public void onUserInfoReceive(List<UserInfo> userInfoList) throws RemoteException {
            DataManager.this.onGetUserInfo(userInfoList);
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

        @Override
        public void onProbableSellerDisconnected(String sellerId) throws RemoteException {
            DataManager.this.onProbableSellerDisconnected(sellerId);
        }

        @Override
        public void onServiceUpdateNeeded(boolean isNeeded) throws RemoteException {
          DataManager.this.onServiceUpdateNeeded(isNeeded);
        }

        @Override
        public void onInterruption(int hardwareState, List<String> permissions) throws RemoteException {
            onInterruptionAction(hardwareState, permissions);
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
    public void sendData(String senderId, String receiverId, String messageId, byte[] data, boolean isNotificationNeeded) throws RemoteException {
        mTmCommunicator.sendData(senderId, receiverId, messageId, data, isNotificationNeeded);
    }

    /**
     * To get the int value for connection type
     *
     * @param nodeID
     * @return
     */
    public int getLinkTypeById(String nodeID) throws RemoteException {
        if (mTmCommunicator != null) {
            return mTmCommunicator.getLinkTypeById(nodeID);
        }
        return 0;
    }

    public String getUserId() throws RemoteException {
        if (mTmCommunicator != null) {
            return mTmCommunicator.getUserId();
        }
        return "";
    }

    public void saveDiscoveredUserInfo(String userId, String userName) throws RemoteException {
        if (mTmCommunicator != null) {
            mTmCommunicator.saveDiscoveredUserInfo(userId, userName);
        }

    }

    public void saveUserInfo(UserInfo userInfo) throws RemoteException {
        if (mTmCommunicator != null) {

            int configVersion = PreferencesHelperDataplan.on().getConfigVersion();
            userInfo.setConfigVersion(configVersion);

            this.userInfo = userInfo;

            mTmCommunicator.saveUserInfo(userInfo);
        }
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
        try {
            DataEvent dataEvent = new DataEvent();

            String msgText = new String(frameData);
            MeshLog.v("Before decryption " + msgText);

            String userPublicKey = getUserPublicKey(senderId);
            if (!TextUtils.isEmpty(userPublicKey)) {
                String decryptedMessage = CryptoHelper.decryptMessage(WalletService.getInstance(mContext).getPrivateKey(), userPublicKey, msgText);
                MeshLog.v("Decrypted message " + decryptedMessage);

                dataEvent.peerId = senderId;
                dataEvent.data = decryptedMessage.getBytes();

                AppDataObserver.on().sendObserverData(dataEvent);
            } else {
                MeshLog.v("User public not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void onGetUserInfo(List<UserInfo> userInfoList) {

        MeshLog.e("user info list receive in data manager");

        for (UserInfo userInfo : userInfoList) {
            UserInfoEvent userInfoEvent = new UserInfoEvent();
            userInfoEvent.setAddress(userInfo.getAddress());
            userInfoEvent.setAvatar(userInfo.getAvatar());
            userInfoEvent.setUserName(userInfo.getUserName());
            userInfoEvent.setRegTime(userInfo.getRegTime());
            userInfoEvent.setConfigVersion(userInfo.getConfigVersion());
            userInfoEvent.setSync(userInfo.isSync());

            AppDataObserver.on().sendObserverData(userInfoEvent);

            MeshLog.e("user info send to app level");
        }
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

    public String getUserPublicKey(String address) throws RemoteException {
        MeshLog.v("getUserPublicKey dtm " + address);

        if (mTmCommunicator == null) {
            MeshLog.v("mTmCommunicator null");
            return null;
        }
        return mTmCommunicator.getUserPublicKey(address);
    }

    public String getUserNameByAddress(String address) throws RemoteException {
        MeshLog.v("getUserNameByAddress dtm " + address);

        if (mTmCommunicator == null) {
            MeshLog.v("mTmCommunicator null");
        } else {
            return mTmCommunicator.getUserNameByAddress(address);
        }
        return null;
    }

    public void sendPayMessage(String receiverId, String message, String messageId) throws RemoteException {
        if(mTmCommunicator==null){
            return;
        }
        MeshLog.v("sendPayMessage dtm");
        mTmCommunicator.sendPayMessage(receiverId, message, messageId);
    }

    public void onPaymentGotForIncomingMessage(boolean success, String receiver, String sender, String messageId, String msgData) throws RemoteException {
        if(mTmCommunicator==null){
            return;
        }
        mTmCommunicator.onPaymentGotForIncomingMessage(success, receiver, sender, messageId, msgData);
    }

    public void onPaymentGotForOutgoingMessage(boolean success, String receiver, String sender, String messageId, String msgData) throws RemoteException {
        if(mTmCommunicator==null){
            return;
        }
        mTmCommunicator.onPaymentGotForOutgoingMessage(success, receiver, sender, messageId, msgData);
    }

    public List<String> getInternetSellers() throws RemoteException {
        if (mTmCommunicator == null) {
            MeshLog.v("mTmCommunicator null");
            return new ArrayList<>();
        }
        return mTmCommunicator.getInternetSellers();
    }

    public boolean isInternetSeller(String address) throws RemoteException {
        if (mTmCommunicator == null) {
            MeshLog.v("mTmCommunicator null");
            return false;
        }
        return mTmCommunicator.isInternetSeller(address);
    }

    public boolean isUserConnected(String address) throws RemoteException {
        if (mTmCommunicator == null) {
            MeshLog.v("mTmCommunicator null");
            return false;
        }
        return mTmCommunicator.isUserConnected(address);
    }

    public String getCurrentSellerId() throws RemoteException {
        if (mTmCommunicator == null) {
            MeshLog.v("mTmCommunicator null");
        } else {
            return mTmCommunicator.getCurrentSellerId();
        }
        return null;
    }

    public void onBuyerConnected(String address) throws RemoteException {
        if (mTmCommunicator == null) {
            MeshLog.v("mTmCommunicator null");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            MeshLog.v("address dtm null");
        }

        mTmCommunicator.onBuyerConnected(address);
    }

    public void onBuyerDisconnected(String address) throws RemoteException {
        if (mTmCommunicator == null) {
            MeshLog.v("mTmCommunicator null");
            return;
        }
        mTmCommunicator.onBuyerDisconnected(address);
    }

    public void disconnectFromInternet() throws RemoteException {
        if (mTmCommunicator == null) {
            MeshLog.v("mTmCommunicator null");
            return;
        }
        mTmCommunicator.disconnectFromInternet();
    }

    public void stopMesh() {
        MeshLog.v("stop mesh is called");
        try {
            mTmCommunicator.stopMesh();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void restartMesh(int newRole) {
        MeshLog.v("sellerMode dm" + newRole);
        if (mTmCommunicator == null) {
            MeshLog.v("mTmCommunicator null");
        }

        try {
            mTmCommunicator.restartMesh(newRole, mSsid, signalServerUrl);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void destroyMeshService() {
        try {
            if (mTmCommunicator != null) {
                mTmCommunicator.destroyService();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void resetCommunicator() {
        mTmCommunicator = null;
    }


    public void onMessagePayReceived(String sender, byte[] paymentData) {
        MeshLog.v("onMessagePayReceived dtm " + sender);
        PayMessage payMessage = new PayMessage();
        payMessage.sender = sender;
        payMessage.paymentData = paymentData;
        AppDataObserver.on().sendObserverData(payMessage);
    }

    private void onInterruptionAction(int hardwareState, List<String> permissions) {
        Log.v("MIMO_SAHA::", "Permission<><> 1");
        MeshLog.v("onInterruptionEvent " + hardwareState + " " + permissions);
        PermissionInterruptionEvent permissionInterruptionEvent = new PermissionInterruptionEvent();
        permissionInterruptionEvent.hardwareState = hardwareState;
        permissionInterruptionEvent.permissions = permissions;

        AppDataObserver.on().sendObserverData(permissionInterruptionEvent);
    }

    public void onPayMessageAckReceived(String sender, String receiver, String messageId) {
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

    private void onTransportInit(String nodeId, String publicKey, boolean success, String msg) {

        MeshLog.v("onTransportInit dtm " + nodeId);
        TransportInit transportInit = new TransportInit();
        transportInit.nodeId = nodeId;
        transportInit.publicKey = publicKey;
        transportInit.success = success;
        transportInit.msg = msg;

        AppDataObserver.on().sendObserverData(transportInit);
    }


    private void onProbableSellerDisconnected(String sellerId) {

        MeshLog.v("onProbableSellerDisconnected dtm " + sellerId);
        SellerRemoved sellerRemoved = new SellerRemoved();
        sellerRemoved.sellerId = sellerId;

        AppDataObserver.on().sendObserverData(sellerRemoved);
    }

    private void onServiceUpdateNeeded(boolean isNeeded){
        ServiceUpdate serviceUpdate = new ServiceUpdate();
        serviceUpdate.isNeeded = isNeeded;
        AppDataObserver.on().sendObserverData(serviceUpdate);
    }


    public void writeLogIntoTxtFile(String text, boolean isAppend) {
        try {
            String sdCard = Constant.Directory.PARENT_DIRECTORY + Constant.Directory.MESH_LOG;
            File directory = new File(sdCard);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            if (Constant.CURRENT_LOG_FILE_NAME == null) {
                Constant.CURRENT_LOG_FILE_NAME = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date()) + ".txt";
            }
            File file = new File(directory, Constant.CURRENT_LOG_FILE_NAME);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(file, isAppend);

            OutputStreamWriter osw = new
                    OutputStreamWriter(fOut);

            osw.write("\n" + text);
            //  osw.append(text)
            osw.flush();
            osw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }
}
