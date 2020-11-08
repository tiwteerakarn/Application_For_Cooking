package com.example.cookingapp.notifications;

public class SenderAll {
    private Data5 data;
    private String[] registration_ids;

    public SenderAll() {
    }

    public SenderAll(Data5 data, String[] registration_ids) {
        this.data = data;
        this.registration_ids = registration_ids;
    }

    public Data5 getData() {
        return data;
    }

    public void setData(Data5 data) {
        this.data = data;
    }

    public String[] getRegistration_ids() {
        return registration_ids;
    }

    public void setRegistration_ids(String[] registration_ids) {
        this.registration_ids = registration_ids;
    }
}
