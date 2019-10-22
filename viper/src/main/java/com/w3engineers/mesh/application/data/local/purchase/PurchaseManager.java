package com.w3engineers.mesh.application.data.local.purchase;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.w3engineers.eth.data.helper.PreferencesHelperPaylib;
import com.w3engineers.eth.data.remote.EthereumService;
import com.w3engineers.mesh.application.data.local.db.DatabaseService;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.util.EthereumServiceUtil;
import com.w3engineers.mesh.util.MeshApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class PurchaseManager {

    protected PayController payController;
    protected EthereumService ethService;
    protected Context mContext;
    protected DatabaseService databaseService;
    protected PreferencesHelperDataplan preferencesHelperDataplan;
    protected PreferencesHelperPaylib preferencesHelperPaylib;

    PurchaseManager(){
        payController = PayController.getInstance();
        mContext = MeshApp.getContext();
        ethService = EthereumServiceUtil.getInstance(mContext).getEthereumService();
        ethService.setCredential(payController.getCredentials());
        databaseService = DatabaseService.getInstance(mContext);
        preferencesHelperDataplan = PreferencesHelperDataplan.on();
        preferencesHelperPaylib = PreferencesHelperPaylib.onInstance(mContext);
    }


    public LiveData<Double> getTotalEarnByUser(int endPointType) throws ExecutionException, InterruptedException {
        return databaseService.getTotalEarnByUser(ethService.getAddress(), endPointType);
    }

    public LiveData<Double> getTotalSpentByUser(int endPointType) throws ExecutionException, InterruptedException {
        return databaseService.getTotalSpentByUser(ethService.getAddress(), endPointType);
    }
    protected void setEndPointInfoInJson(JSONObject jsonObject, int endPoint) throws JSONException {
        jsonObject.put(PurchaseConstants.JSON_KEYS.END_POINT_TYPE, endPoint);
    }
}
