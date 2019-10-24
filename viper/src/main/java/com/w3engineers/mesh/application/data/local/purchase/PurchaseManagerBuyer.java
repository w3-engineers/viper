package com.w3engineers.mesh.application.data.local.purchase;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.w3engineers.eth.data.helper.PreferencesHelper;
import com.w3engineers.eth.data.remote.EthereumService;
import com.w3engineers.eth.util.helper.HandlerUtil;
import com.w3engineers.eth.util.helper.ToastUtil;
import com.w3engineers.mesh.application.data.local.db.datausage.Datausage;
import com.w3engineers.mesh.application.data.local.db.purchase.Purchase;
import com.w3engineers.mesh.application.ui.dataplan.ManageSellerList;
import com.w3engineers.mesh.util.DialogUtil;
import com.w3engineers.mesh.util.MeshApp;
import com.w3engineers.mesh.application.data.local.db.DatabaseService;
import com.w3engineers.mesh.application.data.local.model.Seller;
import com.w3engineers.mesh.util.EthereumServiceUtil;
import com.w3engineers.mesh.util.MeshLog;
import com.w3engineers.mesh.util.NotificationUtil;
import com.w3engineers.mesh.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PurchaseManagerBuyer implements PayController.PayControllerListenerForBuyer {
    private static PurchaseManagerBuyer purchaseManagerBuyer;
    private PayController payController;
    private Context mContext;
    private EthereumService ethService;
    private double totalDataAmount;
    private PurchaseManagerBuyerListener purchaseManagerBuyerListener;
    private DatabaseService databaseService;
    private Seller currentSeller;
    private MyBalanceInfoListener myBalanceInfoListener;
    private EtherRequestListener etherRequestListener;
    private TokenRequestListener tokenRequestListener;
    private boolean isWarningShown;
    private String currentSellerId;

    private PurchaseManagerBuyer() {

        payController = PayController.getInstance();
        setListeners();

        mContext = MeshApp.getContext();
        ethService = EthereumServiceUtil.getInstance(mContext).getEthereumService();
        ethService.setCredential(payController.getCredentials());

        databaseService = DatabaseService.getInstance(mContext);
    }

    public static PurchaseManagerBuyer getInstance() {
        if (purchaseManagerBuyer == null) {
            purchaseManagerBuyer = new PurchaseManagerBuyer();
        }
        return purchaseManagerBuyer;
    }

    public void setListeners() {
        if (payController !=null){
            payController.setBuyerListener(this);
        }
    }

    /*public List<Seller> getSellerList() {

        List<Seller> sellers = new ArrayList<>();

        List<String> sellerIds = payController.transportManager.getInternetSellers();

        for (String sellerId : sellerIds) {
            Seller s = new Seller();
            try {
                //TODO pick closing purchases and set button text
                Purchase purchase = databaseService.getPurchaseByState(PurchaseConstants.CHANNEL_STATE.OPEN, ethService.getAddress(), sellerId);
                s.setBtnEnabled(true);
                s.setId(sellerId);
                s.setName(sellerId);
                s.setDataPrice(PurchaseConstants.PRICE_PER_MB);

                if (purchase != null) {
                    s.setBtnText(purchase.balance < purchase.deposit ? "Close" : "Purchase");
                    s.setPurchasedData(purchase.totalDataAmount);
                    s.setUsedData(purchase.usedDataAmount);
                    s.setBlockNumber(purchase.openBlockNumber);
                } else {
                    s.setBtnText("Purchase");
                    s.setPurchasedData(0);
                    s.setUsedData(0);
                }

                if (currentSeller != null && sellerId.equalsIgnoreCase(currentSeller.getId())) {
                    s.setBtnEnabled(currentSeller.isBtnEnabled());
                    s.setBtnText(currentSeller.getBtnText());
                } else {
//                    syncWithSeller(sellerId);
                }
                sellers.add(s);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sellers.addAll(getPurchasedSellers());
        return sellers;
    }*/

    /*private List<Seller> getPurchasedSellers() {
        List<Seller> sellers = new ArrayList<>();
        try {
            //TODO pick closing purchases and set button text
            List<Purchase> purchaseList = databaseService.getMyPurchasesWithState(ethService.getAddress(), PurchaseConstants.CHANNEL_STATE.OPEN);
            for (Purchase p : purchaseList) {
                if (payController.transportManager.isUserConnected(p.sellerAddress))
                    continue;

                Seller s = new Seller();
                s.setPurchasedData(p.totalDataAmount);
                s.setBtnEnabled(true);
                s.setName(p.sellerAddress);
                s.setBlockNumber(p.openBlockNumber);
                s.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.CLOSE);
                s.setId(p.sellerAddress);
                s.setDataPrice(PurchaseConstants.PRICE_PER_MB);
                s.setUsedData(p.usedDataAmount);
                sellers.add(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sellers;
    }*/

    public void sendPurchaseMsg(String sellerAddress) {
//        MeshLog.p("Message sending");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("fr", ethService.getAddress());
            jsonObject.put("text", "wanna purchase data");

            int endPointMode = PreferencesHelper.onInstance(mContext).getEndpointMode();

            setEndPointInfoInJson(jsonObject, endPointMode);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        payController.sendInitPurchase(jsonObject, sellerAddress);
    }

    @Override
    public void onInitPurchaseOkReceived(String sellerAddress, double ethBalance, double tokenBallance,
                                         int nonce, double allowance, int endPointType) {
        MeshLog.v("onInitPurchaseOkReceived  " + sellerAddress + " " + ethBalance + " " + tokenBallance + " " + nonce + " " + allowance);

        double totalPrice = totalDataAmount * PurchaseConstants.PRICE_PER_MB;

        if (ethBalance == 0 || tokenBallance < totalPrice) {
            setCurrentSellerWithStatus(sellerAddress, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
            if (purchaseManagerBuyerListener != null) {
                purchaseManagerBuyerListener.onPurchaseFailed(sellerAddress, "Not enough balance");
            }

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
    public void onInitPurchaseErrorReceived(String sellerAddress, String msg) {
        MeshLog.v(msg);
        setCurrentSellerWithStatus(sellerAddress, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
        if (purchaseManagerBuyerListener != null) {
            purchaseManagerBuyerListener.onPurchaseFailed(sellerAddress, msg);
        }
    }

    @Override
    public void onBuyTokenResponseReceived(String from, double tokenValue, double ethValue, int endPointType) {
        EthereumServiceUtil.getInstance(mContext).updateCurrencyAndToken(endPointType, ethValue, tokenValue);
        if (tokenRequestListener != null) {
            tokenRequestListener.onTokenRequestResponseReceived(true, from, "Congratulatioms, Token added to your account.", tokenValue, ethValue);
        }
    }

    @Override
    public void onChannelCreateOkayReceived(String from, long openBlock, double deposit, int endPointType) {
        MeshLog.v("onChannelCreateOkayReceived " + from + " " + openBlock + "  " + deposit);

        try {
            Purchase purchase = databaseService.getPurchaseByBlockNumber(openBlock, ethService.getAddress(), from);
            if (purchase == null) {
                double totalDta = deposit / PurchaseConstants.PRICE_PER_MB;
                databaseService.insertPurchase(ethService.getAddress(), from, totalDta, 0, openBlock, deposit, "",
                        0, "", 0, PurchaseConstants.CHANNEL_STATE.OPEN, endPointType);

                setCurrentSellerWithStatus(null, PurchaseConstants.SELLERS_BTN_TEXT.CLOSE);
                if (purchaseManagerBuyerListener != null) {
                    purchaseManagerBuyerListener.onPurchaseSuccess(from, totalDta, openBlock);
                }
            }
        } catch (Exception e) {
            MeshLog.v("Exception " + e.getMessage());
        }
    }

    public LiveData<Double> getTotalSpentByUser(int endPointType) throws ExecutionException, InterruptedException {
        return databaseService.getTotalSpentByUser(ethService.getAddress(), endPointType);
    }

    /*public LiveData<Double> getTotalEarnByUser(int endPointType) throws ExecutionException, InterruptedException {
        return databaseService.getTotalEarnByUser(ethService.getAddress(), endPointType);
    }*/


    @Override
    public void onChannelCreateErrorReceived(String from, String msg) {
        MeshLog.v("onChannelCreateErrorReceived " + from + " " + msg);
        setCurrentSellerWithStatus(from, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
        if (purchaseManagerBuyerListener != null) {
            purchaseManagerBuyerListener.onPurchaseFailed(from, msg);
        }
    }

    @Override
    public void onUserConnected(String address) {
        if (payController.transportManager.isInternetSeller(address)) {
          //  syncWithSeller(address);
//            giftEther(address);
            ManageSellerList.getInstance(mContext).processAllUsers();
        }
    }

    @Override
    public void onUserDisconnected(String address) {

//        int endPointMode = PreferencesHelper.onInstance(mContext).getEndpointMode();
        com.w3engineers.mesh.application.data.local.helper.PreferencesHelper preferencesHelper =
                com.w3engineers.mesh.application.data.local.helper.PreferencesHelper.on();

        if (!TextUtils.isEmpty(giftRequestedSeller)
                && giftRequestedSeller.equals(address)){
            if (preferencesHelper.getEtherRequestStatus(1) == PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER){
                preferencesHelper.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, 1);
            }
            if (preferencesHelper.getEtherRequestStatus(2) == PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER){
                preferencesHelper.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, 2);
            }
        }

        ManageSellerList.getInstance(mContext).precessDisconnectedSeller(address);
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

        HandlerUtil.postForeground(() -> Toast.makeText(mContext, message, Toast.LENGTH_LONG).show());

    }

    @Override
    public void onPendingMessageInfo(String fromAddress, long dataSize, String msg_id) {
        MeshLog.o("onPendingMessageInfo " + fromAddress + " " + msg_id);
        try {
            Purchase purchase = databaseService.getPurchaseByState(PurchaseConstants.CHANNEL_STATE.OPEN, ethService.getAddress(), fromAddress);
            if (purchase != null) {
                double dataSizeMb = Util.convertBytesToMegabytes(dataSize);
                double dataPrice = PurchaseConstants.PRICE_PER_MB * dataSizeMb;
                double totalBalance = purchase.balance + dataPrice;
                if (totalBalance <= purchase.deposit) {

                    if (purchase.deposit - totalBalance < 0.5) {
                        double remain = purchase.totalDataAmount - purchase.usedDataAmount;
                        if (!isWarningShown) {
                            //  purchaseManagerBuyerListener.onBalancedFinished(purchase.sellerAddress,remain); // for this line check null
                            NotificationUtil.showNotification(mContext, "Internet Usage", "Your purchased internet is almost finished");
                            isWarningShown = true;
                        }

                        if (purchaseManagerBuyerListener != null) {
                            purchaseManagerBuyerListener.onBalancedFinished(purchase.sellerAddress, 1); // for this line check null
                        }
                    }

                    int endPointType = PreferencesHelper.onInstance(mContext).getEndpointMode();
                    String bps = ethService.getBalanceProof(purchase.sellerAddress, purchase.openBlockNumber, totalBalance, endPointType);
//                    MeshLog.p("balanceproofcheck 1 " + purchase.sellerAddress);
//                    MeshLog.p("balanceproofcheck 2 " + purchase.openBlockNumber);
//                    MeshLog.p("balanceproofcheck 3 " + totalBalance);
//                    MeshLog.p("balance proof " + bps);

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

                    NotificationUtil.showNotification(mContext, "Internet Usage", "Your current internet volume insufficient");
                    if (purchaseManagerBuyerListener != null) {
                        purchaseManagerBuyerListener.onBalancedFinished(purchase.sellerAddress, 0);
                    } else {
                        HandlerUtil.postForeground(() -> ToastUtil.showLong(mContext, "Your purchased data has finished."));
                    }
                }
            } else {
                JSONObject jo = new JSONObject();
                jo.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                jo.put(PurchaseConstants.JSON_KEYS.MESSAGE_TEXT, "User has no purchase from this seller.");
                jo.put(PurchaseConstants.JSON_KEYS.MESSAGE_ID, msg_id);

                payController.sendPayForMessageError(jo, fromAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buyData(double amount, String sellerAddress) {
        totalDataAmount = amount;

        try {

            int currentEndPoint = PreferencesHelper.onInstance(mContext).getEndpointMode();

            Purchase purchase = databaseService.getPurchaseByState(PurchaseConstants.CHANNEL_STATE.OPEN,
                    ethService.getAddress(), sellerAddress, currentEndPoint);

            prepareChannel(sellerAddress, purchase);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setCurrentSellerWithStatus(sellerAddress, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASING);

        if (purchaseManagerBuyerListener != null) {
            purchaseManagerBuyerListener.onConnectingWithSeller(sellerAddress);
        }
    }

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
                setCurrentSellerWithStatus(sellerId, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
                if (purchaseManagerBuyerListener != null) {
                    purchaseManagerBuyerListener.onPurchaseFailed(sellerId, "no purchase");
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void destroyObject() {
        payController.setBuyerListener(null);

        purchaseManagerBuyer = null;
        payController = null;
        ethService = null;
        purchaseManagerBuyerListener = null;
        databaseService = null;
        myBalanceInfoListener = null;
        etherRequestListener = null;
        tokenRequestListener = null;
    }

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

    public void setPurchaseManagerBuyerListener(PurchaseManagerBuyerListener listener) {
        this.purchaseManagerBuyerListener = listener;
    }

    public void getMyInfo(String receiver, String qyery, int purpose, int endPointType) {
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

    public interface SellerSearchListener {
        void onSellerFoundSuccess(String sellerId);

        void onSellerFoundError(String msg);
    }

    public void getSellerForInternetMessage(SellerSearchListener listener) {
        try {
            List<Purchase> purchaseList = databaseService.getMyPurchasesWithState(ethService.getAddress(), PurchaseConstants.CHANNEL_STATE.OPEN);

            if (purchaseList != null && purchaseList.size() > 0) {
                Purchase purchase = null;
                for (Purchase p : purchaseList) {
                    if (payController.transportManager.isUserConnected(p.sellerAddress)) {
                        purchase = p;
                        break;
                    }
                }

                if (purchase == null) {
                    listener.onSellerFoundError("no seller connected.");
                } else {
                    if (purchase.deposit > purchase.balance) {
                        listener.onSellerFoundSuccess(purchase.sellerAddress);
                    } else {
                        listener.onSellerFoundError("Not enough data balance.");
                    }
                }
            } else {
                listener.onSellerFoundError("You don't have any purchase.");
            }
        } catch (Exception e) {
//            MeshLog.p("Exception " + e.getMessage());
            listener.onSellerFoundError(e.getMessage());
        }

    }

    public void setCurrentSellerId(String sellerId){
        MeshLog.v("CurrentSellerId " + sellerId);
        this.currentSellerId = sellerId;
    }

    public String getCurrentSellerId(){
        return this.currentSellerId;
    }

/*    public void syncWithSeller(String sellerAddress) {
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
            jsonObject.put(PurchaseConstants.JSON_KEYS.SELLER_ADDRESS, sellerAddress);

        } catch (JSONException e) {
        }

        payController.saySyncSeller(jsonObject, sellerAddress);
    }*/

    @Override
    public void onSyncMessageOKReceived(String buyerAddress, String sellerAddress, long blockNumber,
                                        double usedDataAmount, double totalDataAmount, double balance,
                                        String bps, String chs, int endPointType) {
//        MeshLog.p( "onSyncMessageOKReceived: " + sellerAddress);
//
        if (blockNumber == 0) {
//            buyDataListener.onStatusUpdated(sellerAddress, "Purchase");
        } else {
//            MeshLog.p("onSyncMessageOKReceived: ud=" + usedDataAmount + " td=" + totalDataAmount);
            Purchase purchase = null;
            try {
                purchase = DatabaseService.getInstance(this.mContext).getPurchaseByBlockNumber(blockNumber, ethService.getAddress(), sellerAddress);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (purchase != null) {
                if (purchase.balance != balance) {
                    purchase.balance = balance;
                    purchase.usedDataAmount = usedDataAmount;
                    purchase.balanceProof = bps;
                    purchase.closingHash = chs;
                    purchase.blockChainEndpoint = endPointType;

                    if (purchase.totalDataAmount != totalDataAmount) {
                        purchase.totalDataAmount = totalDataAmount;
                        double deposit = totalDataAmount * PurchaseConstants.PRICE_PER_MB;
                        purchase.deposit = deposit;

                    }
                    MeshLog.p("balancemismatchcheck2 " + balance + "  " + purchase.usedDataAmount);
                    databaseService.updatePurchase(purchase);
                }

            } else {

                double deposit = totalDataAmount * PurchaseConstants.PRICE_PER_MB;
                try {
                    databaseService.insertPurchase(buyerAddress, sellerAddress, totalDataAmount,
                            usedDataAmount, blockNumber, deposit, bps, balance, chs, 0.0,
                            PurchaseConstants.CHANNEL_STATE.OPEN, endPointType);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

            sendSyncOkMessage(sellerAddress);

        }
    }

    public void sendSyncOkMessage(String sellerAddress) {
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
            jsonObject.put(PurchaseConstants.JSON_KEYS.SELLER_ADDRESS, sellerAddress);

        } catch (JSONException e) {
        }

        payController.sendSyncOkMessage(jsonObject, sellerAddress);
    }

    @Override
    public void onInfoOkayReceived(String from, int purpose, JSONObject infoJson, int endPointType) {
        switch (purpose) {
            case PurchaseConstants.INFO_PURPOSES.REFRESH_BALANCE:
                try {
                    // TODO you have endPoint
                    double ethBalance = infoJson.optDouble(PurchaseConstants.INFO_KEYS.ETH_BALANCE)/* ? infoJson.getDouble(PurchaseConstants.INFO_KEYS.ETH_BALANCE) : ethService.getMyEthBalance()*/;
                    double tknBalance = infoJson.optDouble(PurchaseConstants.INFO_KEYS.TKN_BALANCE)/* ? infoJson.getDouble(PurchaseConstants.INFO_KEYS.TKN_BALANCE) : ethService.getMyTokenBalance()*/;

                    EthereumServiceUtil.getInstance(mContext).updateCurrencyAndToken(endPointType, ethBalance, tknBalance);

                    if (myBalanceInfoListener != null) {
                        myBalanceInfoListener.onBalanceInfoReceived(ethBalance, tknBalance);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PurchaseConstants.INFO_PURPOSES.BUY_TOKEN:
                double token = EthereumServiceUtil.getInstance(mContext).getToken(endPointType);
                double currency = EthereumServiceUtil.getInstance(mContext).getCurrency(endPointType);
                try {
                    // TODO you have endPoint


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

                            if (tokenRequestListener != null) {
                                tokenRequestListener.onTokenRequestResponseReceived(true, from, "Purchase request sent.", token, currency);
                            }


                        } else {
                            if (tokenRequestListener != null) {
                                tokenRequestListener.onTokenRequestResponseReceived(false, from, Util.getCurrencyTypeMessage("You don't have enough %s."), token, currency);
                            }
                        }
                    } else {
                        if (tokenRequestListener != null) {
                            tokenRequestListener.onTokenRequestResponseReceived(false, from, "Can't reach network, please try again later.", token, currency);
                        }
                    }


                } catch (JSONException e) {
                    if (tokenRequestListener != null) {
                        tokenRequestListener.onTokenRequestResponseReceived(false, from, e.getMessage(), token, currency);
                    }
                } catch (Exception e) {
                    if (tokenRequestListener != null) {
                        tokenRequestListener.onTokenRequestResponseReceived(false, from, e.getMessage(), token, currency);
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
//                        ethService.setMyEthBalance(ethBalance);


                        if (ethBalance > 0) {

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());

                            JSONArray array = new JSONArray();

                            if (purchaseList != null) {
                                for (Purchase purchase : purchaseList) {

                                    String signedMessage = ethService.close(purchase.sellerAddress, purchase.openBlockNumber,
                                            purchase.balance, purchase.balanceProof, purchase.closingHash, nonce, endPointType);

                                    MeshLog.p("balanceproofcheck 5 " + purchase.sellerAddress);
                                    MeshLog.p("balanceproofcheck 6 " + purchase.openBlockNumber);
                                    MeshLog.p("balanceproofcheck 7 " + purchase.balance);
                                    MeshLog.p("balanceproofcheck 8 " + purchase.balanceProof);
                                    MeshLog.p("balanceproofcheck 9 " + purchase.closingHash);


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

                                if (purchaseManagerBuyerListener != null) {
                                    purchaseManagerBuyerListener.showToastMessage("Closing request sent.");
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
                    double dataPrice = totalDataAmount * PurchaseConstants.PRICE_PER_MB;
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
                        } else if (sharedData > 0 && sharedData < totalDataAmount) {
                            failedMessage = "Seller does not have enough data to sell.";
                        } else if (ethBalance == 0) {
                            failedMessage = Util.getCurrencyTypeMessage("You do not have enough %s.");
                        } else if (tknBalance < dataPrice) {
                            failedMessage = "You do not have enough token.";
                        }

                    }
                    if (failedMessage != null) {
                        setCurrentSellerWithStatus(from, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
                        if (purchaseManagerBuyerListener != null) {
                            purchaseManagerBuyerListener.onPurchaseFailed(from, failedMessage);
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
                if (myBalanceInfoListener != null) {
                    myBalanceInfoListener.onBalanceErrorReceived(msg);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onReceivedEtherRequestResponse(String from, int responseCode) {
        if (responseCode == 200) {
            if (etherRequestListener != null) {
                etherRequestListener.onResponseReceived(true, from, Util.getCurrencyTypeMessage("Request has been sent, %s will be added to your address soon."));
            }
        } else {
            if (etherRequestListener != null) {
                etherRequestListener.onResponseReceived(false, from, Util.getCurrencyTypeMessage("%s request rejected!"));
            }
        }
    }

    @Override
    public void onReceivedEther(String from, double balance) {

    }

    @Override
    public void onChannelCloseReceived(String fromAddress, String sellerAddress, long open_block, int endPointType) {
        try {
            Purchase closingPurchase = databaseService.getPurchaseByBlockNumber(open_block, ethService.getAddress(), sellerAddress);
            closingPurchase.state = PurchaseConstants.CHANNEL_STATE.CLOSED;
            closingPurchase.withdrawnBalance = closingPurchase.balance;
            closingPurchase.blockChainEndpoint = endPointType;

            databaseService.updatePurchase(closingPurchase);

            setCurrentSellerWithStatus(null, PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);

            if (purchaseManagerBuyerListener != null) {
                purchaseManagerBuyerListener.onPurchaseCloseSuccess(closingPurchase.sellerAddress);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChannelTopupReceived(String fromAddress, long openBlock, double deposit, int endPointType) {

        try {
            Purchase topupPurchase = databaseService.getPurchaseByBlockNumber(openBlock, ethService.getAddress(), fromAddress);
            double totalData = topupPurchase.deposit / PurchaseConstants.PRICE_PER_MB;

            topupPurchase.deposit = deposit;
            topupPurchase.totalDataAmount = totalData;
            topupPurchase.blockChainEndpoint = endPointType;

            databaseService.updatePurchase(topupPurchase);

            setCurrentSellerWithStatus(null, PurchaseConstants.SELLERS_BTN_TEXT.CLOSE);
            if (purchaseManagerBuyerListener != null) {
                purchaseManagerBuyerListener.onPurchaseSuccess(fromAddress, totalData, openBlock);
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
                    if (purchaseManagerBuyerListener != null) {
                        purchaseManagerBuyerListener.onPurchaseFailed(fromAddress, msg);
                    }
                }
                break;

            case PurchaseConstants.REQUEST_TYPES.BUY_TOKEN:
                if (tokenRequestListener != null) {
                    tokenRequestListener.onTokenRequestResponseReceived(success, fromAddress, msg, 0, 0);
                }
                break;

            case PurchaseConstants.REQUEST_TYPES.CLOSE_CHANNEL:
                if (purchaseManagerBuyerListener != null) {
                    if (success) {
                        purchaseManagerBuyerListener.showToastMessage(msg);
                    } else {
                        //Todo check from adress and seller adress are same
                        purchaseCloseFailed(fromAddress, msg);
                    }
                }

                break;
        }


    }

    public interface MyBalanceInfoListener {
        void onBalanceInfoReceived(double ethBalance, double tknBalance);

        void onBalanceErrorReceived(String msg);
    }

    public void getMyBalanceInfo(MyBalanceInfoListener listener) {
        MeshLog.p("getMyBalanceInfo");
        myBalanceInfoListener = listener;
        List<String> sellerIds = payController.transportManager.getInternetSellers();
        if (sellerIds.size() == 0) {
            if (myBalanceInfoListener != null)
                myBalanceInfoListener.onBalanceErrorReceived("No internet seller connected.");
        } else {
            String sellerId = sellerIds.get(0);
            String query = PurchaseConstants.INFO_KEYS.ETH_BALANCE + "," + PurchaseConstants.INFO_KEYS.TKN_BALANCE;
            int endPointType = PreferencesHelper.onInstance(mContext).getEndpointMode();

            getMyInfo(sellerId, query, PurchaseConstants.INFO_PURPOSES.REFRESH_BALANCE, endPointType);
        }
    }

    public interface EtherRequestListener {
        void onResponseReceived(boolean success, String from, String msg);
    }

    public void sendEtherRequest(EtherRequestListener listener) {

        etherRequestListener = listener;

        List<String> sellerIds = payController.transportManager.getInternetSellers();
        if (sellerIds.size() == 0) {
            listener.onResponseReceived(false, null, "No internet provider connected");
        } else {
            String sellerId = sellerIds.get(0);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                int endpointType = PreferencesHelper.onInstance(mContext).getEndpointMode();
                setEndPointInfoInJson(jsonObject, endpointType);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            payController.sendEtherRequestMessage(jsonObject, sellerId);
        }
    }

    public interface TokenRequestListener {
        void onTokenRequestResponseReceived(boolean success, String from, String msg, double tokenValue, double ethValue);
    }

    public void sendTokenRequest(TokenRequestListener listener) {
        tokenRequestListener = listener;
        List<String> sellerIds = payController.transportManager.getInternetSellers();
        if (sellerIds.size() == 0) {
            listener.onTokenRequestResponseReceived(false, null, "No internet provider connected", 0, 0);
        } else {
            String sellerId = sellerIds.get(0);

            String query = PurchaseConstants.INFO_KEYS.ETH_BALANCE + "," + PurchaseConstants.INFO_KEYS.NONCE;

            int endPointType = PreferencesHelper.onInstance(mContext).getEndpointMode();

            getMyInfo(sellerId, query, PurchaseConstants.INFO_PURPOSES.BUY_TOKEN, endPointType);

        }

    }

    public void closePurchase(String sellerId) {
        String receiverId = null;
        if (payController.transportManager.isUserConnected(sellerId)) {
            receiverId = sellerId;
        } else {
            List<String> sellerIds = payController.transportManager.getInternetSellers();
            if (sellerIds.size() > 0) {
                receiverId = sellerIds.get(0);
            }
        }

        if (receiverId == null) {

            purchaseCloseFailed(sellerId, "No internet provider connected");
        } else {
            try {
                Purchase purchase = databaseService.getPurchaseByState(PurchaseConstants.CHANNEL_STATE.OPEN, ethService.getAddress(), sellerId);

                if (purchase != null && purchase.balance > 0) {

                    purchase.state = PurchaseConstants.CHANNEL_STATE.CLOSING;
                    databaseService.updatePurchase(purchase);

                    setCurrentSellerWithStatus(sellerId, PurchaseConstants.SELLERS_BTN_TEXT.CLOSING);

                    if (purchaseManagerBuyerListener != null) {
                        purchaseManagerBuyerListener.onPurchaseClosing(sellerId);
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

    private void purchaseCloseFailed(String sellerId, String s) {
        setCurrentSellerWithStatus(sellerId, PurchaseConstants.SELLERS_BTN_TEXT.CLOSE);

        if (purchaseManagerBuyerListener != null) {
            purchaseManagerBuyerListener.onPurchaseCloseFailed(sellerId, s);
        }
    }


    private void setCurrentSellerWithStatus(String id, String status) {
        ManageSellerList.getInstance(mContext).setCurrentSeller(id, status);
    }

    private void setEndPointInfoInJson(JSONObject jsonObject, int endPoint) throws JSONException {
        jsonObject.put(PurchaseConstants.JSON_KEYS.END_POINT_TYPE, endPoint);
    }

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
                if (myBalanceInfoListener != null) {
                    myBalanceInfoListener.onBalanceErrorReceived(internetProviderError);
                }
                break;

            case PurchaseConstants.INFO_PURPOSES.BUY_TOKEN:
                if (tokenRequestListener != null) {
                    tokenRequestListener.onTokenRequestResponseReceived(false, null, internetProviderError, 0, 0);
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
                if (etherRequestListener != null) {
                    etherRequestListener.onResponseReceived(false, null, internetProviderError);
                }
                break;

            case PurchaseConstants.MESSAGE_TYPES.GIFT_ETHER_REQUEST:
                com.w3engineers.mesh.application.data.local.helper.PreferencesHelper preferencesHelper = com.w3engineers.mesh.application.data.local.helper.PreferencesHelper.on();

                if (preferencesHelper.getEtherRequestStatus(1) == PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER){
                    preferencesHelper.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, 1);
                }
                if (preferencesHelper.getEtherRequestStatus(2) == PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER){
                    preferencesHelper.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, 2);
                }
                break;
        }
    }

    @Override
    public void giftRequestSubmitted(boolean status, String submitMessage, String etherTransactionHash,
                                     String tokenTransactionHash, int endPoint, String failedBy) {

        MeshLog.v("giftEther giftRequestSubmitted " + status + " failedby " + failedBy);

        com.w3engineers.mesh.application.data.local.helper.PreferencesHelper preferencesHelper =
                com.w3engineers.mesh.application.data.local.helper.PreferencesHelper.on();

        if (status) {

            preferencesHelper.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.GOT_TRANX_HASH, endPoint);
            preferencesHelper.setGiftEtherHash(etherTransactionHash, endPoint);
            preferencesHelper.setGiftTokenHash(tokenTransactionHash, endPoint);
//            preferencesHelper.setGiftEndpointType(endPoint);

            String toastMessage = Util.getCurrencyTypeMessage("Congratulations!!!\nYou have been awarded 1 %s and 50 token.\nBalance will be added within few minutes.");

            Activity currentActivity = MeshApp.getCurrentActivity();
            if (currentActivity != null) {
                HandlerUtil.postForeground(() -> DialogUtil.showConfirmationDialog(currentActivity, "Gift Awarded!", toastMessage, null, "OK", null));
//            HandlerUtil.postForeground(() -> Toast.makeText(mContext, toastMessage, Toast.LENGTH_LONG).show());
            }else {
                //TODO send notifications
            }
        } else {
            MeshLog.v("giftEther giftRequestSubmitted " + submitMessage);
            if (failedBy.equals("admin")){
                preferencesHelper.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.GOT_GIFT_ETHER, endPoint);
                getMyBalanceInfo(null);
            }else {
                preferencesHelper.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, endPoint);
                Activity currentActivity = MeshApp.getCurrentActivity();
                if (currentActivity != null) {
                    HandlerUtil.postForeground(() -> DialogUtil.showConfirmationDialog(currentActivity, "Gift Awarded!", submitMessage, null, "OK", null));
                }
            }

        }

    }

    private String giftRequestedSeller = null;

    @Override
    public void giftResponse(boolean status, double ethBalance, double tokenBalance, int endPoint) {
        MeshLog.v("giftEther giftResponse " + status + " eth " + ethBalance + " tiken " + tokenBalance);
        com.w3engineers.mesh.application.data.local.helper.PreferencesHelper preferencesHelper =
                com.w3engineers.mesh.application.data.local.helper.PreferencesHelper.on();

        if (status) {

            preferencesHelper.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.GOT_GIFT_ETHER, endPoint);
            preferencesHelper.setGiftEtherHash(null, endPoint);
            preferencesHelper.setGiftTokenHash(null, endPoint);
            databaseService.updateCurrencyAndToken(endPoint, ethBalance, tokenBalance);

//            HandlerUtil.postForeground(() -> Toast.makeText(mContext, "Congratulations!!!\nBalance has been added to your account.", Toast.LENGTH_LONG).show());
            Activity currentActivity = MeshApp.getCurrentActivity();
            if (currentActivity != null){
                HandlerUtil.postForeground(() -> DialogUtil.showConfirmationDialog(currentActivity, "Gift Awarded!", "Congratulations!!!\nBalance has been added to your account.", null, "OK", null));
            }else {
                //TODO send notifications
            }


        } else {
            //TODO detect fail type
            preferencesHelper.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET, endPoint);
        }

    }

    public boolean giftEther(String sellerAddress) {
        MeshLog.v("giftEther start");
        try {

            com.w3engineers.mesh.application.data.local.helper.PreferencesHelper preferencesHelper =
                    com.w3engineers.mesh.application.data.local.helper.PreferencesHelper.on();
            int endPointMode = PreferencesHelper.onInstance(mContext).getEndpointMode();
            int requestState = preferencesHelper.getEtherRequestStatus(endPointMode);

            if (requestState == PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET || requestState == PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER) {

                long currentTime = new Date().getTime();
                long requestTime = preferencesHelper.getEtherRequestTimeStamp(endPointMode);
                if (currentTime > (requestTime + 20000)){

                    giftRequestedSeller = sellerAddress;

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());

                    setEndPointInfoInJson(jsonObject, endPointMode);

                    preferencesHelper.setRequestedForEther(PurchaseConstants.GIFT_REQUEST_STATE.REQUESTED_TO_SELLER, endPointMode);
                    preferencesHelper.setEtherRequestTimeStamp(currentTime, endPointMode);
//                    preferencesHelper.setGiftEndpointType(endPointMode);
                    payController.sendGiftRequest(jsonObject, sellerAddress);
                    return true;

                }
            } else if (requestState == PurchaseConstants.GIFT_REQUEST_STATE.GOT_TRANX_HASH) {

                String ethTranxHash = preferencesHelper.getGiftEtherHash(endPointMode);
                String tknTranxHash = preferencesHelper.getGiftTokenHash(endPointMode);
//                int endPointType = preferencesHelper.getGiftEndpointType();

                if (!TextUtils.isEmpty(ethTranxHash) && !TextUtils.isEmpty(tknTranxHash)) {

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAME_FROM, ethService.getAddress());
                    jsonObject.put(PurchaseConstants.JSON_KEYS.GIFT_ETH_HASH_REQUEST_SUBMIT, ethTranxHash);
                    jsonObject.put(PurchaseConstants.JSON_KEYS.GIFT_TKN_HASH_REQUEST_SUBMIT, tknTranxHash);

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
    public boolean giftEtherForOtherNetwork(){
        List<String> sellerIds = payController.transportManager.getInternetSellers();
        if (sellerIds.size() > 0) {
            return  giftEther(sellerIds.get(0));
        }
        return false;
    }
}
