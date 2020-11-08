package com.example.cookingapp.Model;

public class LatLngModel {
    String title,lat,lng,id_user;


    public LatLngModel() {
    }

    public LatLngModel(String title, String lat, String lng) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
    }

    public LatLngModel(String title, String lat, String lng, String id_user) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.id_user = id_user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }
}
