package com.w3engineers.mesh.ui.Nearby;

import com.w3engineers.mesh.model.UserModel;

public interface NearbyCallBack {
    void onUserFound(UserModel model);

    void onDisconnectUser(String userId);

}
