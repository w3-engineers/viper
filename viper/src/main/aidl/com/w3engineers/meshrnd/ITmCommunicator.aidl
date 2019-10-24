package com.w3engineers.meshrnd;

interface ITmCommunicator {
   void sendData(in String senderId, in String receiverId, in String messageId, in byte[] data);
   int  getLinkTypeById(in String nodeID);
   void startMesh(in String ssid);
   String getUserId();
}
