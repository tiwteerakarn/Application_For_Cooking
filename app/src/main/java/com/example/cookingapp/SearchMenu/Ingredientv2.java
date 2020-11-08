package com.example.cookingapp.SearchMenu;

public class Ingredientv2 {
    String id,name,user;

    public Ingredientv2() {
    }

    public Ingredientv2(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Ingredientv2(String id, String name, String user) {
        this.id = id;
        this.name = name;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
