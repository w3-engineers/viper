package com.w3engineers.mesh.application.ui.dataplan;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.w3engineers.mesh.application.data.local.dataplan.DataPlan;


public class DataLimitModel {

    private boolean isDataLimited;
    private long fromDate;
    private long toDate;
    private int mInitialRole;
    private int numberOfDay = 7;

    private LiveData<Long> usedData;
    private MutableLiveData<Long> sharedData = new MutableLiveData<>();
    private static DataLimitModel dataLimitModelObj;

    public DataLimitModel(Context context){

        isDataLimited = DataPlan.getInstance().getDataAmountMode() == 1;
        fromDate = DataPlan.getInstance().getSellFromDate();
        toDate = DataPlan.getInstance().getSellToDate();
        sharedData.postValue(DataPlan.getInstance().getSellDataAmount());

        if (!isDataLimited){
            long toDate1 = System.currentTimeMillis();
            long fromDate1 = toDate1 - (numberOfDay*24*60*60*1000);
            usedData = DataPlan.getInstance().getDataUsage(context, fromDate1, toDate1);
        } else {
            usedData = DataPlan.getInstance().getDataUsage(context, fromDate, toDate);
        }
    }

    public static DataLimitModel getInstance(Context context){
        if (dataLimitModelObj == null){
            dataLimitModelObj = new DataLimitModel(context);
        }
        return dataLimitModelObj;
    }

    public MutableLiveData<Long> getSharedData() {
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
        DataPlan.getInstance().setSellFromDate(fromDate);
    }

    public boolean getDataLimited(){
        return isDataLimited;
    }

    public void setDataLimited(boolean dataLimited) {
        isDataLimited = dataLimited;
        DataPlan.getInstance().setDataAmountMode((dataLimited) ? 1 : 0);
    }

    public void setSharedData(Long sharedData) {
        this.sharedData.setValue(sharedData);
        DataPlan.getInstance().setSellDataAmount(sharedData);
    }

    public long getToDate() {
        return toDate;
    }
    public void setToDate(long toDate) {
        this.toDate = toDate;
        DataPlan.getInstance().setSellToDate(toDate);
    }

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
