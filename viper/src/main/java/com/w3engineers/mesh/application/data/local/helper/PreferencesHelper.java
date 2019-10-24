package com.w3engineers.mesh.application.data.local.helper;


import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseConstants;

public class PreferencesHelper{

    public static final int  MESH_USER = 0;
    public static final int DATA_SELLER = 1;
    public static final int DATA_BUYER = 2;
    public static final int INTERNET_USER = 3;

    public static final int DATA_AMOUNT_UNLIMITED = 0;
    public static final int DATA_AMOUNT_LIMITED = 1;

    private static final String DATA_SHARE_MODE = "data_share_mode"; //Mesh User, Buyer, Seller
    private static final String DATA_AMOUNT_MODE = "data_amount_mode"; // Unlimited, Limited
    private static final String SELL_DATA_AMOUNT = "sell_data_amount";
    private static final String SELL_FROM_DATE = "sell_from_date";
    private static final String SELL_TO_DATE = "sell_to_date";
    private static final String CHANNEL_CREATED_LATEST_BLOCK = "channel_create_latest_block";
    private static final String CHANNEL_CLOSED_LATEST_BLOCK = "channel_closed_latest_block";
    private static final String CHANNEL_TOPUP_LATEST_BLOCK = "channel_topup_latest_block";
    private static final String BALANCE_APPROVED_BLOCK = "balance_approved_block";
    private static final String CHANNEL_WITHDRAWN_BLOCK = "channel_withdrawn_block";
    private static final String TOKEN_MINTED_BLOCK = "token_minted_block";
    private static final String REQUESTED_FOR_ETHER_STATUS = "requested_for_ether";
    private static final String GIFT_ETHER_TRANX_HASH = "requested_ether_hash";
    private static final String GIFT_TOKEN_TRANX_HASH = "requested_token_hash";
    private static final String GIFT_ENDPOINT_TYPE = "gift_end_point_type";
    private static final String ETHER_REQUEST_TIME = "ether_request_time";
//    private static final String CURRENCY_MODE = "currency_mode";


    private PreferencesHelper() {

    }

    private static PreferencesHelper sInstance;

    synchronized public static PreferencesHelper on() {
        if (sInstance == null) {
            sInstance = new PreferencesHelper();
        }
        return sInstance;
    }

    public void setDataShareMode(int value) {
        SharedPref.write(DATA_SHARE_MODE, value);
    }
    public int getDataShareMode() {
      return SharedPref.readInt(DATA_SHARE_MODE, MESH_USER);
    }

    public void setDataAmountMode(int value) {
        SharedPref.write(DATA_AMOUNT_MODE, value);
    }
    public int getDataAmountMode() {
        return SharedPref.readInt(DATA_AMOUNT_MODE, DATA_AMOUNT_UNLIMITED);
    }

    public void setSellDataAmount(long value) {
        SharedPref.write(SELL_DATA_AMOUNT, value);
    }
    public long getSellDataAmount() {
        long value = SharedPref.readLong(SELL_DATA_AMOUNT);
        return value;
    }

    public void setSellFromDate(long value) {
        SharedPref.write(SELL_FROM_DATE, value);
    }
    public long getSellFromDate() {
        return SharedPref.readLong(SELL_FROM_DATE);
    }

    public void setSellToDate(long value) {
        SharedPref.write(SELL_TO_DATE, value);
    }
    public long getSellToDate() {
        return SharedPref.readLong(SELL_TO_DATE);
    }

    public void setChannelCreatedBlock(long value){
        SharedPref.write(CHANNEL_CREATED_LATEST_BLOCK, value);
    }

    public long getChannelCreatedBlock(){
        return SharedPref.readLong(CHANNEL_CREATED_LATEST_BLOCK);
    }

    public void setChannelClosedBlock(long value){
        SharedPref.write(CHANNEL_CLOSED_LATEST_BLOCK, value);
    }

    public long getChannelClosedBlock(){
        return SharedPref.readLong(CHANNEL_CLOSED_LATEST_BLOCK);
    }

    public void setChannelTopupBlock(long value){
        SharedPref.write(CHANNEL_TOPUP_LATEST_BLOCK, value);
    }

    public long getChannelTopupBlock(){
        return SharedPref.readLong(CHANNEL_TOPUP_LATEST_BLOCK);
    }

    public void setBalanceApprovedBlock(long value){
        SharedPref.write(BALANCE_APPROVED_BLOCK, value);
    }

    public long getBalanceApprovedBlock(){
        return SharedPref.readLong(BALANCE_APPROVED_BLOCK);
    }

    public void setChannelWithdrawnBlock(long value){
        SharedPref.write(CHANNEL_WITHDRAWN_BLOCK, value);
    }

    public long getChannelWithdrawnBlock(){
        return SharedPref.readLong(CHANNEL_WITHDRAWN_BLOCK);
    }

    public void setTokenMintedBlock(long value){
        SharedPref.write(TOKEN_MINTED_BLOCK, value);
    }

    public long getTokenMintedBlock(){
        return SharedPref.readLong(TOKEN_MINTED_BLOCK);
    }

    public void setRequestedForEther(int requestState, int endpoint) {
        SharedPref.write(REQUESTED_FOR_ETHER_STATUS+endpoint, requestState);
    }

    public int getEtherRequestStatus(int endpoint) {
        return SharedPref.readInt(REQUESTED_FOR_ETHER_STATUS+endpoint, PurchaseConstants.GIFT_REQUEST_STATE.NOT_REQUESTED_YET);
    }

    public void setGiftEtherHash(String tranxHash, int endpoint) {
        SharedPref.write(GIFT_ETHER_TRANX_HASH+endpoint, tranxHash);
    }

    public String getGiftEtherHash(int endpoint) {
        return SharedPref.read(GIFT_ETHER_TRANX_HASH+endpoint);
    }

    public void setGiftTokenHash(String tranxHash, int endpoint) {
        SharedPref.write(GIFT_TOKEN_TRANX_HASH+endpoint, tranxHash);
    }

    public String getGiftTokenHash( int endpoint) {
        return SharedPref.read(GIFT_TOKEN_TRANX_HASH+endpoint);
    }

    public long getEtherRequestTimeStamp( int endpoint) {
        return SharedPref.readLong(ETHER_REQUEST_TIME+endpoint);
    }

    public void setEtherRequestTimeStamp(long time, int endpoint) {
        SharedPref.write(ETHER_REQUEST_TIME+endpoint, time);
    }

}
