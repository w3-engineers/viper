package com.w3engineers.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TokenGuideLine {
    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("link")
    private List<String> linkList;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getLinkList() {
        return linkList;
    }

    public void setLinkList(List<String> linkList) {
        this.linkList = linkList;
    }
}
