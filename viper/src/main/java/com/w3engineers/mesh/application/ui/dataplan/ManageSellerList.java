package com.w3engineers.mesh.application.ui.dataplan;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.content.Context;
import android.text.TextUtils;

import com.w3engineers.eth.data.remote.EthereumService;
import com.w3engineers.mesh.application.data.local.db.DatabaseService;
import com.w3engineers.mesh.application.data.local.db.purchase.Purchase;
import com.w3engineers.mesh.application.data.local.model.Seller;
import com.w3engineers.mesh.application.data.local.purchase.PayController;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseConstants;
import com.w3engineers.mesh.util.EthereumServiceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.subjects.BehaviorSubject;

public class ManageSellerList {

    private static ManageSellerList manageSellerList;
    private DatabaseService databaseService;
    private PayController payController;
    private EthereumService ethService;
    private String currentSellerId;
    private String currentSellerStatus;
    private Context context;
    private List<Seller> finalSeller;

    private BehaviorSubject<List<Seller>> sellers = BehaviorSubject.create();

    private ManageSellerList(Context context) {
        this.context = context;
        finalSeller = new ArrayList<>();
        databaseService = DatabaseService.getInstance(context);
        payController = PayController.getInstance();
        ethService = EthereumServiceUtil.getInstance(context).getEthereumService();
    }

    public static ManageSellerList getInstance(Context context) {
        if (manageSellerList == null) {
            manageSellerList = new ManageSellerList(context);
        }
        return manageSellerList;
    }

    public void processAllUsers() {
        List<String> connectedSellers = payController.transportManager.getInternetSellers();
        if (connectedSellers != null)
            processAllUsers(connectedSellers);
    }

    public void setCurrentSeller(String sellerId, String currentSellerStatus) {
        this.currentSellerId = sellerId;
        this.currentSellerStatus = currentSellerStatus;

        if (TextUtils.isEmpty(currentSellerId)) {
            processAllUsers();
        }
    }

    public void processAllUsers(List<String> connectedSellers) {

        try {

            List<Purchase> purchaseSellers = databaseService.getMyActivePurchases(ethService.getAddress());

            List<Seller> connectedWithPurchasesClose = new ArrayList<>();
            List<Seller> connectedWithPurchasesOpen = new ArrayList<>();

            for (int i = purchaseSellers.size() - 1; i >= 0; i--) {
                Purchase purchase = purchaseSellers.get(i);

                if (!TextUtils.isEmpty(purchase.sellerAddress) && connectedSellers.contains(purchase.sellerAddress)) {

                    if(purchase.balance < purchase.deposit) {
                        connectedWithPurchasesClose.add(purchase.toSeller());
                    } else {
                        connectedWithPurchasesOpen.add(purchase.toSeller());
                    }

                    connectedSellers.remove(purchase.sellerAddress);
                    purchaseSellers.remove(purchase);
                }
            }

            finalSeller.clear();

            if (connectedSellers.size() > 0) {
                finalSeller.add(getLabelSeller(1));

                for (String sellerId : connectedSellers) {
                    finalSeller.add(getSellerById(sellerId));
                }

                // Add all top up seller in connected seller list
                finalSeller.addAll(connectedWithPurchasesOpen);
            }

            if (connectedWithPurchasesClose.size() > 0) {
                finalSeller.add(getLabelSeller(2));

                // Add all close action seller
                // in connected with existed seller list
                finalSeller.addAll(connectedWithPurchasesClose);
            }

            if (purchaseSellers.size() > 0) {
                finalSeller.add(getLabelSeller(3));

                for (Purchase purchase : purchaseSellers) {

                    // Ony added seller for close action seller
                    // Because those are not connected
                    if(purchase.balance < purchase.deposit) {
                        finalSeller.add(purchase.toSeller());
                    }
                }
            }

            if (currentSellerId != null) {
                for (int i = 0; i < finalSeller.size(); i++) {
                    Seller seller = finalSeller.get(i);

                    if (seller.getId().equals(currentSellerId)) {

                        seller.setBtnEnabled(!currentSellerStatus.equals(PurchaseConstants.SELLERS_BTN_TEXT.PURCHASING)
                                && !currentSellerStatus.equals(PurchaseConstants.SELLERS_BTN_TEXT.CLOSING));
                        seller.setBtnText(currentSellerStatus);

                        finalSeller.set(i, seller);
                        break;
                    }
                }
            }

            sellers.onNext(finalSeller);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void precessDisconnectedSeller(String sellerId) {
        if (finalSeller == null || finalSeller.size() == 0)
            return;

        for (Seller seller : finalSeller) {

            if (seller.getId().equals(sellerId)) {

                //
                List<String> connectedSellers = payController.transportManager.getInternetSellers();
                if (connectedSellers != null){
                    connectedSellers.remove(sellerId);

                    processAllUsers(connectedSellers);
                }


                return;
            }
        }
    }

    private Seller getSellerById(String sellerId) {
        return new Seller()
                .setId(sellerId)
                .setName(sellerId)
                .setPurchasedData(0)
                .setUsedData(0)
                .setBtnEnabled(true)
                .setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
    }

    private Seller getLabelSeller(int tag) {
        return new Seller().setId("" + tag);
    }

    public LiveData<List<Seller>> getAllSellers() {
        return LiveDataReactiveStreams.fromPublisher(sellers.toFlowable(BackpressureStrategy.LATEST));
    }
}
