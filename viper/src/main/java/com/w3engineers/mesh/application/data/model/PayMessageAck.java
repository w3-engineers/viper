package com.w3engineers.mesh.application.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PayMessageAck extends Event implements Parcelable {

    public String sender;
    public String receiver;
    public String messageId;

    public PayMessageAck(){}

    protected PayMessageAck(Parcel in) {
         sender = in.readString();
         receiver = in.readString();
         messageId = in.readString();
    }

    public static final Creator<PayMessageAck> CREATOR = new Creator<PayMessageAck>() {
        @Override
        public PayMessageAck createFromParcel(Parcel in) {
            return new PayMessageAck(in);
        }

        @Override
        public PayMessageAck[] newArray(int size) {
            return new PayMessageAck[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sender);
        dest.writeString(receiver);
        dest.writeString(messageId);
    }
}
