package com.w3engineers.mesh.application.data.local.db.buyerpendingmessage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class BuyerPendingMessage {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "sender")
    public String sender;

    @ColumnInfo(name = "owner")
    public String owner;

    @ColumnInfo(name = "msg_data")
    public String msgData;

    @ColumnInfo(name = "msg_id")
    public String msgId;

    @ColumnInfo(name = "data_size")
    public long dataSize;

    @ColumnInfo(name = "status")
    public int status;

    @ColumnInfo(name = "comment")
    public String comment;

    @ColumnInfo(name = "create_time")
    public long createTime;

    @ColumnInfo(name = "update_time")
    public long updateTime;

    @ColumnInfo(name = "is_incomming")
    public boolean isIncomming;


}

