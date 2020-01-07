package com.w3engineers.eth.data.helper;

import android.content.Context;

import com.w3engineers.eth.data.constant.PayLibConstant;
import com.w3engineers.eth.util.lib.shared_preferences.SharedPreferencePrivateMode;

/**
 * Created by Monir Zzaman on 10/30/2017.
 * This helper class will be used from any place of the application to save/read data
 * from shared preference library
 */
public class PreferencesHelperPaylib extends SharedPreferencePrivateMode {

    private static PreferencesHelperPaylib sInstance;
    private static final String CHANNEL_CREATED_LATEST_BLOCK = "channel_create_latest_block";
    private static final String CHANNEL_CLOSED_LATEST_BLOCK = "channel_closed_latest_block";
    private static final String CHANNEL_TOPUP_LATEST_BLOCK = "channel_topup_latest_block";
    private static final String BALANCE_APPROVED_BLOCK = "balance_approved_block";
    private static final String CHANNEL_WITHDRAWN_BLOCK = "channel_withdrawn_block";
    private static final String TOKEN_MINTED_BLOCK = "token_minted_block";
    private static final String TOKEN_TRANSFERRED_BLOCK = "token_transferred_block";

    private PreferencesHelperPaylib(Context context) {
        super(context);
    }

    synchronized public static PreferencesHelperPaylib onInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesHelperPaylib(context);
        }
        return sInstance;
    }

    private static final String ADDRESS = "address";
//    private static final String TOKEN_WEI_VALUE = "token_balance";
//    private static final String ETHER_WEI_VALUE = "ether_balance";
    private static final String ENDPOINT_MODE = "currency_mode";

    public void saveAddress(String value) {
        writeString(ADDRESS, value);
    }

    public String getAddress() {
        return readString(ADDRESS, null);
    }

//    public void setTokenBalance(double value) {
//        writeString(TOKEN_WEI_VALUE, value+"");
//    }

//    public double getTokenBalance() {
//        String str = readString(TOKEN_WEI_VALUE, "0.0");
//        return new Double(str).doubleValue();
//    }

    /*public void setEtherBalance(double value) {
        writeString(ETHER_WEI_VALUE, value+"");
    }*/

//    public double getEtherBalance() {
//        String str = readString(ETHER_WEI_VALUE, "0.0");
//        return new Double(str).doubleValue();
//    }

    public void setEndPointMode(int mode) {
        writeInt(ENDPOINT_MODE, mode);
    }

    public int getEndpointMode() {
        return readInt(ENDPOINT_MODE, PayLibConstant.END_POINT_TYPE.ETC_KOTTI);
    }

    public void setChannelCreatedBlock(long value, int endpoint){
        writeLong(CHANNEL_CREATED_LATEST_BLOCK + "_" + endpoint, value);
    }

    public long getChannelCreatedBlock(int endpoint){
        return readLong(CHANNEL_CREATED_LATEST_BLOCK + "_" + endpoint, 0);
    }

    public void setChannelClosedBlock(long value, int endpoint){
        writeLong(CHANNEL_CLOSED_LATEST_BLOCK + "_" + endpoint, value);
    }

    public long getChannelClosedBlock(int endpoint){
        return readLong(CHANNEL_CLOSED_LATEST_BLOCK + "_" + endpoint,0);
    }

    public void setChannelTopupBlock(long value, int endpoint){
        writeLong(CHANNEL_TOPUP_LATEST_BLOCK + "_" + endpoint, value);
    }

    public long getChannelTopupBlock(int endpoint){
        return readLong(CHANNEL_TOPUP_LATEST_BLOCK + "_" + endpoint,0);
    }

    public void setBalanceApprovedBlock(long value, int endpoint){
        writeLong(BALANCE_APPROVED_BLOCK + "_" + endpoint, value);
    }

    public long getBalanceApprovedBlock(int endpoint){
        return readLong(BALANCE_APPROVED_BLOCK + "_" + endpoint,0);
    }

    public void setChannelWithdrawnBlock(long value, int endpoint){
        writeLong(CHANNEL_WITHDRAWN_BLOCK + "_" + endpoint, value);
    }

    public long getChannelWithdrawnBlock(int endpoint){
        return readLong(CHANNEL_WITHDRAWN_BLOCK + "_" + endpoint,0);
    }

    public void setTokenMintedBlock(long value, int endpoint){
        writeLong(TOKEN_MINTED_BLOCK + "_" + endpoint, value);
    }

    public long getTokenMintedBlock(int endpoint){
        return readLong(TOKEN_MINTED_BLOCK + "_" + endpoint,0);
    }

    public void setTokenTransferredBlock(long value, int endpoint){
        writeLong(TOKEN_TRANSFERRED_BLOCK + "_" + endpoint, value);
    }

    public long getTokenTransferredBlock(int endpoint){
        return readLong(TOKEN_TRANSFERRED_BLOCK + "_" + endpoint,0);
    }


}
