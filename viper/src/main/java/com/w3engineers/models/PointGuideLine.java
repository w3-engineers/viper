package com.w3engineers.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PointGuideLine {
    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("point_link")
    private List<PointLink> pointLinks;

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

    public List<PointLink> getPointLinks() {
        return pointLinks;
    }

    public void setPointLinks(List<PointLink> pointLinks) {
        this.pointLinks = pointLinks;
    }
}
