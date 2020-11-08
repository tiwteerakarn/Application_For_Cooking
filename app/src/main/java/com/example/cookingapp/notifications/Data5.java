package com.example.cookingapp.notifications;

public class Data5 {
    private String user , title , sent ,body;

    public Data5() {
    }

    public Data5(String user, String title, String sent, String body) {
        this.user = user;
        this.title = title;
        this.sent = sent;
        this.body = body;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
