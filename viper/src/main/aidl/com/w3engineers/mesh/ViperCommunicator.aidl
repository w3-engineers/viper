// ViperCommunicator.aidl
package com.w3engineers.mesh;


/**
 This interface will be implementaed in client service,
 remote service will call this interface to send data to client
*/

interface ViperCommunicator {

    void onPeerAdd(in String peerId);
    void onPeerRemoved(in String nodeId);
    void onRemotePeerAdd (in String peerId);
    void onDataReceived(in String senderId, in byte[] frameData);
    void onAckReceived(in String messageId, in int status);
    void onServiceAvailable(in int status);

    void setServiceForeground(boolean isForeGround);
    void onMessagePayReceived(in String sender, in byte[] paymentData);
    void onPayMessageAckReceived(in String sender, in String receiver, in String messageId);
    void buyerInternetMessageReceived(in String sender, in String receiver, in String messageId, in String messageData, in long dataLength, in boolean isIncoming);
    void onTransportInit(in String nodeId, in String publicKey, in boolean success, in String msg);

}
