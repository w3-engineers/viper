package com.w3engineers.mesh.application.ui.meshlog.ui.meshloghistory;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.ui.meshlog.data.model.MeshLogHistoryModel;
import com.w3engineers.mesh.application.ui.meshlog.ui.base.ItemClickListener;
import com.w3engineers.mesh.application.ui.meshlog.ui.meshlogdetails.MeshLogDetailsActivity;
import com.w3engineers.mesh.databinding.ActivityMeshLogHistoryBinding;
import com.w3engineers.mesh.util.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MeshLogHistoryActivity extends AppCompatActivity implements ItemClickListener<MeshLogHistoryModel>, View.OnClickListener {

    private ActivityMeshLogHistoryBinding mBinding;
    private MeshLogHistoryAdapter mAdapter;
    private String currentLogFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_mesh_log_history);

        initView();

        readLogList();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageView_back) {
            finish();
        }
    }

    @Override
    public void onItemClick(View view, MeshLogHistoryModel item) {
        Intent intent = new Intent(this, MeshLogDetailsActivity.class);
        intent.putExtra(MeshLogDetailsActivity.class.getName(), item.getName());
        if (currentLogFile.equals(item.getName())) {
            intent.putExtra(MeshLogHistoryActivity.class.getName(), true);
        }
        startActivity(intent);
    }

    private void initView() {
        mBinding.imageViewBack.setOnClickListener(this);

        mBinding.recyclerViewLog.setHasFixedSize(true);
        mBinding.recyclerViewLog.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MeshLogHistoryAdapter();
        mAdapter.setItemClickListener(this);
        mBinding.recyclerViewLog.setAdapter(mAdapter);
    }

    private void readLogList() {
        List<MeshLogHistoryModel> models = new ArrayList<>();
        /*File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() +
                "/MeshRnD");*/
        String sdCard = Constant.Directory.PARENT_DIRECTORY + Constant.Directory.MESH_LOG;
        File directory = new File(sdCard);

        File[] files = directory.listFiles();
        if (files != null) {
            Log.d("Files", "Size: " + files.length);
            for (int i = 0; i < files.length; i++) {
                models.add(0, new MeshLogHistoryModel(files[i].getName(), files[i].getAbsolutePath()));
                Log.d("Files", "FileName:" + files[i].getName());
                currentLogFile = files[i].getName();
            }
        }

        mAdapter.addItem(models);
    }

}
