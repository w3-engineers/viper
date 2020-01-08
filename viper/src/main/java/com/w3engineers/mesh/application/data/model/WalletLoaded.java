package com.w3engineers.mesh.application.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class WalletLoaded extends Event implements Parcelable {
    public boolean success;
    public String walletAddress;
    public String publicKey;
    public String message;

    public WalletLoaded(){}

    protected WalletLoaded(Parcel in) {
        success = in.readByte() == 1;
        walletAddress = in.readString();
        publicKey = in.readString();
        message = in.readString();
    }

    public static final Creator<WalletLoaded> CREATOR = new Creator<WalletLoaded>() {
        @Override
        public WalletLoaded createFromParcel(Parcel in) {
            return new WalletLoaded(in);
        }

        @Override
        public WalletLoaded[] newArray(int size) {
            return new WalletLoaded[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (success ? 1 : 0));
        dest.writeString(walletAddress);
        dest.writeString(publicKey);
        dest.writeString(message);
    }
}
