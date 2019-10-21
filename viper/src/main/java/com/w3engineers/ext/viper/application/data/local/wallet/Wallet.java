package com.w3engineers.ext.viper.application.data.local.wallet;

import android.content.Context;
import android.content.Intent;

import com.w3engineers.ext.viper.application.ui.wallet.WalletActivity;

public class Wallet {
    public static void openActivity(Context context){
        Intent intent = new Intent(context, WalletActivity.class);
        context.startActivity(intent);
    }
}
