// ViperCommunicator.aidl
package com.w3engineers.ext.viper;

interface ViperCommunicator {

    void userConnection(String userId, boolean isConnected);

    void dataReceive(in String userId, in byte[] data);
}
