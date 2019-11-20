package com.w3engineers.mesh.application.data.local.meshlog;

import android.content.Context;
import android.content.Intent;

import com.w3engineers.mesh.application.ui.meshlog.MeshLogHistoryActivity;


public class MeshLogManager {
    private static MeshLogManager meshLogManager;
    public static MeshLogManager getInstance(){
        if (meshLogManager == null){
            meshLogManager = new MeshLogManager();
        }
        return meshLogManager;
    }

    public static void openActivity(Context context){
        Intent intent = new Intent(context, MeshLogHistoryActivity.class);
        context.startActivity(intent);
    }

}
