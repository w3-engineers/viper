package com.w3engineers.eth.util.lib.okhttp3;

/**
 * Created by Dell on 10/30/2017.
 */

public interface OkHttpResponse {
    void onSuccess(String postUrl, String responseData);
    void onFail(String postUrl);
}
