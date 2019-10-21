package com.w3engineers.ext.viper.application.ui.dataplan;

import android.arch.lifecycle.MutableLiveData;

import com.w3engineers.ext.strom.application.ui.base.BaseRxViewModel;
import com.w3engineers.ext.viper.application.data.local.model.Seller;
import com.w3engineers.ext.viper.util.Constant;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataPlanViewModel extends BaseRxViewModel {

    public MutableLiveData<List<Seller>> getBuyerUsers = new MutableLiveData<>();

    private List<Seller> prepareBuyerList() {
        List<Seller> sellerUsers = new ArrayList<>();
        Random random = new Random();
        String[] names = {"Danial Alvez", "Andre Russle", "Alvie D Costa", "Devid Warner", "Moin Aly"};
        int[] status = {Constant.SellerStatus.PURCHASE, Constant.SellerStatus.PURCHASING, Constant.SellerStatus.PURCHASED, Constant.SellerStatus.CONNECTING, Constant.SellerStatus.CONNECTED, Constant.SellerStatus.DISCONNECT, Constant.SellerStatus.DISCONNECTING,
                Constant.SellerStatus.DISCONNECTED, Constant.SellerStatus.CLOSE, Constant.SellerStatus.CLOSING, Constant.SellerStatus.CLOSED};

        for (String name : names) {
            int randomIndex = random.nextInt(3);
            Seller seller = new Seller();
            seller.setName(name);
            seller.setUsedData(randomIndex == 0 ? 0 : +random.nextInt(100));
            seller.setStatus(status[randomIndex]);

            sellerUsers.add(seller);
        }
        return sellerUsers;
    }

    public void getBuyerList() {
        getBuyerUsers.postValue(prepareBuyerList());
    }

}
