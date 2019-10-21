package com.w3engineers.ext.viper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.w3engineers.mesh.application.data.local.dataplan.DataPlan;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_ac);


        DataPlan.openActivity(this);
    }
}
