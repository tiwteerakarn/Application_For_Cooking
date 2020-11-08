package com.example.cookingapp.Model;

import java.util.List;

public class Ingredients {
    String name;
    List<String> ingredients;

    public Ingredients(String name, List<String> ingredients) {
        this.name = name;
        this.ingredients = ingredients;
    }

    public Ingredients(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }
}
