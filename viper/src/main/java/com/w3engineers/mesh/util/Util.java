package com.w3engineers.mesh.util;

import android.content.Context;
import android.text.TextUtils;

import com.w3engineers.eth.data.constant.PayLibConstant;
import com.w3engineers.eth.data.helper.PreferencesHelperPaylib;
import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.data.local.db.DatabaseService;
import com.w3engineers.mesh.application.data.local.db.message.Message;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class Util {

    public interface ConnectionCheck {
        void onConnectionCheck(boolean isConnected);
    }

    public static double convertBytesToMegabytes(long bytes) {
        double val = (double) bytes / (1024.0 * 1024.0);
//        MeshLog.p("val " + val);
        return val;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        String st = String.format(Locale.ENGLISH, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
        return st;
    }

    public static String humanReadableByteCount(long bytes) {
        return humanReadableByteCount(bytes, false);
    }


    public static String buildInternetSendingMessage(String sender, String originalReceiver, byte[] data) {

        JSONObject js = new JSONObject();
        try {
            js.put(PurchaseConstants.JSON_KEYS.MESSAGE_MODE, PurchaseConstants.MESSAGE_MODE.INTERNET_SEND);
            js.put(PurchaseConstants.JSON_KEYS.MESSAGE_SENDER, sender);
            js.put(PurchaseConstants.JSON_KEYS.MESSAGE_RECEIVER, originalReceiver);
            js.put(PurchaseConstants.JSON_KEYS.MESSAGE_DATA, new String(data));

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return js.toString();
    }

    public static String buildLocalMessage(byte[] data) {

        JSONObject js = new JSONObject();
        try {
            js.put(PurchaseConstants.JSON_KEYS.MESSAGE_MODE, PurchaseConstants.MESSAGE_MODE.LOCAL);
            js.put(PurchaseConstants.JSON_KEYS.MESSAGE_DATA, new String(data));

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return js.toString();
    }

    public static String buildInternetReceivingMessage(byte[] data, String sellerId) {

        JSONObject js = new JSONObject();
        try {
            js.put(PurchaseConstants.JSON_KEYS.MESSAGE_MODE, PurchaseConstants.MESSAGE_MODE.INTERNET_RECEIVE);
            js.put(PurchaseConstants.JSON_KEYS.SELLER_ADDRESS, sellerId);
            js.put(PurchaseConstants.JSON_KEYS.MESSAGE_DATA, new String(data));

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return js.toString();
    }

    public static String buildInternetSendingAckBody(String originalReceiver) {
        JSONObject js = new JSONObject();
        try {
            js.put(PurchaseConstants.JSON_KEYS.ACK_MODE, PurchaseConstants.MESSAGE_MODE.INTERNET_SEND_ACK);
            js.put(PurchaseConstants.JSON_KEYS.MESSAGE_RECEIVER, originalReceiver);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return js.toString();
    }

    public static String buildInternetReceivingAckBody(String sellerId) {
        JSONObject js = new JSONObject();
        try {
            js.put(PurchaseConstants.JSON_KEYS.ACK_MODE, PurchaseConstants.MESSAGE_MODE.INTERNET_RECEIVE_ACK);
            js.put(PurchaseConstants.JSON_KEYS.SELLER_ADDRESS, sellerId);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return js.toString();
    }

    public static String buildLocalAckBody() {
        JSONObject js = new JSONObject();
        try {
            js.put(PurchaseConstants.JSON_KEYS.ACK_MODE, PurchaseConstants.MESSAGE_MODE.LOCAL_ACK);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return js.toString();
    }


    public static String buildSocketMessage(byte[] frameData, String receiverId, String messageId) {
        String message = new String(frameData);
        JSONObject jsonObject = null;
        if (!TextUtils.isEmpty(message)) {
//TODO arif
            /*try {
                jsonObject = new JSONObject();
                jsonObject.put("action", "send");
                jsonObject.put("receiver", receiverId);
                jsonObject.put("text", message);
                jsonObject.put("txn", messageId);
                jsonObject.put("app", InternetLink.APP_NAME);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
        }

        return jsonObject.toString();
    }

    public static boolean saveMessage(String senderId, String receiverId, String messageId, byte[] data, Context mContext) {
        if (!senderId.equals(receiverId)) {

            Message message = null;
            try {
                message = DatabaseService.getInstance(mContext).getMessageById(messageId);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (message != null) {
                return true;
            } else {
                message = new Message();
                message.setReceiverId(receiverId);
                message.setSenderId(senderId);
                message.setMessageId(messageId);
                message.setData(data);

                DatabaseService.getInstance(mContext).insertMessage(message);
                return true;
            }
        }
        return false;
    }

    public static String getCurrencyTypeMessage(String message) {
        String currencyMode = getEndPointCurrency();
        return String.format(message, currencyMode);
    }

    public static String getEndPointCurrency() {
        int endPoint = PreferencesHelperPaylib.onInstance(MeshApp.getContext()).getEndpointMode();
        return endPoint == PayLibConstant.END_POINT_TYPE.ETH_ROPSTEN ? MeshApp.getContext().getString(R.string.eth) : MeshApp.getContext().getString(R.string.etc);
    }

    public static void isConnected(ConnectionCheck connectionCheck) {
        new Thread(() -> {
            try {
                final String command = "ping -c 1 google.com";
                boolean isSuccess = Runtime.getRuntime().exec(command).waitFor() == 0;
                connectionCheck.onConnectionCheck(isSuccess);
            } catch (InterruptedException | IOException e) {
                connectionCheck.onConnectionCheck(false);
            }
        }).start();

    }
}
