package com.example.cookingapp.Admin;

public class Report_model {
    String detail,id_food,id_user,key;

    public Report_model() {
    }

    public Report_model(String detail, String id_food, String id_user) {
        this.detail = detail;
        this.id_food = id_food;
        this.id_user = id_user;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getId_food() {
        return id_food;
    }

    public void setId_food(String id_food) {
        this.id_food = id_food;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
