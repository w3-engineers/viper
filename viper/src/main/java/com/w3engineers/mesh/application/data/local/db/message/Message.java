package com.w3engineers.mesh.application.data.local.db.message;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

@Entity(indices = {@Index(value = {"messageId"},
        unique = true)})
public class Message implements Parcelable {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    public int pid;

    @ColumnInfo(name = "senderId")
    public String senderId;

    @ColumnInfo(name = "receiverId")
    public String receiverId;


    @ColumnInfo(name = "messageId")
    public String messageId;

    @ColumnInfo(name = "data")
    public byte[] data;

    public Message(){

    }

    protected Message(Parcel in) {
        pid = in.readInt();
        senderId = in.readString();
        receiverId = in.readString();
        messageId = in.readString();
        data = in.createByteArray();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(pid);
        dest.writeString(senderId);
        dest.writeString(receiverId);
        dest.writeString(messageId);
        dest.writeByteArray(data);
    }
}
