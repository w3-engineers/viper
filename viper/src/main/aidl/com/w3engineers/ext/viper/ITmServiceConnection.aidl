// ITmServiceConnection.aidl
package com.w3engineers.ext.viper;

import com.w3engineers.ext.viper.ITmCommunicator;
import com.w3engineers.ext.viper.application.data.remote.model.MeshData;
import com.w3engineers.ext.viper.application.data.remote.model.BaseMeshData;

interface ITmServiceConnection {

    void setBroadCastActionString(in String actionString);

    void setServiceToCloseWithTask(in boolean isToCloseWithTask);

    void setProfile(in byte[] profileInfo, in String userId);

    void setProfileInfo(in byte[] profileInfo);

    String sendMeshData(in MeshData meshData);

    void setTmCommunicator(ITmCommunicator iTmCommunicator);

    void setServiceForeground(in boolean isForeGround);

    void resetCommunicator(ITmCommunicator iTmCommunicator);

    List<BaseMeshData> getLivePeers();

    void openTmSettings();

    void stopTmService();

    void stopMeshProcess();

    String getMyId();

    void restartMeshService();

    List<String> getCurrentSellers();

    int getUserLinkType(in String userId);
}
