package com.w3engineers.ext.viper.util.lib.mesh;

import android.content.Context;

import com.w3engineers.ext.viper.application.data.remote.model.MeshPeer;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class TelemeshClient {

    private static TelemeshClient mTelemeshClient;
    private static final Object lock = new Object();

    protected TelemeshClient(Context context, Context activityContext, String networkPrefix) {

        DataManager.getInstance().doBindService();

    }

    public static TelemeshClient on(Context context, Context activityContext, String networkPrefix) {
        TelemeshClient instance = mTelemeshClient;
        if (instance == null) {
            synchronized (lock) {
                instance = mTelemeshClient;
                if (instance == null) {
                    instance = mTelemeshClient = new TelemeshClient(context, activityContext, networkPrefix);
                }
            }
        }
        return instance;
    }

    // Todo: here we will set all the api to contact with application and at the same time we will take the decision which AIDL interface have to call

    public void sendMessage(String senderId, String receiverId, String messageId, byte[] data) {

    }


    public int getLinkTypeById(String nodeID) {

        return 0;
    }

    /**
     * Listener is initialise here to get all kind of callback here
     * and after that send callback to app level
     */

    LinkStateListener linkStateListener = new LinkStateListener() {

        @Override
        public void onLocalUserConnected(String nodeId, byte[] data) {


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
