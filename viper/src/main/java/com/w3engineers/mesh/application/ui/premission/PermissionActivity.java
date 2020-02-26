package com.w3engineers.mesh.application.ui.premission;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.w3engineers.ext.strom.util.helper.PermissionUtil;
import com.w3engineers.mesh.util.lib.mesh.ViperClient;
import static com.w3engineers.mesh.util.lib.mesh.ViperClient.avatar;
import static com.w3engineers.mesh.util.lib.mesh.ViperClient.configData;
import static com.w3engineers.mesh.util.lib.mesh.ViperClient.isSync;
import static com.w3engineers.mesh.util.lib.mesh.ViperClient.packageName;
import static com.w3engineers.mesh.util.lib.mesh.ViperClient.publicKey;
import static com.w3engineers.mesh.util.lib.mesh.ViperClient.regTime;
import static com.w3engineers.mesh.util.lib.mesh.ViperClient.usersName;
import static com.w3engineers.mesh.util.lib.mesh.ViperClient.wallerAddress;

public class PermissionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
    }

    private void checkPermission() {
        if (PermissionUtil.on(this).request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            closeCurrentActivity();
        }
    }

    private void closeCurrentActivity() {
        ViperClient.on(this, packageName, usersName, wallerAddress, publicKey, avatar, regTime, isSync, configData).startClient(wallerAddress, publicKey);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        checkPermission();
    }
}
