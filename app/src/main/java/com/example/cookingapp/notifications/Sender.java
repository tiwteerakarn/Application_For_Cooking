package com.example.cookingapp.notifications;

public class Sender {
    private Data5 data;
    private String to;

    public Sender() {
    }

    public Sender(Data5 data, String to) {
        this.data = data;
        this.to = to;
    }

    public Data5 getData() {
        return data;
    }

    public void setData(Data5 data) {
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
