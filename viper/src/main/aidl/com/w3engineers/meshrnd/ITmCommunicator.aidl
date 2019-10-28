package com.w3engineers.meshrnd;
import com.w3engineers.mesh.ViperCommunicator;

interface ITmCommunicator {
   void setViperCommunicator(in ViperCommunicator viperCommunicator);
   void sendData(in String senderId, in String receiverId, in String messageId, in byte[] data);
   int  getLinkTypeById(in String nodeID);
   void startMesh(in String ssid);
   String getUserId();
}
