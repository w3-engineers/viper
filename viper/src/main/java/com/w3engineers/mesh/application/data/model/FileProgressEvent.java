package com.w3engineers.mesh.application.data.model;

public class FileProgressEvent extends Event {
   private String fileMessageId;
   private int percentage;

    public String getFileMessageId() {
        return fileMessageId;
    }

    public void setFileMessageId(String fileMessageId) {
        this.fileMessageId = fileMessageId;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
}
