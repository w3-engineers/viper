package com.w3engineers.mesh.application.ui.dataplan;

import android.arch.lifecycle.MutableLiveData;

import com.w3engineers.ext.strom.application.ui.base.BaseRxViewModel;
import com.w3engineers.mesh.application.data.local.dataplan.DataPlan;

class DataPlanViewModel extends BaseRxViewModel implements DataPlan.DataPlanListener {



    void roleSwitch(int newRole) {
        DataPlan.getInstance().roleSwitch(newRole);
    }

    @Override
    public void onConnectingWithSeller(String sellerAddress) {

    }

    @Override
    public void onPurchaseFailed(String sellerAddress, String msg) {

    }

    @Override
    public void onPurchaseSuccess(String sellerAddress, double purchasedData, long blockNumber) {

    }

    @Override
    public void onPurchaseClosing(String sellerAddress) {

    }

    @Override
    public void onPurchaseCloseFailed(String sellerAddress, String msg) {

    }

    @Override
    public void onPurchaseCloseSuccess(String sellerAddress) {

    }

    @Override
    public void showToastMessage(String msg) {

    }

    @Override
    public void onBalancedFinished(String sellerAddress, int remain) {

    }

    @Override
    public void onTopUpFailed(String sellerAddress, String msg) {

    }
}
