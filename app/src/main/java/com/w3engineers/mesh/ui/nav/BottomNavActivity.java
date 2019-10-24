package com.w3engineers.mesh.ui.nav;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.w3engineers.ext.viper.R;
import com.w3engineers.ext.viper.databinding.ActivityCreateGroupBinding;
import com.w3engineers.mesh.application.data.local.db.SharedPref;

import com.w3engineers.mesh.model.MessageModel;
import com.w3engineers.mesh.model.UserModel;
import com.w3engineers.mesh.ui.Nearby.NearbyFragment;
import com.w3engineers.mesh.ui.Nearby.UserConnectionCallBack;
import com.w3engineers.mesh.ui.base.BaseFragment;
import com.w3engineers.mesh.ui.chat.ChatDataProvider;
import com.w3engineers.mesh.ui.diagram.DiagramFragment;
import com.w3engineers.mesh.ui.history.HistoryFragment;
import com.w3engineers.mesh.ui.meshlog.MeshLogFragment;
import com.w3engineers.mesh.ui.network.NetworkFragment;
import com.w3engineers.mesh.util.Constant;

import com.w3engineers.mesh.util.HandlerUtil;
import com.w3engineers.mesh.util.MeshLog;
import com.w3engineers.mesh.util.TimeUtil;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BottomNavActivity extends AppCompatActivity implements UserConnectionCallBack,  BottomMessageListener {

    private static final String TAG = BottomNavActivity.class.getSimpleName();

    private BaseFragment mCurrentFragment;
    private BaseFragment baseFragment;
    private ActivityCreateGroupBinding mBinding;
    private MenuItem myDataPlanMenuItem;
    TextView connectedUser;
    BottomNavigationView navigation;

  //  private ConnectionManager connectionManager;

    /*
     * All message sending process
     * */
    private MenuItem msgSendingStatusMenuItem;
    private boolean isAllMessageProcessClicked;
    private int msgSendCount = 0;
    private int userCount;
    private HashMap<String, UserModel> messageMap;



    public interface StateListener {
        void onInit(boolean isSuccess);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //MeshLog.clearLog();
        //  Constant.CURRENT_LOG_FILE_NAME = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date()) + ".txt";

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



    }

/*
    @Override
    protected void onResume() {
        super.onResume();
        List<UserModel> list = ConnectionManager.on().getUserList();
        if (list.size() > 0) {
            BottomMenuHelper.showBadge(this, navigation, R.id.navigation_nearby, String.valueOf(list.size()));
        } else {
            BottomMenuHelper.removeBadge(navigation, R.id.navigation_nearby);
        }
    }
*/

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
            case R.id.navigation_network:
                baseFragment = (NetworkFragment) getSupportFragmentManager()
                        .findFragmentByTag(NetworkFragment.class.getName());
                if (baseFragment == null) {
                    baseFragment = new NetworkFragment();
                }
                break;
            case R.id.navigation_nearby:
                baseFragment = (NearbyFragment) getSupportFragmentManager()
                        .findFragmentByTag(NearbyFragment.class.getName());
                if (baseFragment == null) {
                    baseFragment = new NearbyFragment();
                }
                break;
            case R.id.navigation_message:
                baseFragment = (MeshLogFragment) getSupportFragmentManager()
                        .findFragmentByTag(MeshLogFragment.class.getName());
                if (baseFragment == null) {
                    baseFragment = new MeshLogFragment();
                }
                break;

            case R.id.navigation_database:
                baseFragment = (HistoryFragment) getSupportFragmentManager()
                        .findFragmentByTag(HistoryFragment.class.getName());
                if (baseFragment == null) {
                    baseFragment = new HistoryFragment();
                }
                break;

            case R.id.navigation_diagram:
                baseFragment = (DiagramFragment) getSupportFragmentManager()
                        .findFragmentByTag(DiagramFragment.class.getName());
                if (baseFragment == null) {
                    baseFragment = new DiagramFragment();
                }
                break;
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

    private void sendAllHelloMessage() {
      //  userCount = list.size();
        MessageModel messageModel = new MessageModel();
        messageModel.message = "Hello Bro\n" + TimeUtil.parseMillisToTime(System.currentTimeMillis());
        messageModel.incoming = false;

/*        if (!list.isEmpty()) {
            runOnUiThread(() -> msgSendingStatusMenuItem.setTitle("0"));
            messageMap = new HashMap<>();
            if (mCurrentFragment instanceof NearbyFragment) {
                ((NearbyFragment) mCurrentFragment).resetScreen();
            }
        }*/

/*        HandlerUtil.postForeground(() -> {
            msgSendCount = 0;

            for (UserModel model : list) {
                messageModel.friendsId = model.getUserId();
                String msgId = UUID.randomUUID().toString();
                messageModel.messageId = msgId;

                messageMap.put(msgId, model);

                ConnectionManager.on().sendMessage(model.getUserId(), messageModel);

                ChatDataProvider.On().insertMessage(messageModel, model);
            }
        }, 1000); // we added delay because to show reset menu text*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bottom_nav, menu);

        msgSendingStatusMenuItem = menu.findItem(R.id.menu_msg_sending_status);
        myDataPlanMenuItem = menu.getItem(1);

        // msgSendingStatusMenuItem.setVisible(false);

        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
/*        if (item.getItemId() == R.id.menu_data_plan_setting) {
            startActivity(new Intent(this, DataPlanActivity.class));
        } else if (item.getItemId() == R.id.menu_ping_self) {
            connectionManager.sendPing();
        } else if (item.getItemId() == R.id.menu_log_history) {
            startActivity(new Intent(this, MeshLogHistoryActivity.class));
        } else if (item.getItemId() == R.id.menu_connectivity_diagram) {
            startActivity(new Intent(this, ConnectivityDiagramActiviy.class));
        } else if (item.getItemId() == R.id.menu_send_all_message) {
            isAllMessageProcessClicked = true;
            sendAllHelloMessage();
        } else if (item.getItemId() == R.id.menu_get_nodeInfo) {
            connectionManager.printConnectedNodeInfo();
            Toast.makeText(this, "Details printed in Log", Toast.LENGTH_SHORT).show();

        }*/

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
                msgSendingStatusMenuItem.setTitle(msgSendCount + "/" + userCount);

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
