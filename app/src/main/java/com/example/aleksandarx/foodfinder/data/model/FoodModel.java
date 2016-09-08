package com.example.aleksandarx.foodfinder.data.model;

import java.util.HashMap;

/**
 * Created by aleksandarx on 7/2/16.
 */
public class FoodModel {

    private long article_id;
    private long db_id;
    private String article_location;
    private long article_location_id;
    private String article_location_name;
    private String article_name;
    private String article_description;
    private String meal_type;
    private String article_origin;
    private String article_image;

    public static String[] FIELDS = {"user_id", "articleName", "articleDescription", "isFood", "origin", "foodType", "mealType", "locationName", "locationAddress", "place_id", "locationLat", "locationLng"};

    private HashMap<String, String> attributes;

    public FoodModel() {
        this.attributes = new HashMap<>();
        article_image = null;
    }

    public void addItem(String attributeName, String attributeValue){
        for(int i = 0; i < FIELDS.length; i++){
            if(FIELDS[i].equals(attributeName)){
                attributes.put(attributeName, attributeValue);
            }
        }
    }

    public String getItem(String attributeName){
        if(attributes.containsKey(attributeName)){
            return attributes.get(attributeName);
        }
        else
            return "ERROR";
    }

    public FoodModel(long article_id, String article_location, long article_location_id, String article_location_name, String article_name, String article_description, String meal_type, String article_origin, String article_image) {
        this.article_id = article_id;
        this.article_location = article_location;
        this.article_location_id = article_location_id;
        this.article_location_name = article_location_name;
        this.article_name = article_name;
        this.article_description = article_description;
        this.meal_type = meal_type;
        this.article_origin = article_origin;
        this.article_image = article_image;
    }

    public void setDb_id(long db_id) {
        this.db_id = db_id;
    }

    public long getDb_id() {

        return db_id;
    }

    public long getArticle_id() {
        return article_id;
    }

    public String getArticle_location() {
        return article_location;
    }

    public long getArticle_location_id() {
        return article_location_id;
    }

    public String getArticle_location_name() {
        return article_location_name;
    }

    public String getArticle_name() {
        return article_name;
    }

    public String getArticle_description() {
        return article_description;
    }

    public String getMeal_type() {
        return meal_type;
    }

    public String getArticle_origin() {
        return article_origin;
    }

    public String getArticle_image() {
        return article_image;
    }

    public void setArticle_id(long article_id) {
        this.article_id = article_id;
    }

    public void setArticle_location(String article_location) {
        this.article_location = article_location;
    }

    public void setArticle_location_id(long article_location_id) {
        this.article_location_id = article_location_id;
    }

    public void setArticle_location_name(String article_location_name) {
        this.article_location_name = article_location_name;
    }

    public void setArticle_name(String article_name) {
        this.article_name = article_name;
    }

    public void setArticle_description(String article_description) {
        this.article_description = article_description;
    }

    public void setMeal_type(String meal_type) {
        this.meal_type = meal_type;
    }

    public void setArticle_origin(String article_origin) {
        this.article_origin = article_origin;
    }

    public void setArticle_image(String article_image) {
        this.article_image = article_image;
    }

    @Override
    public String toString() {
        return article_name;//this.getItem("articleName");
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof FoodModel){
            FoodModel tmp = (FoodModel) o;
            if(article_id == tmp.getArticle_id())
                return true;
        }
        return false;
    }
}


