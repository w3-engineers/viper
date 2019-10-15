package com.w3engineers.ext.viper.application.data.local.service;
 
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
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.w3engineers.ext.strom.util.Text;
import com.w3engineers.ext.viper.IRmCommunicator;
import com.w3engineers.ext.viper.IRmServiceConnection;
import com.w3engineers.ext.viper.application.data.remote.model.BaseMeshData;
import com.w3engineers.ext.viper.application.data.remote.model.MeshData;
import com.w3engineers.ext.viper.application.data.remote.service.BaseRmServiceNotificationHelper;
import com.w3engineers.ext.viper.util.lib.mesh.MeshConfig;

import java.util.ArrayList;
import java.util.List;
// TODO mesh
public class MeshService extends Service /*implements MeshProvider.ProviderCallback*/ {

    private IRmCommunicator getInfo;
    // TODO mesh
//    private MeshProvider meshProvider;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void initMesh(byte[] profileInfo) {
        if (profileInfo == null)
            return;

        //Please do not modify here, this is an auto generated property from developers
        //local.properties class
        MeshConfig meshConfig = new MeshConfig();
        meshConfig.mPort = 10626;

        // TODO mesh
        /*meshProvider = MeshProvider.getInstance();

        meshProvider.setConfig(meshConfig);
        meshProvider.setMyProfileInfo(profileInfo);
        meshProvider.setProviderCallback(this);

        meshProvider.startMesh();*/
    }

    private void startInForeground(){
        new BaseRmServiceNotificationHelper(MeshService.this).startForegroundService();
    }

    private void stopInForeground() {
        new BaseRmServiceNotificationHelper(MeshService.this).stopForegroundService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // TODO mesh
        /*if (meshProvider != null) {
            meshProvider.stopMesh(true);
        }*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            if(Text.isNotEmpty(action)) {
                if (BaseRmServiceNotificationHelper.ACTION_STOP_SERVICE.equals(action)) {
                    closeProcess();
                }
            }
        }

        return START_STICKY;
    }

    //Stopping service
    private void closeProcess() {
        new BaseRmServiceNotificationHelper(this).stopForegroundService();
        stopTheService(true);
        stopSelf();
    }

    public void stopProcess() {
        Process.killProcess(Process.myPid());
    }

    private void stopTheService(boolean isStopProcess) {
        // TODO mesh
        /*if (meshProvider != null) {
            meshProvider.stopMesh(isStopProcess);
        }*/
    }

    IRmServiceConnection.Stub iRmServiceConnection = new IRmServiceConnection.Stub() {
        @Override
        public void setBroadCastActionString(String actionString) throws RemoteException {

        }

        @Override
        public void setServiceToCloseWithTask(boolean isToCloseWithTask) throws RemoteException {

        }

        @Override
        public void setProfile(byte[] profileInfo, String userId) throws RemoteException {
            initMesh(profileInfo);
        }

        @Override
        public void setProfileInfo(byte[] profileInfo) throws RemoteException {
            initMesh(profileInfo);
        }

        @Override
        public String sendMeshData(MeshData meshData) throws RemoteException {
            // TODO mesh
            return /*meshProvider.sendMeshData(meshData)*/ null;
        }

        @Override
        public void setRmCommunicator(IRmCommunicator iRmCommunicator) throws RemoteException {
            getInfo = iRmCommunicator;
        }

        @Override
        public void setServiceForeground(boolean isForeGround) throws RemoteException {
            if(isForeGround){
                startInForeground();
            }else {
                stopInForeground();
            }
        }

        @Override
        public void resetCommunicator(IRmCommunicator iRmCommunicator) throws RemoteException {

        }

        @Override
        public List<BaseMeshData> getLivePeers() throws RemoteException {
            return null;
        }

        @Override
        public void openRmSettings() throws RemoteException {

        }

        @Override
        public void stopRmService() throws RemoteException {
            stopTheService(false);
        }

        @Override
        public void stopMeshProcess() throws RemoteException {
            stopProcess();
        }

        @Override
        public String getMyId() throws RemoteException {
            // TODO mesh
            return /*meshProvider.getMyUserId()*/ null;
        }

        @Override
        public void restartMeshService() throws RemoteException {
            // TODO mesh
            /*if (meshProvider != null) {
                meshProvider.restartMesh();
            }*/
        }

        @Override
        public List<String> getCurrentSellers() throws RemoteException {
            // TODO mesh
            /*if (meshProvider != null) {
                return meshProvider.getAllSellers();
            }*/
            return new ArrayList<>();
        }

        @Override
        public int getUserLinkType(String userId) throws RemoteException {
            // TODO mesh
            /*if (meshProvider != null) {
                return meshProvider.getUserActiveStatus(userId);
            }*/
            return 0;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Any task is available now as connection is being established
        return iRmServiceConnection;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Any task is available now as connection is being established
        return false;
    }

    // TODO mesh
    /*@Override
    public void meshStart() {
        try {
            if (getInfo != null) {
                getInfo.onLibraryInitSuccess();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionAdd(MeshData meshData) {
        try {
            if (getInfo != null) {
                getInfo.onProfileInfo(meshData);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionRemove(MeshPeer meshPeer) {
        try {
            if (getInfo != null) {
                getInfo.onPeerRemoved(meshPeer);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveData(MeshData meshData) {
        try {
            if (getInfo != null) {
                getInfo.onMeshData(meshData);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveAck(MeshAcknowledgement meshAcknowledgement) {
        try {
            if (getInfo != null) {
                getInfo.onMeshAcknowledgement(meshAcknowledgement);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void meshStop() {
        try {
            if (getInfo != null) {
                getInfo.onServiceDestroy();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isNodeExist(String nodeId, int userActiveStatus) {
        try {
            if (getInfo != null) {
                return getInfo.isNodeExist(nodeId, userActiveStatus);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void showMeshLog(String log) {
        try {
            if (getInfo != null) {
                getInfo.showMeshLog(log);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onlyNodeDiscover(String nodeId) {
        try {
            if (getInfo != null) {
                getInfo.nodeDiscovered(nodeId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }*/
}
