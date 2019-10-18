package com.w3engineers.ext.viper.util.lib.mesh;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

import android.content.Context;
import android.content.Intent;

import com.w3engineers.ext.viper.ClientLibraryService;
import com.w3engineers.ext.viper.util.Constant;

public class DataManager {

    private LinkStateListener linkStateListener;

    public static class AppDataManagerHolder {
        public static DataManager appDataManager = new DataManager();
    }

    public static DataManager getInstance() {
        return AppDataManagerHolder.appDataManager;
    }


    void doBindService(Context context, String networkPrefix, LinkStateListener linkStateListener) {
        this.linkStateListener = linkStateListener;
        Intent mIntent = new Intent(context, ClientLibraryService.class);
        mIntent.putExtra(Constant.IntentKey.SSID, networkPrefix);
        context.startService(mIntent);
    }

}
