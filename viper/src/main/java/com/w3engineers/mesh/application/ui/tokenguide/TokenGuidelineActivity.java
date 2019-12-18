package com.w3engineers.mesh.application.ui.tokenguide;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mBinding.recyclerViewLink.setHasFixedSize(true);
        mBinding.recyclerViewLink.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TokenGuidelineAdapter();
        mBinding.recyclerViewLink.setAdapter(mAdapter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.token_guideline));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadWebView();
    }

    private void loadWebView() {
        mBinding.webView.loadUrl(FileStoreUtil.getWebFile());
    }
}
