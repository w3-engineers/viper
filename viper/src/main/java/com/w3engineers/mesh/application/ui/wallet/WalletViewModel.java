package com.w3engineers.mesh.application.ui.wallet;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.w3engineers.eth.data.helper.PreferencesHelperPaylib;
import com.w3engineers.ext.strom.application.ui.base.BaseRxViewModel;
import com.w3engineers.mesh.application.data.local.db.DatabaseService;
import com.w3engineers.mesh.application.data.local.db.networkinfo.NetworkInfo;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseConstants;
import com.w3engineers.mesh.util.MeshApp;

import java.util.concurrent.ExecutionException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class WalletViewModel extends BaseRxViewModel {

    public MutableLiveData<NetworkInfo> networkMutableLiveData = new MutableLiveData<>();
    public MutableLiveData<Double> pendingEarningMutable = new MutableLiveData<>();
    public MutableLiveData<Double> totalEarnMutable = new MutableLiveData<>();

    private DatabaseService databaseService;

    public WalletViewModel() {
        databaseService = DatabaseService.getInstance(MeshApp.getContext());
    }

    public void getCurrencyAmount() {
        try {
            getCompositeDisposable().add(databaseService.getNetworkInfoByNetworkType()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(networkInfos -> {
                        for (NetworkInfo networkInfo : networkInfos) {

                            if (networkInfo.networkType == PreferencesHelperPaylib.onInstance(MeshApp.getContext()).getEndpointMode()) {
                                networkMutableLiveData.postValue(networkInfo);
                            }
                        }
                    }, Throwable::printStackTrace));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public LiveData<Double> getTotalEarn(String myAddress) {
        try {
            int endPointType = PreferencesHelperPaylib.onInstance(MeshApp.getContext()).getEndpointMode();

            return databaseService.getTotalEarnByUser(myAddress, endPointType);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LiveData<Double> getTotalSpent(String myAddress) {

        try {
            int endPointType = PreferencesHelperPaylib.onInstance(MeshApp.getContext()).getEndpointMode();
            return databaseService.getTotalSpentByUser(myAddress, endPointType);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LiveData<Double> getTotalPendingEarning(String myAddress) {
        try {
            int endPointType = PreferencesHelperPaylib.onInstance(MeshApp.getContext()).getEndpointMode();

            return databaseService.getTotalPendingEarningBySeller(myAddress, PurchaseConstants.CHANNEL_STATE.OPEN, endPointType);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LiveData<Integer> getDifferentNetworkData(String myAddress) {
        try {
            int endPointType = PreferencesHelperPaylib.onInstance(MeshApp.getContext()).getEndpointMode();

            if (PreferencesHelperDataplan.on().getDataShareMode() ==
                    PreferencesHelperDataplan.DATA_SELLER) {

                return databaseService.getDifferentNetworkData(myAddress, endPointType);

            } else if (PreferencesHelperDataplan.on().getDataShareMode() ==
                    PreferencesHelperDataplan.DATA_BUYER) {

                return databaseService.getDifferentNetworkPurchase(myAddress, endPointType);
            }

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
