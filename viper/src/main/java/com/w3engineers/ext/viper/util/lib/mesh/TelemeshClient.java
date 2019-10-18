package com.w3engineers.ext.viper.util.lib.mesh;

import android.content.Context;

import com.w3engineers.ext.viper.application.data.remote.model.MeshPeer;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class TelemeshClient {

    private static TelemeshClient mTelemeshClient;

    //private constructor.
    private TelemeshClient(){
        //Prevent form the reflection api.
        if (mTelemeshClient != null){
            throw new RuntimeException("Use on() method to get the single instance of this class.");
        }
    }

    protected TelemeshClient(Context context, Context activityContext, String networkPrefix) {
        DataManager.getInstance().doBindService(context, networkPrefix, linkStateListener);
    }

    public static TelemeshClient on(Context context, Context activityContext, String networkPrefix) {

        //Double check locking pattern
        if (mTelemeshClient == null) { //Check for the first time

            synchronized (TelemeshClient.class) {   //Check for the second time.
                //if there is no instance available... create new one
                if (mTelemeshClient == null) mTelemeshClient = new TelemeshClient(context, activityContext, networkPrefix);
            }
        }
        return mTelemeshClient;
    }

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
