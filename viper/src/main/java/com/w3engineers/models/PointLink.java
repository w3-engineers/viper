package com.w3engineers.models;

import com.google.gson.annotations.SerializedName;

public class PointLink {
    @SerializedName("link")
    private String link;

    @SerializedName("header")
    private String header;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
