package com.w3engineers.mesh.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.w3engineers.ext.strom.util.Text;
import com.w3engineers.ext.viper.BuildConfig;
import com.w3engineers.ext.viper.R;
import com.w3engineers.ext.viper.ViperApp;
import com.w3engineers.mesh.application.data.ApiEvent;
import com.w3engineers.mesh.application.data.AppDataObserver;
import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.model.DataAckEvent;
import com.w3engineers.mesh.application.data.model.DataEvent;
import com.w3engineers.mesh.application.data.model.PeerAdd;
import com.w3engineers.mesh.application.data.model.PeerRemoved;
import com.w3engineers.mesh.application.data.model.PermissionInterruptionEvent;
import com.w3engineers.mesh.application.data.model.ServiceUpdate;
import com.w3engineers.mesh.application.data.model.UserInfoEvent;
import com.w3engineers.mesh.data.AppCredentials;
import com.w3engineers.mesh.model.MessageModel;
import com.w3engineers.mesh.model.UserModel;
import com.w3engineers.mesh.ui.Nearby.NearbyCallBack;
import com.w3engineers.mesh.ui.Nearby.UserConnectionCallBack;
import com.w3engineers.mesh.ui.chat.ChatDataProvider;
import com.w3engineers.mesh.ui.chat.MessageListener;
import com.w3engineers.mesh.ui.main.MainActivity;
import com.w3engineers.mesh.util.lib.mesh.DataManager;
import com.w3engineers.mesh.util.lib.mesh.ViperClient;
import com.w3engineers.models.MeshControlConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConnectionManager {
    private static final String NETWORK_PREFIX = "major";
    private static final String APP_NAME = "viper";
    private static ConnectionManager mConnectionManager;
    private ViperClient viperClient;
    private static Context mContext;
    private NearbyCallBack nearbyCallBack;
    private Map<String, UserModel> discoverUserMap;
    private Map<String, String> requestUserInfoList;


    public static ConnectionManager on(Context context) {
        if (mConnectionManager == null) {
            synchronized (ConnectionManager.class) {
                if (mConnectionManager == null)
                    mConnectionManager = new ConnectionManager(context);
            }
        }
        return mConnectionManager;
    }

    private ConnectionManager(Context context) {
        MeshLog.e("Connection Manager is called");
        mContext = context;
        discoverUserMap = Collections.synchronizedMap(new HashMap());
        requestUserInfoList = Collections.synchronizedMap(new HashMap<>());
        startAllObserver();
    }

    public void startViper(){
        try {
          //  String jsonData = loadJSONFromAsset(mContext);

            String jsonData = AppCredentials.getInstance().getConfiguration();

            if (!TextUtils.isEmpty(jsonData)) {

                String AUTH_USER_NAME = AppCredentials.getInstance().getAuthUserName();
                String AUTH_PASSWORD = AppCredentials.getInstance().getAuthPassword();
                String FILE_REPO_LINK = AppCredentials.getInstance().getFileRepoLink();
                String PARSE_APP_ID = AppCredentials.getInstance().getParseAppId();
                String PARSE_URL = AppCredentials.getInstance().getParseUrl();
                String SIGNAL_SERVER_URL = AppCredentials.getInstance().getSignalServerUrl();
                //String CONFIG_DATA = AppCredentials.getInstance().getConfiguration();

                /*MeshControlConfig meshControlConfig = new MeshControlConfig().setAppDownloadEnable(true)
                        .setMessageEnable(false).setDiscoveryEnable(true).setBlockChainEnable(true);

                String meshControlConfigData = new Gson().toJson(meshControlConfig);*/

                viperClient = ViperClient.on(mContext, "com.w3engineers.ext.viper", SharedPref.read(Constant.KEY_USER_NAME),
                        SharedPref.read(Constant.PreferenceKeys.ADDRESS), SharedPref.read(Constant.PreferenceKeys.PUBLIC_KEY), CONFIG_DATA)
                        .setConfig(AUTH_USER_NAME, AUTH_PASSWORD, FILE_REPO_LINK, PARSE_URL, PARSE_APP_ID, SIGNAL_SERVER_URL, BuildConfig.VERSION_CODE);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAllObserver() {

        AppDataObserver.on().startObserver(ApiEvent.PEER_ADD, event -> {
            PeerAdd peerAdd = (PeerAdd) event;

            boolean isUserExist = ChatDataProvider.On().checkUserExistence(peerAdd.peerId);
            if (isUserExist) {
                MeshLog.v("startAllObserver  peerAdd.peerId " + peerAdd.peerId);


                UserModel userModel = ChatDataProvider.On().getUserInfoById(peerAdd.peerId);
                discoverUserMap.put(peerAdd.peerId, userModel);
                if (nearbyCallBack != null) {
                    nearbyCallBack.onUserFound(userModel);
                }
            } else {
                //  reqUserInfo(peerAdd.peerId);

                UserModel userModel = new UserModel();
                userModel.setUserName("Anonymous");
                userModel.setUserId(peerAdd.peerId);
                discoverUserMap.put(peerAdd.peerId, userModel);
                if (nearbyCallBack != null) {
                    nearbyCallBack.onUserFound(userModel);
                }
            }

        });

        AppDataObserver.on().startObserver(ApiEvent.PEER_REMOVED, event -> {
            PeerRemoved peerRemoved = (PeerRemoved) event;

            discoverUserMap.remove(peerRemoved.peerId);
            if (nearbyCallBack != null) {
                MeshLog.e("[-] Direct User Removed: " + peerRemoved.peerId.substring(peerRemoved.peerId.length() - 3));
                nearbyCallBack.onDisconnectUser(peerRemoved.peerId);
            }
        });

        AppDataObserver.on().startObserver(ApiEvent.USER_INFO, event -> {
            UserInfoEvent userInfoEvent = (UserInfoEvent) event;

            MeshLog.e("user info found in app level");

            UserModel userModel = new UserModel();
            userModel.setUserId(userInfoEvent.getAddress());
            userModel.setUserName(userInfoEvent.getUserName());


            discoverUserMap.put(userModel.getUserId(), userModel);
            ChatDataProvider.On().upSertUser(userModel);
            if (nearbyCallBack != null) {
                MeshLog.e("[+] User Added");
                nearbyCallBack.onUserFound(userModel);
            }

        });


        AppDataObserver.on().startObserver(ApiEvent.SERVICE_UPDATE, event -> {
            ServiceUpdate serviceUpdate = (ServiceUpdate) event;
            MeshLog.e("Service update needed:" + serviceUpdate.isNeeded);
        });



        AppDataObserver.on().startObserver(ApiEvent.DATA, event -> {
            DataEvent dataEvent = (DataEvent) event;


            MeshLog.v("*** recieve frame! ***");
            try {
                String jsonString = new String(dataEvent.data).trim();
                MeshLog.mm("****link did recieved frame: ****: " + jsonString);
                JSONObject jo = new JSONObject(jsonString);
                MeshLog.v("linkDidReceiveFrame " + jsonString);
                int dataType = getDataType(jo);
                //  MeshLog.v("****link did recieved frame: ****" + dataType);
                //  MeshLog.mm("Message received type =" + dataType);
                switch (dataType) {
                    case JsonKeys.TYPE_USER_INFO:
                        UserModel userModel = UserModel.fromJSON(jo);
                        if (userModel == null) return;

                        // userModel.setUserId(sender); // Todo this line have to open if cause any issue related to sender and receiver

                        MeshLog.mm(" RECEIVED USER INFO => " + userModel.toString());

                        discoverUserMap.put(userModel.getUserId(), userModel);
                        ChatDataProvider.On().upSertUser(userModel);
                        if (nearbyCallBack != null) {
                            MeshLog.e("[+] User Added");
                            nearbyCallBack.onUserFound(userModel);
                        } else {
                            MeshLog.mm("Nearby call back object is null ");
                            HandlerUtil.postForeground(() -> Toast.makeText(ViperApp.getContext(), "Discovered ::  " +
                                    "" + userModel.getUserName(), Toast.LENGTH_SHORT).show());
                        }

                        //     viperClient.saveDiscoveredUserInfo(userModel.getUserId(), userModel.getUserName());

                        break;
                    case JsonKeys.TYPE_TEXT_MESSAGE:
                        MessageModel messageModel = MessageModel.getMessage(jo);
                        // insert the message into db
                        if (messageModel != null) {
                            UserModel userModel1 = discoverUserMap.get(messageModel.friendsId);
                            MeshLog.k("[Message saved in DB]");
                            messageModel.receiveTime = System.currentTimeMillis();
                            ChatDataProvider.On().insertMessage(messageModel, userModel1);
                            if (messageListener != null) {
                                messageListener.onMessageReceived(messageModel);
                            } else {
                                if (userModel1 != null) {
                                    HandlerUtil.postForeground(() -> Toast.makeText(ViperApp.getContext(), "From:  " +
                                            "" + userModel1.getUserName() + "\n" + "Text:   " + messageModel.message, Toast.LENGTH_SHORT).show());
                                }

                                MeshLog.k("MessageListener call back object is null ");
                            }
                        } else {
                            HandlerUtil.postForeground(() -> Toast.makeText(ViperApp.getContext(), "Empty Message model", Toast.LENGTH_SHORT).show());
                        }

                        break;

                    case JsonKeys.TYPE_REQ_USR_INFO:
                        MeshLog.v("****Recieve type TYPE_REQ_USR_INFO ****" + dataEvent.peerId);
                        HandlerUtil.postBackground(() -> sendMyInfo(dataEvent.peerId));
                        break;
                }
            } catch (JSONException e) {
                MeshLog.e("JSONException occurred at connection manager on linkDidReceiveFrame " + e.getMessage());
            }
        });

        AppDataObserver.on().startObserver(ApiEvent.DATA_ACKNOWLEDGEMENT, event -> {
            DataAckEvent dataAckEvent = (DataAckEvent) event;

            HandlerUtil.postForeground(() -> {
                if (dataAckEvent.status == Constant.MessageStatus.RECEIVED) {
                    ChatDataProvider.On().updateMessageAck(dataAckEvent.dataId, dataAckEvent.status);
                    if (messageListener != null) {
                        messageListener.onMessageDelivered();
                    }
                /*    if (bottomMessageListener != null) {
                        bottomMessageListener.onMessageReceived(dataAckEvent.dataId);
                    }*/
                } else if (dataAckEvent.status == Constant.MessageStatus.DELIVERED) {
                    int messageStatus = ChatDataProvider.On().getMessageStatus(dataAckEvent.dataId);
                    MeshLog.k("message status from app:: " + messageStatus);
                    if (messageStatus != Constant.MessageStatus.RECEIVED) {
                        ChatDataProvider.On().updateMessageAck(dataAckEvent.dataId, dataAckEvent.status);
                    }
                    if (messageListener != null) {
                        messageListener.onMessageDelivered();
                    }
                } else if (dataAckEvent.status == Constant.MessageStatus.SEND) {
                    if (requestUserInfoList.containsKey(dataAckEvent.dataId)) {
                        String nodeId = requestUserInfoList.get(dataAckEvent.dataId);
                        MeshLog.v("startAllObserver  nodeId " + nodeId);

                        UserModel userModel = ChatDataProvider.On().getUserInfoById(nodeId);
                        if (userModel == null) {
                            UserModel userModel1 = new UserModel();
                            userModel1.setUserId(nodeId);
                            userModel1.setUserName("Anonymous");

                            ChatDataProvider.On().insertUser(userModel1);
                            requestUserInfoList.remove(dataAckEvent.dataId);
                        } else {
                            // userModel = UserModel.buildUserTempData(nodeId);
                            requestUserInfoList.remove(dataAckEvent.dataId);
                        }
                    }

                    int messageStatus = ChatDataProvider.On().getMessageStatus(dataAckEvent.dataId);
                    if (messageStatus != Constant.MessageStatus.DELIVERED && messageStatus != Constant.MessageStatus.RECEIVED) {
                        ChatDataProvider.On().updateMessageAck(dataAckEvent.dataId, dataAckEvent.status);
                    }
                    if (messageListener != null) {
                        messageListener.onMessageDelivered();
                    }
                }
            });

        });

        AppDataObserver.on().startObserver(ApiEvent.PERMISSION_INTERRUPTION, event -> {


            PermissionInterruptionEvent permissionInterruptionEvent = (PermissionInterruptionEvent) event;
            if (permissionInterruptionEvent != null) {
                com.w3engineers.mesh.util.lib.mesh.HandlerUtil.postForeground(() -> showPermissionEventAlert(permissionInterruptionEvent.hardwareState, permissionInterruptionEvent.permissions, MeshApp.getCurrentActivity()));
            }
        });
    }
    public void showPermissionEventAlert(int hardwareEvent, List<String> permissions, Activity activity) {

        if (activity == null) return;
        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogView = inflater.inflate(R.layout.alert_hardware_permission, null);
        dialogBuilder.setView(dialogView);

        android.app.AlertDialog alertDialog = dialogBuilder.create();

        TextView title = dialogView.findViewById(R.id.interruption_title);
        TextView message = dialogView.findViewById(R.id.interruption_message);
        Button okay = dialogView.findViewById(R.id.okay_button);

        String finalTitle = "", finalMessage = "";

        boolean isPermission = false;

        if (permissions == null || permissions.isEmpty()) {

            String event = "";

            if (hardwareEvent == DataPlanConstants.INTERRUPTION_EVENT.USER_DISABLED_BT) {
                event = "Bluetooth";
            } else if (hardwareEvent == DataPlanConstants.INTERRUPTION_EVENT.USER_DISABLED_WIFI) {
                event = "Wifi";
            } else if (hardwareEvent == DataPlanConstants.INTERRUPTION_EVENT.LOCATION_PROVIDER_OFF) {
                event = "Location ";
            }

            if (!TextUtils.isEmpty(event)) {
                finalMessage = String.format(activity.getResources().getString(R.string.hardware_interruption), event);
                finalTitle = String.format(activity.getResources().getString(R.string.interruption_title), "Hardware");
            }

        } else {

            String event = "";
            for (String permission : permissions) {
                if (!TextUtils.isEmpty(permission)) {
                    if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        event = "Location";
                    } else if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        event = "Storage";
                    }
                }
            }

            if (!TextUtils.isEmpty(event)) {
                finalMessage = String.format(activity.getResources().getString(R.string.permission_interruption), event);
                finalTitle = String.format(activity.getResources().getString(R.string.interruption_title), "Permission");
            }

            isPermission = true;

        }

        boolean finalIsPermission = isPermission;
        okay.setOnClickListener(v -> {
            if (finalIsPermission) {
                DataManager.on().allowMissingPermission(permissions);
            }
            alertDialog.dismiss();
        });

        if (!TextUtils.isEmpty(finalTitle) && !TextUtils.isEmpty(finalMessage)) {
            title.setText(finalTitle);
            message.setText(finalMessage);

            alertDialog.show();
        }
    }

    private void sendMyInfo(String nodeId) {
        if (Text.isNotEmpty(nodeId)) {
            MeshLog.v(" Send info to => " + nodeId.substring(nodeId.length() - 3));

            UUID uniqueId = UUID.randomUUID();
            try {
                String userId = getUserId();
                String userJson = UserModel.getUserJson(userId);
                viperClient.sendMessage(userId, nodeId, uniqueId.toString(), userJson.getBytes(), false);
            } catch (Exception e) {
                e.printStackTrace();
                showToast(e.getMessage());
            }
        }

    }

    private MessageListener messageListener;

    public void initMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    private NearbyCallBack mNearbyCallBack;

    public void initNearByCallBackForChatActivity(NearbyCallBack nearbyCallBack) {
        this.mNearbyCallBack = nearbyCallBack;
    }

    private void reqUserInfo(String nodeId) {
        //TODO check whether it is okay or not send message only bl link or all

        String userJson = UserModel.buildUserInfoReqJson();
        if (nodeId == null || nodeId.length() < 3) {
            MeshLog.e(" Send info request  to.. =" + nodeId);
        } else {
            MeshLog.i(" Send info request  to.. =" + nodeId.substring(nodeId.length() - 3));
            UUID uniqueId = UUID.randomUUID();
            String messageId = uniqueId.toString();


            requestUserInfoList.put(messageId, nodeId);

            try {
                String userId = getUserId();
                viperClient.sendMessage(userId, nodeId, messageId, userJson.getBytes(), true);
            } catch (Exception e) {
                e.printStackTrace();
                showToast(e.getMessage());
            }
        }
    }

    public void sendMessage(String receiverId, MessageModel messageModel) {
        try {
            String userId = getUserId();
            String msgJson = MessageModel.buildMessage(messageModel, userId);
            viperClient.sendMessage(userId, receiverId, messageModel.messageId, msgJson.getBytes(), true);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(e.getMessage());
        }
    }

    public void checkConnectionStatus(String userId){
        viperClient.checkConnectionStatus(userId);
    }

    private int getDataType(JSONObject jo) {
        try {
            return jo.getInt(JsonKeys.KEY_DATA_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public <T> void initListener(T... type) {
        if (type == null) return;
        for (T item : type) {
            if (item instanceof NearbyCallBack) {
                nearbyCallBack = (NearbyCallBack) item;
                MeshLog.k("NearBy callback is init");
            } else if (item instanceof UserConnectionCallBack) {
                //   userConnectionCallBack = (UserConnectionCallBack) item;
                //  MeshLog.k("UserConnection  callback is init");
            }
        }
    }

    public List<UserModel> getUserList() {
        return new ArrayList<>(discoverUserMap.values());
    }

    public String getConnectionType(String nodeId) {

        int type = 0;
        try {
            type = viperClient.getLinkTypeById(nodeId);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(e.getMessage());
        }

        if (type == Link.Type.NA.getValue()) {
            return "Close";
        } else if (type == Link.Type.WIFI.getValue()) {
            return "WiFi";
        } else if (type == Link.Type.BT.getValue()) {
            return "BT";
        } else if (type == Link.Type.WIFI_MESH.getValue()) {
            return "WIFI MESH";
        } else if (type == Link.Type.BT_MESH.getValue()) {
            return "BT MESH";
        } else if (type == Link.Type.INTERNET.getValue()) {
            return "Internet";
        }else if (type == Link.Type.HB.getValue()) {
            return "HB";
        } else if (type == Link.Type.HB_MESH.getValue()) {
            return "HB MESH";
        }else {
            MeshLog.v("User Type Invalid");
        }
        return "P2P";
    }

    private String getUserId() {
        if (TextUtils.isEmpty(SharedPref.read(Constant.KEY_USER_ID))) {
            try {
                String userId = viperClient.getUserId();
                SharedPref.write(Constant.KEY_USER_ID, userId);
                return userId;
            } catch (Exception e) {
                e.printStackTrace();
                showToast(e.getMessage());
                return null;
            }
        } else {
            return SharedPref.read(Constant.KEY_USER_ID);
        }
    }

/*    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("config.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }*/

    private void showToast(String msg) {
        if (BuildConfig.DEBUG){
            HandlerUtil.postForeground(() -> Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show());
        }
    }

}
