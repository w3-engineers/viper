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
        return readInt(ENDPOINT_MODE, PayLibConstant.END_POINT_TYPE.TETH_PRIVATE);
    }

}
