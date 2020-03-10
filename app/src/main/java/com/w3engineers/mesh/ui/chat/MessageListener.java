package com.w3engineers.mesh.ui.chat;

import com.w3engineers.mesh.model.MessageModel;

public interface MessageListener {
    void onMessageReceived(MessageModel message);

    void onMessageDelivered();

    void onFileProgressReceived(String fileMessageId, int progress);

    void onFileTransferEvent(String fileMessageId, boolean isSuccess);

}
