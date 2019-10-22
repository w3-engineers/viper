package com.w3engineers.mesh.application.data.local;

import android.os.RemoteException;

/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

public abstract class BaseMeshDataSource {

    //Todo: This one will be used if we don't want to start a extra service

/*    private ITmServiceConnection iSetInfo;
    private Context context;
    private byte[] profileInfo;

    protected BaseMeshDataSource(Context context, byte[] profileInfo) {

        //intentional hard string
        if(context == null)
            throw new NullPointerException("Context can not be null");

        this.context = context;
        this.profileInfo = profileInfo;

        connectToService();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            iSetInfo = ITmServiceConnection.Stub.asInterface(service);

            try {
                iSetInfo.setServiceForeground(false);
                iSetInfo.setTmCommunicator(iGetInfo);
                iSetInfo.setProfileInfo(profileInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(iSetInfo != null){
                iSetInfo = null;
            }
        }
    };

    public String sendMeshData(MeshDataOld meshData) {
        try {
            if(iSetInfo != null){
                return iSetInfo.sendMeshData(meshData);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stopMeshService() {
        try {
            if(iSetInfo != null){
                iSetInfo.stopTmService();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopMeshProcess() {
        try {
            if(iSetInfo != null){
                iSetInfo.stopMeshProcess();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public String getMyMeshId() {
        try {
            if(iSetInfo != null){
                return iSetInfo.getMyId();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void restartMesh() {
        try {
            if(iSetInfo != null){
                iSetInfo.restartMeshService();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllSellers() {
        try {
            if(iSetInfo != null){
                return iSetInfo.getCurrentSellers();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public int getUserActiveStatus(String userId) {
        try {
            if(iSetInfo != null){
                return iSetInfo.getUserLinkType(userId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    *//**
     * To check underlying service properly initiated or not
     * @return true if connected
     *//*
    public boolean isServiceConnected() {
        return iSetInfo != null;
    }

    *//**
     * If service is not initiated properly then this method throws {@link IllegalStateException}.
     * Before using the method check service initiation through {@link #isServiceConnected()}
     * @param isForeGround - set boolean for foreground mode
     *//*
    public void setServiceForeground(boolean isForeGround) {

        if(iSetInfo == null) {
            throw new IllegalStateException("Service not initiated properly");
        }

        try {
            iSetInfo.setServiceForeground(isForeGround);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void connectToService() {
        //Normally this should not be required but found in some devices service restarts upon
        // calling start service. e.g: Symphony ZVi
        Intent serviceIntent = new Intent(context, MeshService.class);
        context.startService(serviceIntent);
        context.bindService(serviceIntent, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    *//**
     * Overridable method to receive the event of Library init
     * @throws RemoteException
     *//*
    protected abstract void onRmOn();

    *//**
     * Called upon receiving any Peer data
     * @param profileInfo
     *//*
    protected abstract void onPeer(BaseMeshData profileInfo);

    *//**
     * Calls upon disappearing of peers
     * @param meshPeer
     *//*
    protected abstract void onPeerGone(MeshPeer meshPeer);

    *//**
     * Upon receiving any data from any peer
     * @param meshData
     *//*
    protected abstract void onData(MeshDataOld meshData);

    *//**
     * Upon receiving Data delivery acknowledgement
     * @param meshAcknowledgement
     *//*
    protected abstract void onAcknowledgement(MeshAcknowledgement meshAcknowledgement);

    protected abstract String getOwnUserId();

    protected abstract boolean isNodeAvailable(String nodeId, int userActiveStatus);

    protected abstract void showLog(String log);

    protected abstract void nodeIdDiscovered(String nodeId);

    *//**
     * Overridable method to receive the event of library destroy
     * @throws RemoteException
     *//*
    protected abstract void onRmOff();

    ITmCommunicator.Stub iGetInfo = new ITmCommunicator.Stub() {
        @Override
        public void onLibraryInitSuccess() throws RemoteException {
            onRmOn();
        }

        @Override
        public void onServiceDestroy() throws RemoteException {
            onRmOff();
        }

        @Override
        public void onProfileInfo(BaseMeshData baseMeshData) throws RemoteException {
            onPeer(baseMeshData);
        }

        @Override
        public void onPeerRemoved(MeshPeer meshPeer) throws RemoteException {
            onPeerGone(meshPeer);
        }

        @Override
        public void onMeshData(MeshDataOld meshData) throws RemoteException {
            onData(meshData);
        }

        @Override
        public void onMeshAcknowledgement(MeshAcknowledgement meshAcknowledgement) throws RemoteException {
            onAcknowledgement(meshAcknowledgement);
        }

        @Override
        public boolean isNodeExist(String nodeId, int userActiveStatus) throws RemoteException {
            return isNodeAvailable(nodeId, userActiveStatus);
        }

        @Override
        public void showMeshLog(String log) throws RemoteException {
            showLog(log);
        }

        @Override
        public void nodeDiscovered(String nodeId) throws RemoteException {
            nodeIdDiscovered(nodeId);
        }
    };*/
}
