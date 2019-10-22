package com.w3engineers.mesh.application.data.local.purchase;

import android.content.Context;

import com.w3engineers.eth.data.remote.EthereumService;
import com.w3engineers.mesh.application.data.local.db.DatabaseService;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelper;

public class PurchaseManager {

    private PayController payController;
    private EthereumService ethService;
    private Context mContext;
    private DatabaseService databaseService;
    private PreferencesHelper preferencesHelper;
}
