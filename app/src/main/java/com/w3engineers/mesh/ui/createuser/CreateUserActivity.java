package com.w3engineers.mesh.ui.createuser;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.w3engineers.eth.data.remote.EthereumService;
import com.w3engineers.ext.viper.R;
import com.w3engineers.ext.viper.databinding.ActivityCreateUserBinding;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.local.wallet.WalletManager;
import com.w3engineers.mesh.application.data.local.wallet.WalletService;
import com.w3engineers.mesh.ui.nav.BottomNavActivity;
import com.w3engineers.mesh.util.Constant;
import com.w3engineers.mesh.util.MeshLog;
import com.w3engineers.mesh.util.PermissionUtil;


import java.util.UUID;

public class CreateUserActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityCreateUserBinding mBInding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBInding = DataBindingUtil.setContentView(this, R.layout.activity_create_user);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle("MeshRnd");
        mBInding.createUser.setOnClickListener(this);

        CheckLogin();
    }

    private void CheckLogin() {

        if (!TextUtils.isEmpty(SharedPref.read(Constant.KEY_USER_NAME))) {

            Intent intent = new Intent(this, BottomNavActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.create_user) {

     //       requestPermission();

            if (!mBInding.editTextName.getText().toString().equals("")) {
                String uuid = UUID.randomUUID().toString();
                goNext(uuid);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        requestPermission();
    }

    private void requestPermission(){
        if (PermissionUtil.init(this).request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            showFileChooser();
        }
    }

    private void goNext(String userId) {
        SharedPref.write(Constant.KEY_USER_NAME, mBInding.editTextName.getText().toString());
       // SharedPref.write(Constant.KEY_USER_ID, userId);
        Intent intent = new Intent(CreateUserActivity.this, BottomNavActivity.class);
        startActivity(intent);
        finish();
    }

    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }


/*    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
          if (requestCode == FILE_SELECT_CODE){
            Uri path = data.getData();
            //  String savePtah = Web3jWalletHelper.onInstance(activity).getKeyStoreFilePath("wallet/", "wallet/");

            WalletManager.getInstance().importWallet(this, "123456789", path, new WalletManager.WalletImportListener() {
                @Override
                public void onWalletImported(String walletAddress, String publicKey) {
                    Log.e("wallet_adress", "wallet adress imported" );

                    if (!mBInding.editTextName.getText().toString().equals("")) {
                        String uuid = UUID.randomUUID().toString();
                        goNext(uuid);


                    } else {
                      //  Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String message) {

                }
            });

            //  Log.e("filePath", "file:: " +  path +"\nfile path save:: " + savePtah);
        }

    }*/
}
