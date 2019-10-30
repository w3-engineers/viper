package com.w3engineers.mesh.application.ui.premission;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.w3engineers.ext.strom.util.helper.PermissionUtil;
import com.w3engineers.mesh.util.lib.mesh.ViperClient;

import static com.w3engineers.mesh.util.lib.mesh.ViperClient.appName;
import static com.w3engineers.mesh.util.lib.mesh.ViperClient.networkPrefix;

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
        ViperClient.on(this, appName, networkPrefix).startClient();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        checkPermission();
    }
}
