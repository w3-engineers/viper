package com.w3engineers.mesh.application.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class DataEvent extends Event implements Parcelable {

    public String peerId;
    public byte[] data;
    public byte dataType;

    public DataEvent() {

    }

    protected DataEvent(Parcel in) {
        peerId = in.readString();
        data = in.createByteArray();
        dataType = in.readByte();
    }

    public static final Creator<DataEvent> CREATOR = new Creator<DataEvent>() {
        @Override
        public DataEvent createFromParcel(Parcel in) {
            return new DataEvent(in);
        }

        @Override
        public DataEvent[] newArray(int size) {
            return new DataEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(peerId);
        parcel.writeByteArray(data);
        parcel.writeByte(dataType);
    }
}