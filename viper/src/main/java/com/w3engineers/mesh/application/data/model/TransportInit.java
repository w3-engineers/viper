package com.w3engineers.mesh.application.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TransportInit extends Event implements Parcelable {
    public String nodeId;
    public String publicKey;
    public boolean success;
    public String msg;


    public TransportInit(){

    }
    protected TransportInit(Parcel in) {
        nodeId = in.readString();
        publicKey = in.readString();
        success = in.readByte() == 1;
        msg = in.readString();
    }

    public static final Creator<TransportInit> CREATOR = new Creator<TransportInit>() {
        @Override
        public TransportInit createFromParcel(Parcel in) {
            return new TransportInit(in);
        }

        @Override
        public TransportInit[] newArray(int size) {
            return new TransportInit[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nodeId);
        dest.writeString(publicKey);
        dest.writeByte((byte) (success ? 1 : 0));
        dest.writeString(msg);
    }
}
