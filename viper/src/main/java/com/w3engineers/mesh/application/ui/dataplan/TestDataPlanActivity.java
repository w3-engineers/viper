package com.w3engineers.mesh.application.ui.dataplan;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.data.BaseServiceLocator;
import com.w3engineers.mesh.application.ui.base.TelemeshBaseActivity;
import com.w3engineers.mesh.application.ui.util.ExpandableButton;

public class TestDataPlanActivity extends TelemeshBaseActivity {

    ExpandableButton localButton;
    ExpandableButton sellerButton;
    ExpandableButton buyerButton;
    ExpandableButton internetOnlyButton;
    private Toolbar mTopToolbar;


    @Override
    protected BaseServiceLocator getBaseServiceLocator() {
        return null;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.test_activity_data_plan;
    }

    @Override
    protected void startUI() {

        setTitle();

        localButton = findViewById(R.id.localButton);
        localButton.setTopViewGone();
        sellerButton = findViewById(R.id.sellDataButton);
        buyerButton = findViewById(R.id.buyDataButton);
        internetOnlyButton = findViewById(R.id.internetOnlyButton);
        internetOnlyButton.setBottomViewGone();


        setListenerForAllExpandable();
    }

    private void setListenerForAllExpandable() {
        localButton.setCallbackListener(new ExpandableButton.ExpandableButtonListener() {
            @Override
            public void onViewExpanded() {
                Toast.makeText(TestDataPlanActivity.this, "local Button Expanded", Toast.LENGTH_SHORT).show();
                collapseView(localButton);
            }

            @Override
            public void onViewCollapsed() {
                Toast.makeText(TestDataPlanActivity.this, "local Button Collapsed", Toast.LENGTH_SHORT).show();
            }
        });

        sellerButton.setCallbackListener(new ExpandableButton.ExpandableButtonListener() {
            @Override
            public void onViewExpanded() {
                Toast.makeText(TestDataPlanActivity.this, "seller Button Expanded", Toast.LENGTH_SHORT).show();
                collapseView(sellerButton);
            }

            @Override
            public void onViewCollapsed() {
                Toast.makeText(TestDataPlanActivity.this, "seller Button Collapsed", Toast.LENGTH_SHORT).show();
            }
        });

        buyerButton.setCallbackListener(new ExpandableButton.ExpandableButtonListener() {
            @Override
            public void onViewExpanded() {
                Toast.makeText(TestDataPlanActivity.this, "buyer Button  Button Expanded", Toast.LENGTH_SHORT).show();
                collapseView(buyerButton);
            }

            @Override
            public void onViewCollapsed() {
                Toast.makeText(TestDataPlanActivity.this, "buyer Button Button Collapsed", Toast.LENGTH_SHORT).show();
            }
        });

        internetOnlyButton.setCallbackListener(new ExpandableButton.ExpandableButtonListener() {
            @Override
            public void onViewExpanded() {
                Toast.makeText(TestDataPlanActivity.this, "internet Only Button Expanded", Toast.LENGTH_SHORT).show();
                collapseView(internetOnlyButton);
            }

            @Override
            public void onViewCollapsed() {
                Toast.makeText(TestDataPlanActivity.this, "internetOnly Button Collapsed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setTitle() {
        mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mTopToolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
    }

    private void collapseView(ExpandableButton button ){
        if (button.getId() == R.id.localButton) {
            sellerButton.collapseView();
            buyerButton.collapseView();
            internetOnlyButton.collapseView();
        }else if (button.getId() == R.id.sellDataButton){
            localButton.collapseView();
            buyerButton.collapseView();
            internetOnlyButton.collapseView();
        }else if (button.getId() == R.id.buyDataButton){
            localButton.collapseView();
            sellerButton.collapseView();
            internetOnlyButton.collapseView();
        }else if (button.getId() == R.id.internetOnlyButton){
            localButton.collapseView();
            sellerButton.collapseView();
            buyerButton.collapseView();
        }
    }


    public void childClicked(View view) {
        ((TextView) view).setText("Task Completed (Expandable Button color changed)");
        buyerButton.setBarColor(Color.parseColor("#297e55"));
    }
}
