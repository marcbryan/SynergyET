package com.synergy.synergyet.model;

public class ChatUser {
    private String uid;
    private String displayName;
    private String imageURL;

    public ChatUser(String uid, String displayName, String imageURL) {
        this.uid = uid;
        this.displayName = displayName;
        this.imageURL = imageURL;
    }

    public ChatUser(){}

    public String getUid() {
        return uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
