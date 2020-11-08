package com.example.cookingapp.Model;

public class VipPointModel {
    String vip;
    int point;

    public VipPointModel() {
    }

    public VipPointModel(String vip, int point) {
        this.vip = vip;
        this.point = point;
    }

    public String getVip() {
        return vip;
    }

    public void setVip(String vip) {
        this.vip = vip;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }
}
