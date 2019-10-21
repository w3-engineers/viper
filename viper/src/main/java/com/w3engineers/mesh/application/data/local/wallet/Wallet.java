package com.w3engineers.mesh.application.data.local.wallet;

import android.content.Context;
import android.content.Intent;

import com.w3engineers.mesh.application.ui.wallet.WalletActivity;


public class Wallet {
    private static Wallet wallet;

    public static void openActivity(Context context){
        Intent intent = new Intent(context, WalletActivity.class);
        context.startActivity(intent);
    }
    public static Wallet getInstance(){
        if (wallet == null){
            wallet = new Wallet();
        }
        return wallet;
    }
}
