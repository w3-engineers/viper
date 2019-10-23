package com.w3engineers.mesh.application.data.local.dataplan;

import android.content.Context;
import android.content.Intent;

import com.w3engineers.mesh.application.ui.dataplan.DataPlanActivity;


public class DataPlan {

    private boolean isDataLimited;
    private long fromDate;
    private long toDate;
    private int mInitialRole;
    private int numberOfDay = 7;
    private static DataPlan dataPlan;

    public static DataPlan getInstance(){
        if (dataPlan == null){
            dataPlan = new DataPlan();
        }
        return dataPlan;
    }

    public static void openActivity(Context context){
        Intent intent = new Intent(context, DataPlanActivity.class);
        context.startActivity(intent);
    }

    public int getDataPlanMode(){
        return 0;
    }
    public void setDataPlanMode(int mode){

    }

    public int getDataLimitMode(){
        return 0;
    }
    public void setDataLimitMode(int mode){

    }

    public double getDataLimit(){
        return 0;
    }
    public void setDataLimit(double mode){

    }

    public long getFromDate() {
        return fromDate;
    }
    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }
    public interface PurchaseManagerBuyerListener {
        void onConnectingWithSeller(String sellerAddress);

        void onPurchaseFailed(String sellerAddress, String msg);

        void onPurchaseSuccess(String sellerAddress, double purchasedData, long blockNumber);

        void onPurchaseClosing(String sellerAddress);

        void onPurchaseCloseFailed(String sellerAddress, String msg);

        void onPurchaseCloseSuccess(String sellerAddress);

        void showToastMessage(String msg);

        void onBalancedFinished(String sellerAddress, int remain);

        void onTopUpFailed(String sellerAddress, String msg);
    }

    public int getDataPlanRole() {
        //TODO
//        return preferencesHelperDataplan.getDataPlanRole();
        return 1;
    }



}
