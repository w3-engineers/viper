package com.w3engineers.mesh.application.data.local.dataplan;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.text.TextUtils;

import com.w3engineers.eth.data.remote.EthereumService;
import com.w3engineers.mesh.application.data.ApiEvent;
import com.w3engineers.mesh.application.data.AppDataObserver;
import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.db.DatabaseService;
import com.w3engineers.mesh.application.data.local.db.purchase.Purchase;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.local.model.Seller;
import com.w3engineers.mesh.application.data.local.purchase.PayController;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseConstants;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerBuyer;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerSeller;
import com.w3engineers.mesh.application.ui.dataplan.DataPlanActivity;
import com.w3engineers.mesh.util.EthereumServiceUtil;
import com.w3engineers.mesh.util.lib.mesh.DataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.BehaviorSubject;


public class DataPlanManager {

    private static DataPlanManager dataPlanManager;

    private PreferencesHelperDataplan preferencesHelperDataplan;
    private DataPlanListener dataPlanListener;
    private PayController payController;

    private String currentSellerId;
    private String currentSellerStatus;
    private List<Seller> finalSeller;

    private BehaviorSubject<List<Seller>> sellers = BehaviorSubject.create();

    private DataPlanManager() {
        preferencesHelperDataplan = PreferencesHelperDataplan.on();
        payController = PayController.getInstance();
        finalSeller = new ArrayList<>();
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

        try {
            preferencesHelperDataplan.setDataPlanRole(newRole);
            payController.getDataManager().restartMesh(newRole);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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


    private void setSellers(List<Seller> sellerList) {
        sellers.onNext(sellerList);
    }

    public Flowable<List<Seller>> getAllSellers() {
        return sellers.toFlowable(BackpressureStrategy.LATEST);
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

    public void processAllSeller(Context context) {
        try {
            List<String> connectedSellers = payController.getDataManager().getInternetSellers();
            if (connectedSellers != null)
                processAllSeller(context, connectedSellers);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void setCurrentSeller(Context context, String sellerId, String currentSellerStatus) {
        this.currentSellerId = sellerId;
        this.currentSellerStatus = currentSellerStatus;

        if (TextUtils.isEmpty(currentSellerId)) {
            processAllSeller(context);
        }
    }

    private void processAllSeller(Context context, List<String> connectedSellers) {

        try {

            DatabaseService databaseService = DatabaseService.getInstance(context);
            EthereumService ethService = EthereumServiceUtil.getInstance(context).getEthereumService();

            List<Purchase> purchaseSellers = databaseService.getMyActivePurchases(ethService.getAddress());

            List<Seller> connectedWithPurchasesClose = new ArrayList<>();
            List<Seller> connectedWithPurchasesOpen = new ArrayList<>();

            for (int i = purchaseSellers.size() - 1; i >= 0; i--) {
                Purchase purchase = purchaseSellers.get(i);

                if (!TextUtils.isEmpty(purchase.sellerAddress) && connectedSellers.contains(purchase.sellerAddress)) {

                    if(purchase.balance < purchase.deposit) {
                        connectedWithPurchasesClose.add(purchase.toSeller(DataPlanConstants.SELLER_LABEL.ONLINE_PURCHASED));
                    } else {
                        connectedWithPurchasesOpen.add(purchase.toSeller(DataPlanConstants.SELLER_LABEL.ONLINE_NOT_PURCHASED));
                    }

                    connectedSellers.remove(purchase.sellerAddress);
                    purchaseSellers.remove(purchase);
                }
            }

            finalSeller.clear();

            if (connectedSellers.size() > 0) {

                for (String sellerId : connectedSellers) {
                    finalSeller.add(getSellerById(sellerId, DataPlanConstants.SELLER_LABEL.ONLINE_NOT_PURCHASED));
                }

                // Add all top up seller in connected seller list
                finalSeller.addAll(connectedWithPurchasesOpen);
            }

            if (connectedWithPurchasesClose.size() > 0) {
                // Add all close action seller
                // in connected with existed seller list
                finalSeller.addAll(connectedWithPurchasesClose);
            }

            if (purchaseSellers.size() > 0) {

                for (Purchase purchase : purchaseSellers) {

                    // Ony added seller for close action seller
                    // Because those are not connected
                    if(purchase.balance < purchase.deposit) {
                        finalSeller.add(purchase.toSeller(DataPlanConstants.SELLER_LABEL.OFFLINE_PURCHASED));
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

            setSellers(finalSeller);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void precessDisconnectedSeller(Context context, String sellerId) {
        if (finalSeller == null || finalSeller.size() == 0)
            return;

        for (Seller seller : finalSeller) {

            if (seller.getId().equals(sellerId)) {

                try {
                    List<String> connectedSellers = payController.getDataManager().getInternetSellers();
                    if (connectedSellers != null){
                        connectedSellers.remove(sellerId);

                        processAllSeller(context, connectedSellers);
                    }
                }catch (RemoteException e){
                    e.printStackTrace();
                }

                return;
            }
        }
    }

    private Seller getSellerById(String sellerId, int label) {
        return new Seller()
                .setId(sellerId)
                .setName(sellerId)
                .setPurchasedData(0)
                .setUsedData(0)
                .setBtnEnabled(true)
                .setLabel(label)
                .setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
    }


    private void setDataManagerObserver(){
        AppDataObserver.on().startObserver(ApiEvent.TRANSPORT_INIT, event -> {
            roleSwitchCompleted();
        });
    }
}