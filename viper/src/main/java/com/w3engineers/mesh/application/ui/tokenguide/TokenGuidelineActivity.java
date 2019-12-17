package com.w3engineers.mesh.application.ui.tokenguide;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.data.BaseServiceLocator;
import com.w3engineers.mesh.application.ui.base.TelemeshBaseActivity;
import com.w3engineers.mesh.application.ui.util.FileStoreUtil;
import com.w3engineers.mesh.databinding.ActivityTokenGuidelineBinding;

public class TokenGuidelineActivity extends TelemeshBaseActivity {

    private ActivityTokenGuidelineBinding mBinding;
    private TokenGuidelineAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_token_guideline;
    }

    @Override
    protected void startUI() {
        super.startUI();
        mBinding = (ActivityTokenGuidelineBinding) getViewDataBinding();

        initView();
    }

    @Override
    protected BaseServiceLocator getServiceLocator() {
        return null;
    }

    private void initView() {
        mBinding.recyclerViewLink.setHasFixedSize(true);
        mBinding.recyclerViewLink.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TokenGuidelineAdapter();
        mBinding.recyclerViewLink.setAdapter(mAdapter);

        loadWebView();
    }

    private void loadWebView() {
        mBinding.webView.loadUrl(FileStoreUtil.getWebFile());
    }
}
