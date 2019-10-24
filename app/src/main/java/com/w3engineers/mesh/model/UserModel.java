package com.w3engineers.mesh.model;


import android.arch.persistence.room.Ignore;
import android.text.TextUtils;

import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.util.Constant;
import com.w3engineers.mesh.util.JsonKeys;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.IndexType;
import io.objectbox.annotation.Unique;

/*
 *  ****************************************************************************
 *  * Created by : Md. Azizul Islam on 1/16/2019 at 5:06 PM.
 *  * Email : azizul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md. Azizul Islam on 1/14/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */
@Entity
public class UserModel implements Serializable, Comparable<UserModel> {

    @Id
    private long id;
    private String userName;


    @Unique
    @Index(type = IndexType.VALUE)
    private String userId;

    @Ignore
    private boolean isSent;

/*    @Index(type = IndexType.VALUE)
    private String messageId;

    private int userInfoStatus;*/

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

/*    public int getUserInfoStatus() {
        return userInfoStatus;
    }

    public void setUserInfoStatus(int userInfoStatus) {
        this.userInfoStatus = userInfoStatus;
    }*/

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

/*
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String userId) {
        this.messageId = userId;
    }
*/

    @Override
    public String toString() {
        return "UserModel{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    public static UserModel fromJSON(JSONObject jo) {
        try {
            String userName = jo.getString(JsonKeys.KEY_USER_NAME);
            String userId = jo.has(JsonKeys.KEY_USER_ID) ? jo.getString(JsonKeys.KEY_USER_ID) : "";
            UserModel user = new UserModel();
            user.setUserName(userName);
            user.setUserId(userId);
            return user;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUserJson(String userId) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JsonKeys.KEY_USER_ID, userId);
            jsonObject.put(JsonKeys.KEY_USER_NAME, SharedPref.read(Constant.KEY_USER_NAME));
            jsonObject.put(JsonKeys.KEY_DATA_TYPE, JsonKeys.TYPE_USER_INFO);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static String buildUserInfoReqJson() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JsonKeys.KEY_DATA_TYPE, JsonKeys.TYPE_REQ_USR_INFO);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static UserModel buildUserTempData(String userId) {
        UserModel user = new UserModel();
        user.setUserId(userId);
        return user;
    }

    @Override
    public int compareTo(UserModel userModel) {
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userModel.userName))
            return 0;
        return this.userName.length() - userModel.userName.length();
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }
}

