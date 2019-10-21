package com.w3engineers.ext.viper.application.data.local.purchase;

public interface PayEventListener {
     void onMessageReceived(String sender, byte[] paymentData);
     void onPayMessageAckReceived(String sender, String receiver, String messageId);
     void onUserConnected(String nodeId);
     void onUserDisconnected(String nodeId);
}
