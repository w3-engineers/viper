package com.w3engineers.mesh.ui.chat;

import com.w3engineers.mesh.model.MessageModel;

public interface MessageListener {
    void onMessageReceived(MessageModel message);
    void onMessageDelivered();
}
