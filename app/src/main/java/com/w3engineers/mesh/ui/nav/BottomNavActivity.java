package com.w3engineers.mesh.ui.nav;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.w3engineers.ext.strom.util.helper.Toaster;
import com.w3engineers.ext.viper.R;
import com.w3engineers.ext.viper.databinding.ActivityCreateGroupBinding;
import com.w3engineers.mesh.application.data.ApiEvent;
import com.w3engineers.mesh.application.data.AppDataObserver;
import com.w3engineers.mesh.application.data.local.dataplan.DataPlanManager;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.model.TransportInit;
import com.w3engineers.mesh.application.data.model.WalletLoaded;
import com.w3engineers.mesh.model.UserModel;
import com.w3engineers.mesh.ui.Nearby.NearbyFragment;
import com.w3engineers.mesh.ui.Nearby.UserConnectionCallBack;
import com.w3engineers.mesh.ui.base.BaseFragment;
import com.w3engineers.mesh.ui.history.HistoryFragment;
import com.w3engineers.mesh.ui.meshlog.MeshLogFragment;
import com.w3engineers.mesh.util.Constant;
import com.w3engineers.mesh.util.MeshLog;

import java.util.HashMap;

public class BottomNavActivity extends AppCompatActivity implements UserConnectionCallBack,  BottomMessageListener {

    private static final String TAG = BottomNavActivity.class.getSimpleName();

    private BaseFragment mCurrentFragment;
    private BaseFragment baseFragment;
    private ActivityCreateGroupBinding mBinding;
    private MenuItem myDataPlanMenuItem;
    TextView connectedUser;
    BottomNavigationView navigation;

    private boolean isAllMessageProcessClicked;
    private int msgSendCount = 0;
    private int userCount;
    private HashMap<String, UserModel> messageMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_group);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        commitFragment(R.id.fragment_container, new NearbyFragment());
        MeshLog.v("-------------------------------");
        MeshLog.i(" Info");
        MeshLog.w(" Warning");
        MeshLog.e(" Error");
        MeshLog.v(" Special");
        MeshLog.v("-------------------------------");
        navigation.setSelectedItemId(R.id.navigation_nearby);

        getSupportActionBar().setTitle("Me" + " : " + SharedPref.read(Constant.KEY_USER_NAME));
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.drawable_reg_page_shape));



        AppDataObserver.on().startObserver(ApiEvent.WALLET_LOADED, event -> {

            WalletLoaded walletLoaded = (WalletLoaded) event;
            if (walletLoaded.success && myDataPlanMenuItem != null) {
                runOnUiThread(() -> {
                    myDataPlanMenuItem.setEnabled(true);
                });
            }else {
                runOnUiThread(() -> {
                    Toaster.showLong("Error occurred.");
                });
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onConnectDisconnect(String userId) {
        Log.e("user_found", "user found in bottom");
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        baseFragment = null;
        switch (item.getItemId()) {
/*            case R.id.navigation_network:
                baseFragment = (NetworkFragment) getSupportFragmentManager()
                        .findFragmentByTag(NetworkFragment.class.getName());
                if (baseFragment == null) {
                    baseFragment = new NetworkFragment();
                }
                break;*/
            case R.id.navigation_nearby:
                baseFragment = (NearbyFragment) getSupportFragmentManager()
                        .findFragmentByTag(NearbyFragment.class.getName());
                if (baseFragment == null) {
                    baseFragment = new NearbyFragment();
                }
                break;

            case R.id.navigation_history:
                baseFragment = (HistoryFragment) getSupportFragmentManager()
                        .findFragmentByTag(HistoryFragment.class.getName());
                if (baseFragment == null) {
                    baseFragment = new HistoryFragment();
                }
                break;

            case R.id.navigation_log:
                baseFragment = (MeshLogFragment) getSupportFragmentManager()
                        .findFragmentByTag(MeshLogFragment.class.getName());
                if (baseFragment == null) {
                    baseFragment = new MeshLogFragment();
                }
                break;



/*            case R.id.navigation_diagram:
                baseFragment = (DiagramFragment) getSupportFragmentManager()
                        .findFragmentByTag(DiagramFragment.class.getName());
                if (baseFragment == null) {
                    baseFragment = new DiagramFragment();
                }
                break;*/

        }
        commitFragment(R.id.fragment_container, baseFragment);
        return true;
    };

    /**
     * Commit child fragment of BaseFragment on a frameLayout
     *
     * @param viewId       int value
     * @param baseFragment BaseFragment object
     * @return void
     */
    protected void commitFragment(int viewId, BaseFragment baseFragment) {
        if (baseFragment == null) return;

        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(viewId, baseFragment, baseFragment.getClass().getName())
                    .addToBackStack(baseFragment.getClass().getName())
                    .commit();
        } catch (IllegalStateException illegalStateException) {
            illegalStateException.printStackTrace();
        }

        mCurrentFragment = baseFragment;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bottom_nav, menu);
        myDataPlanMenuItem = menu.getItem(0);

/*        msgSendingStatusMenuItem = menu.findItem(R.id.menu_msg_sending_status);
        myDataPlanMenuItem = menu.getItem(1);*/

        // msgSendingStatusMenuItem.setVisible(false);

        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_data_plan_setting) {
            DataPlanManager.openActivity(this);
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
   /*     if (connectionManager != null) {
            connectionManager.stopMesh();
        }*/
    }

    @Override
    public void onMessageReceived(String messageId) {
        if (messageMap == null) return;

        UserModel userModel = messageMap.get(messageId);
        if (isAllMessageProcessClicked && userModel != null) {
            runOnUiThread(() -> {
                msgSendCount++;
                //msgSendingStatusMenuItem.setTitle(msgSendCount + "/" + userCount);

                if (mCurrentFragment instanceof NearbyFragment) {
                    userModel.setSent(true);
                    ((NearbyFragment) mCurrentFragment).updateSentMessageScreen(userModel);
                    messageMap.remove(messageId);
                }

                if (userCount == msgSendCount) {
                    //msgSendingStatusMenuItem.setVisible(false);
                    // invalidateOptionsMenu();
                    isAllMessageProcessClicked = false;
                }
            });
        }

    }



}
