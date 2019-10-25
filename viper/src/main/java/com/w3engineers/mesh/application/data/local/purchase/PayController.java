package com.w3engineers.mesh.application.data.local.purchase;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import com.w3engineers.mesh.application.data.ApiEvent;
import com.w3engineers.mesh.application.data.AppDataObserver;
import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.local.wallet.WalletService;
import com.w3engineers.mesh.application.data.model.PayMessage;
import com.w3engineers.mesh.application.data.model.PayMessageAck;
import com.w3engineers.mesh.application.data.model.PeerAdd;
import com.w3engineers.mesh.application.data.model.PeerRemoved;
import com.w3engineers.mesh.application.data.remote.model.BuyerPendingMessage;
import com.w3engineers.mesh.util.MeshApp;
import com.w3engineers.mesh.util.MeshLog;
import com.w3engineers.mesh.util.lib.mesh.DataManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PayController {
    private static PayController payController;
//    public DemoTransport transportManager;
    private static PayControllerListenerForSeller payControllerListenerForSeller;
    private static PayControllerListenerForBuyer payControllerListenerForBuyer;
    private DataManager dataManager;

    private Handler handler;
    private long delayTime = 30 * 1000;
    private ConcurrentHashMap<String, TimeoutModel> timeoutMap = new ConcurrentHashMap<>();

    PayController() {
//        transportManager = DemoTransport.getInstance();
        dataManager = DataManager.on();
        setDataManagerObserver();
    }

    public static PayController getInstance() {
        if (payController == null) {
            payController = new PayController();
        }
        return payController;
    }

    public Credentials getCredentials() {
        return WalletService.getInstance(MeshApp.getContext()).getCredentials();
    }

    public DataManager getDataManager(){
        if (dataManager == null){
            dataManager = DataManager.on();
        }
        return dataManager;
    }

    private void setDataManagerObserver(){
        AppDataObserver.on().startObserver(ApiEvent.PEER_ADD,event -> {
            String nodeId = ((PeerAdd) event).peerId;
            sendUserConnected(nodeId, true);
        });


        AppDataObserver.on().startObserver(ApiEvent.PEER_REMOVED,event -> {
            String nodeId = ((PeerRemoved) event).peerId;
            sendUserConnected(nodeId, false);
        });


        AppDataObserver.on().startObserver(ApiEvent.PAY_MESSAGE,event -> {
            PayMessage msg = (PayMessage) event;
            onMessageReceived(msg.sender, msg.paymentData);
        });

        AppDataObserver.on().startObserver(ApiEvent.PAY_MESSAGE_ACK,event -> {
            PayMessageAck msgAck = (PayMessageAck) event;

            onPayMessageAckReceived(msgAck.sender, msgAck.receiver, msgAck.messageId);
        });

        AppDataObserver.on().startObserver(ApiEvent.BUYER_PENDING_MESSAGE,event -> {
            BuyerPendingMessage pendingMessage = (BuyerPendingMessage) event;
            if (payControllerListenerForSeller != null){
                payControllerListenerForSeller.buyerInternetMessageReceived(pendingMessage.sender, pendingMessage.receiver, pendingMessage.messageId, pendingMessage.messageData, pendingMessage.dataLength, pendingMessage.isIncoming);
            }
        });


    }

    private void onMessageReceived(String sender, byte[] paymentData) {
        String receiveMsg = new String(paymentData);


        //String receiveMsg = new String(paymentData);
        //String mainMessage = CryptoHelper.decryptMessage(transportManager.getContext(), sender, receiveMsg);

        MeshLog.p("Received pay message (payController) =" + receiveMsg);
        try {

            JSONObject jsonObject = new JSONObject(receiveMsg);
            int type = jsonObject.getInt(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE), nonce, purpose;
            String fromAddress = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAME_FROM), msg, sellerAddress;
            String msg_id;
            double balance, ethValue = 0, deposit;
            long open_block;
            int endPointType = jsonObject.optInt(PurchaseConstants.JSON_KEYS.END_POINT_TYPE);
            switch (type) {

                case PurchaseConstants.MESSAGE_TYPES.INIT_PURCHASE:
                    payControllerListenerForSeller.onPurchaseInitRequested(fromAddress, endPointType);
                    break;

                case PurchaseConstants.MESSAGE_TYPES.INIT_PURCHASE_OK:
                    double ethB = ((Number) jsonObject.get("eth")).doubleValue();
                    double tknB = ((Number) jsonObject.get("tkn")).doubleValue();
                    double allowance = ((Number) jsonObject.get("allowance")).doubleValue();
                    nonce = jsonObject.getInt("nonce");

                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onInitPurchaseOkReceived(fromAddress, ethB, tknB, nonce, allowance, endPointType);
                    } else {
                        MeshLog.v("INIT_PURCHASE_OK Listener not found");
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.INIT_PURCHASE_ERROR:
                    msg = jsonObject.getString("msg");
                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onInitPurchaseErrorReceived(fromAddress, msg);
                    } else {
                        MeshLog.v("INIT_PURCHASE_ERROR Listener not found");
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.CREATE_CHANNEL:
                    JSONArray jArray = null;
                    if (jsonObject.has(PurchaseConstants.JSON_KEYS.REQUEST_LIST)) {
                        jArray = jsonObject.getJSONArray(PurchaseConstants.JSON_KEYS.REQUEST_LIST);
                    }

                    if (payControllerListenerForSeller != null) {
                        payControllerListenerForSeller.onCreateChannelRequested(fromAddress, jArray, endPointType);
                    } else {
                        MeshLog.v("INIT_PURCHASE_ERROR Listener not found");
                    }
                    break;
                case PurchaseConstants.MESSAGE_TYPES.BUY_TOKEN:
                    JSONArray array = null;
                    if (jsonObject.has(PurchaseConstants.JSON_KEYS.REQUEST_LIST)) {
                        array = jsonObject.getJSONArray(PurchaseConstants.JSON_KEYS.REQUEST_LIST);
                    }
                    if (payControllerListenerForSeller != null) {
                        payControllerListenerForSeller.onBuyTokenRequested(fromAddress, array, endPointType);
                    } else {
                        MeshLog.v("BUY_TOKEN Listener not found");
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.BUY_TOKEN_RESPONSE:

                    double tokenValue = jsonObject.getDouble(PurchaseConstants.INFO_KEYS.TKN_BALANCE);
                    if (jsonObject.has(PurchaseConstants.INFO_KEYS.ETH_BALANCE)){
                        ethValue = jsonObject.getDouble(PurchaseConstants.INFO_KEYS.ETH_BALANCE);
                    }
                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onBuyTokenResponseReceived(fromAddress, tokenValue, ethValue, endPointType);
                    } else {
                        MeshLog.v("BUY_TOKEN_RESPONSE Listener not found");
                    }
                    break;
                case PurchaseConstants.MESSAGE_TYPES.CREATE_CHANNEL_OK:

                    long openBlock = jsonObject.getLong(PurchaseConstants.JSON_KEYS.OPEN_BLOCK);
                    deposit = jsonObject.getLong(PurchaseConstants.JSON_KEYS.DEPOSIT);

                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onChannelCreateOkayReceived(fromAddress, openBlock, deposit, endPointType);
                    } else {
                        MeshLog.v("CREATE_CHANNEL_OK Listener not found");
                    }
                    break;
                case PurchaseConstants.MESSAGE_TYPES.CREATE_CHANNEL_ERROR:
                    msg = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_TEXT);
                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onChannelCreateErrorReceived(fromAddress, msg);
                    } else {
                        MeshLog.v("CREATE_CHANNEL_ERROR Listener not found");
                    }
                    break;
                case PurchaseConstants.MESSAGE_TYPES.INFO_QUERY:
                    String query = jsonObject.getString(PurchaseConstants.JSON_KEYS.INFO_KEYS);
                    purpose = jsonObject.getInt(PurchaseConstants.JSON_KEYS.INFO_PURPOSE);

                    if (payControllerListenerForSeller != null) {
                        payControllerListenerForSeller.onInfoQueryReceived(fromAddress, query, purpose, endPointType);
                    } else {
                        MeshLog.v("INFO_QUERY Listener not found");
                    }
                    break;
                case PurchaseConstants.MESSAGE_TYPES.INFO_OK:
                    JSONObject infoJson = jsonObject.has(PurchaseConstants.JSON_KEYS.INFO_JSON)? jsonObject.getJSONObject(PurchaseConstants.JSON_KEYS.INFO_JSON): null;
                    purpose = jsonObject.getInt(PurchaseConstants.JSON_KEYS.INFO_PURPOSE);

                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onInfoOkayReceived(fromAddress, purpose, infoJson, endPointType);
                    } else {
                        MeshLog.v("INFO_OK Listener not found");
                    }
                    break;
                case PurchaseConstants.MESSAGE_TYPES.INFO_ERROR:
                    msg = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_TEXT);
                    purpose = jsonObject.getInt(PurchaseConstants.JSON_KEYS.INFO_PURPOSE);
                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onInfoErrorReceived(fromAddress, purpose, msg);
                    } else {
                        MeshLog.v("INFO_ERROR Listener not found");
                    }
                    break;
                case PurchaseConstants.MESSAGE_TYPES.GOT_MESSAGE:
                    long dataSize = jsonObject.getLong(PurchaseConstants.JSON_KEYS.DATA_SIZE);
                    msg_id = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_ID);
                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onPendingMessageInfo(fromAddress, dataSize, msg_id);
                    } else {
                        MeshLog.v("GOT_MESSAGE Listener not found");
                    }
                    break;
                case PurchaseConstants.MESSAGE_TYPES.BUYER_UPDATE_NOTIFYER:
                    MeshLog.v("Message Queuing 22");
                    String msg_Id = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_ID);
                    MeshLog.o("********** buyer update callback recieved ************");
                    payControllerListenerForSeller.onBuyerUpdateNotified(msg_Id, fromAddress);
                    break;
                case PurchaseConstants.MESSAGE_TYPES.PAY_FOR_MESSAGE_OK:
                    MeshLog.o("Payment ok");
                    MeshLog.v("Message Queuing 9");
                    balance = jsonObject.getDouble(PurchaseConstants.JSON_KEYS.BPS_BALANCE);
                    open_block = jsonObject.getLong(PurchaseConstants.JSON_KEYS.OPEN_BLOCK);
                    String bps = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_BPS);
                    msg_id = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_ID);
                    if (payControllerListenerForSeller != null) {
                        payControllerListenerForSeller.onPayForMessageOkReceived(fromAddress, msg_id, bps, balance, open_block);
                    } else {
                        MeshLog.v("PAY_FOR_MESSAGE_OK Listener not found");
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.PAY_FOR_MESSAGE_ERROR:
                    MeshLog.o("Payment error");
                    String errorText = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_TEXT);
                    msg_id = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_ID);
                    if (payControllerListenerForSeller != null) {
                        payControllerListenerForSeller.onPayForMessageErrorReceived(fromAddress, msg_id, errorText);
                    } else {
                        MeshLog.v("PAY_FOR_MESSAGE_ERROR Listener not found");
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.PAY_FOR_MESSAGE_RESPONSE:
                    bps = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_BPS);
                    double bps_balance = jsonObject.getDouble(PurchaseConstants.JSON_KEYS.BPS_BALANCE);
                    String chs = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_CHS);
                    openBlock = jsonObject.getLong(PurchaseConstants.JSON_KEYS.OPEN_BLOCK);
                    long byeSize = jsonObject.getLong(PurchaseConstants.JSON_KEYS.DATA_SIZE);
                    String messageId = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_ID);

                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onInternetMessageResponseSuccess(fromAddress, bps, bps_balance, chs, openBlock, byeSize, messageId);
                    } else {
//                        MeshLog.p("PAY_FOR_MESSAGE_RESPONSE listener not found");
                    }
                    break;
                case PurchaseConstants.MESSAGE_TYPES.SYNC_BUYER:
                    sellerAddress = jsonObject.getString(PurchaseConstants.JSON_KEYS.SELLER_ADDRESS);
                    if (payControllerListenerForSeller != null) {
                       // payControllerListenerForSeller.onSyncMessageReceived(fromAddress, sellerAddress);
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.SYNC_SELLER_OK:
                    String buyerAddress = jsonObject.getString(PurchaseConstants.JSON_KEYS.BUYER_ADDRESS);
                    balance = 0.0;
                    String BPS = "";
                    String CHS = "";
                    open_block = 0;
                    double usedDataAmount = 0, totalDataAmount = 0;
                    if (jsonObject.has(PurchaseConstants.JSON_KEYS.OPEN_BLOCK)) {
                        open_block = jsonObject.getLong(PurchaseConstants.JSON_KEYS.OPEN_BLOCK);
                        balance = jsonObject.getDouble(PurchaseConstants.JSON_KEYS.BPS_BALANCE);
                        usedDataAmount = jsonObject.getDouble(PurchaseConstants.JSON_KEYS.USED_DATA);
                        totalDataAmount = jsonObject.getDouble(PurchaseConstants.JSON_KEYS.TOTAL_DATA);
                        BPS = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_BPS);
                        CHS = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_CHS);
                    }
                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onSyncMessageOKReceived(buyerAddress, fromAddress,
                                open_block, usedDataAmount, totalDataAmount, balance, BPS, CHS, endPointType);
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.SYNC_BUYER_OK:
                    sellerAddress = jsonObject.getString(PurchaseConstants.JSON_KEYS.SELLER_ADDRESS);
                    if (payControllerListenerForSeller != null) {
                        payControllerListenerForSeller.onSynBuyerOKReceive(fromAddress, sellerAddress);
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.ETHER_REQUEST:
                    endPointType = jsonObject.getInt(PurchaseConstants.JSON_KEYS.END_POINT_TYPE);
                    if (payControllerListenerForSeller != null) {
                        payControllerListenerForSeller.onReceivedEtherRequest(fromAddress, endPointType);
                    }
                    break;
                case PurchaseConstants.MESSAGE_TYPES.ETHER_REQUEST_RESPONSE:
                    int responseCode = jsonObject.getInt(PurchaseConstants.JSON_KEYS.RESPONSE_CODE);
                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onReceivedEtherRequestResponse(fromAddress, responseCode);
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.RECEIVED_ETHER:
                    ethValue = jsonObject.getDouble(PurchaseConstants.JSON_KEYS.ETHER);
                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onReceivedEther(fromAddress, ethValue);
                    }
                    break;
                case PurchaseConstants.MESSAGE_TYPES.BLOCKCHAIN_REQUEST:
                    JSONArray requestArray = null;
                    if (jsonObject.has(PurchaseConstants.JSON_KEYS.REQUEST_LIST)) {
                        requestArray = jsonObject.getJSONArray(PurchaseConstants.JSON_KEYS.REQUEST_LIST);
                    }
                    if (payControllerListenerForSeller != null) {
                        payControllerListenerForSeller.onBlockchainRequestReceived(fromAddress, requestArray, endPointType);
                    } else {
                        MeshLog.v("BLOCKCHAIN_REQUEST Listener not found");
                    }
                    break;
                case PurchaseConstants.MESSAGE_TYPES.CHANNEL_CLOSED:
                    open_block = jsonObject.getLong(PurchaseConstants.JSON_KEYS.OPEN_BLOCK);
                    sellerAddress = jsonObject.getString(PurchaseConstants.JSON_KEYS.SELLER_ADDRESS);
                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onChannelCloseReceived(fromAddress, sellerAddress, open_block, endPointType);
                    } else {
                        MeshLog.v("CHANNEL_CLOSED Listener not found");
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.CHANNEL_TOPUP:
                    open_block = jsonObject.getLong(PurchaseConstants.JSON_KEYS.OPEN_BLOCK);
                    deposit = jsonObject.getDouble(PurchaseConstants.JSON_KEYS.DEPOSIT);

                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onChannelTopupReceived(fromAddress, open_block, deposit, endPointType);
                    } else {
                        MeshLog.v("CHANNEL_TOPUP Listener not found");
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.BLOCKCHAIN_RESPONSE:
                    boolean success = jsonObject.getBoolean(PurchaseConstants.JSON_KEYS.REQUEST_SUCCESS);
                    msg = jsonObject.getString(PurchaseConstants.JSON_KEYS.MESSAGE_TEXT);
                    int requestType = jsonObject.getInt(PurchaseConstants.JSON_KEYS.REQUEST_TYPE);


                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.onBlockChainResponseReceived(fromAddress, success, requestType, msg);
                    } else {
                        MeshLog.v("BLOCKCHAIN_RESPONSE Listener not found");
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.GIFT_ETHER_REQUEST:
                    if (payControllerListenerForSeller != null) {
                        payControllerListenerForSeller.requestForGiftEther(fromAddress, endPointType);
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.GIFT_ETHER_REQUEST_SUBMITTED:

                    boolean isSuccess = jsonObject.optBoolean(PurchaseConstants.JSON_KEYS.GIFT_REQUEST_IS_SUBMITTED);
                    String message = jsonObject.optString(PurchaseConstants.JSON_KEYS.GIFT_REQUEST_SUBMIT_MESSAGE);
                    String ethReqHash = jsonObject.optString(PurchaseConstants.JSON_KEYS.GIFT_ETH_HASH_REQUEST_SUBMIT);
                    String tknReqHash = jsonObject.optString(PurchaseConstants.JSON_KEYS.GIFT_TKN_HASH_REQUEST_SUBMIT);
                    String failedBy = jsonObject.optString(PurchaseConstants.JSON_KEYS.GIFT_TKN_FAILED_BY);


                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.giftRequestSubmitted(isSuccess, message, ethReqHash, tknReqHash, endPointType, failedBy);
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.GIFT_ETHER_REQUEST_WITH_HASH:
                    String ethtranxHash = jsonObject.optString(PurchaseConstants.JSON_KEYS.GIFT_ETH_HASH_REQUEST_SUBMIT);
                    String tkntranxHash = jsonObject.optString(PurchaseConstants.JSON_KEYS.GIFT_TKN_HASH_REQUEST_SUBMIT);

                    if (payControllerListenerForSeller != null) {
                        payControllerListenerForSeller.requestForGiftEtherWithHash(fromAddress, ethtranxHash, tkntranxHash, endPointType);
                    }
                    break;

                case PurchaseConstants.MESSAGE_TYPES.GIFT_RESPONSE:
                    boolean isSuccessForGift = jsonObject.optBoolean(PurchaseConstants.JSON_KEYS.GIFT_REQUEST_IS_SUBMITTED);
                    double ethBalance = jsonObject.optDouble(PurchaseConstants.JSON_KEYS.GIFT_ETH_BALANCE);
                    double tknBalance = jsonObject.optDouble(PurchaseConstants.JSON_KEYS.GIFT_TKN_BALANCE);

                    if (payControllerListenerForBuyer != null) {
                        payControllerListenerForBuyer.giftResponse(isSuccessForGift, ethBalance, tknBalance, endPointType);
                    }
                    break;

                default:
                    break;


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void onPayMessageAckReceived(String sender, String receiver, String messageId) {
        MeshLog.v("onPayMessageAckReceived s " + sender + " r " + receiver + " i " + messageId);
        if (!TextUtils.isEmpty(messageId)) {

            if (timeoutMap.containsKey(messageId)) {

                TimeoutModel timeoutModel = timeoutMap.get(messageId);

                if (timeoutModel != null) {

                    int timeOutTrackingPoint = timeoutModel.getTimeoutPointer();
                    if (handler != null && handler.hasMessages(timeOutTrackingPoint)) {
                        MeshLog.v("SellerTimer gotAck " + messageId + " purpose " + timeoutModel.getPurpose());

                        handler.removeMessages(timeOutTrackingPoint);
                        timeoutMap.remove(messageId);
                    }
                }
            }

            if (payControllerListenerForSeller != null) {
                payControllerListenerForSeller.onMessageAcknowledgmentReceived(sender, messageId);
            }
        }
    }

   /* @Override
    public void onInternetMessageResponseReceived(String sender, String message) {
//        MeshLog.p("onInternetMessageResponseReceived" + message);
        try {
            JSONObject jsonObject = new JSONObject(message);
            boolean success = jsonObject.getBoolean(PurchaseConstants.JSON_KEYS.MESSAGE_SENT_SUCCESS);

            if (success) {

                JSONObject successJson = jsonObject.getJSONObject(PurchaseConstants.JSON_KEYS.MESSAGE_BODY);


                String bps = successJson.getString(PurchaseConstants.JSON_KEYS.MESSAGE_BPS);
                double bps_balance = successJson.getDouble(PurchaseConstants.JSON_KEYS.BPS_BALANCE);
                String chs = successJson.getString(PurchaseConstants.JSON_KEYS.MESSAGE_CHS);
                long openBlock = successJson.getLong(PurchaseConstants.JSON_KEYS.OPEN_BLOCK);
                long byeSize = successJson.getLong(PurchaseConstants.JSON_KEYS.DATA_SIZE);

                if (payControllerListenerForBuyer != null) {
                    payControllerListenerForBuyer.onInternetMessageResponseSuccess(sender, bps, bps_balance, chs, openBlock, byeSize, "");
                } else {
//                    MeshLog.p("onInternetMessageResponseReceived listener not found");
                }
            } else {
                String errorMessage = jsonObject.getJSONObject(PurchaseConstants.JSON_KEYS.MESSAGE_BODY).getString(PurchaseConstants.JSON_KEYS.MESSAGE_TEXT);

                if (payControllerListenerForBuyer != null) {
                    payControllerListenerForBuyer.onInternetMessageResponseFailed(sender, errorMessage);
                } else {
//                    MeshLog.p("onInternetMessageResponseReceived  listener not found");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/

    public void sendInfoQueryMessage(JSONObject msgObject, String receiver, int purpose) {
        MeshLog.p("sendInfoQueryMessage" + msgObject.toString());

        try {
            msgObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.INFO_QUERY);

            sendPayWithTimeoutMessage(receiver, msgObject.toString(), purpose);

        } catch (Exception e) {
//            MeshLog.p("Exception" + e.getMessage());
        }

    }

    public void sendBuyerPendingMessageInfo(JSONObject jObject, String receiver) {
        MeshLog.v("Message Queuing 8");
        MeshLog.o("sendBuyerPendingMessageInfo" + receiver);
        try {
            jObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.GOT_MESSAGE);

            //sendPayMessage(receiver, jObject.toString());
            sendPayWithTimeoutMessage(receiver, jObject.toString(), PurchaseConstants.TimeoutPurpose.BUYER_PENDING_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
//            MeshLog.v("Exception" + e.getMessage());
        }
    }

    public void sendNotificationForBuyerUpdate(JSONObject jsonObject, String receiver) {
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.BUYER_UPDATE_NOTIFYER);
            sendPayMessage(receiver, jsonObject.toString());

        } catch (Exception e) {
            MeshLog.v("Exception" + e.getMessage());
        }
    }

    public void sendPayForMessageError(JSONObject jsonObject, String receiver) {
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.PAY_FOR_MESSAGE_ERROR);
            sendPayMessage(receiver, jsonObject.toString());

        } catch (Exception e) {
            MeshLog.v("Exception" + e.getMessage());
        }
    }

    public void sendPayForMessageOkay(JSONObject jsonObject, String receiver) {
        MeshLog.o("sendPayForMessageOkay " + receiver);
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.PAY_FOR_MESSAGE_OK);
            sendPayMessage(receiver, jsonObject.toString());

        } catch (Exception e) {
            MeshLog.v("Exception" + e.getMessage());
        }
    }

    public void sendPayForMessageResponse(JSONObject successJson, String receiver) {
        MeshLog.o("-- sendPayForMessageResponse --");
        MeshLog.v("Message Queuing 21");

        try {
            successJson.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.PAY_FOR_MESSAGE_RESPONSE);
            //sendPayMessage(receiver, successJson.toString());
            sendPayWithTimeoutMessage(receiver, successJson.toString(), PurchaseConstants.TimeoutPurpose.PAY_FOR_MESSAGE_RESPONSE);
        } catch (Exception e) {
            MeshLog.v("Exception" + e.getMessage());
        }
    }

    public void sendInfoOkayMessage(JSONObject responseObject, String receiver) {
//        MeshLog.p("sendInfoOkayMessage  " + responseObject.toString() + "  " + receiver);
        try {
            responseObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.INFO_OK);
            sendPayMessage(receiver, responseObject.toString());

        } catch (Exception e) {
            MeshLog.v("Exception" + e.getMessage());
        }
    }

    public void sendInfoErrorMessage(JSONObject errorObject, String receiver) {
        try {
            errorObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.INFO_ERROR);
            sendPayMessage(receiver, errorObject.toString());

        } catch (Exception e) {
            MeshLog.v("Exception" + e.getMessage());
        }
    }

    public void sendGiftRequest(JSONObject requestObj, String receiver) {
        MeshLog.v("giftEther sendGiftRequest");
        try {
            requestObj.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.GIFT_ETHER_REQUEST);

            sendPayWithTimeoutMessage(receiver, requestObj.toString(), PurchaseConstants.MESSAGE_TYPES.GIFT_ETHER_REQUEST);
        } catch (Exception e) {
            MeshLog.v("Exception" + e.getMessage());
        }
    }

    public void sendGiftRequestSubmitted(JSONObject requestObj, String receiver) {
        MeshLog.v("giftEther sendGiftRequestSubmitted");
        try {
            requestObj.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.GIFT_ETHER_REQUEST_SUBMITTED);
            sendPayMessage(receiver, requestObj.toString());
        } catch (Exception e) {
            MeshLog.v("Exception" + e.getMessage());
        }
    }

    public void sendGiftRequestWthHash(JSONObject requestObj, String receiver) {
        MeshLog.v("giftEther sendGiftRequestWthHash");
        try {
            requestObj.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.GIFT_ETHER_REQUEST_WITH_HASH);

            sendPayMessage(receiver, requestObj.toString());
        } catch (Exception e) {
            MeshLog.v("Exception" + e.getMessage());
        }
    }

    public void sendGiftBalance(JSONObject requestObj, String receiver) {
        MeshLog.v("giftEther sendGiftBalance " + requestObj.toString());
        try {
            requestObj.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.GIFT_RESPONSE);
            sendPayMessage(receiver, requestObj.toString());
        } catch (Exception e) {
            MeshLog.v("Exception" + e.getMessage());
        }
    }

    public interface PayControllerListenerForBuyer {
        void onInitPurchaseOkReceived(String sellerAddress, double ethBalance, double tokenBallance, int nonce, double allowance, int endPointType);

        void onInitPurchaseErrorReceived(String sellerAddress, String msg);

        void onBuyTokenResponseReceived(String from, double tokenValue, double ethValue, int endPointType);

        void onChannelCreateOkayReceived(String from, long openBlock, double deposit, int endPointType);

        void onChannelCreateErrorReceived(String from, String msg);

        void onUserConnected(String address);

        void onUserDisconnected(String address);

        void onInternetMessageResponseSuccess(String sender, String bps, double bps_balance, String chs, long open_block, long byteSize, String messageId);

        void onInternetMessageResponseFailed(String sender, String message);

        void onPendingMessageInfo(String fromAddress, long dataSize, String msg_id);

        void onSyncMessageOKReceived(String buyerAddress, String sellerAddress, long blockNumner,
                                     double usedDataAmount, double totalDataAmount, double balance,
                                     String bps, String chs, int endPointType);

        void onInfoOkayReceived(String from, int purpose, JSONObject infoJson, int endPointType);

        void onInfoErrorReceived(String from, int purpose, String msg);

        void onReceivedEtherRequestResponse(String from, int responseCode);

        void onReceivedEther(String from, double balance);

        void onChannelCloseReceived(String fromAddress, String sellerAddress, long open_block, int endPointType);

        void onChannelTopupReceived(String fromAddress, long openBlock, double deposit, int endPointType);

        void onBlockChainResponseReceived(String fromAddress, boolean success, int requestType, String msg);

        void timeoutCallback(TimeoutModel timeoutModel);

        void giftRequestSubmitted(boolean status, String submitMessage, String etherTransactionHash,
                                  String tokenTransactionHash, int endPoint, String failedBy);

        void giftResponse(boolean status, double ethBalance,
                          double tokenBalance, int endPoint);
    }

    public void setBuyerListener(PayControllerListenerForBuyer listener1) {
      //  MeshLog.v("set listener" + listener1.toString());
        payControllerListenerForBuyer = listener1;
//        if (transportManager != null) {
//            transportManager.initPayListener(this);
//        }
    }

    public interface PayControllerListenerForSeller {
        void onPurchaseInitRequested(String from, int endPointType);

        void onCreateChannelRequested(String from, JSONArray reqList, int endPointType);

        //        void onBuyTokenInitRequested(String from, double etherValue);
        void onBuyTokenRequested(String from, JSONArray jArray, int endPointType);

        void onBlockchainRequestReceived(String from, JSONArray jArray, int endPointType);

        void onUserConnected(String address);

        void onUserDisconnected(String address);

        void onMessageAcknowledgmentReceived(String from, String messageId);

        void onInfoQueryReceived(String fromAddress, String query, int purpose, int endPointType);
//        void onPayForMessageOkReceived(String from, long msg_id, String bps, double bps_balance, long open_block);
//        void onPayForMessageErrorReceived(String from, long msg_id, String errorText);

        void onPayForMessageOkReceived(String from, String msg_id, String bps, double bps_balance, long open_block);

        void onPayForMessageErrorReceived(String from, String msg_id, String errorText);

      //  void onSyncMessageReceived(String from, String sellerAddress);

        void onSynBuyerOKReceive(String from, String sellerAddress);

        void onReceivedEtherRequest(String from, int endpointType);

        void onBuyerUpdateNotified(String msg_Id, String fromAddress);

        void requestForGiftEther(String fromAddress, int endPointType);

        void requestForGiftEtherWithHash(String fromAddress, String ethTranxHash, String tknTranxHash, int endPointType);
        void timeoutCallback(TimeoutModel timeoutModel);

        void buyerInternetMessageReceived(String sender, String owner, String msg_id, String msgData, long dataSize, boolean isIncomming);
    }

    public void setSellerListener(PayControllerListenerForSeller listener2) {
        this.payControllerListenerForSeller = listener2;
//        if (transportManager != null) {
//            transportManager.initPayListener(this);
//        }
    }

    public void sendInitPurchase(JSONObject object, String receiver) {
        MeshLog.p("sendInitPurchase " + object.toString());
        try {
            object.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.INIT_PURCHASE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayWithTimeoutMessage(receiver, object.toString(), PurchaseConstants.TimeoutPurpose.INIT_PURCHASE);
    }

    public void sendInitPurchaseOkay(JSONObject jsonObject, String receiver) {
        MeshLog.v("sendInitPurchaseOkay " + jsonObject.toString());
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.INIT_PURCHASE_OK);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayMessage(receiver, jsonObject.toString());
    }

    public void sendInitPurchaseError(JSONObject jsonObject, String receiver) {
        MeshLog.v("sendInitPurchaseError " + jsonObject.toString());
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.INIT_PURCHASE_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayMessage(receiver, jsonObject.toString());
    }

    public void sendCreateChannel(JSONObject jsonObject, String receiver) {
        MeshLog.v("sendCreateChannel " + jsonObject.toString());
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.CREATE_CHANNEL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayWithTimeoutMessage(receiver, jsonObject.toString(), PurchaseConstants.TimeoutPurpose.INIT_CHANNEL);
    }

    public void sendCreateChannelOkay(JSONObject jsonObject, String receiver, String messageId) {
        MeshLog.v("sendCreateChannelOkay " + jsonObject.toString());
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.CREATE_CHANNEL_OK);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayMessageWithMessageId(receiver, jsonObject.toString(), messageId);
    }

    public void sendTopupChannelOkay(JSONObject jsonObject, String receiver, String messageId) {
        MeshLog.v("sendTopupChannelOkay " + jsonObject.toString());
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.CHANNEL_TOPUP);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayMessageWithMessageId(receiver, jsonObject.toString(), messageId);
    }

    public void sendChannelClosed(JSONObject jsonObject, String receiver, String messageId) {
        MeshLog.v("sendChannelClosed " + jsonObject.toString());
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.CHANNEL_CLOSED);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayMessageWithMessageId(receiver, jsonObject.toString(), messageId);
    }

    public void sendCreateChannelError(JSONObject jsonObject, String receiver) {
        MeshLog.v("sendCreateChannelError " + jsonObject.toString());
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.CREATE_CHANNEL_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayMessage(receiver, jsonObject.toString());
    }

    public void sendBlockChainRequest(JSONObject jsonObject, String receiver, int purpose) {

        MeshLog.v("sendBlockChainRequest " + jsonObject.toString());
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.BLOCKCHAIN_REQUEST);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayWithTimeoutMessage(receiver, jsonObject.toString(), purpose);
//        sendPayMessage(receiver, jsonObject.toString());
    }

    public void sendBlockChainResponse(JSONObject jsonObject, String receiver) {

        MeshLog.v("sendBlockChainResponse " + jsonObject.toString());
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.BLOCKCHAIN_RESPONSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayMessage(receiver, jsonObject.toString());
    }

    public void sendBuyToken(JSONObject jsonObject, String receiver) {

        MeshLog.v("sendBuyToken " + jsonObject.toString());
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.BUY_TOKEN);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayWithTimeoutMessage(receiver, jsonObject.toString(), PurchaseConstants.INFO_PURPOSES.BUY_TOKEN);
//        sendPayMessage(receiver, jsonObject.toString());
    }

    public void sendBuyTokenResponse(JSONObject jsonObject, String receiver, String msg_id) {

        MeshLog.v("sendBuyToken " + jsonObject.toString());
        try {
            jsonObject.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.BUY_TOKEN_RESPONSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayMessageWithMessageId(receiver, jsonObject.toString(), msg_id);

    }

    private void removeSellerTimer(String address){
        for (ConcurrentHashMap.Entry<String, TimeoutModel> entry : timeoutMap.entrySet()) {
            TimeoutModel timeoutModel = entry.getValue();
            String key = entry.getKey();
            if (timeoutModel.getReceiverId().equalsIgnoreCase(address)){

                int timeOutTrackingPoint = timeoutModel.getTimeoutPointer();
                if (handler != null && handler.hasMessages(timeOutTrackingPoint)) {

                    MeshLog.v("SellerTimer buyer disconnected " + key + " purpose " + timeoutModel.getPurpose());

                    handler.removeMessages(timeOutTrackingPoint);
                    timeoutMap.remove(key);
                }
            }
        }

    }

    private void sendUserConnected(String address, boolean isConnected) {
        int userMode = PreferencesHelperDataplan.on().getDataPlanRole();

        if (userMode == DataPlanConstants.USER_ROLE.DATA_SELLER) {
            if (payControllerListenerForSeller != null) {
                if (isConnected) {
                    payControllerListenerForSeller.onUserConnected(address);
                } else {
                    removeSellerTimer(address);
                    payControllerListenerForSeller.onUserDisconnected(address);
                }

            } else {
                MeshLog.v("listener not found");
            }
        } else if (userMode == DataPlanConstants.USER_ROLE.DATA_BUYER) {
            if (payControllerListenerForBuyer != null) {
                if (isConnected) {
                    payControllerListenerForBuyer.onUserConnected(address);
                } else {
                    payControllerListenerForBuyer.onUserDisconnected(address);
                }
            } else {
                MeshLog.v("listener not found");
            }
        }
    }

    private void sendPayMessage(String receiver, String message) {
        //  long messageId = 0l;//System.currentTimeMillis();
        String messageId = "";
        //TODO encrypt message
        //transportManager.sendPayMessage(receiver, message, messageId);
        sendPayMessageToTransport(receiver, message, messageId.toString());
    }

    private void sendPayWithTimeoutMessage(String receiver, String message, int purpose) {
        MeshLog.p("sendPayWithTimeoutMessage");
        String requestId = UUID.randomUUID().toString();
        setRequestTimeout(requestId, receiver, purpose);
        sendPayMessageWithMessageId(receiver, message, requestId);
    }

    private void sendPayMessageWithMessageId(String receiver, String message, String messageId) {
        //TODO encrypt message
        //transportManager.sendPayMessage(receiver, message, messageId);
        sendPayMessageToTransport(receiver, message, messageId);
    }

    private void sendPayMessageToTransport(String address, String message, String messageId) {
        //String encryptedMessage = CryptoHelper.encryptMessage(transportManager.getContext(), address, message.getBytes());
        try {
            dataManager.sendPayMessage(address, message, messageId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendSyncOkMessage(JSONObject object, String receiver) {
        try {
            object.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.SYNC_BUYER_OK);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendPayMessage(receiver, object.toString());
    }

    public void sendSyncMessageOK(JSONObject object, String receiver) {
        MeshLog.v("sendSyncMessageOK"+object.toString());
        try {
            object.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.SYNC_SELLER_OK);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayMessage(receiver, object.toString());
    }

    public void sendEtherRequestMessage(JSONObject jo, String receiver) {
        MeshLog.p("sendEtherRequestMessage " + jo.toString() + " " + receiver);
        try {
            jo.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.ETHER_REQUEST);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayWithTimeoutMessage(receiver, jo.toString(), PurchaseConstants.TimeoutPurpose.INIT_ETHER);
    }

    public void sendEtherRequestMessageResponse(JSONObject jo, String receiver) {
        MeshLog.p("sendEtherRequestMessageOk " + jo.toString());
        try {
            jo.put(PurchaseConstants.JSON_KEYS.MESSAGE_TYPE, PurchaseConstants.MESSAGE_TYPES.ETHER_REQUEST_RESPONSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendPayMessage(receiver, jo.toString());
    }

    @SuppressLint("HandlerLeak")
    private void setRequestTimeout(String requestId, String receiverId, int purpose) {

        MeshLog.v("SellerTimer start " + requestId + " purpose " + purpose);

        int timeOutTrackingPoint = (int) System.currentTimeMillis();
        TimeoutModel timeoutModel = new TimeoutModel()
                .setTimeoutPointer(timeOutTrackingPoint)
                .setPurpose(purpose)
                .setReceiverId(receiverId);

        if (handler == null){
            HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
            handlerThread.start();
            Looper looper = handlerThread.getLooper();
            handler = new Handler(looper) {
                @Override
                public void handleMessage(Message msg) {
                    String requestId_ = (String) msg.obj;

                    TimeoutModel mapTimeoutModel = timeoutMap.get(requestId_);

                    if (mapTimeoutModel != null) {
                        timeoutMap.remove(requestId_);

                        if (mapTimeoutModel.getPurpose() == PurchaseConstants.TimeoutPurpose.BUYER_PENDING_MESSAGE || mapTimeoutModel.getPurpose() == PurchaseConstants.TimeoutPurpose.PAY_FOR_MESSAGE_RESPONSE){
                            if (payControllerListenerForSeller != null){
                                payControllerListenerForSeller.timeoutCallback(mapTimeoutModel);
                            }
                        } else {
                            if (payControllerListenerForBuyer != null) {
                                payControllerListenerForBuyer.timeoutCallback(mapTimeoutModel);
                            }
                        }
                    }
                }
            };
        }

        Message msg = handler.obtainMessage(timeOutTrackingPoint, requestId);
        timeoutMap.put(requestId, timeoutModel);
        handler.sendMessageDelayed(msg, delayTime);
    }
}


