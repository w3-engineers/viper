package com.w3engineers.meshrnd;
import com.w3engineers.mesh.ViperCommunicator;
import com.w3engineers.models.UserInfo;

interface ITmCommunicator {
   void setViperCommunicator(in ViperCommunicator viperCommunicator, in String packageName);
   void onStartForeground(in boolean isNeeded);
   void sendData(in String senderId, in String receiverId, in String messageId, in byte[] data, in boolean isNotificationNeeded);
   int  getLinkTypeById(in String nodeID);
   boolean startMesh(in int existingRole, in UserInfo userInfo, in String signalServerUrl);
   String getUserId();
   void saveDiscoveredUserInfo(String userId, String userName);
   void saveUserInfo(in UserInfo userInfo);
   void saveOtherUserInfo(in UserInfo userInfo);
   void stopMesh();


   void sendPayMessage(in String receiverId, in String message, in String messageId);
   void onPaymentGotForIncomingMessage(in boolean success, in String receiver, in String sender, in String messageId, in String msgData);
   void onPaymentGotForOutgoingMessage(in boolean success, in String receiver, in String sender, in String messageId, in String msgData);
   List<String> getInternetSellers();
   boolean isInternetSeller(in String address);
   boolean isUserConnected(in String address);
   void onBuyerConnected(in String address);
   void onBuyerDisconnected(in String address);
   void restartMesh(in int newRole, in String signalServerUrl);
   String getUserPublicKey(in String address);
   void disconnectFromInternet();
   String getCurrentSellerId();
   String getUserNameByAddress(in String address, in String appToken);
   void destroyService();

   void allowPermissions(in List<String> permissions);

   void startService(in String appToken);

   void isLocalUseConnected(in String userId);
}
