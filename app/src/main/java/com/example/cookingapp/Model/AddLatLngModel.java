package com.example.cookingapp.Model;

public class AddLatLngModel {
    String title,lat,lng,id_user,name,id_ingredient,id_latlng;

    public AddLatLngModel() {
    }

    public AddLatLngModel(String title, String lat, String lng, String id_user, String name, String id_ingredient, String id_latlng) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.id_user = id_user;
        this.name = name;
        this.id_ingredient = id_ingredient;
        this.id_latlng = id_latlng;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId_ingredient() {
        return id_ingredient;
    }

    public void setId_ingredient(String id_ingredient) {
        this.id_ingredient = id_ingredient;
    }

    public String getId_latlng() {
        return id_latlng;
    }

    public void setId_latlng(String id_latlng) {
        this.id_latlng = id_latlng;
    }
}
