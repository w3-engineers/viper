// IRmServiceConnection.aidlnull
package com.w3engineers.ext.viper;

import com.w3engineers.ext.viper.application.data.remote.model.BaseMeshData;
import com.w3engineers.ext.viper.application.data.remote.model.MeshData;
import com.w3engineers.ext.viper.application.data.remote.model.MeshPeer;
import com.w3engineers.ext.viper.application.data.remote.model.MeshAcknowledgement;
import com.w3engineers.ext.viper.IPeer;

interface ITmCommunicator {

   void sendData(in String senderId, in String receiverId, in String messageId, in byte[] data);
   int  getLinkTypeById(in String nodeID);
   void startMesh(in String ssid);

}
