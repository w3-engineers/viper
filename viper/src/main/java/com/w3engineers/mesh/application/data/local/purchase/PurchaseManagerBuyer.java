package com.w3engineers.mesh.application.data.local.purchase;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.net.Network;
import android.os.RemoteException;
import android.text.TextUtils;
import android.widget.Toast;
import android.support.v7.app.AlertDialog;

import com.w3engineers.eth.contracts.RaidenMicroTransferChannels;
import com.w3engineers.eth.contracts.TmeshToken;
import com.w3engineers.eth.data.remote.EthereumService;
import com.w3engineers.eth.util.data.NetworkMonitor;
import com.w3engineers.eth.util.helper.HandlerUtil;
import com.w3engineers.ext.strom.util.helper.Toaster;
import com.w3engineers.mesh.application.data.local.dataplan.DataPlanManager;
import com.w3engineers.mesh.application.data.local.db.DatabaseService;
import com.w3engineers.mesh.application.data.local.db.datausage.Datausage;
import com.w3engineers.mesh.application.data.local.db.purchase.Purchase;
import com.w3engineers.mesh.application.data.local.db.purchaserequests.PurchaseRequests;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.local.helper.crypto.CryptoHelper;
import com.w3engineers.mesh.application.data.local.wallet.WalletManager;
import com.w3engineers.mesh.application.ui.util.ToastUtil;
import com.w3engineers.mesh.util.DialogUtil;
import com.w3engineers.mesh.util.EthereumServiceUtil;
import com.w3engineers.mesh.util.MeshApp;
import com.w3engineers.mesh.util.MeshLog;
import com.w3engineers.mesh.util.NotificationUtil;
import com.w3engineers.mesh.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PurchaseManagerBuyer extends PurchaseManager implements PayController.PayControllerListenerForBuyer, EthereumService.TransactionObserverBuyer, NetworkMonitor.NetworkInterfaceListener {
    private static PurchaseManagerBuyer purchaseManagerBuyer;
    private double totalDataAmount;
    private PurchaseManagerBuyerListener purchaseManagerBuyerListener;

    private boolean isWarningShown;
    private String giftRequestedSeller = null;

    private WalletManager.WalletListener walletListener;
    private String probableSellerId = null;
    private AlertDialog datalimitAlert = null;

    private PurchaseManagerBuyer() {
        super();
        setPayControllerListener();
        ethService.setTransactionObserverBuyer(this);
        NetworkMonitor.setNetworkInterfaceListeners(this::onNetworkAvailable);
    }

    public static PurchaseManagerBuyer getInstance() {
        if (purchaseManagerBuyer == null) {
            purchaseManagerBuyer = new PurchaseManagerBuyer();
        }
        return purchaseManagerBuyer;
    }





    public void setWalletListener(WalletManager.WalletListener walletListener) {
        this.walletListener = walletListener;
    }

    //***************************************************//
    //******************Private Methods******************//
    //***************************************************//
    private void prepareChannel(String sellerAddress, Purchase purchase) {
        MeshLog.p("prepareChannel: ");
        if (purchase != null) {
            topupChannel(sellerAddress, purchase.blockChainEndpoint);
        } else {
            sendPurchaseMsg(sellerAddress);
        }
    }

    private void topupChannel(String fromAddress, int endPointType) {
        String query = PurchaseConstants.INFO_KEYS.ETH_BALANCE + "," + PurchaseConstants.INFO_KEYS.TKN_BALANCE + "," + PurchaseConstants.INFO_KEYS.ALOWANCE + "," + PurchaseConstants.INFO_KEYS.NONCE + "," + PurchaseConstants.INFO_KEYS.PENDING_REQUEST_NUMBER + "," + PurchaseConstants.INFO_KEYS.SHARED_DATA;
        getMyInfo(fromAddress, query, PurchaseConstants.INFO_PURPOSES.TOPUP_CHANNEL, endPointType);
    }

    private void sendTopupRequest(String sellerId, double price, double allowance, int nonce, int endPointType) {

        try {

            Purchase currentPurchase = databaseService.getPurchaseByState(PurchaseConstants.CHANNEL_STATE.OPEN, ethService.getAddress(), sellerId);
            if (currentPurchase != null) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());

                JSONArray array = new JSONArray();

                if (allowance > 0 && allowance < price) {
                    String signedMessage_approve_zero = ethService.approve(0, nonce, endPointType);
                    MeshLog.v("signedMessage_approve_zero " + signedMessage_approve_zero);
                    JSONObject approveZero = new JSONObject();
                    approveZero.put(PurchaseConstants.JSON_KEYS.REQUEST_TYPE, PurchaseConstants.REQUEST_TYPES.APPROVE_ZERO);
                    approveZero.put(PurchaseConstants.JSON_KEYS.SIGNED_MESSAGE, signedMessage_approve_zero);
                    approveZero.put(PurchaseConstants.JSON_KEYS.REQUEST_VALUE, 0);
                    approveZero.put(PurchaseConstants.JSON_KEYS.NONCE, nonce);
                    array.put(approveZero);
                    nonce++;
                }

                if (allowance < price) {
                    String signedMessage_approve = ethService.approve(price, nonce, endPointType);
                    MeshLog.v("signedMessage_approve " + signedMessage_approve);
                    JSONObject approveToken = new JSONObject();
                    approveToken.put(PurchaseConstants.JSON_KEYS.REQUEST_TYPE, PurchaseConstants.REQUEST_TYPES.APPROVE_TOKEN);
                    approveToken.put(PurchaseConstants.JSON_KEYS.SIGNED_MESSAGE, signedMessage_approve);
                    approveToken.put(PurchaseConstants.JSON_KEYS.REQUEST_VALUE, price);
                    approveToken.put(PurchaseConstants.JSON_KEYS.NONCE, nonce);
                    array.put(approveToken);
                    nonce++;
                }

                String signedMessage_create = ethService.topup(sellerId, currentPurchase.openBlockNumber, price, nonce, endPointType);
                MeshLog.v("signedMessage_create " + signedMessage_create);
                JSONObject create = new JSONObject();
                create.put(PurchaseConstants.JSON_KEYS.REQUEST_TYPE, PurchaseConstants.REQUEST_TYPES.TOPUP_CHANNEL);
                create.put(PurchaseConstants.JSON_KEYS.SIGNED_MESSAGE, signedMessage_create);
                create.put(PurchaseConstants.JSON_KEYS.REQUEST_VALUE, price);
                create.put(PurchaseConstants.JSON_KEYS.NONCE, nonce);
                array.put(create);

                jsonObject.put(PurchaseConstants.JSON_KEYS.REQUEST_LIST, array);

                setEndPointInfoInJson(jsonObject, endPointType);
                payController.sendBlockChainRequest(jsonObject, sellerId, PurchaseConstants.INFO_PURPOSES.TOPUP_CHANNEL);
            } else {
                //1
                setCurrentSellerWithStatus(null, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
                if (dataPlanListener != null) {
                    dataPlanListener.onPurchaseFailed(sellerId, "no purchase");
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getMyInfo(String receiver, String qyery, int purpose, int endPointType) {
        MeshLog.p("getMyInfo " + receiver);
        try {
            JSONObject msgObject = new JSONObject();
            msgObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
            msgObject.put(PurchaseConstants.JSON_KEYS.INFO_KEYS, qyery);
            msgObject.put(PurchaseConstants.JSON_KEYS.INFO_PURPOSE, purpose);

            setEndPointInfoInJson(msgObject, endPointType);

            payController.sendInfoQueryMessage(msgObject, receiver, purpose);
        } catch (Exception e) {
            MeshLog.v("Exception" + e.getMessage());
        }
    }

    private void purchaseCloseFailed(String sellerId, String s) {
        //2
//        setCurrentSellerWithStatus(sellerId, PurchaseConstants.SELLERS_BTN_TEXT.CLOSE);

        if (dataPlanListener != null) {
            dataPlanListener.onPurchaseCloseFailed(sellerId, s);
        }
    }

    private void setCurrentSellerWithStatus(String id, String status) {
        DataPlanManager.getInstance().setCurrentSeller(mContext, id, status);
    }

    private boolean giftEther(String sellerAddress) {
        MeshLog.v("giftEther start");
        try {

            PreferencesHelperDataplan preferencesHelperDataplan = PreferencesHelperDataplan.on();

            int endPointMode = getEndpoint();
            int requestState = preferencesHelperDataplan.getEtherRequestStatus(endPointMode);

            if (requestState == PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET ||
                    requestState == PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER) {

                long currentTime = new Date().getTime();
                long requestTime = preferencesHelperDataplan.getEtherRequestTimeStamp(endPointMode);
                if (currentTime > (requestTime + 20000)){

                    String requestData = getRequestData();
                    if (requestData != null){
                        giftRequestedSeller = sellerAddress;

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                        jsonObject.put(PurchaseConstants.JSON_KEYS.GIFT_REQUEST_DATA, requestData);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.USER_PUBLIC_KEY, payController.walletService.getPublicKey());

                        setEndPointInfoInJson(jsonObject, endPointMode);

                        preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER, endPointMode);
                        preferencesHelperDataplan.setEtherRequestTimeStamp(currentTime, endPointMode);
//                    preferencesHelperDataplan.setGiftEndpointType(endPointMode);
                        payController.sendGiftRequest(jsonObject, sellerAddress);
                    }
                    return true;
                }
            } else if (requestState == PurchaseConstants.GIFT_REQUEST_STATE.GOT_TRANX_HASH) {

                String ethTranxHash = preferencesHelperDataplan.getGiftEtherHash(endPointMode);
                String tknTranxHash = preferencesHelperDataplan.getGiftTokenHash(endPointMode);
//                int endPointType = preferencesHelperDataplan.getGiftEndpointType();

                if (!TextUtils.isEmpty(ethTranxHash) && !TextUtils.isEmpty(tknTranxHash)) {
                    double ethValue = preferencesHelperDataplan.getGiftEtherValue(endPointMode);
                    double tknValue = preferencesHelperDataplan.getGiftTokenValue(endPointMode);


                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                    jsonObject.put(PurchaseConstants.JSON_KEYS.GIFT_ETH_HASH_REQUEST_SUBMIT, ethTranxHash);
                    jsonObject.put(PurchaseConstants.JSON_KEYS.GIFT_TKN_HASH_REQUEST_SUBMIT, tknTranxHash);
                    jsonObject.put(PurchaseConstants.JSON_KEYS.GIFT_ETH_BALANCE, ethValue);
                    jsonObject.put(PurchaseConstants.JSON_KEYS.GIFT_TKN_BALANCE, tknValue);

                    setEndPointInfoInJson(jsonObject, endPointMode);

                    payController.sendGiftRequestWthHash(jsonObject, sellerAddress);
                    return true;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void sendPurchaseMsg(String sellerAddress) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("fr", ethService.getAddress());
            jsonObject.put("text", "wanna purchase data");

            int endPointMode = getEndpoint();

            setEndPointInfoInJson(jsonObject, endPointMode);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        payController.sendInitPurchase(jsonObject, sellerAddress);
    }

    //***************************************************//
    //******************Public Medthods******************//
    //***************************************************//
    public void buyData(double amount, String sellerAddress) {
        try {


            if (!payController.getDataManager().isUserConnected(sellerAddress)) {
                if (dataPlanListener != null) {
                    dataPlanListener.onPurchaseFailed(sellerAddress, "Seller is not connected now.");
                }
                return;
            }

            String currentSellerId = payController.getDataManager().getCurrentSellerId();

            if ((!TextUtils.isEmpty(probableSellerId) && !sellerAddress.equalsIgnoreCase(probableSellerId)) || (!TextUtils.isEmpty(currentSellerId) && !sellerAddress.equalsIgnoreCase(currentSellerId))){
                if (dataPlanListener != null) {
                    dataPlanListener.onPurchaseFailed(sellerAddress, "You already have an active purchase in the same network.");
                }
            } else {
                totalDataAmount = amount;

                int currentEndPoint = getEndpoint();

                Purchase purchase = databaseService.getPurchaseByState(PurchaseConstants.CHANNEL_STATE.OPEN,
                        ethService.getAddress(), sellerAddress, currentEndPoint);

                prepareChannel(sellerAddress, purchase);
                //3
                setCurrentSellerWithStatus(sellerAddress, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASING);

                if (dataPlanListener != null) {
                    dataPlanListener.onConnectingWithSeller(sellerAddress);
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void getMyBalanceInfo() {
        MeshLog.p("getMyBalanceInfo");

        if (NetworkMonitor.isOnline()){
            try {

                int endPointType = getEndpoint();

                Double etherBalance = ethService.getUserEthBalance(ethService.getAddress(), endPointType);
                Double tokenBalance = ethService.getUserTokenBalance(ethService.getAddress(), endPointType);
                if (etherBalance != null) {
                    EthereumServiceUtil.getInstance(mContext).updateCurrency(endPointType, etherBalance);
                }

                if (tokenBalance != null) {
                    EthereumServiceUtil.getInstance(mContext).updateToken(endPointType, tokenBalance);
                }

                if (etherBalance == null || tokenBalance == null) {
                    if(walletListener != null) {
                        walletListener.onBalanceInfo(false, "Can't reach network, please try again later.");
                    }
                } else {

                    if(walletListener != null) {
                        walletListener.onBalanceInfo(true, "Balance updated");
                    }
                }
            } catch (Exception e) {
                if(walletListener != null) {
                    walletListener.onBalanceInfo(false, e.getMessage());
                }
            }
        } else {
            try {
                List<String> sellerIds = payController.getDataManager().getInternetSellers();
                if (sellerIds.size() == 0) {

                    if (walletListener != null) {
                        walletListener.onBalanceInfo(false, "No internet seller connected.");
                    }
                } else {
                    String sellerId = sellerIds.get(0);
                    String query = PurchaseConstants.INFO_KEYS.ETH_BALANCE + "," + PurchaseConstants.INFO_KEYS.TKN_BALANCE;
                    int endPointType = getEndpoint();

                    getMyInfo(sellerId, query, PurchaseConstants.INFO_PURPOSES.REFRESH_BALANCE, endPointType);
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }



/*    public void sendEtherRequest() {

        try {
            List<String> sellerIds = payController.getDataManager().getInternetSellers();
            if (sellerIds.size() == 0) {

                if (walletListener != null) {
                    walletListener.onEtherRequestResponse(false, "No internet provider connected");
                }
            } else {
                String sellerId = sellerIds.get(0);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                    int endpointType = getEndpoint();
                    setEndPointInfoInJson(jsonObject, endpointType);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                payController.sendEtherRequestMessage(jsonObject, sellerId);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }*/

    public void sendTokenRequest() {

        try {
            List<String> sellerIds = payController.getDataManager().getInternetSellers();
            if (sellerIds.size() == 0) {

                if (walletListener != null) {
                    walletListener.onTokenRequestResponse(false, "No internet provider connected");
                }

            } else {
                String sellerId = sellerIds.get(0);

                String query = PurchaseConstants.INFO_KEYS.ETH_BALANCE + "," + PurchaseConstants.INFO_KEYS.NONCE;

                int endPointType = getEndpoint();

                getMyInfo(sellerId, query, PurchaseConstants.INFO_PURPOSES.BUY_TOKEN, endPointType);

            }
        }catch (Exception ex){
            ex.printStackTrace();
        }


    }


    private void closePurchaseWithOwnInternet(String sellerId){
        try {
            String myAddress = ethService.getAddress();
            Purchase purchase = databaseService.getPurchaseByState(PurchaseConstants.CHANNEL_STATE.OPEN, myAddress, sellerId);

            if (purchase != null) {

                purchase.state = PurchaseConstants.CHANNEL_STATE.CLOSING;
                databaseService.updatePurchase(purchase);
                //TODO following two function call may be doing the same thing, need to check.
                //4
//                setCurrentSellerWithStatus(sellerId, PurchaseConstants.SELLERS_BTN_TEXT.CLOSING);

                if (dataPlanListener != null) {
                    dataPlanListener.onPurchaseClosing(sellerId);
                }

                int nonce = ethService.getUserNonce(myAddress, purchase.blockChainEndpoint);
                double ethBalance = ethService.getUserEthBalance(myAddress, purchase.blockChainEndpoint);

                EthereumServiceUtil.getInstance(mContext).updateCurrency(purchase.blockChainEndpoint, ethBalance);

                if (ethBalance > 0) {
                    String signedMessage = ethService.close(purchase.sellerAddress, purchase.openBlockNumber, purchase.balance, purchase.balanceProof, purchase.closingHash, nonce, purchase.blockChainEndpoint);

                    PurchaseRequests purchaseRequest = new PurchaseRequests();
                    purchaseRequest.buyerAddress = myAddress;
                    purchaseRequest.requesterAddress = myAddress;
                    purchaseRequest.requestType = PurchaseConstants.REQUEST_TYPES.CLOSE_CHANNEL;
                    purchaseRequest.signedMessage = signedMessage;
                    purchaseRequest.requestValue = purchase.balance;
                    purchaseRequest.nonce = nonce;
                    purchaseRequest.state = PurchaseConstants.REQUEST_STATE.RECEIVED;
                    purchaseRequest.blockChainEndpoint = purchase.blockChainEndpoint;
                    int rid = databaseService.insertPurchaseRequest(purchaseRequest);

                    MeshLog.v("purchaseRequest" + purchaseRequest.toString());
                        setObserverForRequest(purchaseRequest.requestType, purchaseRequest.blockChainEndpoint);

                        ethService.submitRequest(purchaseRequest.signedMessage, rid,purchaseRequest.blockChainEndpoint, new EthereumService.SubmitRequestListener() {
                            @Override
                            public void onRequestSubmitted(String hash, int forRId) {

                                if (!TextUtils.isEmpty(hash)) {
                                    try {
                                        PurchaseRequests purchaseRequestLatest = databaseService.getPurchaseRequestById(forRId);
                                        if (purchaseRequestLatest.state < PurchaseConstants.REQUEST_STATE.PENDING) {
                                            purchaseRequestLatest.trxHash = hash;
                                            purchaseRequestLatest.state = PurchaseConstants.REQUEST_STATE.PENDING;
                                            databaseService.updatePurchaseRequest(purchaseRequestLatest);
                                            dataPlanListener.showToastMessage("Request submitted");
                                        }
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    try {
                                        PurchaseRequests purchaseRequestLatest = databaseService.getPurchaseRequestById(forRId);
                                        if (purchaseRequestLatest.state < PurchaseConstants.REQUEST_STATE.PENDING) {
                                            databaseService.deletePurchaseRequest(purchaseRequestLatest);
                                            purchase.state = PurchaseConstants.CHANNEL_STATE.OPEN;
                                            databaseService.updatePurchase(purchase);
                                            purchaseCloseFailed(purchase.sellerAddress, "Request submission error");
                                        }
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onRequestSubmitError(String msg, int forRId) {
                                try {
                                    PurchaseRequests purchaseRequestLatest = databaseService.getPurchaseRequestById(forRId);
                                    if (purchaseRequestLatest.state < PurchaseConstants.REQUEST_STATE.PENDING) {
                                        databaseService.deletePurchaseRequest(purchaseRequestLatest);

                                        purchase.state = PurchaseConstants.CHANNEL_STATE.OPEN;
                                        databaseService.updatePurchase(purchase);

                                        purchaseCloseFailed(purchase.sellerAddress, msg);
                                    }
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                } else {
                    purchase.state = PurchaseConstants.CHANNEL_STATE.OPEN;
                    databaseService.updatePurchase(purchase);
                    purchaseCloseFailed(purchase.sellerAddress, Util.getCurrencyTypeMessage("You do not have enough %s."));
                }
            } else {
                purchaseCloseFailed(sellerId, "No active purchase found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            purchaseCloseFailed(sellerId, e.getMessage());
        }
    }

    private void cloasePurchaseWithSeller(String sellerId){
        String receiverId = null;

        try {
            if (payController.getDataManager().isUserConnected(sellerId)) {
                receiverId = sellerId;
            } else {
                List<String> sellerIds = payController.getDataManager().getInternetSellers();
                if (sellerIds.size() > 0) {
                    receiverId = sellerIds.get(0);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        if (receiverId == null) {

            purchaseCloseFailed(sellerId, "No internet provider connected");
        } else {
            try {
                Purchase purchase = databaseService.getPurchaseByState(PurchaseConstants.CHANNEL_STATE.OPEN, ethService.getAddress(), sellerId);

                if (purchase != null) {

                    purchase.state = PurchaseConstants.CHANNEL_STATE.CLOSING;
                    databaseService.updatePurchase(purchase);
                    //5
//                    setCurrentSellerWithStatus(sellerId, PurchaseConstants.SELLERS_BTN_TEXT.CLOSING);

                    if (dataPlanListener != null) {
                        dataPlanListener.onPurchaseClosing(sellerId);
                    }
                    String query = PurchaseConstants.INFO_KEYS.ETH_BALANCE + "," + PurchaseConstants.INFO_KEYS.NONCE;
                    getMyInfo(receiverId, query, PurchaseConstants.INFO_PURPOSES.CLOSE_PURCHASE, purchase.blockChainEndpoint);

                } else {
                    purchaseCloseFailed(sellerId, "No active purchase found");
                }
            } catch (Exception e) {
                e.printStackTrace();

                purchaseCloseFailed(sellerId, e.getMessage());
            }
        }
    }

    public void closePurchase(String sellerId) {
        if (NetworkMonitor.isOnline()){
            closePurchaseWithOwnInternet(sellerId);
        } else {
            cloasePurchaseWithSeller(sellerId);
        }
    }

    public boolean giftEtherForOtherNetwork() {

        if (NetworkMonitor.isOnline()){
            return requestForGiftForBuyerWithOwnInternet();
        } else {
            try {
                List<String> sellerIds = payController.getDataManager().getInternetSellers();
                if (sellerIds.size() > 0) {
                    return giftEther(sellerIds.get(0));
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
            return false;
        }
    }

    public boolean requestForGiftForBuyerWithOwnInternet() {

        if (NetworkMonitor.isOnline()) {

            String address = ethService.getAddress();
            int endpoint = getEndpoint();
            int requestState = preferencesHelperDataplan.getEtherRequestStatus(endpoint);

            if (requestState == PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET || requestState == PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER) {
                long currentTime = new Date().getTime();
                long requestTime = preferencesHelperDataplan.getEtherRequestTimeStamp(endpoint);
                if (currentTime > (requestTime + 20000)) {

                    preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER, endpoint);
                    preferencesHelperDataplan.setEtherRequestTimeStamp(currentTime, endpoint);

                    String requestData = getRequestData();
                    if (requestData != null) {
                        ethService.requestGiftEther(address, endpoint, requestData, payController.walletService.getPublicKey(), new EthereumService.GiftEther() {
                            @Override
                            public void onEtherGiftRequested(boolean success, String msg, String ethTX, String tknTx, String failedBy, double ethValue, double tknValue) {
                                MeshLog.v("giftEther onEtherGiftRequested " + "success " + success + " msg " + msg + " ethTX " + ethTX + " tknTx " + tknTx + " failedby " + failedBy);

                                PreferencesHelperDataplan preferencesHelperDataplan = PreferencesHelperDataplan.on();

                                if (success) {

                                    preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.GOT_TRANX_HASH, endpoint);
                                    preferencesHelperDataplan.setGiftEtherHash(ethTX, endpoint);
                                    preferencesHelperDataplan.setGiftTokenHash(tknTx, endpoint);
                                    preferencesHelperDataplan.setGiftEtherValue(ethValue, endpoint);
                                    preferencesHelperDataplan.setGiftTokenValue(tknValue, endpoint);

                                    String toastMessage = Util.getCurrencyTypeMessage("Congratulations!!!\nYou have been awarded with " + tknValue + " points which will be added within few minutes."); //changed per decision

                                    sendGiftListener(success, false, toastMessage);
                                } else {
                                    MeshLog.v("giftEther giftRequestSubmitted " + msg);
                                    if (failedBy.equals("admin")) {
                                        preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.GOT_GIFT_ETHER, endpoint);
                                        getMyBalanceInfo();
                                    } else {
                                        preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, endpoint);
                                        sendGiftListener(success, false, msg);
                                    }
                                }
                            }
                        });
                    }
                    return true;
                }
            } else if (requestState == PurchaseConstants.GIFT_REQUEST_STATE.GOT_TRANX_HASH) {

                String ethTranxHash = preferencesHelperDataplan.getGiftEtherHash(endpoint);
                String tknTranxHash = preferencesHelperDataplan.getGiftTokenHash(endpoint);

                if (!TextUtils.isEmpty(ethTranxHash) && !TextUtils.isEmpty(tknTranxHash)) {
                    double ethValue = preferencesHelperDataplan.getGiftEtherValue(endpoint);
                    double tknValue = preferencesHelperDataplan.getGiftTokenValue(endpoint);
                    ethService.getStatusOfGift(address, ethTranxHash, tknTranxHash, endpoint, ethValue, tknValue);
                    return true;
                }

            }
        }
        return false;
    }


    public void destroyObject() {
        payController.setBuyerListener(null);

        purchaseManagerBuyer = null;
        walletListener = null;
    }

    public void setPayControllerListener() {
        if (payController !=null){
            payController.setBuyerListener(this);
        }
    }


    //*********************************************************//
    //******************PayControllerListener******************//
    //*********************************************************//
    @Override
    public void onUserConnected(String address) {
        try {
            if (payController.getDataManager().isInternetSeller(address)) {
                DataPlanManager.getInstance().processAllSeller(mContext);
                if (TextUtils.isEmpty(payController.getDataManager().getCurrentSellerId()) && TextUtils.isEmpty(probableSellerId)){
                    Purchase purchase = databaseService.getPurchaseByState(PurchaseConstants.CHANNEL_STATE.OPEN, ethService.getAddress(), address);
                    if (purchase != null && purchase.totalDataAmount > purchase.usedDataAmount){
                        probableSellerId = purchase.sellerAddress;

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                        jsonObject.put(PurchaseConstants.JSON_KEYS.BUYER_ADDRESS, purchase.buyerAddress);

                        jsonObject.put(PurchaseConstants.JSON_KEYS.SELLER_ADDRESS, purchase.sellerAddress);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.OPEN_BLOCK, purchase.openBlockNumber);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.USED_DATA, purchase.usedDataAmount);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.TOTAL_DATA, purchase.totalDataAmount);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.BPS_BALANCE, purchase.balance);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_BPS, purchase.balanceProof);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_CHS, purchase.closingHash);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.TRX_HASH, purchase.trxHash);

                        setEndPointInfoInJson(jsonObject, purchase.blockChainEndpoint);
                        payController.sendSyncMessageToSeller(jsonObject, purchase.sellerAddress);
                    }else {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                        jsonObject.put(PurchaseConstants.JSON_KEYS.BUYER_ADDRESS, ethService.getAddress());
                        jsonObject.put(PurchaseConstants.JSON_KEYS.SELLER_ADDRESS, address);

                        payController.sendSyncMessageToSeller(jsonObject, address);
                    }
                }
            }


        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUserDisconnected(String address) {

        if (probableSellerId != null && probableSellerId.equalsIgnoreCase(address)){
            probableSellerId = null;
        }

        if (!TextUtils.isEmpty(giftRequestedSeller) && giftRequestedSeller.equals(address)){
            if (preferencesHelperDataplan.getEtherRequestStatus(1) == PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER){
                preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, 1);
            }
            if (preferencesHelperDataplan.getEtherRequestStatus(2) == PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER){
                preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, 2);
            }
        }
        DataPlanManager.getInstance().precessDisconnectedSeller(mContext, address);
    }

    @Override
    public void onInfoOkayReceived(String from, int purpose, JSONObject infoJson, int endPointType) {
        switch (purpose) {
            case PurchaseConstants.INFO_PURPOSES.REFRESH_BALANCE:
                try {

                    double ethBalance = infoJson.optDouble(PurchaseConstants.INFO_KEYS.ETH_BALANCE)/* ? infoJson.getDouble(PurchaseConstants.INFO_KEYS.ETH_BALANCE) : ethService.getMyEthBalance()*/;
                    double tknBalance = infoJson.optDouble(PurchaseConstants.INFO_KEYS.TKN_BALANCE)/* ? infoJson.getDouble(PurchaseConstants.INFO_KEYS.TKN_BALANCE) : ethService.getMyTokenBalance()*/;

                    EthereumServiceUtil.getInstance(mContext).updateCurrencyAndToken(endPointType, ethBalance, tknBalance);

                    if (walletListener != null) {
                        walletListener.onBalanceInfo(true, "Balance updated");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PurchaseConstants.INFO_PURPOSES.BUY_TOKEN:
                double token = EthereumServiceUtil.getInstance(mContext).getToken(endPointType);
                double currency = EthereumServiceUtil.getInstance(mContext).getCurrency(endPointType);
                try {



                    if (infoJson.has(PurchaseConstants.INFO_KEYS.NONCE) && infoJson.has(PurchaseConstants.INFO_KEYS.ETH_BALANCE)) {
                        int nonce = infoJson.getInt(PurchaseConstants.INFO_KEYS.NONCE);
                        double ethBalance = infoJson.getDouble(PurchaseConstants.INFO_KEYS.ETH_BALANCE);
                        EthereumServiceUtil.getInstance(mContext).updateCurrency(endPointType, ethBalance);
//                        ethService.setMyEthBalance(ethBalance);

                        if (ethBalance > PurchaseConstants.BUY_TOKEN_ETHER_VALUE) {
                            String signedMessage = ethService.buyToken(PurchaseConstants.BUY_TOKEN_ETHER_VALUE, nonce, endPointType);

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());

                            JSONArray array = new JSONArray();

                            JSONObject buyToken = new JSONObject();
                            buyToken.put(PurchaseConstants.JSON_KEYS.REQUEST_TYPE, PurchaseConstants.REQUEST_TYPES.BUY_TOKEN);
                            buyToken.put(PurchaseConstants.JSON_KEYS.SIGNED_MESSAGE, signedMessage);
                            buyToken.put(PurchaseConstants.JSON_KEYS.REQUEST_VALUE, PurchaseConstants.BUY_TOKEN_ETHER_VALUE);
                            buyToken.put(PurchaseConstants.JSON_KEYS.NONCE, nonce);
                            array.put(buyToken);

                            jsonObject.put(PurchaseConstants.JSON_KEYS.REQUEST_LIST, array);

                            setEndPointInfoInJson(jsonObject, endPointType);

                            payController.sendBuyToken(jsonObject, from);

                            if (walletListener != null) {
                                walletListener.onTokenRequestResponse(true, "Purchase request sent.");
                            }

                        } else {

                            if (walletListener != null) {
                                walletListener.onTokenRequestResponse(false, Util.getCurrencyTypeMessage("You don't have enough %s."));
                            }
                        }
                    } else {

                        if (walletListener != null) {
                            walletListener.onTokenRequestResponse(false, "Can't reach network, please try again later.");
                        }
                    }


                } catch (Exception e) {

                    if (walletListener != null) {
                        walletListener.onTokenRequestResponse(false, e.getMessage());
                    }
                }

                break;
            case PurchaseConstants.INFO_PURPOSES.CLOSE_PURCHASE:

                try {

                    List<Purchase> purchaseList = databaseService.getMyPurchasesWithState(ethService.getAddress(), PurchaseConstants.CHANNEL_STATE.CLOSING);

                    if (infoJson.has(PurchaseConstants.INFO_KEYS.NONCE) && infoJson.has(PurchaseConstants.INFO_KEYS.ETH_BALANCE)) {

                        int nonce = infoJson.getInt(PurchaseConstants.INFO_KEYS.NONCE);
                        double ethBalance = infoJson.getDouble(PurchaseConstants.INFO_KEYS.ETH_BALANCE);

                        EthereumServiceUtil.getInstance(mContext).updateCurrency(endPointType, ethBalance);

                        if (ethBalance > 0) {

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());

                            JSONArray array = new JSONArray();

                            if (purchaseList != null) {
                                for (Purchase purchase : purchaseList) {

                                    String signedMessage = ethService.close(purchase.sellerAddress, purchase.openBlockNumber,
                                            purchase.balance, purchase.balanceProof, purchase.closingHash, nonce, endPointType);

                                    JSONObject cJson = new JSONObject();
                                    cJson.put(PurchaseConstants.JSON_KEYS.REQUEST_TYPE, PurchaseConstants.REQUEST_TYPES.CLOSE_CHANNEL);
                                    cJson.put(PurchaseConstants.JSON_KEYS.SIGNED_MESSAGE, signedMessage);
                                    cJson.put(PurchaseConstants.JSON_KEYS.REQUEST_VALUE, purchase.balance);
                                    cJson.put(PurchaseConstants.JSON_KEYS.NONCE, nonce);
                                    array.put(cJson);

                                    nonce++;
                                }
                                jsonObject.put(PurchaseConstants.JSON_KEYS.REQUEST_LIST, array);

                                setEndPointInfoInJson(jsonObject, endPointType);

                                payController.sendBlockChainRequest(jsonObject, from,
                                        PurchaseConstants.INFO_PURPOSES.CLOSE_PURCHASE);

                                if (dataPlanListener != null) {
                                    dataPlanListener.showToastMessage("Closing request sent.");
                                }
                            }
                        } else {
                            if (purchaseList != null) {
                                for (Purchase purchase : purchaseList) {
                                    purchase.state = PurchaseConstants.CHANNEL_STATE.OPEN;
                                    databaseService.updatePurchase(purchase);

                                    purchaseCloseFailed(purchase.sellerAddress, Util.getCurrencyTypeMessage("You do not have enough %s."));
                                }
                            }
                        }
                    } else {
                        if (purchaseList != null) {
                            for (Purchase purchase : purchaseList) {
                                purchase.state = PurchaseConstants.CHANNEL_STATE.OPEN;
                                databaseService.updatePurchase(purchase);

                                purchaseCloseFailed(purchase.sellerAddress, "Can't reach network, please try again later.");
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case PurchaseConstants.INFO_PURPOSES.TOPUP_CHANNEL:
                try {
                    String failedMessage = null;
                    double dataPrice = totalDataAmount * PreferencesHelperDataplan.on().getPerMbTokenValue();
                    if (!infoJson.has(PurchaseConstants.INFO_KEYS.ETH_BALANCE) || !infoJson.has(PurchaseConstants.INFO_KEYS.TKN_BALANCE)
                            || !infoJson.has(PurchaseConstants.INFO_KEYS.NONCE) || !infoJson.has(PurchaseConstants.INFO_KEYS.ALOWANCE)){
                        failedMessage = "Can't reach network, please try again later.";
                    }else {
                        double ethBalance =  infoJson.getDouble(PurchaseConstants.INFO_KEYS.ETH_BALANCE);
                        double tknBalance = infoJson.getDouble(PurchaseConstants.INFO_KEYS.TKN_BALANCE);

                        EthereumServiceUtil.getInstance(mContext).updateCurrencyAndToken(endPointType, ethBalance, tknBalance);

                        /*ethService.setMyEthBalance(ethBalance);
                        ethService.setMyTokenBalance(tknBalance);*/

                        double sharedData = infoJson.getInt(PurchaseConstants.INFO_KEYS.SHARED_DATA);
                        int pendingRequestNumber = infoJson.getInt(PurchaseConstants.INFO_KEYS.PENDING_REQUEST_NUMBER);

                        if (pendingRequestNumber > 0) {
                            failedMessage = "You already have " + pendingRequestNumber + " pending requests.";
                        } else if (sharedData > 0 && sharedData < Util.convertMegabytesToBytes(totalDataAmount)){
                            failedMessage = "Seller does not have enough data to sell.";
                        } else if (ethBalance == 0) {
                            failedMessage = Util.getCurrencyTypeMessage("You do not have enough %s.");
                        } else if (tknBalance < dataPrice) {
                            failedMessage = "You do not have enough token.";
                        }

                    }
                    if (failedMessage != null) {
                        //6
                        setCurrentSellerWithStatus(from, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
                        if (dataPlanListener != null) {
                            dataPlanListener.onPurchaseFailed(from, failedMessage);
                        }
                    } else {
                        int nonce = infoJson.getInt(PurchaseConstants.INFO_KEYS.NONCE);
                        double allowance = infoJson.getDouble(PurchaseConstants.INFO_KEYS.ALOWANCE);

                        sendTopupRequest(from, dataPrice, allowance, nonce, endPointType);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onInfoErrorReceived(String from, int purpose, String msg) {
        switch (purpose) {
            case PurchaseConstants.INFO_PURPOSES.REFRESH_BALANCE:

                if (walletListener != null) {
                    walletListener.onBalanceInfo(false, msg);
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void giftRequestSubmitted(boolean status, String submitMessage, String etherTransactionHash,
                                     String tokenTransactionHash, int endPoint, String failedBy, double ethValue, double tknValue) {

        MeshLog.v("giftEther giftRequestSubmitted " + status + " failedby " + failedBy);

        PreferencesHelperDataplan preferencesHelperDataplan =
                PreferencesHelperDataplan.on();

        if (status) {

            preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.GOT_TRANX_HASH, endPoint);
            preferencesHelperDataplan.setGiftEtherHash(etherTransactionHash, endPoint);
            preferencesHelperDataplan.setGiftTokenHash(tokenTransactionHash, endPoint);
            preferencesHelperDataplan.setGiftEtherValue(ethValue, endPoint);
            preferencesHelperDataplan.setGiftTokenValue(tknValue, endPoint);
//            preferencesHelperDataplan.setGiftEndpointType(endPoint);

            String toastMessage = Util.getCurrencyTypeMessage("Congratulations!!!\nYou have been awarded with "+tknValue+" points which will be added within few minutes.");

            sendGiftListener(status, false, toastMessage);

            /*Activity currentActivity = MeshApp.getCurrentActivity();
            if (currentActivity != null) {
                HandlerUtil.postForeground(() -> DialogUtil.showConfirmationDialog(currentActivity, "Gift Awarded!", toastMessage, null, "OK", null));
//            HandlerUtil.postForeground(() -> Toast.makeText(mContext, toastMessage, Toast.LENGTH_LONG).show());
            }else {
                //TODO send notifications
            }*/
        } else {
            MeshLog.v("giftEther giftRequestSubmitted " + submitMessage);
            if (failedBy.equals("admin")){
                preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.GOT_GIFT_ETHER, endPoint);
                getMyBalanceInfo();
            }else {
                preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, endPoint);

                sendGiftListener(status, false, submitMessage);

                /*Activity currentActivity = MeshApp.getCurrentActivity();
                if (currentActivity != null) {
                    HandlerUtil.postForeground(() -> DialogUtil.showConfirmationDialog(currentActivity, "Gift Awarded!", submitMessage, null, "OK", null));
                }*/
            }

        }

    }

    private void sendGiftListener(boolean status, boolean isGifted, String message) {
        if (walletListener != null) {
            walletListener.onGiftResponse(status, isGifted, message);
        }
    }

    @Override
    public void giftResponse(boolean status, double ethBalance, double tokenBalance, int endPoint, double giftEtherValue, double giftTokenValue) {
        MeshLog.v("giftEther giftResponse " + status + " eth " + ethBalance + " tiken " + tokenBalance);
        PreferencesHelperDataplan preferencesHelperDataplan = PreferencesHelperDataplan.on();

        if (status) {

            preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.GOT_GIFT_ETHER, endPoint);
            preferencesHelperDataplan.setGiftEtherHash(null, endPoint);
            preferencesHelperDataplan.setGiftTokenHash(null, endPoint);
            databaseService.updateCurrencyAndToken(endPoint, ethBalance, tokenBalance);

            sendGiftListener(status, true, "Congratulations!!!\n" + giftTokenValue + " Points have been added to your account.");
        } else {
            sendGiftListener(status, true, "Failed");
            //TODO detect fail type
            preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, endPoint);
        }

    }

    @Override
    public void onProbableSellerDisconnected(String sellerId) {
        MeshLog.v("onProbableSellerDisconnected " + sellerId);

        if (probableSellerId != null && probableSellerId.equalsIgnoreCase(sellerId)){
            probableSellerId = null;
        }
        try {
            List<Purchase> myOpenPurcheses  =  databaseService.getMyPurchasesWithState(ethService.getAddress(), PurchaseConstants.CHANNEL_STATE.OPEN);

            //TODO looping may arise if there are multiple sellers connected, and buyer has multiple purchases, and no seller can connect the buyer as they finish their shared data.
            for (Purchase p : myOpenPurcheses){
                if (!sellerId.equalsIgnoreCase(p.sellerAddress) && payController.getDataManager().isUserConnected(p.sellerAddress) && p.totalDataAmount > p.usedDataAmount){
                    probableSellerId = p.sellerAddress;

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                    jsonObject.put(PurchaseConstants.JSON_KEYS.BUYER_ADDRESS, p.buyerAddress);
                    jsonObject.put(PurchaseConstants.JSON_KEYS.SELLER_ADDRESS, p.sellerAddress);
                    jsonObject.put(PurchaseConstants.JSON_KEYS.OPEN_BLOCK, p.openBlockNumber);
                    jsonObject.put(PurchaseConstants.JSON_KEYS.USED_DATA, p.usedDataAmount);
                    jsonObject.put(PurchaseConstants.JSON_KEYS.TOTAL_DATA, p.totalDataAmount);
                    jsonObject.put(PurchaseConstants.JSON_KEYS.BPS_BALANCE, p.balance);
                    jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_BPS, p.balanceProof);
                    jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_CHS, p.closingHash);
                    jsonObject.put(PurchaseConstants.JSON_KEYS.TRX_HASH, p.trxHash);

                    setEndPointInfoInJson(jsonObject, p.blockChainEndpoint);

                    payController.sendSyncMessageToSeller(jsonObject, p.sellerAddress);

                    break;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnectedBySeller(String sellerAddress, String msg) {
        try {
            HandlerUtil.postForeground(new Runnable() {
                @Override
                public void run() {
                    Toaster.showLong(msg);
                }
            });

            if (payController.getDataManager().getCurrentSellerId().equalsIgnoreCase(sellerAddress)) {
                payController.getDataManager().disconnectFromInternet();
            }

            if (probableSellerId != null && probableSellerId.equalsIgnoreCase(sellerAddress)){
                onProbableSellerDisconnected(sellerAddress);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onInitPurchaseOkReceived(String sellerAddress, double ethBalance, double tokenBallance,
                                         int nonce, double allowance, int endPointType, long sharedData) {
        MeshLog.v("onInitPurchaseOkReceived  " + sellerAddress + " " + ethBalance + " " + tokenBallance + " " + nonce + " " + allowance + " " + sharedData);

        double totalPrice = totalDataAmount * PreferencesHelperDataplan.on().getPerMbTokenValue();

        if (ethBalance == 0 || tokenBallance < totalPrice) {
            //7
            setCurrentSellerWithStatus(sellerAddress, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
            if (dataPlanListener != null) {
                dataPlanListener.onPurchaseFailed(sellerAddress, "Not enough balance");
            }
            return;
        }

        if (sharedData > 0 && sharedData < Util.convertMegabytesToBytes(totalDataAmount)){
            //8
            setCurrentSellerWithStatus(sellerAddress, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
            if (dataPlanListener != null) {
                dataPlanListener.onPurchaseFailed(sellerAddress, "Seller does not have enough data to sell.");
            }
            return;
        }



        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());

            JSONArray array = new JSONArray();

            if (allowance > 0 && allowance < totalPrice) {
                String signedMessage_approve_zero = ethService.approve(0, nonce, endPointType);
                MeshLog.v("signedMessage_approve_zero " + signedMessage_approve_zero);
                JSONObject approveZero = new JSONObject();
                approveZero.put(PurchaseConstants.JSON_KEYS.REQUEST_TYPE, PurchaseConstants.REQUEST_TYPES.APPROVE_ZERO);
                approveZero.put(PurchaseConstants.JSON_KEYS.SIGNED_MESSAGE, signedMessage_approve_zero);
                approveZero.put(PurchaseConstants.JSON_KEYS.REQUEST_VALUE, 0);
                approveZero.put(PurchaseConstants.JSON_KEYS.NONCE, nonce);
                array.put(approveZero);
                nonce++;
            }

            if (allowance < totalPrice) {
                String signedMessage_approve = ethService.approve(totalPrice, nonce, endPointType);
                MeshLog.v("signedMessage_approve " + signedMessage_approve);
                JSONObject approveToken = new JSONObject();
                approveToken.put(PurchaseConstants.JSON_KEYS.REQUEST_TYPE, PurchaseConstants.REQUEST_TYPES.APPROVE_TOKEN);
                approveToken.put(PurchaseConstants.JSON_KEYS.SIGNED_MESSAGE, signedMessage_approve);
                approveToken.put(PurchaseConstants.JSON_KEYS.REQUEST_VALUE, totalPrice);
                approveToken.put(PurchaseConstants.JSON_KEYS.NONCE, nonce);
                array.put(approveToken);
                nonce++;
            }


            String signedMessage_create = ethService.createChannel(sellerAddress, totalPrice, nonce, endPointType);
            MeshLog.v("signedMessage_create " + signedMessage_create);
            JSONObject create = new JSONObject();
            create.put(PurchaseConstants.JSON_KEYS.REQUEST_TYPE, PurchaseConstants.REQUEST_TYPES.CREATE_CHANNEL);
            create.put(PurchaseConstants.JSON_KEYS.SIGNED_MESSAGE, signedMessage_create);
            create.put(PurchaseConstants.JSON_KEYS.REQUEST_VALUE, totalPrice);
            create.put(PurchaseConstants.JSON_KEYS.NONCE, nonce);
            array.put(create);

            setEndPointInfoInJson(jsonObject, endPointType);
            jsonObject.put(PurchaseConstants.JSON_KEYS.REQUEST_LIST, array);

            payController.sendCreateChannel(jsonObject, sellerAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceivedEtherRequestResponse(String from, int responseCode) {
        if (responseCode == 200) {

            if (walletListener != null) {
                walletListener.onEtherRequestResponse(true, Util.getCurrencyTypeMessage("Request has been sent, %s will be added to your address soon."));
            }
        } else {

            if (walletListener != null) {
                walletListener.onEtherRequestResponse(false, Util.getCurrencyTypeMessage("%s request rejected!"));
            }
        }
    }

    @Override
    public void onReceivedEther(String from, double balance) { }

    @Override
    public void onBuyTokenResponseReceived(String from, double tokenValue, double ethValue, int endPointType) {
        EthereumServiceUtil.getInstance(mContext).updateCurrencyAndToken(endPointType, ethValue, tokenValue);

        if (walletListener != null) {
            walletListener.onTokenRequestResponse(true, "Congratulatioms, Token added to your account.");
        }
    }

    @Override
    public void onInitPurchaseErrorReceived(String sellerAddress, String msg) {
        MeshLog.v(msg);
        //9
        setCurrentSellerWithStatus(sellerAddress, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
        if (dataPlanListener != null) {
            dataPlanListener.onPurchaseFailed(sellerAddress, msg);
        }
    }

    @Override
    public void onChannelCreateOkayReceived(String from, long openBlock, double deposit, int endPointType, String trx_hash, String chs) {
        MeshLog.v("onChannelCreateOkayReceived " + from + " " + openBlock + "  " + deposit);

        try {
            Purchase purchase = databaseService.getPurchaseByBlockNumber(openBlock, ethService.getAddress(), from);

            String bps = "";
            if (purchase == null) {
                double totalDta = deposit / PreferencesHelperDataplan.on().getPerMbTokenValue();
                bps = ethService.getBalanceProof(from, openBlock, 0, endPointType);

                databaseService.insertPurchase(ethService.getAddress(), from, totalDta, 0, openBlock, deposit, bps,
                        0, chs, 0, PurchaseConstants.CHANNEL_STATE.OPEN, endPointType, trx_hash);
                //10
                setCurrentSellerWithStatus(null, PurchaseConstants.SELLERS_BTN_TEXT.CLOSE);
                if (dataPlanListener != null) {
                    dataPlanListener.onPurchaseSuccess(from, totalDta, openBlock);
                }
            }

            sendSyncOkMessageToSeller(from, bps, trx_hash);
        } catch (Exception e) {
            MeshLog.v("Exception " + e.getMessage());
        }
    }

    @Override
    public void onChannelCreateErrorReceived(String from, String msg) {
        MeshLog.v("onChannelCreateErrorReceived " + from + " " + msg);
        //11
        setCurrentSellerWithStatus(from, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
        if (dataPlanListener != null) {
            dataPlanListener.onPurchaseFailed(from, msg);
        }
    }

    @Override
    public void onPendingMessageInfo(String fromAddress, long dataSize, String msg_id, boolean isIncoming) {
        MeshLog.o("onPendingMessageInfo " + fromAddress + " " + msg_id);
        try {
            Purchase purchase = databaseService.getPurchaseByState(PurchaseConstants.CHANNEL_STATE.OPEN, ethService.getAddress(), fromAddress);
            if (purchase != null) {
                double dataSizeMb = Util.convertBytesToMegabytes(dataSize);
                double dataPrice = PreferencesHelperDataplan.on().getPerMbTokenValue() * dataSizeMb;
                double totalBalance = purchase.balance + dataPrice;
                if (totalBalance <= purchase.deposit) {

                    if (purchase.deposit - totalBalance < 0.5) {
                      //  double remain = purchase.totalDataAmount - purchase.usedDataAmount;
                        if (!isWarningShown) {
                            NotificationUtil.showNotification(mContext, "Internet Usage", "Your purchased internet is almost finished");
                            isWarningShown = true;
                        }

                        if (dataPlanListener != null) {
                            dataPlanListener.onBalancedFinished(purchase.sellerAddress, 1); // for this line check null
                        }
                    }

                    int endPointType = getEndpoint();
                    String bps = ethService.getBalanceProof(purchase.sellerAddress, purchase.openBlockNumber, totalBalance, endPointType);

//                    ethService.verifyBalanceProofSignature(purchase.sellerAddress, purchase.openBlockNumber, totalBalance, bps, new EthereumService.VerifyBalanceProof() {
//                        @Override
//                        public void onBalanceProofVerified(String sender) {
//                            MeshLog.p("bps verified " + sender);
//                        }
//                    });



                    JSONObject js = new JSONObject();
                    js.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                    js.put(PurchaseConstants.JSON_KEYS.BPS_BALANCE, totalBalance);
                    js.put(PurchaseConstants.JSON_KEYS.OPEN_BLOCK, purchase.openBlockNumber);
                    js.put(PurchaseConstants.JSON_KEYS.MESSAGE_BPS, bps);
                    js.put(PurchaseConstants.JSON_KEYS.MESSAGE_ID, msg_id);

                    payController.sendPayForMessageOkay(js, fromAddress);
                } else {
                    JSONObject jo = new JSONObject();
                    jo.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                    jo.put(PurchaseConstants.JSON_KEYS.MESSAGE_TEXT, "User does not have enough purchased balance.");
                    jo.put(PurchaseConstants.JSON_KEYS.MESSAGE_ID, msg_id);
                    payController.sendPayForMessageError(jo, fromAddress);
                    payController.getDataManager().disconnectFromInternet();



                    Activity currentActivity = MeshApp.getCurrentActivity();
                    if (currentActivity != null && (datalimitAlert == null || !datalimitAlert.isShowing())) {
                        HandlerUtil.postForeground(() -> {
                            datalimitAlert  = DialogUtil.showConfirmationDialog(currentActivity, "Internet Usage Finished!", "Your current internet volume insufficient, please purchase again and continue messaging", "No, Thanks", "Ok", new DialogUtil.DialogButtonListener() {
                                @Override
                                public void onClickPositive() {
                                    datalimitAlert = null;
                                    DataPlanManager.openActivity(currentActivity, 0);
                                }

                                @Override
                                public void onCancel() {
                                    datalimitAlert = null;
                                }

                                @Override
                                public void onClickNegative() {
                                    datalimitAlert = null;
                                    onProbableSellerDisconnected(fromAddress);
                                }
                            });
                        });
                    }else {
                        NotificationUtil.showNotification(mContext, "Internet Usage Finished!", "Your current internet volume is insufficient, please purchase again and continue messaging");
                        if (dataPlanListener != null) {
                            dataPlanListener.onBalancedFinished(purchase.sellerAddress, 0);
                        } else {
                            HandlerUtil.postForeground(() -> ToastUtil.showLong(mContext, "Your purchased data has finished."));
                        }
                    }
                }
            } else {
                JSONObject jo = new JSONObject();
                jo.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                jo.put(PurchaseConstants.JSON_KEYS.MESSAGE_TEXT, "User has no purchase from this seller.");
                jo.put(PurchaseConstants.JSON_KEYS.MESSAGE_ID, msg_id);

                payController.sendPayForMessageError(jo, fromAddress);

                payController.getDataManager().disconnectFromInternet();

                onProbableSellerDisconnected(fromAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInternetMessageResponseSuccess(String sender, String bps, double bps_balance, String chs, long open_block, long byteSize, String messageId) {
        MeshLog.o("-----onInternetMessageResponseSuccess-----------" + messageId);
        try {
            Purchase purchase = databaseService.getPurchaseByBlockNumber(open_block, ethService.getAddress(), sender);

            purchase.balanceProof = bps;
            purchase.balance = bps_balance;
            purchase.closingHash = chs;

            double addedData = Util.convertBytesToMegabytes(byteSize);
            purchase.usedDataAmount = purchase.usedDataAmount + addedData;

            MeshLog.o("balancemismatchcheck1 " + bps_balance + "  " + purchase.usedDataAmount);

            databaseService.updatePurchase(purchase);


            Datausage datausage = new Datausage();
            datausage.purchaseId = purchase.pid;
            datausage.dataByte = byteSize;
            datausage.purpose = PurchaseConstants.DATA_USAGE_PURPOSE.MESSAGE;

            databaseService.insertDataUsage(datausage);
            MeshLog.o("buyer updated:" + sender);


            if (!messageId.isEmpty()) {
                JSONObject js = new JSONObject();
                js.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());

                js.put(PurchaseConstants.JSON_KEYS.MESSAGE_ID, messageId);
                payController.sendNotificationForBuyerUpdate(js, sender);
                MeshLog.o("buyer not empty:" + sender);
            } else {
                MeshLog.o("buyer is empty:" + sender);
            }
        } catch (Exception e) {
//                MeshLog.p(" onInternetMessageResponseSuccess EX " + e.getMessage());
        }
    }

    @Override
    public void onInternetMessageResponseFailed(String sender, String message) {

        HandlerUtil.postForeground(() -> Toaster.showShort(message));

    }

    @Override
    public void onSyncSellerToBuyerReceived(String buyerAddress, String sellerAddress, long blockNumber, double usedDataAmount, double totalDataAmount, double balance, String bps, String chs, int endPointType, String trx_hash) {
        if (blockNumber > 0){
            try {
                Purchase purchase = databaseService.getPurchaseByTrxHash(trx_hash);
                if (purchase != null) {
                    purchase.balance = balance;
                    purchase.usedDataAmount = usedDataAmount;
                    purchase.balanceProof = bps;
                    purchase.closingHash = chs;
                    purchase.blockChainEndpoint = endPointType;
                    purchase.state = PurchaseConstants.CHANNEL_STATE.OPEN;

                    if (purchase.totalDataAmount != totalDataAmount) {
                        purchase.totalDataAmount = totalDataAmount;
                        double deposit = totalDataAmount * PreferencesHelperDataplan.on().getPerMbTokenValue();
                        purchase.deposit = deposit;
                    }
                    if (blockNumber != purchase.openBlockNumber){
                        MeshLog.v("blockUpdatedBuyer " + purchase.openBlockNumber + " new " + blockNumber);
                        purchase.openBlockNumber = blockNumber;

                        bps = ethService.getBalanceProof(purchase.sellerAddress, purchase.openBlockNumber, purchase.balance, endPointType);
                        purchase.balanceProof = bps;
                    }

                    MeshLog.p("balancemismatchcheck2 " + balance + "  " + purchase.usedDataAmount);
                    databaseService.updatePurchase(purchase);

                } else {
                    double deposit = totalDataAmount * PreferencesHelperDataplan.on().getPerMbTokenValue();
                    bps = ethService.getBalanceProof(sellerAddress, blockNumber, balance, endPointType);
                    databaseService.insertPurchase(buyerAddress, sellerAddress, totalDataAmount, usedDataAmount, blockNumber, deposit, bps, balance, chs, 0.0,
                            PurchaseConstants.CHANNEL_STATE.OPEN, endPointType, trx_hash);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            sendSyncOkMessageToSeller(sellerAddress, bps, trx_hash);
        }
    }

    private void sendSyncOkMessageToSeller(String sellerAddress, String bps, String trxHash){
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
            jsonObject.put(PurchaseConstants.JSON_KEYS.SELLER_ADDRESS, sellerAddress);
            jsonObject.put(PurchaseConstants.JSON_KEYS.TRX_HASH, trxHash);
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_BPS, bps);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        payController.sendSyncOkMessageToSeller(jsonObject, sellerAddress);

    }

    @Override
    public void onChannelCloseReceived(String fromAddress, String sellerAddress, long open_block, int endPointType) {

        try {
            Purchase closingPurchase = databaseService.getPurchaseByBlockNumber(open_block, ethService.getAddress(), sellerAddress);
            if (closingPurchase != null){
                closingPurchase.state = PurchaseConstants.CHANNEL_STATE.CLOSED;
                closingPurchase.withdrawnBalance = closingPurchase.balance;
//                closingPurchase.blockChainEndpoint = endPointType;

                databaseService.updatePurchase(closingPurchase);

                //12
//                setCurrentSellerWithStatus(null, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
                if (dataPlanListener != null) {
                    dataPlanListener.onPurchaseCloseSuccess(closingPurchase.sellerAddress);
                }

                if (payController.getDataManager().getCurrentSellerId().equalsIgnoreCase(sellerAddress)) {
                    payController.getDataManager().disconnectFromInternet();
                    onProbableSellerDisconnected(sellerAddress);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChannelTopupReceived(String fromAddress, long openBlock, double deposit, int endPointType) {

        try {
            Purchase topupPurchase = databaseService.getPurchaseByBlockNumber(openBlock, ethService.getAddress(), fromAddress);
            if (topupPurchase != null){
                double totalData = deposit / PreferencesHelperDataplan.on().getPerMbTokenValue();

                topupPurchase.deposit = deposit;
                topupPurchase.totalDataAmount = totalData;
                topupPurchase.blockChainEndpoint = endPointType;

                databaseService.updatePurchase(topupPurchase);

                //13
                setCurrentSellerWithStatus(null, PurchaseConstants.SELLERS_BTN_TEXT.CLOSE);

                if (dataPlanListener != null) {
                    dataPlanListener.onPurchaseSuccess(fromAddress, totalData, openBlock);
                }

                sendSyncOkMessageToSeller(fromAddress, topupPurchase.balanceProof, topupPurchase.trxHash);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBlockChainResponseReceived(String fromAddress, boolean success, int requestType, String msg) {
        switch (requestType) {
            case PurchaseConstants.REQUEST_TYPES.APPROVE_ZERO:
            case PurchaseConstants.REQUEST_TYPES.APPROVE_TOKEN:
            case PurchaseConstants.REQUEST_TYPES.CREATE_CHANNEL:
            case PurchaseConstants.REQUEST_TYPES.TOPUP_CHANNEL:
                if (!success) {
                    if (dataPlanListener != null) {
                        dataPlanListener.onPurchaseFailed(fromAddress, msg);
                    }
                }
                break;

            case PurchaseConstants.REQUEST_TYPES.BUY_TOKEN:

                if (walletListener != null) {
                    walletListener.onTokenRequestResponse(success, msg);
                }
                break;

            case PurchaseConstants.REQUEST_TYPES.CLOSE_CHANNEL:
                if (dataPlanListener != null) {
                    if (success) {
                        dataPlanListener.showToastMessage(msg);
                    } else {
                        //Todo check from adress and seller adress are same
                        purchaseCloseFailed(fromAddress, msg);
                    }
                }

                break;
        }


    }

    @Override
    public void timeoutCallback(TimeoutModel timeoutModel) {
        if (timeoutModel == null)
            return;

        String receiverId = timeoutModel.getReceiverId();
        String sellerErrorMessage = "Seller is not connected.";
        String internetProviderError = "No internet seller connected.";

        switch (timeoutModel.getPurpose()) {
            case PurchaseConstants.TimeoutPurpose.INIT_PURCHASE:
            case PurchaseConstants.INFO_PURPOSES.TOPUP_CHANNEL:
                onInitPurchaseErrorReceived(receiverId, sellerErrorMessage);
                break;

            case PurchaseConstants.TimeoutPurpose.INIT_CHANNEL:
                onChannelCreateErrorReceived(receiverId, sellerErrorMessage);
                break;

            case PurchaseConstants.INFO_PURPOSES.REFRESH_BALANCE:
                if (walletListener != null) {
                    walletListener.onBalanceInfo(false, internetProviderError);
                }
                break;

            case PurchaseConstants.INFO_PURPOSES.BUY_TOKEN:

                if (walletListener != null) {
                    walletListener.onTokenRequestResponse(false, internetProviderError);
                }
                break;

            case PurchaseConstants.INFO_PURPOSES.CLOSE_PURCHASE:

                try {
                    Purchase purchase = databaseService.getPurchaseByState(PurchaseConstants.CHANNEL_STATE.CLOSING,
                            ethService.getAddress(), receiverId);

                    if (purchase != null && purchase.balance > 0) {
                        purchase.state = PurchaseConstants.CHANNEL_STATE.OPEN;
                        databaseService.updatePurchase(purchase);

                        purchaseCloseFailed(receiverId, internetProviderError);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;

            case PurchaseConstants.TimeoutPurpose.INIT_ETHER:

                if (walletListener != null) {
                    walletListener.onEtherRequestResponse(false, internetProviderError);
                }
                break;

            case PurchaseConstants.MESSAGE_TYPES.GIFT_ETHER_REQUEST:
                PreferencesHelperDataplan preferencesHelperDataplan = PreferencesHelperDataplan.on();

                if (preferencesHelperDataplan.getEtherRequestStatus(1) == PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER){
                    preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, 1);
                }
                if (preferencesHelperDataplan.getEtherRequestStatus(2) == PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER){
                    preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, 2);
                }
                break;
        }
    }


    @Override
    public void onGiftCompleted(String address, int endpoint, boolean status, double ethValue, double tknValue) {
        MeshLog.v("giftEther onGiftCompleted address " + address + " endpoint " + endpoint +  " Status " + status);

        try{
            double ethBalance = ethService.getUserEthBalance(address, endpoint);
            double tknBalance = ethService.getUserTokenBalance(address, endpoint);

            if (address.equalsIgnoreCase(ethService.getAddress())){
                if (status) {

                    preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.GOT_GIFT_ETHER, endpoint);
                    preferencesHelperDataplan.setGiftEtherHash(null, endpoint);
                    preferencesHelperDataplan.setGiftTokenHash(null, endpoint);
                    databaseService.updateCurrencyAndToken(endpoint, ethBalance, tknBalance);

                    sendGiftListener(status, true, "Congratulations!!!\n"+tknValue+" Points have been added to your account.");

                    /*Activity currentActivity = MeshApp.getCurrentActivity();
                    if (currentActivity != null){
                        HandlerUtil.postForeground(() -> DialogUtil.showConfirmationDialog(currentActivity, "Gift Awarded!", "Congratulations!!!\nBalance has been added to your account.", null, "OK", null));
                    }else {
                        //TODO send notifications
                    }*/


                } else {
                    sendGiftListener(status, true, "Failed");
                    //TODO detect fail type
                    preferencesHelperDataplan.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, endpoint);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onChannelClosedLog(RaidenMicroTransferChannels.ChannelSettledEventResponse typedResponse) {
        MeshLog.v("onChannelClosedLog buyer " + typedResponse._sender_address + " " + typedResponse._balance.toString());


        preferencesHelperDataplan.setChannelClosedBlock(typedResponse.log.getBlockNumber().longValue());

        try {
            String buyerAddress = typedResponse._sender_address;
            double balance = ethService.getETHorTOKEN(typedResponse._balance);
            PurchaseRequests purchaseRequests = databaseService.getRequestByTrxHash(typedResponse.log.getTransactionHash());

            if (purchaseRequests != null && purchaseRequests.state >= PurchaseConstants.REQUEST_STATE.COMPLETED) {
                return;
            }

            if (purchaseRequests == null) {
                purchaseRequests = databaseService.getPendingRequest(buyerAddress, balance, PurchaseConstants.REQUEST_TYPES.CLOSE_CHANNEL, PurchaseConstants.REQUEST_STATE.PENDING);
            }

            if (purchaseRequests != null) {
                purchaseRequests.state = PurchaseConstants.REQUEST_STATE.NOTIFIED;
                purchaseRequests.trxHash = typedResponse.log.getTransactionHash();
                purchaseRequests.trxBlock = typedResponse.log.getBlockNumber().longValue();

                databaseService.updatePurchaseRequest(purchaseRequests);
                payController.getDataManager().onBuyerDisconnected(buyerAddress);
                //TODO notify seller about this channel close



            } else {
                MeshLog.v("onChannelClosedLog buyer purchaseRequest not found");
            }


            if (typedResponse._sender_address.equalsIgnoreCase(ethService.getAddress())) {
                Purchase purchase = databaseService.getPurchaseByBlockNumber(typedResponse._open_block_number.longValue(), typedResponse._sender_address, typedResponse._receiver_address);
                if (purchase != null){
                    purchase.state = PurchaseConstants.CHANNEL_STATE.CLOSED;
                    purchase.withdrawnBalance = balance;
                    databaseService.updatePurchase(purchase);
                    payController.getDataManager().disconnectFromInternet();
                    onProbableSellerDisconnected(typedResponse._receiver_address);

                    if (dataPlanListener != null) {
                        dataPlanListener.onPurchaseCloseSuccess(typedResponse._receiver_address);
                    }

                    if (payController.getDataManager().isUserConnected(typedResponse._receiver_address)){
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                        jsonObject.put(PurchaseConstants.JSON_KEYS.OPEN_BLOCK, typedResponse._open_block_number.longValue());
                        jsonObject.put(PurchaseConstants.JSON_KEYS.BPS_BALANCE, balance);

                        payController.sendChannelClosedByBuyer(jsonObject, typedResponse._receiver_address);
                    }
                }
            }

        } catch (ExecutionException | InterruptedException | RemoteException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNetworkAvailable(boolean isOnline, Network network, boolean isWiFi) {
        MeshLog.v("PurchaseManagerBuyer-->onNetworkAvailable " + isOnline + "  " + (isWiFi ? "wifi" : "cellular") );
        if (!isOnline){
            try {
                List<Purchase> myOpenPurcheses  =  databaseService.getMyPurchasesWithState(ethService.getAddress(), PurchaseConstants.CHANNEL_STATE.OPEN);

                //TODO looping may arise if there are multiple sellers connected, and buyer has multiple purchases, and no seller can connect the buyer as they finish their shared data.
                for (Purchase p : myOpenPurcheses){
                    if (payController.getDataManager().isUserConnected(p.sellerAddress) && p.totalDataAmount > p.usedDataAmount){
                        probableSellerId = p.sellerAddress;

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                        jsonObject.put(PurchaseConstants.JSON_KEYS.BUYER_ADDRESS, p.buyerAddress);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.SELLER_ADDRESS, p.sellerAddress);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.OPEN_BLOCK, p.openBlockNumber);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.USED_DATA, p.usedDataAmount);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.TOTAL_DATA, p.totalDataAmount);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.BPS_BALANCE, p.balance);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_BPS, p.balanceProof);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_CHS, p.closingHash);
                        jsonObject.put(PurchaseConstants.JSON_KEYS.TRX_HASH, p.trxHash);

                        setEndPointInfoInJson(jsonObject, p.blockChainEndpoint);

                        payController.sendSyncMessageToSeller(jsonObject, p.sellerAddress);

                        break;
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    //*********************************************************//
    //************************interface************************//
    //*********************************************************//

    public interface PurchaseManagerBuyerListener {
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

    public LiveData<Integer> getDifferentNetworkData(String myAddress, int endpoint) {
        try {
            return databaseService.getDifferentNetworkPurchase(myAddress, endpoint);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
