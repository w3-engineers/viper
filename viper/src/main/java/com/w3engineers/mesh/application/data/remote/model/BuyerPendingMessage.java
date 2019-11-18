package com.w3engineers.mesh.application.data.remote.model;

import com.w3engineers.mesh.application.data.model.Event;

import android.os.Parcel;
import android.os.Parcelable;

public class BuyerPendingMessage extends Event implements Parcelable {
    public String sender;
    public String receiver;
    public String messageId;
    public String messageData;
    public long dataLength;
    public boolean isIncoming;

    public BuyerPendingMessage(){
    }
    protected BuyerPendingMessage(Parcel in) {
        sender = in.readString();
        receiver = in.readString();
        messageId = in.readString();
        messageData = in.readString();
        dataLength = in.readLong();
        isIncoming = in.readByte() == 1;
    }

    public static final Creator<BuyerPendingMessage> CREATOR = new Creator<BuyerPendingMessage>() {
        @Override
        public BuyerPendingMessage createFromParcel(Parcel in) {
            return new BuyerPendingMessage(in);
        }

        @Override
        public BuyerPendingMessage[] newArray(int size) {
            return new BuyerPendingMessage[size];
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
        dest.writeString(messageData);
        dest.writeLong(dataLength);
        dest.writeByte((byte) (isIncoming ? 1 : 0));
    }
}
