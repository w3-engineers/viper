package com.w3engineers.mesh.util;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.w3engineers.ext.strom.util.Text;
import com.w3engineers.ext.viper.ViperApp;
import com.w3engineers.mesh.application.data.ApiEvent;
import com.w3engineers.mesh.application.data.AppDataObserver;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.model.DataAckEvent;
import com.w3engineers.mesh.application.data.model.DataEvent;
import com.w3engineers.mesh.application.data.model.PeerAdd;
import com.w3engineers.mesh.application.data.model.PeerRemoved;
import com.w3engineers.mesh.model.Message;
import com.w3engineers.mesh.model.MessageModel;
import com.w3engineers.mesh.model.UserModel;
import com.w3engineers.mesh.ui.Nearby.NearbyCallBack;
import com.w3engineers.mesh.ui.Nearby.UserConnectionCallBack;
import com.w3engineers.mesh.ui.chat.ChatDataProvider;
import com.w3engineers.mesh.ui.chat.MessageListener;
import com.w3engineers.mesh.util.lib.mesh.ViperClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConnectionManager {
    private static final String NETWORK_PREFIX = "ifli";
    private static ConnectionManager mConnectionManager;
    private ViperClient viperClient;
    private static Context mContext;
    private NearbyCallBack nearbyCallBack;
    private Map<String, UserModel> discoverUserMap;

    private Map<String, String> requestUserInfoList;


    public static ConnectionManager on(Context context) {
        if (mConnectionManager == null) {
            synchronized (ViperClient.class) {
                if (mConnectionManager == null)
                    mConnectionManager = new ConnectionManager(context, NETWORK_PREFIX);
            }
        }
        return mConnectionManager;
    }

    private ConnectionManager(Context context, String networkPrefix) {
        mContext = context;
        discoverUserMap = Collections.synchronizedMap(new HashMap());
        requestUserInfoList = Collections.synchronizedMap(new HashMap<>());

        viperClient = ViperClient.on(context, networkPrefix);

        startAllObserver();
    }

    private void startAllObserver() {

        AppDataObserver.on().startObserver(ApiEvent.PEER_ADD, event -> {
            PeerAdd peerAdd = (PeerAdd) event;

            sendMyInfo(peerAdd.peerId);

            UserModel userModel = new UserModel();
            userModel.setUserName("Anonymous");
            userModel.setUserId(peerAdd.peerId);
            discoverUserMap.put(peerAdd.peerId, userModel);
            if (nearbyCallBack != null) {
                nearbyCallBack.onUserFound(userModel);
            }

/*            boolean isUserExist = ChatDataProvider.On().checkUserExistence(peerAdd.peerId);
            if (isUserExist) {
                UserModel userModel = ChatDataProvider.On().getUserInfoById(peerAdd.peerId);
                discoverUserMap.put(peerAdd.peerId, userModel);
                if (nearbyCallBack != null) {
                    nearbyCallBack.onUserFound(userModel);
                }
            } else {
                reqUserInfo(peerAdd.peerId);

                UserModel userModel = new UserModel();
                userModel.setUserName("Anonymous");
                userModel.setUserId(peerAdd.peerId);
                discoverUserMap.put(peerAdd.peerId, userModel);
                if (nearbyCallBack != null) {
                    nearbyCallBack.onUserFound(userModel);
                }
            }*/

        });

        AppDataObserver.on().startObserver(ApiEvent.PEER_REMOVED, event -> {
            PeerRemoved peerRemoved = (PeerRemoved) event;
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

        });
    }

    private void sendMyInfo(String nodeId) {
        if (Text.isNotEmpty(nodeId)) {
            MeshLog.v(" Send info to => " + nodeId.substring(nodeId.length() - 3));

            UUID uniqueId = UUID.randomUUID();
            try {
                String userId = getUserId();
                String userJson = UserModel.getUserJson(userId);
                viperClient.sendMessage(userId, nodeId, uniqueId.toString(), userJson.getBytes());
            } catch (RemoteException e) {
                e.printStackTrace();
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
                viperClient.sendMessage(userId, nodeId, messageId, userJson.getBytes());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String receiverId, MessageModel messageModel) {
        try {
            String userId = getUserId();
            String msgJson = MessageModel.buildMessage(messageModel, userId);
            viperClient.sendMessage(userId, receiverId, messageModel.messageId, msgJson.getBytes());
        } catch (RemoteException e) {
            e.printStackTrace();
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
        } catch (RemoteException e) {
            e.printStackTrace();
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
            } catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return SharedPref.read(Constant.KEY_USER_ID);
        }
    }

}