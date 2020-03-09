package com.w3engineers.meshrnd;
import com.w3engineers.mesh.ViperCommunicator;
import com.w3engineers.models.UserInfo;

interface ITmCommunicator {
   void startTeleMeshService(in ViperCommunicator viperCommunicator, in String appToken,in UserInfo userInfo,in String signalServer);
   void onStartForeground(in boolean isNeeded);
   void sendData(in String senderId, in String receiverId, in String messageId, in byte[] data, in boolean isNotificationNeeded,in String appToken);
   int  getLinkTypeById(in String nodeID);
   boolean startMesh(in String appToken);
   String getUserId();
   void saveDiscoveredUserInfo(String userId, String userName);
   void saveUserInfo(in UserInfo userInfo);
   void saveOtherUserInfo(in UserInfo userInfo);
   void stopMesh();

   void sendPayMessage(in String receiverId, in String message, in String messageId,in String appToken);
   void onPaymentGotForIncomingMessage(in boolean success, in String receiver, in String sender, in String messageId, in String msgData,in String appToken);
   void onPaymentGotForOutgoingMessage(in boolean success, in String receiver, in String sender, in String messageId, in String msgData,in String appToken);
   List<String> getInternetSellers(in String appToken);
   boolean isInternetSeller(in String address);
   boolean isUserConnected(in String address);
   void onBuyerConnected(in String address);
   void onBuyerDisconnected(in String address);
   void restartMesh(in int newRole, in String signalServerUrl);
   String getUserPublicKey(in String address);
   void disconnectFromInternet(in String appToken);
   String getCurrentSellerId(in String appToken);
   String getUserNameByAddress(in String address, in String appToken);
   void destroyService();

   void allowPermissions(in List<String> permissions);

//   void startService(in String appToken);

   void isLocalUseConnected(in String userId);

   void sendMyVersionToOthers(in String receiverId, in String appToken, in int version);

    void sendAppUpdateRequest(in String receiverId, in String appToken);

    void openWalletCreationUI(in String appToken);

    void openDataplanUI(in String appToken);
    void openWalletUI(in String appToken, in byte[] pictureData);
    void openSellerInterfaceUI(in String appToken);
}
