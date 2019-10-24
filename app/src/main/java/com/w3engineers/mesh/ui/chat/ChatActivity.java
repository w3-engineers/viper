package com.w3engineers.mesh.ui.chat;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.w3engineers.ext.viper.R;
import com.w3engineers.ext.viper.databinding.ActivityChatBinding;
import com.w3engineers.mesh.model.MessageModel;
import com.w3engineers.mesh.model.UserModel;
import com.w3engineers.mesh.ui.Nearby.NearbyCallBack;
import com.w3engineers.mesh.util.ConnectionManager;
import com.w3engineers.mesh.util.Constants;
import com.w3engineers.mesh.util.HandlerUtil;
import com.w3engineers.mesh.util.TimeUtil;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


/**
 * * ============================================================================
 * * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * * Unauthorized copying of this file, via any medium is strictly prohibited
 * * Proprietary and confidential
 * * ----------------------------------------------------------------------------
 * * Created by: Sikder Faysal Ahmed on [15-Jan-2019 at 1:01 PM].
 * * Email: sikderfaysal@w3engineers.com
 * * ----------------------------------------------------------------------------
 * * Project: meshrnd.
 * * Code Responsibility: <Purpose of code>
 * * ----------------------------------------------------------------------------
 * * Edited by :
 * * --> <First Editor> on [15-Jan-2019 at 1:01 PM].
 * * --> <Second Editor> on [15-Jan-2019 at 1:01 PM].
 * * ----------------------------------------------------------------------------
 * * Reviewed by :
 * * --> <First Reviewer> on [15-Jan-2019 at 1:01 PM].
 * * --> <Second Reviewer> on [15-Jan-2019 at 1:01 PM].
 * * ============================================================================
 **/
public class ChatActivity extends AppCompatActivity implements View.OnClickListener, MessageListener, NearbyCallBack, DbUpdate {

    private ActivityChatBinding mBinding;
    private ChatAdapter mChatAdapter;
    private UserModel mUserModel;
    private MenuItem status, repeatedMsg;
    private Timer mTimer;
    private boolean isRepeatModeOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGuiWithUserdata();
        initAdapter();
        setUserInfo();
        ConnectionManager.on(this).initMessageListener(this);
        ConnectionManager.on(this).initNearByCallBackForChatActivity(this);
        ChatDataProvider.On().setUpdateListener(this::updateUI);
        fetchAllConversationWithThisUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_active_status, menu);
        status = menu.findItem(R.id.menu_active_state);
        menu.findItem(R.id.menu_send_large_message).setEnabled(true);
        repeatedMsg = menu.findItem(R.id.menu_send_continuous_msg).setEnabled(true);
        status.setEnabled(true);
        List<UserModel> list = ConnectionManager.on(this).getUserList();
        Collections.sort(list);
        for (UserModel userModel : list) {
            if (mUserModel.getUserId().equalsIgnoreCase(userModel.getUserId())) {
                String connectionType = ConnectionManager.on(this).getConnectionType(mUserModel.getUserId());
                //  status.setTitle(getString(R.string.status_online));
                status.setTitle(connectionType);
            }
        }
        return true;
    }

    private void initGuiWithUserdata() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUserModel = (UserModel) getIntent().getSerializableExtra(UserModel.class.getName());
        mBinding.imageButtonSend.setOnClickListener(this);
    }

    private void initAdapter() {
        mChatAdapter = new ChatAdapter();
        mBinding.recyclerViewMessage.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerViewMessage.setAdapter(mChatAdapter);
    }

    private void fetchAllConversationWithThisUser() {
        //resendFailedMessage(mUserModel.getUserId());
        mChatAdapter.addItem(ChatDataProvider.On().getAllConversation(mUserModel.getUserId()));
        scrollSmoothly();
    }

/*    private void resendFailedMessage(String userId) {
        List<MessageModel> SendingFailedMessage = ChatDataProvider.On().getSendFailedConversation(userId);
        if (SendingFailedMessage.size() > 0) {
            for (MessageModel message : SendingFailedMessage) {
                ConnectionManager.on().sendMessage(mUserModel.getUserId(), message);
            }
        }
    }*/

    private void setUserInfo() {
        getSupportActionBar().setTitle(mUserModel.getUserName());
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.drawable_reg_page_shape));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       ConnectionManager.on(this).initMessageListener(null);
       ConnectionManager.on(this).initNearByCallBackForChatActivity(null);
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    @Override
    public void onClick(View v) {
        String inputValue = mBinding.edittextMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(inputValue)) return;
        mBinding.edittextMessageInput.setText("");
        MessageModel messageModel = new MessageModel();
        messageModel.message = inputValue + "\n" + TimeUtil.parseMillisToTime(System.currentTimeMillis());
        messageModel.incoming = false;
        messageModel.friendsId = mUserModel.getUserId();
        messageModel.messageId = UUID.randomUUID().toString();

        ChatDataProvider.On().insertMessage(messageModel, mUserModel);
        mChatAdapter.addItem(messageModel);
        scrollSmoothly();

        //  messageModel.messageLongId = System.currentTimeMillis();

        //   String messageId = ConnectionManager.on().sendMessage(mUserModel.getUserId(), messageModel);

        ConnectionManager.on(this).sendMessage(mUserModel.getUserId(), messageModel);

