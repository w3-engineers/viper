package com.w3engineers.ext.viper.application.data.local.model;

import java.math.BigInteger;

public class Seller {

    private String id;
    private String name;
    private double purchasedData;
    private double usedData;
    private double dataPrice;
    private long blockNumber;
    private String btnText;
    private int status;
    private boolean isBtnEnabled;


    public Seller(){

    }

    public String getId(){
        return this.id;
    }
    public Seller setId(String id){
        this.id = id;
        return this;
    }

    public String getName(){
        return this.name;
    }
    public Seller setName(String name){
        this.name = name;
        return this;
    }

    public double getPurchasedData(){
        return this.purchasedData;
    }
    public Seller setPurchasedData(double value){
        this.purchasedData = value;
        return this;
    }
    public double getUsedData(){
        return this.usedData;
    }
    public Seller setUsedData(double value){
        this.usedData = value;
        return this;
    }

    public double getDataPrice(){
        return this.dataPrice;
    }
    public Seller setDataPrice(double dataPrice){
        this.dataPrice = dataPrice;
        return this;
    }

    public long getBlockNumber() {
        return this.blockNumber;
    }

    public Seller setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
        return this;
    }

    public String getBtnText() {
        return btnText;
    }

    public Seller setBtnText(String btnText) {
        this.btnText = btnText;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public Seller setStatus(int status) {
        this.status = status;
        return this;
    }

    public boolean isBtnEnabled() {
        return isBtnEnabled;
    }

    public Seller setBtnEnabled(boolean btnEnabled) {
        isBtnEnabled = btnEnabled;
        return this;
    }
}
