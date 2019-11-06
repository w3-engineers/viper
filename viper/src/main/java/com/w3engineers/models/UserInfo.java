package com.w3engineers.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserInfo implements Parcelable {
    private String address;
    private int avatar;
    private String userName;
    private long regTime;
    private boolean isSync;


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

    public boolean isSync() {
        return isSync;
    }

    public void setSync(boolean sync) {
        isSync = sync;
    }

    protected UserInfo(Parcel in) {
        address = in.readString();
        avatar = in.readInt();
        userName = in.readString();
        regTime = in.readLong();
        isSync = in.readByte() != 0;
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
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
        dest.writeByte((byte) (isSync ? 1 : 0));
    }
}
