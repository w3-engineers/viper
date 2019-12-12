
package com.w3engineers.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Network {

    @SerializedName("network_type")
    @Expose
    private Integer networkType;
    @SerializedName("network_name")
    @Expose
    private String networkName;
    @SerializedName("network_url")
    @Expose
    private String networkUrl;
    @SerializedName("currency_symbol")
    @Expose
    private String currencySymbol;
    @SerializedName("token_symbol")
    @Expose
    private String tokenSymbol;
    @SerializedName("token_address")
    @Expose
    private String tokenAddress;
    @SerializedName("channel_address")
    @Expose
    private String channelAddress;
    @SerializedName("gas_price")
    @Expose
    private Integer gasPrice;
    @SerializedName("gas_limit")
    @Expose
    private Integer gasLimit;
    @SerializedName("token_amount")
    @Expose
    private Integer tokenAmount;
    @SerializedName("currency_amount")
    @Expose
    private Integer currencyAmount;

    public Integer getNetworkType() {
        return networkType;
    }

    public void setNetworkType(Integer networkType) {
        this.networkType = networkType;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getNetworkUrl() {
        return networkUrl;
    }

    public void setNetworkUrl(String networkUrl) {
        this.networkUrl = networkUrl;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }

    public void setTokenSymbol(String tokenSymbol) {
        this.tokenSymbol = tokenSymbol;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public String getChannelAddress() {
        return channelAddress;
    }

    public void setChannelAddress(String channelAddress) {
        this.channelAddress = channelAddress;
    }

    public Integer getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(Integer gasPrice) {
        this.gasPrice = gasPrice;
    }

    public Integer getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(Integer gasLimit) {
        this.gasLimit = gasLimit;
    }

    public Integer getTokenAmount() {
        return tokenAmount;
    }

    public void setTokenAmount(Integer tokenAmount) {
        this.tokenAmount = tokenAmount;
    }

    public Integer getCurrencyAmount() {
        return currencyAmount;
    }

    public void setCurrencyAmount(Integer currencyAmount) {
        this.currencyAmount = currencyAmount;
    }

}
