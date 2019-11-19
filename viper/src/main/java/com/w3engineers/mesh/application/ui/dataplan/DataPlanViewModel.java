package com.w3engineers.mesh.application.ui.dataplan;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.w3engineers.ext.strom.application.ui.base.BaseRxAndroidViewModel;
import com.w3engineers.ext.strom.application.ui.base.BaseRxViewModel;
import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.dataplan.DataPlanManager;
import com.w3engineers.mesh.application.data.local.model.Seller;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

class DataPlanViewModel extends BaseRxAndroidViewModel {


    public DataPlanViewModel(@NonNull Application application) {
        super(application);
    }

    void roleSwitch(int newRole) {
        DataPlanManager.getInstance().roleSwitch(newRole);
    }

    MutableLiveData<List<Seller>> allSellers = new MutableLiveData<>();

    void getAllSellers() {

        getCompositeDisposable().add(DataPlanManager.getInstance().getAllSellers()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sellers -> {

                    List<Seller> onlineNotPurchased = new ArrayList<>();
                    List<Seller> onlinePurchased = new ArrayList<>();
                    List<Seller> offlinePurchased = new ArrayList<>();

                    List<Seller> finalList = new ArrayList<>();

                    for (Seller seller : sellers) {
                        if (seller.getLabel() == DataPlanConstants.SELLER_LABEL.ONLINE_NOT_PURCHASED) {
                            onlineNotPurchased.add(seller);
                        } else if (seller.getLabel() == DataPlanConstants.SELLER_LABEL.ONLINE_PURCHASED) {
                            onlinePurchased.add(seller);
                        } else if (seller.getLabel() == DataPlanConstants.SELLER_LABEL.OFFLINE_PURCHASED) {
                            offlinePurchased.add(seller);
                        }
                    }

                    if (onlineNotPurchased.size() > 0) {
//                        finalList.add(getLabelSeller(DataPlanConstants.SELLER_LABEL.ONLINE_NOT_PURCHASED));
                        finalList.addAll(onlineNotPurchased);
                    }

                    if (onlinePurchased.size() > 0) {
//                        finalList.add(getLabelSeller(DataPlanConstants.SELLER_LABEL.ONLINE_PURCHASED));
                        finalList.addAll(onlinePurchased);
                    }

                    if (offlinePurchased.size() > 0) {
//                        finalList.add(getLabelSeller(DataPlanConstants.SELLER_LABEL.OFFLINE_PURCHASED));
                        finalList.addAll(offlinePurchased);
                    }
                    allSellers.postValue(finalList);

                }, Throwable::printStackTrace));

        DataPlanManager.getInstance().processAllSeller(getApplication().getApplicationContext());
    }

    private Seller getLabelSeller(int tag) {
        return new Seller().setId("" + tag);
    }
}
