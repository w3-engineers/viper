package com.w3engineers.ext.viper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.w3engineers.mesh.application.data.local.dataplan.DataPlanManager;
import com.w3engineers.mesh.util.lib.mesh.DataManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_ac);


//        DataPlanManager.openActivity(this);
//        Wallet.openActivity(this);

        Button helloBtn = findViewById(R.id.helloBtn);

        Button dataPlan = findViewById(R.id.data_plan);

        DataManager.getInstance().doBindService(this, "jkhlh");

        helloBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              int id =  DataManager.getInstance().getLinkTypeById("peerId");
                Toast.makeText(MainActivity.this, "Link Type:: " + id, Toast.LENGTH_SHORT).show();
            }
        });

        dataPlan.setOnClickListener(view -> {
            DataPlanManager.openActivity(MainActivity.this);
        });
    }
}
