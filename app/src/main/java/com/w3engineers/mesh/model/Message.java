package com.w3engineers.mesh.model;



/*
 *  ****************************************************************************
 *  * Created by : Md. Azizul Islam on 11/29/2018 at 6:57 PM.
 *  * Email : azizul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md. Azizul Islam on 11/29/2018.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

import com.w3engineers.mesh.util.JsonKeys;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {

    public long id = 0;
    public String messageId;
    public String message;
    public long time;
    public boolean incoming;
    public String senderId;
    public String receiverId;

    public static String buildMessage(Message message){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JsonKeys.KEY_DATA_TYPE, JsonKeys.TYPE_TEXT_MESSAGE);
            jsonObject.put(JsonKeys.KEY_MESSAGE_ID, message.senderId);
            jsonObject.put(JsonKeys.KEY_RECEIVER_ID, message.receiverId);
            jsonObject.put(JsonKeys.KEY_SENDER_ID, message.senderId);
            jsonObject.put(JsonKeys.KEY_MESSAGE, message.message);
            return jsonObject.toString();
        }catch (JSONException e){}

        return null;
    }

    public static Message getMessage(JSONObject jo){
        try {
            String message = jo.getString(JsonKeys.KEY_MESSAGE);
            String messageId = jo.getString(JsonKeys.KEY_MESSAGE_ID);
            String receiverId = jo.getString(JsonKeys.KEY_RECEIVER_ID);
            String senderId = jo.getString(JsonKeys.KEY_SENDER_ID);

            Message msg = new Message();
            msg.message = message;
            msg.messageId = messageId;
            msg.senderId = senderId;
            msg.receiverId = receiverId;
            msg.incoming = true;
            return msg;
        }catch (JSONException e){}

        return null;
    }

}
