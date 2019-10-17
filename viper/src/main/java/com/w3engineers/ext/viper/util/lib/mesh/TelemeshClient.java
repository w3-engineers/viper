package com.w3engineers.ext.viper.util.lib.mesh;

import android.content.Context;

import com.w3engineers.ext.viper.application.data.remote.model.MeshPeer;

public class TelemeshClient {

    private static TelemeshClient mTelemeshClient;
    private static final Object lock = new Object();

    protected TelemeshClient(Context context, Context activityContext, String networkPrefix, String multiverseUrl, LinkStateListener linkStateListener) {

        DataManager.getInstance().doBindService();

    }

    public static TelemeshClient on(Context context, Context activityContext, String networkPrefix, String multiverseUrl, LinkStateListener linkStateListener) {
        TelemeshClient instance = mTelemeshClient;
        if (instance == null) {
            synchronized (lock) {
                instance = mTelemeshClient;
                if (instance == null) {
                    instance = mTelemeshClient = new TelemeshClient(context, activityContext, networkPrefix, multiverseUrl, linkStateListener);
                }
            }
        }
        return instance;
    }

    // Todo: here we will set all the api to contact with application and at the same time we will take the decision which AIDL interface have to call


}
