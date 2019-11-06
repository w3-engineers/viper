package com.w3engineers.eth.data.helper.model;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

public class PayLibNetworkInfo {
    
    public int networkType;
    public String networkName;
    public String networkUrl;
    public String currencySymbol;
    public String tokenSymbol;
    public String tokenAddress;
    public String channelAddress;
    public long gasPrice;
    public long gasLimit;
    public double tokenAmount;
    public double currencyAmount;

    public PayLibNetworkInfo setNetworkType(int networkType) {
        this.networkType = networkType;
        return this;
    }

    public PayLibNetworkInfo setNetworkName(String networkName) {
        this.networkName = networkName;
        return this;
    }

    public PayLibNetworkInfo setNetworkUrl(String networkUrl) {
        this.networkUrl = networkUrl;
        return this;
    }

    public PayLibNetworkInfo setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
        return this;
    }

    public PayLibNetworkInfo setTokenSymbol(String tokenSymbol) {
        this.tokenSymbol = tokenSymbol;
        return this;
    }

    public PayLibNetworkInfo setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
        return this;
    }

    public PayLibNetworkInfo setChannelAddress(String channelAddress) {
        this.channelAddress = channelAddress;
        return this;
    }

    public PayLibNetworkInfo setGasPrice(long gasPrice) {
        this.gasPrice = gasPrice;
        return this;
    }

    public PayLibNetworkInfo setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
        return this;
    }

    public PayLibNetworkInfo setTokenAmount(double tokenAmount) {
        this.tokenAmount = tokenAmount;
        return this;
    }

    public PayLibNetworkInfo setCurrencyAmount(double currencyAmount) {
        this.currencyAmount = currencyAmount;
        return this;
    }
}
