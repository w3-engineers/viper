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
import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.db.DatabaseService;
import com.w3engineers.mesh.application.data.local.db.networkinfo.NetworkInfo;
import com.w3engineers.mesh.application.data.local.db.networkinfo.WalletInfo;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseConstants;
import com.w3engineers.mesh.application.data.local.wallet.Wallet;
import com.w3engineers.mesh.util.MeshApp;

import java.util.concurrent.ExecutionException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class WalletViewModel extends BaseRxViewModel {

    public MutableLiveData<WalletInfo> networkMutableLiveData = new MutableLiveData<>();
    private Wallet wallet;

    public WalletViewModel() {
        wallet = Wallet.getInstance();
    }

    public void getCurrencyAmount() {
        getCompositeDisposable().add(wallet.getNetworkInfoByNetworkType()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(walletInfos -> {
                    for (WalletInfo walletInfo : walletInfos) {

                        if (walletInfo.networkType == PreferencesHelperPaylib.onInstance(MeshApp.getContext()).getEndpointMode()) {
                            networkMutableLiveData.postValue(walletInfo);
                        }
                    }
                }, Throwable::printStackTrace));
    }

    public LiveData<Double> getTotalEarn() {
        return wallet.getTotalEarn(wallet.getMyAddress(), wallet.getMyEndpoint());
    }

    public LiveData<Double> getTotalSpent() {
        return wallet.getTotalSpent(wallet.getMyAddress(), wallet.getMyEndpoint());
    }

    public LiveData<Double> getTotalPendingEarning() {
        return wallet.getTotalPendingEarning(wallet.getMyAddress(), wallet.getMyEndpoint());
    }

    public LiveData<Integer> getDifferentNetworkData(String myAddress) {
        return wallet.getDifferentNetworkData(wallet.getMyAddress(), wallet.getMyEndpoint());
    }
}
