package com.synergy.synergyet.notifications;

public class Data {
    private String user;
    private String largeIcon;
    private String body;
    private String title;
    private String sent;

    public Data(String user, String largeIcon, String body, String title, String sent) {
        this.user = user;
        this.largeIcon = largeIcon;
        this.body = body;
        this.title = title;
        this.sent = sent;
    }

    public Data() {}

    public String getUser() {
        return user;
    }

    public String getLargeIcon() {
        return largeIcon;
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public String getSent() {
        return sent;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setLargeIcon(String largeIcon) {
        this.largeIcon = largeIcon;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }
}
