package com.w3engineers.mesh.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.w3engineers.ext.strom.util.Text;
import com.w3engineers.ext.viper.BuildConfig;
import com.w3engineers.ext.viper.ViperApp;
import com.w3engineers.mesh.application.data.ApiEvent;
import com.w3engineers.mesh.application.data.AppDataObserver;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.model.DataAckEvent;
import com.w3engineers.mesh.application.data.model.DataEvent;
import com.w3engineers.mesh.application.data.model.PeerAdd;
import com.w3engineers.mesh.application.data.model.PeerRemoved;
import com.w3engineers.mesh.application.data.model.UserInfoEvent;
import com.w3engineers.mesh.model.MessageModel;
import com.w3engineers.mesh.model.UserModel;
import com.w3engineers.mesh.ui.Nearby.NearbyCallBack;
import com.w3engineers.mesh.ui.Nearby.UserConnectionCallBack;
import com.w3engineers.mesh.ui.chat.ChatDataProvider;
import com.w3engineers.mesh.ui.chat.MessageListener;
import com.w3engineers.mesh.util.lib.mesh.ViperClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConnectionManager {
    private static final String NETWORK_PREFIX = "arif";
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
            String jsonData = loadJSONFromAsset(mContext);
            if (!TextUtils.isEmpty(jsonData)) {

                JSONObject jsonObject = new JSONObject(jsonData);

                String AUTH_USER_NAME = jsonObject.optString("AUTH_USER_NAME");
                String AUTH_PASSWORD = jsonObject.optString("AUTH_PASSWORD");
                String FILE_REPO_LINK = jsonObject.optString("FILE_REPO_LINK");
                String PARSE_APP_ID = jsonObject.optString("PARSE_APP_ID");
                String PARSE_URL = jsonObject.optString("PARSE_URL");

                /*String AUTH_USER_NAME = BuildConfig.AUTH_USER_NAME;
                String AUTH_PASSWORD = BuildConfig.AUTH_PASSWORD;
                String FILE_REPO_LINK = BuildConfig.FILE_REPO_LINK;
                String PARSE_APP_ID = BuildConfig.PARSE_APP_ID;
                String PARSE_URL = BuildConfig.PARSE_URL;*/

//                String GIFT_DONATE_LINK = jsonObject.optString("GIFT_DONATE_LINK");

                viperClient = ViperClient.on(mContext, APP_NAME, "com.w3engineers.ext.viper", NETWORK_PREFIX, SharedPref.read(Constant.KEY_USER_NAME),
                        SharedPref.read(Constant.PreferenceKeys.ADDRESS), SharedPref.read(Constant.PreferenceKeys.PUBLIC_KEY), 1, System.currentTimeMillis(), true)
                        .setConfig(AUTH_USER_NAME, AUTH_PASSWORD, FILE_REPO_LINK/*, GIFT_DONATE_LINK*/, PARSE_URL, PARSE_APP_ID);

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

    public String loadJSONFromAsset(Context context) {
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

    }

    private void showToast(String msg) {
        if (BuildConfig.DEBUG){
            HandlerUtil.postForeground(() -> Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show());
        }
    }

}
