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


}
