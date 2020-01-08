package com.w3engineers.mesh.application.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PayMessage extends Event implements Parcelable {
    public String sender;
    public byte[] paymentData;


    public PayMessage(){

    }

    protected PayMessage(Parcel in) {
        sender = in.readString();
        paymentData = in.createByteArray();
    }

    public static final Creator<PayMessage> CREATOR = new Creator<PayMessage>() {
        @Override
        public PayMessage createFromParcel(Parcel in) {
            return new PayMessage(in);
        }

        @Override
        public PayMessage[] newArray(int size) {
            return new PayMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sender);
        dest.writeByteArray(paymentData);
    }
}