/*        if (messageId !=null){
            messageModel.messageId = messageId;
            ChatDataProvider.On().insertMessage(messageModel, mUserModel);
        }else {
            messageModel.messageStatus = Constant.MessageStatus.FAILED;
        }*/

/*        mChatAdapter.addItem(messageModel);
        scrollSmoothly();*/
    }

    @Override
    protected void onResume() {
        super.onResume();
     //   ConnectionManager.on().initListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_send_large_message:
                sendMessage(Constants.LARGE_MESSAGE);
                break;
            case R.id.menu_send_continuous_msg:
                if (isRepeatModeOn) {
                    isRepeatModeOn = false;
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    repeatedMsg.setIcon(R.drawable.ic_action_playback_repeat);
                } else {
                    isRepeatModeOn = true;
                    Toast.makeText(this, "Repeated message started", Toast.LENGTH_SHORT).show();
                    repeatedMsg.setIcon(R.drawable.ic_action_cancel);
                    repeatMessage();
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void repeatMessage() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendMessage("Hello Bro");
            }
        }, 5 * 1000, 5 * 1000);
    }

    private void sendMessage(String message) {
        MessageModel messageModel = new MessageModel();
        messageModel.message = message + "\n" + TimeUtil.parseMillisToTime(System.currentTimeMillis());
        messageModel.incoming = false;
        messageModel.friendsId = mUserModel.getUserId();
        messageModel.messageId = UUID.randomUUID().toString();

        ChatDataProvider.On().insertMessage(messageModel, mUserModel);
        runOnUiThread(() -> {
            mChatAdapter.addItem(messageModel);
            scrollSmoothly();
        });
        ConnectionManager.on(this).sendMessage(mUserModel.getUserId(), messageModel);
    }

    private void scrollSmoothly() {
        int index = mChatAdapter.getItemCount() - 1;
        if (index > 0) {
            mBinding.recyclerViewMessage.smoothScrollToPosition(index);
        }
    }

    @Override
    public void onMessageReceived(MessageModel message) {
        if (message.friendsId.equalsIgnoreCase(mUserModel.getUserId())) {
            HandlerUtil.postForeground(() -> {
                mChatAdapter.addItem(message);
                scrollSmoothly();
            });
        }
    }

    @Override
    public void onMessageDelivered() {
        updateUI();
    }

    @Override
    public void onUserFound(UserModel model) {
        if (model.getUserId().equalsIgnoreCase(mUserModel.getUserId())) {
            // runOnUiThread(() -> mBinding.edittextMessageInput.setEnabled(true));
            runOnUiThread(() -> {
                if (status != null) {
                  /*  String connectionType = ConnectionManager.on().getConnectionType(model.getUserId());
                    status.setTitle(connectionType);*/
                }
            });

            // resendFailedMessage(model.getUserId());
        }
    }

    @Override
    public void onDisconnectUser(String userId) {
        if (userId == null || userId.isEmpty()) return;
        if (userId.equalsIgnoreCase(mUserModel.getUserId())) {
            runOnUiThread(() -> {
/*                mBinding.edittextMessageInput.setEnabled(false);
                mBinding.edittextMessageInput.setHint("Currently," + " "+ mUserModel.getUserName() + " " + "is not available ");
                mBinding.edittextMessageInput.setHintTextColor(getResources().getColor(R.color.colorAccent));*/

           /*     if (status != null) {
                    status.setTitle(getString(R.string.status_offline));
                }*/
            });
        }
    }


    @Override
    public void updateUI() {
        if (mChatAdapter != null) {
            runOnUiThread(() -> {
                mChatAdapter.clear();
                List<MessageModel> messageModelList = ChatDataProvider.On().getAllConversation(mUserModel.getUserId());
                mChatAdapter.addItem(messageModelList);
            });
        }
    }
}
