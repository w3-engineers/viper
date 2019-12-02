package com.w3engineers.mesh.application.ui.dataplan;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.w3engineers.mesh.application.data.local.dataplan.DataPlanManager;


public class DataLimitModel {

    private boolean isDataLimited;
    private long fromDate;
    private int mInitialRole;
    private int numberOfDay = 7;

    private LiveData<Long> usedData;
    private long sharedData = 0;
    private static DataLimitModel dataLimitModelObj;

    public DataLimitModel(Context context){

        isDataLimited = DataPlanManager.getInstance().getDataAmountMode() == 1;
        fromDate = DataPlanManager.getInstance().getSellFromDate();

        sharedData = DataPlanManager.getInstance().getSellDataAmount();

        if (!isDataLimited){
            long toDate = System.currentTimeMillis();
            long fromDate1 = toDate - (numberOfDay*24*60*60*1000);
            usedData = DataPlanManager.getInstance().getDataUsage(context, fromDate1);
        } else {
            usedData = DataPlanManager.getInstance().getDataUsage(context, fromDate);
        }
    }

    public static DataLimitModel getInstance(Context context){
        if (dataLimitModelObj == null){
            dataLimitModelObj = new DataLimitModel(context);
        }
        return dataLimitModelObj;
    }

    public long getSharedData() {
        return sharedData;
    }

    public LiveData<Long> getUsedData() {
        return usedData;
    }

    public long getFromDate() {
        return fromDate;
    }

    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
        DataPlanManager.getInstance().setSellFromDate(fromDate);
    }

    public boolean isDataLimited(){
        return isDataLimited;
    }

    public void setDataLimited(boolean dataLimited) {
        isDataLimited = dataLimited;
        DataPlanManager.getInstance().setDataAmountMode((dataLimited) ? 1 : 0);
    }

    public void setSharedData(Long sharedData) {
        this.sharedData = sharedData;
        DataPlanManager.getInstance().setSellDataAmount(sharedData);
    }

//    public long getToDate() {
//        return toDate;
//    }
//    public void setToDate(long toDate) {
//        this.toDate = toDate;
//        DataPlanManager.getInstance().setSellToDate(toDate);
//    }

    public int getNumberOfDay(){
        return numberOfDay;
    }

    public int getInitialRole() {
        return mInitialRole;
    }

    public void setInitialRole(int initialRole) {
        mInitialRole = initialRole;
    }
}
