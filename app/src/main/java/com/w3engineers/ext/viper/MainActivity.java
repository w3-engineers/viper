/*
package com.w3engineers.ext.viper;

import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.w3engineers.mesh.application.data.BaseServiceLocator;
import com.w3engineers.mesh.application.ui.base.TelemeshBaseActivity;
import com.w3engineers.mesh.util.lib.mesh.DataManager;

public class MainActivity extends TelemeshBaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.main_ac;
    }


    @Override
    protected void startUI() {
        Button helloBtn = findViewById(R.id.helloBtn);

     //  DataManager.on().doBindService(this, "jkhlh");

        helloBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = 0;
                try {
                    id = DataManager.on().getLinkTypeById("peerId");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "Link Type:: " + id, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataManager.on().stopService();
    }


}
*/
