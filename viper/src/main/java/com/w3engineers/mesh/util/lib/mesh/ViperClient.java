package com.w3engineers.mesh.util.lib.mesh;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.widget.Toast;

import com.w3engineers.ext.strom.util.helper.PermissionUtil;
import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerBuyer;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerSeller;
import com.w3engineers.mesh.application.data.local.wallet.WalletManager;
import com.w3engineers.mesh.application.data.local.wallet.WalletService;
import com.w3engineers.mesh.application.ui.premission.PermissionActivity;
import com.w3engineers.mesh.util.MeshApp;

public class ViperClient {

    private Context mContext;
    private String networkPrefix;
    private static ViperClient mViperClient;

    private ViperClient() {
        //Prevent form the reflection api.
        if (mViperClient != null) {
            throw new RuntimeException("Use on() method to get the single instance of this class.");
        }
    }

    protected ViperClient(Context context, String networkPrefix) {
        this.mContext = context;
        this.networkPrefix = networkPrefix;

        if (PermissionUtil.on(context).isAllowed(Manifest.permission.READ_EXTERNAL_STORAGE)){
            startClient();
        }else {
            startPermissionActivity(context);
        }
    }


    public void startClient(){

        DataManager.on().doBindService(mContext, this.networkPrefix);
        WalletManager.getInstance().readWallet(mContext);
        if (PreferencesHelperDataplan.on().getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER) {
            PurchaseManagerSeller.getInstance().setPayControllerListener();
        }

        if (PreferencesHelperDataplan.on().getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER) {
            PurchaseManagerBuyer.getInstance().setPayControllerListener();
        }
    }

    private void startPermissionActivity(Context context){
        Intent permissionIntent = new Intent(context, PermissionActivity.class);
        permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(permissionIntent);

    }


    public static ViperClient on(Context context, String networkPrefix) {
        if (mViperClient == null) {
            synchronized (ViperClient.class) {
                if (mViperClient == null) mViperClient = new ViperClient(context, networkPrefix);
            }
        }
        return mViperClient;
    }

    public void sendMessage(String senderId, String receiverId, String messageId, byte[] data) throws RemoteException {
        DataManager.on().sendData(senderId, receiverId, messageId, data);
    }


    public int getLinkTypeById(String nodeID) throws RemoteException {
        return DataManager.on().getLinkTypeById(nodeID);
    }

    public String getUserId() throws RemoteException {
        return DataManager.on().getUserId();
    }



}
