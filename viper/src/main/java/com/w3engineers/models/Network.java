
package com.w3engineers.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Network implements Parcelable {

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
    private long gasPrice;
    @SerializedName("gas_limit")
    @Expose
    private long gasLimit;
    @SerializedName("token_amount")
    @Expose
    private double tokenAmount;
    @SerializedName("currency_amount")
    @Expose
    private double currencyAmount;

    protected Network(Parcel in) {
        if (in.readByte() == 0) {
            networkType = null;
        } else {
            networkType = in.readInt();
        }
        networkName = in.readString();
        networkUrl = in.readString();
        currencySymbol = in.readString();
        tokenSymbol = in.readString();
        tokenAddress = in.readString();
        channelAddress = in.readString();
        gasPrice = in.readLong();
        gasLimit = in.readLong();
        tokenAmount = in.readDouble();
        currencyAmount = in.readDouble();
    }

    public static final Creator<Network> CREATOR = new Creator<Network>() {
        @Override
        public Network createFromParcel(Parcel in) {
            return new Network(in);
        }

        @Override
        public Network[] newArray(int size) {
            return new Network[size];
        }
    };

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

    public long getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(long gasPrice) {
        this.gasPrice = gasPrice;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public double getTokenAmount() {
        return tokenAmount;
    }

    public void setTokenAmount(double tokenAmount) {
        this.tokenAmount = tokenAmount;
    }

    public double getCurrencyAmount() {
        return currencyAmount;
    }

    public void setCurrencyAmount(double currencyAmount) {
        this.currencyAmount = currencyAmount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (networkType == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(networkType);
        }
        parcel.writeString(networkName);
        parcel.writeString(networkUrl);
        parcel.writeString(currencySymbol);
        parcel.writeString(tokenSymbol);
        parcel.writeString(tokenAddress);
        parcel.writeString(channelAddress);
        parcel.writeLong(gasPrice);
        parcel.writeLong(gasLimit);
        parcel.writeDouble(tokenAmount);
        parcel.writeDouble(currencyAmount);
    }
}
