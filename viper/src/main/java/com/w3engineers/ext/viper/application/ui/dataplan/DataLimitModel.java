package com.w3engineers.ext.viper.application.ui.dataplan;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.w3engineers.ext.viper.application.data.local.db.DatabaseService;
import com.w3engineers.ext.viper.application.data.local.helper.PreferencesHelper;


public class DataLimitModel {

    private boolean isDataLimited;
    private long fromDate;
    private long toDate;
    private int mInitialRole;
    private int numberOfDay = 7;

    public int getInitialRole() {
        return mInitialRole;
    }

    public void setInitialRole(int initialRole) {
        mInitialRole = initialRole;
    }

    private PreferencesHelper preferencesHelper;
    private LiveData<Long> usedData;
    private MutableLiveData<Long> sharedData = new MutableLiveData<>();
    private static DataLimitModel dataLimitModelObj;

    public DataLimitModel(Context context){
        this.preferencesHelper = PreferencesHelper.on();

        isDataLimited = preferencesHelper.getDataAmountMode() == 1;
        fromDate = preferencesHelper.getSellFromDate();
        toDate = preferencesHelper.getSellToDate();
        sharedData.postValue(preferencesHelper.getSellDataAmount());

        if (!isDataLimited){
            long toDate1 = System.currentTimeMillis();
            long fromDate1 = toDate1 - (numberOfDay*24*60*60*1000);
            usedData = DatabaseService.getInstance(context).getDatausageDao().getDataUsage(fromDate1, toDate1);
        } else {
            usedData = DatabaseService.getInstance(context).getDatausageDao().getDataUsage(fromDate, toDate);
        }
    }

    public static DataLimitModel getInstance(Context context){
        if (dataLimitModelObj == null){
            dataLimitModelObj = new DataLimitModel(context);
        }
        return dataLimitModelObj;
    }

    public long getFromDate() {
        return fromDate;
    }
    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
        preferencesHelper.setSellFromDate(fromDate);
    }

    public boolean getDataLimited(){
        return isDataLimited;
    }
    public void setDataLimited(boolean dataLimited) {
        isDataLimited = dataLimited;
        preferencesHelper.setDataAmountMode((dataLimited) ? 1 : 0);
    }

    public MutableLiveData<Long> getSharedData() {
        return sharedData;
    }
    public void setSharedData(Long sharedData) {
        this.sharedData.setValue(sharedData);
        preferencesHelper.setSellDataAmount(sharedData);
    }

    public long getToDate() {
        return toDate;
    }
    public void setToDate(long toDate) {
        this.toDate = toDate;
        preferencesHelper.setSellToDate(toDate);
    }

    public LiveData<Long> getUsedData() {
        return usedData;
    }

    public int getNumberOfDay(){
        return numberOfDay;
    }
}
