package com.w3engineers.eth.data.helper.model;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.w3engineers.eth.data.remote.BlockRequest;
import com.w3engineers.eth.util.helper.HandlerUtil;

import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class EthGift {
    CopyOnWriteArrayList<EthGiftModel> ethGiftModels = null;
    private static EthGift ethGift;
//    Handler timerHandler = null;
    Runnable timerRunnable = null;
    private static EthGiftListener ethGiftListener;
    private static HashMap<Integer, BlockRequest> blockRequests = null;



    private EthGift(){
        this.ethGiftModels = new CopyOnWriteArrayList<EthGiftModel>();

    }

    public static EthGift on(HashMap<Integer, BlockRequest> hashMap, EthGiftListener listener){
        blockRequests = hashMap;
        ethGiftListener = listener;
        if (ethGift == null){
            ethGift = new EthGift();
        }

        return ethGift;
    }

    public void add(String userAddress, String ethTx, String tknTx, int endpoint, double ethValue, double tknValue){
        EthGiftModel ethGiftModel = new EthGiftModel(userAddress, ethTx, tknTx, endpoint, ethValue, tknValue);
        ethGiftModels.add(ethGiftModel);

        Log.v("Timer", "list added");

        if (timerRunnable == null){
            timerRunnable = new Runnable() {

                @Override
                public void run() {
                    Log.v("Timer",  "time " + ethGiftModels.size());

                    if (ethGiftModels.size() > 0){
                        for (EthGiftModel e : ethGiftModels){

                            try {
                                TransactionReceipt ethTxReceipt = blockRequests.get(e.endpoint).getTransactionReceiptByHash(e.ethTx);
                                TransactionReceipt tknTxReceipt = blockRequests.get(e.endpoint).getTransactionReceiptByHash(e.tknTx);


                                if (ethTxReceipt != null && tknTxReceipt != null){
                                    if (ethTxReceipt.getStatus().equals("0x1") && tknTxReceipt.getStatus().equals("0x1")) {
                                        //success
                                        ethGiftListener.onRequestCompleted(e.userAddress, e.endpoint, true, ethTxReceipt, tknTxReceipt, e.ethValue, e.tknValue);
                                        ethGiftModels.remove(e);
                                    } else if(ethTxReceipt.getStatus().equals("0x0") && tknTxReceipt.getStatus().equals("0x0")) {
                                        //fail
                                        ethGiftListener.onRequestCompleted(e.userAddress, e.endpoint, false, null, null, e.ethValue, e.tknValue);
                                        ethGiftModels.remove(e);
                                    } else if(ethTxReceipt.getStatus().equals("0x1") && tknTxReceipt.getStatus().equals("0x0")) {
                                        Log.v("Timer", "eth true tkn false");
                                        ethGiftListener.onRequestCompleted(e.userAddress, e.endpoint, true, ethTxReceipt, null, e.ethValue, e.tknValue);
                                        ethGiftModels.remove(e);
                                    } else if(ethTxReceipt.getStatus().equals("0x0") && tknTxReceipt.getStatus().equals("0x1")) {
                                        Log.v("Timer", "eth false tkn true");
                                        ethGiftListener.onRequestCompleted(e.userAddress, e.endpoint, true, null, tknTxReceipt, e.ethValue, e.tknValue);
                                        ethGiftModels.remove(e);
                                    } else {
                                        //pending
                                        Log.v("Timer", "pending");
                                    }
                                }
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }
                    }
                    if (ethGiftModels.size() > 0){
                        Log.v("Timer", "do it again");
                        HandlerUtil.postBackground(timerRunnable, 5000);
                    } else {
                        removeTimer();
                    }
                }
            };
            HandlerUtil.postBackground(timerRunnable, 2000);
        }
    }

    public void removeTimer(){
        Log.v("Timer", "remove");
        if (timerRunnable != null) {
            HandlerUtil.removeBackground(timerRunnable);
            timerRunnable = null;
        }
    }

    public interface EthGiftListener {
        public void onRequestCompleted(String address, int endpoint, boolean status, TransactionReceipt ethTransactionReceipt, TransactionReceipt tokenTransactionReceipt, double ethValue, double tknValue);

    }









}
