package com.w3engineers.mesh.application.data.local.db.networkinfo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.w3engineers.eth.data.helper.model.PayLibNetworkInfo;


import java.util.ArrayList;
import java.util.List;


@Entity(indices = {@Index(value = {"network_type"},
        unique = true)})
public class NetworkInfo {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    public int nid;

    @ColumnInfo(name = "network_type")
    public int networkType;

    @ColumnInfo(name = "network_name")
    public String networkName;

    @ColumnInfo(name = "network_url")
    public String networkUrl;

    @ColumnInfo(name = "currency_symbol")
    public String currencySymbol;

    @ColumnInfo(name = "token_symbol")
    public String tokenSymbol;

    @ColumnInfo(name = "token_address")
    public String tokenAddress;

    @ColumnInfo(name = "channel_address")
    public String channelAddress;

    @ColumnInfo(name = "gas_price")
    public long gasPrice;

    @ColumnInfo(name = "gas_limit")
    public long gasLimit;

    @ColumnInfo(name = "token_amount")
    public double tokenAmount;

    @ColumnInfo(name = "currency_amount")
    public double currencyAmount;

    public PayLibNetworkInfo toPayLibNetworkInfo() {
        return new PayLibNetworkInfo().setNetworkType(networkType).setNetworkName(networkName)
                .setNetworkUrl(networkUrl).setCurrencySymbol(currencySymbol).setTokenSymbol(tokenSymbol)
                .setTokenAddress(tokenAddress).setChannelAddress(channelAddress).setGasPrice(gasPrice)
                .setGasLimit(gasLimit).setTokenAmount(tokenAmount).setCurrencyAmount(currencyAmount);
    }

    public NetworkInfo toNetworkInfo(PayLibNetworkInfo payLibNetworkInfo) {
        networkType = payLibNetworkInfo.networkType;
        networkName = payLibNetworkInfo.networkName;
        networkUrl = payLibNetworkInfo.networkUrl;

        currencySymbol = payLibNetworkInfo.currencySymbol;
        tokenSymbol = payLibNetworkInfo.tokenSymbol;
        tokenAddress = payLibNetworkInfo.tokenAddress;

        channelAddress = payLibNetworkInfo.channelAddress;
        gasPrice = payLibNetworkInfo.gasPrice;
        gasLimit = payLibNetworkInfo.gasLimit;

        tokenAmount = payLibNetworkInfo.tokenAmount;
        currencyAmount = payLibNetworkInfo.currencyAmount;
        return this;
    }

    public List<PayLibNetworkInfo> toPayLibNetworkInfos(List<NetworkInfo> networkInfos) {
        if (networkInfos != null) {
            List<PayLibNetworkInfo> payLibNetworkInfos = new ArrayList<>();
            if (networkInfos.size() > 0) {

                for (NetworkInfo networkInfo : networkInfos) {
                    payLibNetworkInfos.add(networkInfo.toPayLibNetworkInfo());
                }
            }
            return payLibNetworkInfos;
        }
        return null;
    }
}
