package com.w3engineers.mesh.application.data.local.purchase;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.w3engineers.eth.data.helper.PreferencesHelperPaylib;
import com.w3engineers.eth.data.remote.EthereumService;
import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.db.DatabaseService;
import com.w3engineers.mesh.application.data.local.db.networkinfo.NetworkInfo;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.util.EthereumServiceUtil;
import com.w3engineers.mesh.util.MeshApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Flowable;

public class PurchaseManager {

    protected PayController payController;
    protected EthereumService ethService;
    protected Context mContext;
    protected DatabaseService databaseService;
    protected PreferencesHelperDataplan preferencesHelperDataplan;
    protected PreferencesHelperPaylib preferencesHelperPaylib;
    private static PurchaseManager purchaseManager;

    PurchaseManager(){
        payController = PayController.getInstance();
        mContext = MeshApp.getContext();
        ethService = EthereumServiceUtil.getInstance(mContext).getEthereumService();
        ethService.setCredential(payController.getCredentials());
        databaseService = DatabaseService.getInstance(mContext);
        preferencesHelperDataplan = PreferencesHelperDataplan.on();
        preferencesHelperPaylib = PreferencesHelperPaylib.onInstance(mContext);
    }

    public static PurchaseManager getInstance(){
        if (purchaseManager == null){
            purchaseManager = new PurchaseManager();
        }
        return purchaseManager;
    }


    protected void setEndPointInfoInJson(JSONObject jsonObject, int endPoint) throws JSONException {
        jsonObject.put(PurchaseConstants.JSON_KEYS.END_POINT_TYPE, endPoint);
    }

    public LiveData<Double> getTotalEarn(String myAddress, int endpoint) {
        try {
            return databaseService.getTotalEarnByUser(myAddress, endpoint);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public EthereumService getEthService(){
        return ethService;
    }

    public int getEndpoint(){
        return preferencesHelperPaylib.getEndpointMode();
    }

    public void setEndpoint(int endpoint){
        preferencesHelperPaylib.setEndPointMode(endpoint);
    }

    public LiveData<Double> getTotalSpent(String myAddress, int endPoint) {

        try {
            return databaseService.getTotalSpentByUser(myAddress, endPoint);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LiveData<Double> getTotalPendingEarning(String myAddress, int endpoint) {
        try {
            return databaseService.getTotalPendingEarningBySeller(myAddress, PurchaseConstants.CHANNEL_STATE.OPEN, endpoint);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Flowable<List<NetworkInfo>> getNetworkInfoByNetworkType() {
        try {
            return databaseService.getNetworkInfoByNetworkType();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
