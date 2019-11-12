package com.w3engineers.mesh.util.lib.mesh;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.text.TextUtils;

import com.w3engineers.ext.strom.util.helper.PermissionUtil;
import com.w3engineers.mesh.application.data.AppDataObserver;
import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.local.helper.crypto.CryptoHelper;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerBuyer;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerSeller;
import com.w3engineers.mesh.application.data.local.wallet.WalletManager;
import com.w3engineers.mesh.application.data.local.wallet.WalletService;
import com.w3engineers.mesh.application.data.model.WalletLoaded;
import com.w3engineers.mesh.application.ui.premission.PermissionActivity;
import com.w3engineers.mesh.util.MeshLog;
import com.w3engineers.models.UserInfo;

public class ViperClient {

    private Context mContext;
    public static String networkPrefix;
    public static String appName;

    public static String usersName;
    public static int avatar;
    public static long regTime;
    public static boolean isSync;
    public static String packageName;

    private static ViperClient mViperClient;

    private ViperClient() {
        //Prevent form the reflection api.
        if (mViperClient != null) {
            throw new RuntimeException("Use on() method to get the single instance of this class.");
        }
    }

    protected ViperClient(Context context, String appName, String packageName, String networkPrefix, String userName, int avatar, long regTime, boolean isSync) {
        this.mContext = context;
        this.appName = appName;
        this.packageName = packageName;
        this.networkPrefix = networkPrefix;
        this.usersName = userName;
        this.avatar = avatar;
        this.regTime = regTime;
        this.isSync = isSync;


        if (PermissionUtil.on(context).isAllowed(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            startClient();
        } else {
            startPermissionActivity(context);
        }
    }

    public static ViperClient on(Context context, String appName, String packageName, String networkPrefix, String userName, int avatar, long regTime, boolean isSync) {
        if (mViperClient == null) {
            synchronized (ViperClient.class) {
                if (mViperClient == null)
                    mViperClient = new ViperClient(context, appName, packageName, networkPrefix, userName, avatar, regTime, isSync);
            }
        }
        return mViperClient;
    }

    public void startClient() {

        WalletManager.getInstance().readWallet(mContext, new WalletManager.WaletListener() {
            @Override
            public void onWalletLoaded(String walletAddress, String publicKey) {
                MeshLog.i(" ViperClient loaded succesful " + walletAddress);

                UserInfo userInfo = new UserInfo();

                userInfo.setAddress(walletAddress);
                userInfo.setAvatar(avatar);
                userInfo.setRegTime(regTime);
                userInfo.setSync(isSync);
                userInfo.setUserName(usersName);
                userInfo.setPublicKey(publicKey);
                userInfo.setPackageName(packageName);

                DataManager.on().doBindService(mContext, appName, networkPrefix, userInfo);

                if (PreferencesHelperDataplan.on().getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER) {
                    PurchaseManagerSeller.getInstance().setPayControllerListener();
                }

                if (PreferencesHelperDataplan.on().getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER) {
                    PurchaseManagerBuyer.getInstance().setPayControllerListener();
                }

                WalletLoaded walletLoaded = new WalletLoaded();
                walletLoaded.walletAddress = walletAddress;
                walletLoaded.publicKey = publicKey;
                walletLoaded.success = true;
                AppDataObserver.on().sendObserverData(walletLoaded);
            }

            @Override
            public void onErrorOccurred(String message) {
                MeshLog.v("ViperClient loading failed " + message);
                WalletLoaded walletLoaded = new WalletLoaded();
                walletLoaded.message = message;
                walletLoaded.success = false;
                AppDataObserver.on().sendObserverData(walletLoaded);
            }
        });
    }

    private void startPermissionActivity(Context context) {
        Intent permissionIntent = new Intent(context, PermissionActivity.class);
        permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(permissionIntent);
    }

    public void sendMessage(String senderId, String receiverId, String messageId, byte[] data) throws RemoteException {
        String plainString = new String(data);
        String userPublicKey = DataManager.on().getUserPublicKey(receiverId);

        if (!TextUtils.isEmpty(userPublicKey)){
            MeshLog.v("Before encryption " + plainString);
            String encryptedMessage = CryptoHelper.encryptMessage(WalletService.getInstance(mContext).getPrivateKey(), userPublicKey , plainString);
            MeshLog.v("Encrypted message " + encryptedMessage);
            DataManager.on().sendData(senderId, receiverId, messageId, encryptedMessage.getBytes());
        } else {
            MeshLog.v("User public key not found " + senderId);
        }
    }

    public int getLinkTypeById(String nodeID) throws RemoteException {
        return DataManager.on().getLinkTypeById(nodeID);
    }

    public String getUserId() throws RemoteException {
        return DataManager.on().getUserId();
    }

    public void stopMesh(){
        DataManager.on().stopMesh();
    }

    public void restartMesh(int currentRole){
        DataManager.on().restartMesh(currentRole);
    }

    public void saveDiscoveredUserInfo(String userId, String userName) throws RemoteException {
        DataManager.on().saveDiscoveredUserInfo(userId, userName);
    }

}
