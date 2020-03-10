package com.w3engineers.mesh.application.data.model;

public class FileReceivedEvent extends Event {
    private String fileMessageId;
    private String filePath;
    private String sourceAddress;

    public String getFileMessageId() {
        return fileMessageId;
    }

    public void setFileMessageId(String fileMessageId) {
        this.fileMessageId = fileMessageId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }
}
