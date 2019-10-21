package com.w3engineers.ext.viper.application.data.local.db.datausage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Datausage {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    public int id;

//    @ColumnInfo(name = "buyer_address")
//    public String buyerAddress;
//
//    @ColumnInfo(name = "seller_address")
//    public String sellerAddress;

    @ColumnInfo(name = "data_in_byte")
    public long dataByte;

    @ColumnInfo(name = "date")
    public long date;

    @ColumnInfo(name = "purpose")
    public int purpose;

    @ColumnInfo(name = "purchase_id")
    public int purchaseId;

}
