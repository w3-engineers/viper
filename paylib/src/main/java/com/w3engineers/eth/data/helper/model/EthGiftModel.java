package com.w3engineers.eth.data.helper.model;

public class EthGiftModel {
    public String userAddress;
    public String ethTx;
    public String tknTx;
    public int endpoint;
    public double ethValue;
    public double tknValue;

    public EthGiftModel(String userAddress, String ethTx, String tknTx, int endpoint, double ethValue, double tknValue){
        this.userAddress = userAddress;
        this.ethTx = ethTx;
        this.tknTx = tknTx;
        this.endpoint = endpoint;
        this.ethValue = ethValue;
        this.tknValue = tknValue;
    }
}
