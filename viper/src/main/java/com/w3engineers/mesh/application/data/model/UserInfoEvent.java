package com.w3engineers.mesh.application.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.w3engineers.models.UserInfo;

public class UserInfoEvent extends Event implements Parcelable {

    private String address;
    private int avatar;
    private String userName;
    private long regTime;
    private int configVersion;
    private boolean isSync;

    public UserInfoEvent(){

    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getRegTime() {
        return regTime;
    }

    public void setRegTime(long regTime) {
        this.regTime = regTime;
    }

    public int getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(int configVersion) {
        this.configVersion = configVersion;
    }

    public boolean isSync() {
        return isSync;
    }

    public void setSync(boolean sync) {
        isSync = sync;
    }

    protected UserInfoEvent(Parcel in) {
        address = in.readString();
        avatar = in.readInt();
        userName = in.readString();
        regTime = in.readLong();
        configVersion = in.readInt();
        isSync = in.readByte() != 0;
    }

    public static final Creator<UserInfoEvent> CREATOR = new Creator<UserInfoEvent>() {
        @Override
        public UserInfoEvent createFromParcel(Parcel in) {
            return new UserInfoEvent(in);
        }

        @Override
        public UserInfoEvent[] newArray(int size) {
            return new UserInfoEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeInt(avatar);
        dest.writeString(userName);
        dest.writeLong(regTime);
        dest.writeInt(configVersion);
        dest.writeByte((byte) (isSync ? 1 : 0));
    }

    @NonNull
    @Override
    public String toString() {
        return "{ Address: "+address+" Avater: "+avatar+" Name: "+userName+" }";
    }
}
