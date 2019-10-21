package com.w3engineers.mesh.util.lib.mesh;


/**
 * <p>App layer public interface</p>
 * <p>
 * 1. Trigger mesh library start process from App
 * 2. Handle All discovery and messaging related call from library
 * 3. Pass node disconnect event to App
 */

public interface LinkStateListener {

    /**
     * <p>Local user connected call back</p>
     * After found a local user this method called from library
     *
     * @param nodeId : String ID of connected node
     */
    void onLocalUserConnected(String nodeId);

    /**
     * <p>Other node id from different mesh network</p>
     *
     * @param nodeId : String ID of connected users
     */
    void onRemoteUserConnected(String nodeId);

    /**
     * <p>Called when connection to device is closed explicitly from either side</p>
     * or because device is out of range.
     *
     * @param nodeId : String disconnected node ID
     */
    void onUserDisconnected(String nodeId);

    /**
     * <p>Called when new data frame is received from remote device.</p>
     *
     * @param senderId  : String message sender ID
     * @param frameData : byte array original message
     */
    void onMessageReceived(String senderId, byte[] frameData);

    /**
     * @param senderId
     * @param frameData
     *//*
    void onLocalMessageReceived(String senderId, byte[] frameData);

    *//**
     *
     * @param senderId
     * @param frameData
     *//*
    void onInternetMessageReceived(String senderId, byte[] frameData);*/


    /**
     * <p>Message delivered ack</p>
     *
     * @param messageId : String message sent id
     * @param status    : Integer{
     *                  0 for sending
     *                  1 for sent
     *                  2 for delivered
     *                  3 for received
     *                  4 for failed }
     */
    void onMessageDelivered(String messageId, int status);
}
