package com.w3engineers.mesh.util.lib.mesh;

import android.content.Context;

public class ViperClient {

    private static ViperClient mViperClient;

    private ViperClient(){
        //Prevent form the reflection api.
        if (mViperClient != null){
            throw new RuntimeException("Use on() method to get the single instance of this class.");
        }
    }

    protected ViperClient(Context context, String networkPrefix) {
        DataManager.getInstance().doBindService(context, networkPrefix, linkStateListener);
    }

    public static ViperClient on(Context context, String networkPrefix) {
        if (mViperClient == null) {
            synchronized (ViperClient.class) {
                if (mViperClient == null) mViperClient = new ViperClient(context, networkPrefix);
            }
        }
        return mViperClient;
    }

    public void sendMessage(String senderId, String receiverId, String messageId, byte[] data) {
        DataManager.getInstance().sendData(senderId, receiverId, messageId, data);
    }


    public int getLinkTypeById(String nodeID) {

        return DataManager.getInstance().getLinkTypeById(nodeID);

    }

    /**
     * Listener is initialise here to get all kind of callback here
     * and after that send callback to app level
     */

    LinkStateListener linkStateListener = new LinkStateListener() {

        @Override
        public void onLocalUserConnected(String nodeId) {


        }

        @Override
        public void onRemoteUserConnected(String nodeId) {

        }

        @Override
        public void onUserDisconnected(String nodeId) {

        }

        @Override
        public void onMessageReceived(String senderId, byte[] frameData) {

        }


        @Override
        public void onMessageDelivered(String messageId, int status) {

        }

    };

}
