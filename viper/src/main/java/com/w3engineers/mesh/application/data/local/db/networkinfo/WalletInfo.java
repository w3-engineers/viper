package com.w3engineers.mesh.application.data.local.db.networkinfo;

import android.arch.persistence.room.ColumnInfo;

public class WalletInfo {
    @ColumnInfo(name = "currency_symbol")
    public String currencySymbol;

    @ColumnInfo(name = "token_symbol")
    public String tokenSymbol;

    @ColumnInfo(name = "token_amount")
    public double tokenAmount;

    @ColumnInfo(name = "currency_amount")
    public double currencyAmount;

    @ColumnInfo(name = "network_type")
    public int networkType;
}
