package com.w3engineers.mesh.application.data.local.db.purchaserequests;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class PurchaseRequests {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    public int rid;

    @ColumnInfo(name = "requester_address")
    public String requesterAddress;

    @ColumnInfo(name = "buyer_address")
    public String buyerAddress;

    @ColumnInfo(name = "request_type")
    public int requestType;

    @ColumnInfo(name = "signed_message")
    public String signedMessage;

    @ColumnInfo(name = "request_value")
    public double requestValue;

    @ColumnInfo(name = "nonce")
    public int nonce;

    @ColumnInfo(name = "trx_hash")
    public String trxHash;

    @ColumnInfo(name = "trx_block")
    public long trxBlock;

    @ColumnInfo(name = "state")
    public int state;

    @ColumnInfo(name = "response_str")
    public String responseString;

    @ColumnInfo(name = "message_id")
    public String messageId;

    @ColumnInfo(name = "create_time")
    public long createTime;

    @ColumnInfo(name = "update_time")
    public long updateTime;

    @ColumnInfo(name = "block_chain_endpoint")
    public int blockChainEndpoint;

}
