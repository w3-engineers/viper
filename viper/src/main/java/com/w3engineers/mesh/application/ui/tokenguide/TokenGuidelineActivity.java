package com.w3engineers.mesh.application.ui.tokenguide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.data.BaseServiceLocator;
import com.w3engineers.mesh.application.ui.base.TelemeshBaseActivity;
import com.w3engineers.mesh.application.ui.util.FileStoreUtil;
import com.w3engineers.mesh.databinding.ActivityTokenGuidelineBinding;
import com.w3engineers.models.TokenGuideLine;

public class TokenGuidelineActivity extends TelemeshBaseActivity {

    private ActivityTokenGuidelineBinding mBinding;
    private TokenGuidelineAdapter mAdapter;
    private TokenGuideLine tokenGuideLine;

    private boolean isTokenZero;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_token_guideline;
    }

    @Override
    protected int statusBarColor() {
        return R.color.colorPrimaryDark;
    }

    @Override
    protected void startUI() {
        super.startUI();
        mBinding = (ActivityTokenGuidelineBinding) getViewDataBinding();
        tokenGuideLine = FileStoreUtil.getGuideline(this);

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

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.op_back) {
            finish();
        }
    }

    private void initView() {

        setClickListener(mBinding.opBack);

        parseIntent();

        mBinding.recyclerViewLink.setHasFixedSize(true);
        mBinding.recyclerViewLink.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TokenGuidelineAdapter();
        mBinding.recyclerViewLink.setAdapter(mAdapter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.token_guideline));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initWebViewController();

        loadWebView();

        if (tokenGuideLine != null) {
            mBinding.textViewTitle.setText(tokenGuideLine.getTitle());
            if (isTokenZero) {
                mAdapter.addItem(tokenGuideLine.getLinkList());
            }
        }
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(TokenGuidelineActivity.class.getName())) {
            isTokenZero = intent.getBooleanExtra(TokenGuidelineActivity.class.getName(), false);
        }
    }

    private void loadWebView() {
        //mBinding.webView.loadUrl(FileStoreUtil.getWebFile());
        if (tokenGuideLine != null) {

            mBinding.webView.loadData(tokenGuideLine.getContent(), "text/html", "UTF-8");
        } else {
            String data = "<p style=\"text-align: center;\"><span style=\"color: #993300;\"><strong>No Internet. Please try again.</strong></span></p>";
            mBinding.webView.loadData(data, "text/html", "UTF-8");
        }
    }

    private void initWebViewController() {
        mBinding.webView.getSettings().setJavaScriptEnabled(true);

        mBinding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (isTokenZero) {
                    view.loadUrl("javascript:document.getElementById('etherium').style.display = 'none'; void(0);");
                } else {
                    view.loadUrl("javascript:document.getElementById('token').style.display = 'none'; void(0);");
                }

            }
        });
    }
}
