package com.w3engineers.eth.data.remote;

import android.content.Context;
import android.net.Network;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.w3engineers.eth.contracts.RaidenMicroTransferChannels;
import com.w3engineers.eth.contracts.TmeshToken;
import com.w3engineers.eth.data.helper.PreferencesHelperPaylib;
import com.w3engineers.eth.data.helper.model.EthGift;
import com.w3engineers.eth.data.helper.model.PayLibNetworkInfo;
import com.w3engineers.eth.data.remote.parse.ParseConstant;
import com.w3engineers.eth.data.remote.parse.ParseManager;
import com.w3engineers.eth.util.data.CellularDataNetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EthereumService implements BlockRequest.BlockTransactionObserver, EthGift.EthGiftListener {

    private static EthereumService instance;
    private Context mContext;
    private Executor executor;
    private Credentials credentials;

    private String TAG = "EthereumService";

    private TransactionObserver transactionObserver;
    private Network network;
    private HashMap<Integer, BlockRequest> blockRequests = null;
    EthGift ethGift;
    private String giftDonateUrl;
    private ParseManager parseManager;
    private NetworkInfoCallback networkInfoCallback;
    private ArrayList<String> txList = new ArrayList<>();
    private PreferencesHelperPaylib preferencesHelperPaylib;

    private EthereumService(Context context, NetworkInfoCallback networkInfoCallback, String giftDonateUrl) {
        mContext = context.getApplicationContext();
        preferencesHelperPaylib = PreferencesHelperPaylib.onInstance(mContext);
        executor = Executors.newSingleThreadExecutor();
        this.giftDonateUrl = giftDonateUrl;

        if (networkInfoCallback == null) {
            throw new NullPointerException("NetworkInfoCallback shouldn't null");
        }

        this.networkInfoCallback = networkInfoCallback;

        if (blockRequests == null) {
            blockRequests = new HashMap<>();

            List<PayLibNetworkInfo> payLibNetworkInfos = networkInfoCallback.getNetworkInfo();

            for (PayLibNetworkInfo payLibNetworkInfo : payLibNetworkInfos) {
                BlockRequest blockRequestETH = new BlockRequest(payLibNetworkInfo.tokenAddress,
                        payLibNetworkInfo.channelAddress,
                        payLibNetworkInfo.networkUrl, mContext,
                        payLibNetworkInfo.gasPrice, payLibNetworkInfo.gasLimit, EthereumService.this, payLibNetworkInfo.networkType);

                blockRequests.put(payLibNetworkInfo.networkType, blockRequestETH);
            }
            ethGift = EthGift.on(blockRequests, EthereumService.this);
        }

        CellularDataNetworkUtil.on(mContext, new CellularDataNetworkUtil.CellularDataNetworkListenerForPurchase() {
            @Override
            public void onAvailable(Network network1) {
                network = network1;
                Log.i(TAG, "onAvailable: " + network.toString());

                for (BlockRequest value : blockRequests.values()) {
                    value.setNetworkInterface(network);
                }
            }

            @Override
            public void onLost() {
                network = null;
            }
        }).initMobileDataNetworkRequest();
    }

    public void setGIftDonateUrl(String giftUrl) {
        this.giftDonateUrl = giftUrl;
        blockRequests = new HashMap<>();

        List<PayLibNetworkInfo> payLibNetworkInfos = networkInfoCallback.getNetworkInfo();

        for (PayLibNetworkInfo payLibNetworkInfo : payLibNetworkInfos) {
            BlockRequest blockRequestETH = new BlockRequest(payLibNetworkInfo.tokenAddress,
                    payLibNetworkInfo.channelAddress,
                    payLibNetworkInfo.networkUrl, mContext,
                    payLibNetworkInfo.gasPrice, payLibNetworkInfo.gasLimit, EthereumService.this, payLibNetworkInfo.networkType);

            blockRequests.put(payLibNetworkInfo.networkType, blockRequestETH);
        }
        ethGift = EthGift.on(blockRequests, EthereumService.this);


        if (network == null) {
            CellularDataNetworkUtil.on(mContext, new CellularDataNetworkUtil.CellularDataNetworkListenerForPurchase() {
                @Override
                public void onAvailable(Network network1) {
                    network = network1;
                    Log.i(TAG, "onAvailable: " + network.toString());

                    for (BlockRequest value : blockRequests.values()) {
                        value.setNetworkInterface(network);
                    }
                }

                @Override
                public void onLost() {
                    network = null;
                }
            }).initMobileDataNetworkRequest();
        } else {
            for (BlockRequest value : blockRequests.values()) {
                value.setNetworkInterface(network);
            }
        }

        if (this.credentials != null){
            setCredential(this.credentials);
        }
    }

    @Override
    public void onRequestCompleted(String address, int endpoint, boolean status, TransactionReceipt ethTxReceipt, TransactionReceipt tknTxReceipt) {
        if (transactionObserver != null)
            transactionObserver.onGiftCompleted(address, endpoint, status);
        if (status && ethTxReceipt != null){

            if (parseManager != null){
                JSONObject log = new JSONObject();
                try {
                    log.put("address", ethTxReceipt.getFrom());
                    log.put("blockHash", ethTxReceipt.getBlockHash());
                    log.put("blockNumber", ethTxReceipt.getBlockNumber());
                    log.put("data", ethTxReceipt.getLogs());
                    log.put("transactionHash", ethTxReceipt.getTransactionHash());
                    log.put("transactionIndex", ethTxReceipt.getTransactionIndex());
                } catch (JSONException e){
                    e.printStackTrace();
                }

                parseManager.sendEtherGifted(ethTxReceipt.getTransactionHash(), ethTxReceipt.getFrom(), ethTxReceipt.getTo(), getWeiValue(1).toString(), log.toString());
            }
        }

        if (status && tknTxReceipt != null){
            if (parseManager != null){
                JSONObject log = new JSONObject();
                try {
                    log.put("address", tknTxReceipt.getFrom());
                    log.put("blockHash", tknTxReceipt.getBlockHash());
                    log.put("blockNumber", tknTxReceipt.getBlockNumber());
                    log.put("data", tknTxReceipt.getLogs());
                    log.put("transactionHash", tknTxReceipt.getTransactionHash());
                    log.put("transactionIndex", tknTxReceipt.getTransactionIndex());
                } catch (JSONException e){
                    e.printStackTrace();
                }

                parseManager.sendTokenGifted(tknTxReceipt.getTransactionHash(), tknTxReceipt.getFrom(), tknTxReceipt.getTo(), getWeiValue(50).toString(), log.toString());
            }
        }
    }

    public void setParseInfo(String parseUrl, String parseAppId) {
        if (parseManager == null){
            parseManager = new ParseManager(parseUrl, parseAppId, mContext);
        }
    }

    public void sendPointRequest(String transactionHash, double rmValue, long block, String userAddress, int currentEndpoint) {
        if (network == null){
            transactionObserver.onTokenRequested(false, "network error", null);
        }else {
            executor.execute(new Runnable() {
                @Override
                public void run() {

                    try {
                        if (TextUtils.isEmpty(giftDonateUrl)){
                            transactionObserver.onTokenRequested(false, "configuration error, please try again later", null);
                        }
                        else {

                            OkHttpClient client = new OkHttpClient();

                            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                            RequestBody body = RequestBody.create(mediaType, "tx="+transactionHash+"&value="+rmValue+ "&block="+block+"&address="+userAddress);
                            Request request = new Request.Builder()
                                    .url("https://airdrop.telemesh.net/request/token")
                                    .post(body)
                                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                                    .addHeader("cache-control", "no-cache")
                                    .addHeader("Postman-Token", "cd5a9cac-258a-4ef9-8bb1-e20201a24e3a")
                                    .build();
                            Response response = client.newCall(request).execute();
                            if (response != null) {
                                if (response.code() == 200){
                                    String jsonData = response.body().string();
                                    JSONObject Jobject = new JSONObject(jsonData);
                                    if (Jobject.getBoolean("success")){
                                        String tknTx = Jobject.getString("tokenTx");

                                        TransactionReceipt tknTxReceipt = getTransactionReceipt(tknTx, currentEndpoint);

                                        if (tknTxReceipt == null){
                                            txList.add(tknTx.toLowerCase());
                                            logTokenTransferred(currentEndpoint);
                                        } else {
                                            if (tknTxReceipt.getStatus().equals("0x1")) {
                                                //success
                                                transactionObserver.onTokenCompleted(true);
                                            } else if(tknTxReceipt.getStatus().equals("0x0")) {
                                                //fail
                                                transactionObserver.onTokenCompleted(false);
                                            } else {
                                                txList.add(tknTx.toLowerCase());
                                                logTokenTransferred(currentEndpoint);
                                            }
                                        }
                                        transactionObserver.onTokenRequested(true, null, tknTx);
                                    } else {
                                        transactionObserver.onTokenRequested(false, Jobject.getString("data"), null);
                                    }
                                } else {
                                    transactionObserver.onTokenRequested(false, "network error", null);
                                }
                            } else {
                                transactionObserver.onTokenRequested(false, "network error", null);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        transactionObserver.onTokenRequested(false, e.getMessage(), null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        transactionObserver.onTokenRequested(false, e.getMessage(), null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        transactionObserver.onTokenRequested(false, e.getMessage(), null);
                    }
                }
            });
        }
    }

    public interface NetworkInfoCallback {
        List<PayLibNetworkInfo> getNetworkInfo();
    }

    synchronized public static EthereumService getInstance(Context context, NetworkInfoCallback networkInfoCallback, String giftDonateUrl) {
        if (instance == null) {
            synchronized (EthereumService.class) {
                if (instance == null) {
                    instance = new EthereumService(context, networkInfoCallback, giftDonateUrl);
                }
            }
        }
        return instance;
    }

    public void setCredential(Credentials credential) {
        this.credentials = credential;
//        Log.i(TAG, "privatekey: " + credential.getEcKeyPair().getPrivateKey().toString(16));
//        Log.i(TAG, "address: " + credential.getAddress());
        saveAddress(this.credentials.getAddress());
        for (BlockRequest value : blockRequests.values()) {
           value.setCredential(credential);
        }
    }

    public void saveAddress(String address) {
        preferencesHelperPaylib.saveAddress(address);
    }

    public String  getAddress() {
        return preferencesHelperPaylib.getAddress();
    }

//    public interface ReqEther {
//        void onEtherRequested(int responseCode);
//    }

    /*public void requestEther(String address, int endPointType, final ReqEther listener) {
        executor.execute(new Runnable() {
            @Override
            public void run() {

                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                RequestBody body = RequestBody.create(mediaType, "address="+address+"&endpoint="+endPointType);
                Request request = new Request.Builder()
                        .url(giftDonateUrl + "eth")
                        .post(body)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .addHeader("cache-control", "no-cache")
                        .addHeader("Postman-Token", "cd5a9cac-258a-4ef9-8bb1-e20201a24e3a")
                        .build();

                *//*OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(faucetDonateUrl + address)
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .build();*//*

                try {
                    Response response = client.newCall(request).execute();
                    if (response != null) {
                        listener.onEtherRequested(response.code());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onEtherRequested(400);
                }
            }
        });
    }*/

    public interface GiftEther {
        void onEtherGiftRequested(boolean success, String msg, String ethTX, String tknTx, String failedBy);
    }

    public void requestGiftEther(String address, int endPointType, final GiftEther listener) {
        if (network == null){
            listener.onEtherGiftRequested(false, "network error", null, null, "system");
        }else {
            executor.execute(new Runnable() {
                @Override
                public void run() {

                    try {
                        Double balance = blockRequests.get(endPointType).getUserEthBalance(address);
                        Integer nonce = blockRequests.get(endPointType).getUserNonce(address);

                        if (balance != null && balance > 0){
                            listener.onEtherGiftRequested(false, "already have balance", null, null, "admin");
                        }
                        else if (nonce != null && nonce > 0){
                            listener.onEtherGiftRequested(false, "already have transactions", null, null, "admin");
                        }
                        else if (TextUtils.isEmpty(giftDonateUrl)){
                            listener.onEtherGiftRequested(false, "configuration error, please try again later", null, null, "system");
                        }
                        else {

                            OkHttpClient client = new OkHttpClient();

                            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                            RequestBody body = RequestBody.create(mediaType, "address="+address+"&endpoint="+endPointType);
                            Request request = new Request.Builder()
                                    .url(giftDonateUrl)
                                    .post(body)
                                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                                    .addHeader("cache-control", "no-cache")
                                    .addHeader("Postman-Token", "cd5a9cac-258a-4ef9-8bb1-e20201a24e3a")
                                    .build();
                            Response response = client.newCall(request).execute();
                            if (response != null) {
                                if (response.code() == 200){
                                    String jsonData = response.body().string();
                                    JSONObject Jobject = new JSONObject(jsonData);
                                    if (Jobject.getBoolean("success")){
                                        JSONObject result = Jobject.getJSONObject("data");
                                        String ethTx = result.getString("ethTX");
                                        String tknTx = result.getString("tokenTx");
                                        listener.onEtherGiftRequested(true, null, ethTx, tknTx, null);

                                        ethGift.add(address, ethTx, tknTx, endPointType);
                                    } else {
                                        listener.onEtherGiftRequested(false, Jobject.getString("data"), null, null, Jobject.getString("failedby"));
                                    }
                                } else {
                                    listener.onEtherGiftRequested(false, "network error", null, null, "system");
                                }
                            } else {
                                listener.onEtherGiftRequested(false, "network error", null, null, "system");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onEtherGiftRequested(false, e.getMessage(), null, null, "system");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onEtherGiftRequested(false, e.getMessage(), null, null, "system");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        listener.onEtherGiftRequested(false, e.getMessage(), null, null, "system");
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        listener.onEtherGiftRequested(false, e.getMessage(), null, null, "system");
                    }catch (Exception e) {
                        e.printStackTrace();
                        listener.onEtherGiftRequested(false, e.getMessage(), null, null, "system");
                    }
                }
            });
        }
    }


    /*public double getMyTokenBalance() {
        double tokenValue = PreferencesHelperPaylib.onInstance(mContext).getTokenBalance();
        return tokenValue;
    }*/

    /*public void setMyTokenBalance(double value) {
        PreferencesHelperPaylib.onInstance(mContext).setTokenBalance(value);
    }*/

    public Double getUserTokenAllowance(final String owner, int endpointType) throws ExecutionException, InterruptedException {
        if (network == null)
            return  null;

        return blockRequests.get(endpointType).getUserTokenAllowance(owner);
    }

    public Tuple2<Double, Double> getChannelInfo(final String sender, final String receiver, final long blockNumber, int endpointType) throws ExecutionException, InterruptedException {

        if (network == null)
            return null;

        return blockRequests.get(endpointType).getChannelInfo(sender, receiver,blockNumber);
    }

    public String getBalanceProof(String receiver, long blockNumber, double balance, int endpointType) {
        return blockRequests.get(endpointType).getBalanceProof(receiver, blockNumber, balance);
    }

    public String verifyBalanceProofSignature( String receiver, long blockNumber,  double balance,  String balanceProof, int endpointType) throws ExecutionException, InterruptedException {

        if (network == null)
            return null;

        return blockRequests.get(endpointType).verifyBalanceProofSignature(receiver, blockNumber, balance, balanceProof);
    }

    public String getClosingHash(String sender, long blockNumber, double balance, int endpointType) {
        return blockRequests.get(endpointType).getClosingHash(sender, blockNumber, balance);
    }

    public String getOwner() {
        return this.credentials.getAddress();
    }

//    public double getMyEthBalance() {
//        double ethValue = PreferencesHelperPaylib.onInstance(mContext).getEtherBalance();
//
//        return ethValue;
//    }

    /*public void setMyEthBalance(double value) {
        PreferencesHelperPaylib.onInstance(mContext).setEtherBalance(value);
    }*/

    public Double getUserTokenBalance(String address, int endpointType) throws ExecutionException, InterruptedException {
        if (network == null)
            return  null;

        Log.v("EthereumService ", "tkn endpoint " + endpointType);

        return blockRequests.get(endpointType).getUserTokenBalance(address);
    }

    public Double getUserEthBalance(String address, int endpointType) throws ExecutionException, InterruptedException{
        if (network == null)
            return  null;

        Log.v("EthereumService ", "eth endpoint " + endpointType);
        return blockRequests.get(endpointType).getUserEthBalance(address);
    }

    public Integer getUserNonce(String address, int endpointType) throws ExecutionException, InterruptedException{
        if (network == null)
            return  null;

        return blockRequests.get(endpointType).getUserNonce(address);
    }

    public String transferToken(String to, double value, int nonce, int endpointType) throws Exception {
        return blockRequests.get(endpointType).transferToken(to, value, nonce);
    }

    public String approve(double value, int nonce, int endpointType) throws Exception {
        return blockRequests.get(endpointType).approve(value, nonce);
    }

    public String createChannel(String sellerAddress, double deposit, int nonce, int endpointType) throws Exception {
        return blockRequests.get(endpointType).createChannel(sellerAddress, deposit, nonce);
    }

    public String topup(String receiver, long blockNumber, double added_deposit, int nonce, int endpointType) throws Exception {
        return blockRequests.get(endpointType).topup(receiver, blockNumber, added_deposit, nonce);
    }

    public String close(String receiver, long block_number, double balance, String balance_signature, String closing_signature, int nonce, int endpointType) throws Exception {
        return blockRequests.get(endpointType).close(receiver, block_number, balance, balance_signature, closing_signature, nonce);
    }

    public String withdraw(long block_number, double balance, String balance_signature, int nonce, int endpointType) throws Exception {
        return blockRequests.get(endpointType).withdraw(block_number, balance, balance_signature, nonce);
    }

    public String buyToken(double value, int nonce, int endpointType) throws Exception {
        return blockRequests.get(endpointType).buyToken(value, nonce);
    }

    public TransactionReceipt getTransactionReceipt(String hash, int endpointType) throws Exception {
        if (network == null)
            return  null;

        return blockRequests.get(endpointType).getTransactionReceiptByHash(hash);
    }

    public interface SubmitRequestListener {
        void onRequestSubmitted(String hash, int forRId);

        void onRequestSubmitError(String msg, int forRId);
    }

    public void submitRequest(String hexValue, int forRId, int endpointType, SubmitRequestListener listener) {
        Log.i(TAG, "submitRequest: " + hexValue);
        if (network != null) {

            blockRequests.get(endpointType).submitRequest(hexValue, forRId, new BlockRequest.SubmitRequestListener() {
                @Override
                public void onRequestSubmitted(String hash, int forRId) {
                    listener.onRequestSubmitted(hash, forRId);
                }

                @Override
                public void onRequestSubmitError(String msg, int forRId) {
                    listener.onRequestSubmitError(msg, forRId);
                }
            });
        } else {
            listener.onRequestSubmitError("network configuration error.", forRId);
        }
    }

    public interface TransactionObserver {
        void onBalanceApprovedLog(TmeshToken.ApprovalEventResponse typedResponse);

        void onChannelCreatedLog(RaidenMicroTransferChannels.ChannelCreatedEventResponse typedResponse);

        void onChannelToppedUpLog(RaidenMicroTransferChannels.ChannelToppedUpEventResponse typedResponse);

        void onChannelClosedLog(RaidenMicroTransferChannels.ChannelSettledEventResponse typedResponse);

        void onChannelWithdrawnLog(RaidenMicroTransferChannels.ChannelWithdrawEventResponse typedResponse);

        void onTokenMintedLog(TmeshToken.MintedEventResponse typedResponse);

        void onTokenTransferredLog(TmeshToken.TransferEventResponse typedResponse);

        void onGiftCompleted(String address, int endpoint, boolean Status);

        void onTokenRequested(boolean success, String msg, String tknTx);

        void onTokenCompleted(boolean success);
    }

    public void setTransactionObserver(TransactionObserver transactionObserver) {
        this.transactionObserver = transactionObserver;
    }

    public void logBalanceApproved(int endpointType) {
        long blockNumber = preferencesHelperPaylib.getBalanceApprovedBlock(endpointType);
        blockRequests.get(endpointType).logBalanceApproved(blockNumber);
    }

    public void logTokenMinted( int endpointType) {
        long blockNumber = preferencesHelperPaylib.getTokenMintedBlock(endpointType);
        blockRequests.get(endpointType).logTokenMinted(blockNumber);
    }

    public void logChannelCreated( int endpointType) {
        long blockNumber = preferencesHelperPaylib.getChannelCreatedBlock(endpointType);
        blockRequests.get(endpointType).logChannelCreated(blockNumber);
    }

    public void logChannelToppedUp( int endpointType) {
        long blockNumber = preferencesHelperPaylib.getChannelTopupBlock(endpointType);
        blockRequests.get(endpointType).logChannelToppedUp(blockNumber);
    }

    public void logChannelClosed( int endpointType) {
        long blockNumber = preferencesHelperPaylib.getChannelClosedBlock(endpointType);
        blockRequests.get(endpointType).logChannelClosed(blockNumber);
    }

    public void logChannelWithdrawn( int endpointType) {
        long blockNumber = preferencesHelperPaylib.getChannelWithdrawnBlock(endpointType);
        blockRequests.get(endpointType).logChannelWithdrawn(blockNumber);
    }

    public void logTokenTransferred( int endpointType) {
        long blockNumber = preferencesHelperPaylib.getTokenTransferredBlock(endpointType);
        blockRequests.get(endpointType).logTokenTransferred(blockNumber);
    }

    @Override
    public void onBalanceApprovedLog(TmeshToken.ApprovalEventResponse typedResponse, int endpoint) {
        preferencesHelperPaylib.setBalanceApprovedBlock(typedResponse.log.getBlockNumber().longValue(), endpoint);
        if (transactionObserver != null)
            transactionObserver.onBalanceApprovedLog(typedResponse);

        /*if (parseManager != null){
            String log = new Gson().toJson(typedResponse.log);
            parseManager.sendBalanceApprovedLog(typedResponse.log.getTransactionHash(), typedResponse._owner, typedResponse._spender, typedResponse._value.toString(), log);
        }*/
    }

    @Override
    public void onChannelCreatedLog(RaidenMicroTransferChannels.ChannelCreatedEventResponse typedResponse, int endpoint) {
        preferencesHelperPaylib.setChannelCreatedBlock(typedResponse.log.getBlockNumber().longValue(), endpoint);
        if (transactionObserver != null)
            transactionObserver.onChannelCreatedLog(typedResponse);

        if (parseManager != null && typedResponse._receiver_address.equalsIgnoreCase(getAddress())){
            String log = new Gson().toJson(typedResponse.log);
            parseManager.sendChannelCreatedLog(typedResponse.log.getTransactionHash(), typedResponse._sender_address, typedResponse._receiver_address, typedResponse._deposit.toString(), log);
        }
    }

    @Override
    public void onChannelToppedUpLog(RaidenMicroTransferChannels.ChannelToppedUpEventResponse typedResponse, int endpoint) {
        preferencesHelperPaylib.setChannelTopupBlock(typedResponse.log.getBlockNumber().longValue(), endpoint);
        if (transactionObserver != null)
            transactionObserver.onChannelToppedUpLog(typedResponse);

        if (parseManager != null && typedResponse._receiver_address.equalsIgnoreCase(getAddress())){
            String log = new Gson().toJson(typedResponse.log);
            parseManager.sendChannelToppedUpLog(typedResponse.log.getTransactionHash(), typedResponse._sender_address, typedResponse._receiver_address, typedResponse._open_block_number.toString(), typedResponse._added_deposit.toString(), log);
        }
    }

    @Override
    public void onChannelClosedLog(RaidenMicroTransferChannels.ChannelSettledEventResponse typedResponse, int endpoint) {
        preferencesHelperPaylib.setChannelClosedBlock(typedResponse.log.getBlockNumber().longValue(), endpoint);
        if (transactionObserver != null)
            transactionObserver.onChannelClosedLog(typedResponse);

        if (parseManager != null){
            String log = new Gson().toJson(typedResponse.log);
            parseManager.sendChannelClosedLog(typedResponse.log.getTransactionHash(), typedResponse._sender_address, typedResponse._receiver_address, typedResponse._open_block_number.toString(), typedResponse._balance.toString(), typedResponse._receiver_tokens.toString(), log);
        }
    }

    @Override
    public void onChannelWithdrawnLog(RaidenMicroTransferChannels.ChannelWithdrawEventResponse typedResponse, int endpoint) {
        preferencesHelperPaylib.setChannelWithdrawnBlock(typedResponse.log.getBlockNumber().longValue(), endpoint);
        if (transactionObserver != null)
            transactionObserver.onChannelWithdrawnLog(typedResponse);

        if (parseManager != null && typedResponse._receiver_address.equalsIgnoreCase(getAddress())){
            String log = new Gson().toJson(typedResponse.log);
            parseManager.sendChannelWithdrawnLog(typedResponse.log.getTransactionHash(), typedResponse._sender_address, typedResponse._receiver_address, typedResponse._open_block_number.toString(), typedResponse._withdrawn_balance.toString(), log);
        }
    }

    @Override
    public void onTokenMintedLog(TmeshToken.MintedEventResponse typedResponse, int endpoint) {
        preferencesHelperPaylib.setTokenMintedBlock(typedResponse.log.getBlockNumber().longValue(), endpoint);
        if (transactionObserver != null)
            transactionObserver.onTokenMintedLog(typedResponse);


        /*if (parseManager != null){
            String log = new Gson().toJson(typedResponse.log);
            parseManager.sendTokenMinteLog(typedResponse.log.getTransactionHash(), typedResponse._to, typedResponse._num.toString(), log);
        }*/
    }

    @Override
    public void onTokenTransferredLog(TmeshToken.TransferEventResponse typedResponse, int endpoint) {
        preferencesHelperPaylib.setTokenTransferredBlock(typedResponse.log.getBlockNumber().longValue(), endpoint);
        if (txList.contains(typedResponse.log.getTransactionHash().toLowerCase())){
            txList.remove(typedResponse.log.getTransactionHash().toLowerCase());
            if (transactionObserver != null)
                transactionObserver.onTokenCompleted(true);
        }else {
            if (transactionObserver != null)
                transactionObserver.onTokenTransferredLog(typedResponse);
        }
    }

    public void saveClaimGiftTransaction(TmeshToken.TransferEventResponse typedResponse){
        Log.v(TAG, "saveClaimGiftTransaction");
        String log = new Gson().toJson(typedResponse.log);
        parseManager.sendTokenTransferredLog(typedResponse.log.getTransactionHash(), typedResponse._from, typedResponse._to, typedResponse._value.toString(), log, ParseConstant.REQUEST_TYPES.RM_CLAIMED);
    }

    public void saveConvertRmTransaction(TmeshToken.TransferEventResponse typedResponse){
        Log.v(TAG, "saveConvertRmTransaction");
        String log = new Gson().toJson(typedResponse.log);
        parseManager.sendTokenTransferredLog(typedResponse.log.getTransactionHash(), typedResponse._from, typedResponse._to, typedResponse._value.toString(), log, ParseConstant.REQUEST_TYPES.RM_CONVERT);
    }

    public BigInteger getWeiValue(double value) {
        BigDecimal weiTokenValue = Convert.toWei(BigDecimal.valueOf(value), Convert.Unit.ETHER);
        BigInteger b = weiTokenValue.toBigInteger();
        Log.i(TAG, "weiTokenValue: " + b.toString());
        return b;
    }

    public double getETHorTOKEN(BigInteger value) {
        BigDecimal tokenValue = Convert.fromWei(new BigDecimal(value), Convert.Unit.ETHER);
        Log.i(TAG, "converted to base es: " + tokenValue.doubleValue());
        return tokenValue.doubleValue();
    }

    public void getStatusOfGift(String fromAddress, String ethTranxHash, String tknTranxHash, int endPointType){
        ethGift.add(fromAddress, ethTranxHash, tknTranxHash, endPointType);
    }

}
