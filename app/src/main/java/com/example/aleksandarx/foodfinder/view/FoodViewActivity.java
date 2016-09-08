package com.example.aleksandarx.foodfinder.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aleksandarx.foodfinder.R;
import com.example.aleksandarx.foodfinder.data.model.FoodModel;
import com.example.aleksandarx.foodfinder.data.sqlite.DBAdapter;
import com.example.aleksandarx.foodfinder.network.controller.UserNetworkController;
import com.example.aleksandarx.foodfinder.settings.Connections;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class FoodViewActivity extends AppCompatActivity {


    private TextView articleName;
    private TextView articleFoodType;
    private TextView articleOrigin;
    private TextView articleMealType;
    private TextView articleLocation;
    private TextView articleLocationAddress;
    private TextView articleDescription;
    private ImageView img;
    private DBAdapter dbAdapter;
    private long id;
    private boolean isFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_view);

        articleName = (TextView) findViewById(R.id.article_name_holder);
        articleFoodType = (TextView) findViewById(R.id.article_food_type);
        articleOrigin = (TextView) findViewById(R.id.article_origin);
        articleMealType = (TextView) findViewById(R.id.article_meal_type);
        articleLocation = (TextView) findViewById(R.id.article_location);
        articleLocationAddress = (TextView) findViewById(R.id.article_location_address);
        articleDescription = (TextView) findViewById(R.id.article_description);
        img = (ImageView) findViewById(R.id.food_image_view);

        dbAdapter = DBAdapter.createAdapter(FoodViewActivity.this);


        Intent invokingIntent = getIntent();
        Bundle personBundle = invokingIntent.getExtras();
        id = 0;
        isFriend = false;
        if(personBundle != null){
            id = personBundle.getLong("id");
        }

        if(getIntent().hasExtra("friend")){
            getOverflowMenu();
            isFriend = true;
        }

        Picasso.with(FoodViewActivity.this)
                .load("https://food-finder-app.herokuapp.com/image?id=" + id)
                .placeholder(R.drawable.ic_cached_black_24dp)
                .error(R.drawable.ic_do_not_disturb_black_24dp)
                .into(img);

        new NetworkThread().execute();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if(isFriend) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.like_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.like_button:
                new Like().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class Like extends AsyncTask<Void, Void, Void> {
        boolean isOk = true;
        boolean isLiked;


        @Override
        protected Void doInBackground(Void... params) {
            try{
                 isLiked = UserNetworkController.like(Connections.liveServerURL + "likeArticle", new JSONObject().put("id", id).toString());
            } catch (Exception e){
                e.printStackTrace();
                isOk = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(isOk){
                if(isLiked)
                 Toast.makeText(FoodViewActivity.this, "Liked", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(FoodViewActivity.this, "Connection error!", Toast.LENGTH_SHORT).show();
        }
    }

    private class NetworkThread extends AsyncTask<Void, Void, Void> {

        boolean isOk = true;
        FoodModel model;

        @Override
        protected void onPreExecute() {
            model = new FoodModel();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                JSONArray objects = UserNetworkController.getArticles(Connections.liveServerURL + "getArticle?id=" + id);
                for(int i = 0; i < objects.length(); i++){
                    JSONObject tmp = objects.getJSONObject(i);
                    model.addItem("articleName", tmp.getString("article_name"));
                    model.addItem("articleDescription", tmp.getString("article_description"));
                    model.addItem("origin", tmp.getString("article_origin"));
                    model.addItem("foodType", tmp.getString("food_type"));
                    model.addItem("mealType", tmp.getString("meal_type"));
                    model.addItem("locationName", tmp.getString("restaurant_name"));
                    model.addItem("locationAddress", tmp.getString("restaurant_address"));
                }
            } catch (Exception e){
                e.printStackTrace();
                isOk = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(isOk){
                articleName.setText(model.getItem("articleName"));
                articleDescription.setText(model.getItem("articleDescription"));
                articleOrigin.setText(model.getItem("origin"));
                articleFoodType.setText(model.getItem("foodType"));
                articleMealType.setText(model.getItem("mealType"));
                articleLocation.setText(model.getItem("locationName"));
                articleLocationAddress.setText(model.getItem("locationAddress"));
            }
            else {
                Toast.makeText(FoodViewActivity.this, "Connection error!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
