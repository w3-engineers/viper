package com.w3engineers.ext.viper;
 
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
import android.os.RemoteException;
import android.support.annotation.Nullable;

public class ClientLibraryService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return viperCommunicator;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    ViperCommunicator.Stub viperCommunicator = new ViperCommunicator.Stub() {
        @Override
        public void userConnection(String userId, boolean isConnected) throws RemoteException {

        }

        @Override
        public void dataReceive(String userId, byte[] data) throws RemoteException {

        }
    };
}
