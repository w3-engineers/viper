package com.w3engineers.mesh.util.lib.mesh;

import android.content.Context;
import android.os.RemoteException;
import android.widget.Toast;

import com.w3engineers.mesh.application.data.local.wallet.WalletManager;
import com.w3engineers.mesh.application.data.local.wallet.WalletService;

public class ViperClient {

    private static ViperClient mViperClient;

    private ViperClient() {
        //Prevent form the reflection api.
        if (mViperClient != null) {
            throw new RuntimeException("Use on() method to get the single instance of this class.");
        }
    }

    protected ViperClient(Context context, String networkPrefix) {
        DataManager.on().doBindService(context, networkPrefix);
        WalletManager.getInstance().readWallet(context);

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
