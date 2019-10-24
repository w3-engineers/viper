package com.w3engineers.mesh.application.data.local.dataplan;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;

import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.db.DatabaseService;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerBuyer;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerSeller;
import com.w3engineers.mesh.application.ui.dataplan.DataPlanActivity;

import java.util.concurrent.ExecutionException;


public class DataPlanManager {

    private static DataPlanManager dataPlanManager;

    private PreferencesHelperDataplan preferencesHelperDataplan;
    private DataPlanListener dataPlanListener;

    private DataPlanManager() {
        preferencesHelperDataplan = PreferencesHelperDataplan.on();
    }

    public static DataPlanManager getInstance(){
        if (dataPlanManager == null){
            dataPlanManager = new DataPlanManager();
        }
        return dataPlanManager;
    }

    public static void openActivity(Context context){
        Intent intent = new Intent(context, DataPlanActivity.class);
        context.startActivity(intent);
    }

    public void setDataPlanListener(DataPlanListener dataPlanListener) {
        this.dataPlanListener = dataPlanListener;

        if (getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER) {
            PurchaseManagerBuyer.getInstance().setDataPlanListener(dataPlanListener);
        }
    }

    public interface DataPlanListener {

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

    public void roleSwitch(int newRole) {
        // TODO Dataplan need to sync with mesh
//        TransportManager.getInstance().setNetworkModeListener(this::onTransportInit);
//        TransportManager.getInstance().restart();
        preferencesHelperDataplan.setDataPlanRole(newRole);
    }

    private void roleSwitchCompleted() {

        if (getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER) {

            PurchaseManagerBuyer.getInstance().setDataPlanListener(dataPlanListener);
            PurchaseManagerSeller.getInstance().destroyObject();

        } else if (getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER) {

            PurchaseManagerBuyer.getInstance().setDataPlanListener(null);
            PurchaseManagerBuyer.getInstance().destroyObject();
        }
    }

    public int getDataPlanRole() {
        return preferencesHelperDataplan.getDataPlanRole();
    }

    public long getSellAmountData() {
        return preferencesHelperDataplan.getSellDataAmount();
    }

    public int getDataAmountMode() {
        return preferencesHelperDataplan.getDataAmountMode();
    }

    public long getSellFromDate() {
        return preferencesHelperDataplan.getSellFromDate();
    }

    public long getSellToDate() {
        return preferencesHelperDataplan.getSellDataAmount();
    }

    public long getSellDataAmount() {
        return preferencesHelperDataplan.getSellDataAmount();
    }


    public void setSellFromDate(long fromDate) {
        preferencesHelperDataplan.setSellFromDate(fromDate);
    }

    public void setDataAmountMode(int mode) {
        preferencesHelperDataplan.setDataAmountMode(mode);
    }

    public void setSellDataAmount(Long sharedData) {
        preferencesHelperDataplan.setSellDataAmount(sharedData);
    }

    public void setSellToDate(long toDate) {
        preferencesHelperDataplan.setSellToDate(toDate);
    }

    public long getUsedData(Context context, long fromDate, long toDate) throws ExecutionException, InterruptedException {
        return DatabaseService.getInstance(context).getDataUsageByDate(fromDate, toDate);
    }

    public LiveData<Long> getDataUsage(Context context, long fromDate, long toDate) {
        return DatabaseService.getInstance(context).getDatausageDao().getDataUsage(fromDate, toDate);
    }

    public void closeAllActiveChannel() {
        PurchaseManagerSeller.getInstance().closeAllActiveBuyerChannel();
    }

    public void initPurchase(double amount, String sellerId) {
        PurchaseManagerBuyer.getInstance().buyData(amount, sellerId);
    }

    public void closePurchase(String sellerId) {
        PurchaseManagerBuyer.getInstance().closePurchase(sellerId);
    }
}
