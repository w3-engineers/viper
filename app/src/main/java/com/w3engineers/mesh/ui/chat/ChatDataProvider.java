package com.w3engineers.mesh.ui.chat;

import android.text.TextUtils;

import com.w3engineers.mesh.model.MessageModel;
import com.w3engineers.mesh.model.MessageModel_;
import com.w3engineers.mesh.model.UserModel;
import com.w3engineers.mesh.model.UserModel_;
import com.w3engineers.mesh.util.Constant;
import com.w3engineers.mesh.util.ObjectBox;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.exception.UniqueViolationException;

/**
 * This class represents the data persistent management for MessageModel management
 * {@link MessageModel}
 */

public class ChatDataProvider {
    private Box<MessageModel> mMessageModelBox = null;
    private Box<UserModel> userModelBox = null;
    private static ChatDataProvider sChatDataProvider = null;
    private DbUpdate dbUpdate;

    private ChatDataProvider() {
        mMessageModelBox = ObjectBox.get().boxFor(MessageModel.class);
        userModelBox = ObjectBox.get().boxFor(UserModel.class);
    }

    public static ChatDataProvider On() {
        if (sChatDataProvider == null) {
            sChatDataProvider = new ChatDataProvider();
        }
        return sChatDataProvider;
    }

    public void setUpdateListener(DbUpdate dbUpdate) {
        this.dbUpdate = dbUpdate;

    }

    /**
     * Will insert message
     *
     * @param messageModel target message
     */
    public void insertMessage(MessageModel messageModel, UserModel userModel) {
        if (messageModel == null) return;
        try {
            mMessageModelBox.put(messageModel);
        }catch (UniqueViolationException e){
            e.printStackTrace();
        }

        if (userModel != null && !checkUserExistence(messageModel.friendsId)) {
            insertUser(userModel);
        }else {
            UserModel userData = ChatDataProvider.On().getUserInfoById(messageModel.friendsId);
            if (userModel !=null && userData !=null && !TextUtils.isEmpty(userModel.getUserName())){
                userData.setUserName(userModel.getUserName());
                insertUser(userData);
            }
        }
    }

    public void updateMessage() {
        dbUpdate.updateUI();
    }

    public void messageDbUpdate(String messageId, int status) {
        if (messageId.isEmpty()) return;
        MessageModel messageModel = mMessageModelBox.query().equal(MessageModel_.messageId, messageId).build().findFirst();
        messageModel.messageStatus = status;
        try {
            mMessageModelBox.put(messageModel);
        }catch (UniqueViolationException e){
            e.printStackTrace();
        }

    }

    public void updateMessageAck(String messageId, int status){
        MessageModel messageModel = mMessageModelBox.query().equal(MessageModel_.messageId, messageId).build().findFirst();
        if(messageModel != null) {
            messageModel.messageStatus = status;
            try {
                mMessageModelBox.put(messageModel);
            }catch (UniqueViolationException e){
                e.printStackTrace();
            }
        }
    }

    public void updateMessageProgress(String messageId,int progress){
        MessageModel messageModel = mMessageModelBox.query().equal(MessageModel_.messageId, messageId).build().findFirst();
        if(messageModel != null) {
            messageModel.progress = progress;
            try {
                mMessageModelBox.put(messageModel);
            }catch (UniqueViolationException e){
                e.printStackTrace();
            }
        }
    }

    public int getMessageStatus(String messageId) {
        MessageModel messageModel = mMessageModelBox.query().equal(MessageModel_.messageId, messageId).build().findFirst();
        if (messageModel !=null){
            return messageModel.messageStatus;
        }else {
            return 0;
        }
    }

    public void insertUser(UserModel userModel) {
        if (userModel == null) return;
        try {
            userModelBox.put(userModel);
        }catch (UniqueViolationException e){
           e.printStackTrace();
        }
    }

    public void upSertUser(UserModel userModel){
        UserModel existUser = getUserInfoById(userModel.getUserId());

        if(existUser != null){
            existUser.setUserName(userModel.getUserName());
            userModelBox.put(existUser);
        }else {
            userModelBox.put(userModel);
        }

    }



    public boolean checkUserExistence(String userId) {
        UserModel userModel = userModelBox.query().equal(UserModel_.userId, userId).build().findFirst();
        return userModel != null;
    }

    public UserModel getUserInfoById(String userId){
        if (TextUtils.isEmpty(userId)) return null;
        UserModel userModel = userModelBox.query().equal(UserModel_.userId, userId).build().findFirst();
        return userModel;
    }


    /**
     * Will fetch all message with device user and selected user
     *
     * @param userId target userId
     * @return conversationList between device user and selected user
     */
    public List<MessageModel> getAllConversation(String userId) {
        return mMessageModelBox.query().equal(MessageModel_.friendsId, userId).build().find();
    }

    public List<MessageModel> getSendFailedConversation(String userId) {
        return mMessageModelBox.query().equal(MessageModel_.friendsId, userId)
                .equal(MessageModel_.incoming, false)
                .equal(MessageModel_.messageStatus, Constant.MessageStatus.SENDING).build().find();
    }

    public List<UserModel> getAllUser() {
        return userModelBox.getAll();
    }

    public String getUserName (String userId){
        UserModel u = getUserInfoById(userId);
        if (u != null)
            return  u.getUserName();

        return null;
    }
}
