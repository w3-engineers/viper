/*
package com.w3engineers.eth.data.remote;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyEthLogs {

    public static String EVENT_CHANNEL_CREATED = "0x986876e67d288f7b8bc5229976a1d5710be919feb66d2e1aec1bf3eadebba207";
    public static String EVENT_CHANNEL_TOPUP = "0x283bcbed58779cdfe40c216a69673863430a43dbf7fe557730c0498890e55126";
    public static String EVENT_CHANNEL_CLOSED = "0x5f32714de7650ec742b858687d8db145623b99b0748db73df3ffc4d718867a8d";
    public static String EVENT_TOKEN_APPROVE = "0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925";
    private Executor executor;
    private static MyEthLogs myEthLogs;
    private String TAG = "MyEthLogs";

    private MyEthLogs(){
        executor = Executors.newSingleThreadExecutor();
    }

    public static MyEthLogs getInstance(){
        if (myEthLogs == null){
            myEthLogs = new MyEthLogs();
        }
        return myEthLogs;
    }


    public String getTopicFromAddress(String sender){
        int index = 1;
        String stringToBeInserted = "000000000000000000000000";
        String newString = sender.substring(0, index + 1) + stringToBeInserted + sender.substring(index + 1);
        return newString;
    }

    public String getLongToHex (long value) {
        return  "0x" + Integer.toHexString((int) value);
    }

    public JSONArray getLog(long fromBlock, String event, String contractAddress, String sender) {

//        OkHttpClient client = new OkHttpClient();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();


        JSONArray result = new JSONArray();
        try {
            JSONArray topics = new JSONArray();
            topics.put(event);//EVENT
            topics.put(getTopicFromAddress(sender));//SENDER

            JSONObject params = new JSONObject();
            params.put("fromBlock", getLongToHex(fromBlock));
            params.put("toBlock","latest");
            params.put("address",contractAddress);//SMART CONTRACT
            params.put("topics", topics);

            JSONArray paramsArray = new JSONArray();
            paramsArray.put(params);

            JSONObject json = new JSONObject();
            json.put("jsonrpc","2.0");
            json.put("method","eth_getLogs");
            json.put("params",paramsArray);
            json.put("id",1);

            MediaType JSON = MediaType.parse("application/json");
            String bodystr = json.toString();

            Log.i(TAG, "body: " + bodystr);

            RequestBody body = RequestBody.create(JSON, bodystr);
            Request request = new Request.Builder()
                    .url(EthereumService.web3jRpcURL)
                    .post(body)
                    .addHeader("Content-Type","application/json")
                    .build();

            Response response = client.newCall(request).execute();
            if (response != null) {
                String jsonData = response.body().string();
                Log.i(TAG, "run result : " + jsonData);
                JSONObject Jobject = new JSONObject(jsonData);
                result = Jobject.getJSONArray("result");

            }


        } catch (IOException | JSONException e) {
            Log.i(TAG, "run error : " + e.getMessage());
        }
        return result;
    }

    public interface EthLogChannelCreated{
        void onGetChannels(JSONArray logs);
    }
    public void getChannelCreated(long fromBlock, String myAddress, EthLogChannelCreated listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                JSONArray logs = getLog(fromBlock, EVENT_CHANNEL_CREATED, EthereumService.channelAddress, myAddress);
                listener.onGetChannels(logs);
            }
        });
    }


    public interface EthLogChannelToppedUp {
        void onGetChannels(JSONArray logs);
    }
    public void getChannelToppedUp(long fromBlock, String myAddress, EthLogChannelToppedUp listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                JSONArray logs = getLog(fromBlock, EVENT_CHANNEL_TOPUP, EthereumService.channelAddress, myAddress);
                listener.onGetChannels(logs);
            }
        });
    }


    public interface EthLogChannelClosed {
        void onGetChannels(JSONArray logs);
    }
    public void getChannelClosed(long fromBlock, String myAddress, EthLogChannelClosed listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                JSONArray logs = getLog(fromBlock, EVENT_CHANNEL_CLOSED, EthereumService.channelAddress, myAddress);
                listener.onGetChannels(logs);
            }
        });
    }
}
*/
