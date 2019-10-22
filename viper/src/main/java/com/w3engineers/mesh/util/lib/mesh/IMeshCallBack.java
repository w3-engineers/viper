package com.w3engineers.mesh.util.lib.mesh;


import com.w3engineers.mesh.application.data.remote.model.BaseMeshData;
import com.w3engineers.mesh.application.data.remote.model.MeshAcknowledgement;
import com.w3engineers.mesh.application.data.remote.model.MeshDataOld;
import com.w3engineers.mesh.application.data.remote.model.MeshPeer;

/*
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * ============================================================================
 */
public interface IMeshCallBack {

    void onMesh(MeshDataOld meshDataOld);
    void onMesh(MeshAcknowledgement meshAcknowledgement);

    void onProfileInfo(BaseMeshData baseMeshData);
    void onPeerRemoved(MeshPeer meshPeer);

    void onInitSuccess(MeshPeer selfMeshPeer);
    void onInitFailed(int reason);

}
