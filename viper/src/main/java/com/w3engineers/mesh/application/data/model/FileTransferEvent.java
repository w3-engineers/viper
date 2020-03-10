package com.w3engineers.mesh.application.data.model;

public class FileTransferEvent extends Event {
    private String fileMessageId;
    private boolean isSuccess;

    public String getFileMessageId() {
        return fileMessageId;
    }

    public void setFileMessageId(String fileMessageId) {
        this.fileMessageId = fileMessageId;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
