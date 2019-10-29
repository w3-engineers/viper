package com.w3engineers.mesh.application.ui.premission;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.w3engineers.ext.strom.util.helper.PermissionUtil;
import com.w3engineers.mesh.R;
import com.w3engineers.mesh.util.lib.mesh.ViperClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PermissionActivity extends AppCompatActivity {
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    public static final int REQUEST_ID_WRITE_SETTINGS = 106;
    public static final int REQUEST_ID_LOCATION_PERMISSION = 201;
    public static final int REQUEST_OVERLAY_PERMISSSION = 202;
    private static final int REQUEST_ENABLE_DSC = 107;
    private AlertDialog mAlertDialog;
    private BluetoothAdapter mBluetoothAdapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkPermission();
    }


    private void checkPermission() {
        if (PermissionUtil.on(this).request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            closeCurrentActivity();
        }
    }

    private void closeCurrentActivity() {
        ViperClient.on(this, "").startClient();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        checkPermission();
    }

}
