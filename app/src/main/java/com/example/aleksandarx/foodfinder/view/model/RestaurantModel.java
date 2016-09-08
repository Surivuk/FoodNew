package com.example.aleksandarx.foodfinder.view.model;

/**
 * Created by EuroPATC on 9/8/2016.
 */
public class RestaurantModel {

    public String restaurant_name;
    public int restaurant_id;
    public int total_likes;

    public RestaurantModel(int restaurant_id, String restaurant_name, int total_likes) {
        this.restaurant_id = restaurant_id;
        this.restaurant_name = restaurant_name;
        this.total_likes = total_likes;
    }
}
