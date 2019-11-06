package com.w3engineers.eth.util.lib.okhttp3;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Dell on 10/16/2017.
 */

public class OkHttpUtil {

    private static OkHttpUtil okHttpUtil;

    private OkHttpClient okHttpClient;

    private OkHttpUtil() {
        //okhttp config
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.MINUTES)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public static OkHttpUtil getOkHttpUtil() {
        if (okHttpUtil == null) {
            okHttpUtil = new OkHttpUtil();
        }
        return okHttpUtil;
    }

    /**
     * OkHttp post request
     *
     * @param postUrl
     * @param requestBody
     * @param okHttpResponse
     */
    public void postRequest(final String postUrl, RequestBody requestBody, final OkHttpResponse okHttpResponse) {

        Request request = new Request.Builder()
                .url(postUrl)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                okHttpResponse.onFail(postUrl);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    String responseBody = response.body().string();
                    okHttpResponse.onSuccess(postUrl, responseBody);
                }
            }
        });
    }


    /**
     * OkHttp get request
     *
     * @param postUrl
     * @param okHttpResponse
     */
    public void getRequest(String requestTag, final String postUrl, final OkHttpResponse okHttpResponse) {

        Request request;

        if (requestTag != null) {
            for (Call call : okHttpClient.dispatcher().runningCalls()) {
                if (call.request().tag().equals(requestTag))
                    call.cancel();
                //return;
            }
            request = new Request.Builder()
                    .url(postUrl)
                    .tag(requestTag)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(postUrl)
                    .build();
        }


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                okHttpResponse.onFail(postUrl);
            }


            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (!response.isSuccessful()) {
                } else {
                    String responseBody = response.body().string();
                    okHttpResponse.onSuccess(postUrl, responseBody);
                }
            }
        });
    }


}
