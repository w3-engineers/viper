package com.w3engineers.meshrnd;
import com.w3engineers.mesh.ViperCommunicator;
import com.w3engineers.models.UserInfo;

interface ITmCommunicator {
   void setViperCommunicator(in ViperCommunicator viperCommunicator);
   void onStartForeground(in boolean isNeeded);
   void sendData(in String senderId, in String receiverId, in String messageId, in byte[] data);
   int  getLinkTypeById(in String nodeID);
   void startMesh(in String ssid);
   String getUserId();
   void saveDiscoveredUserInfo(String userId, String userName);
   void saveUserInfo(in UserInfo userInfo);

   void sendPayMessage(in String receiverId, in String message, in String messageId);
   void onPaymentGotForIncomingMessage(in boolean success, in String receiver, in String sender, in String messageId, in String msgData);
   void onPaymentGotForOutgoingMessage(in boolean success, in String receiver, in String sender, in String messageId, in String msgData);
   List<String> getInternetSellers();
   boolean isInternetSeller(in String address);
   boolean isUserConnected(in String address);
   void onBuyerConnected(in String address);
   void onBuyerDisconnected(in String address);
   void restartMesh(in int newRole);
   String getUserPublicKey(in String address);
}
