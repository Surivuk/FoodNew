package com.example.aleksandarx.foodfinder.view;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.aleksandarx.foodfinder.R;
import com.example.aleksandarx.foodfinder.network.controller.UserNetworkController;
import com.example.aleksandarx.foodfinder.settings.Connections;
import com.example.aleksandarx.foodfinder.share.UserPreferences;
import com.example.aleksandarx.foodfinder.view.model.FriendModel;
import com.example.aleksandarx.foodfinder.view.model.RestaurantModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TopRestaurantsActivity extends AppCompatActivity {

    private ListView friendList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.top_restaturants_layout);

        friendList = (ListView) findViewById(R.id.top_restaurants);

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        new NetworkTask().execute();
    }

    private void loadList(JSONArray data){
        try {
            List<RestaurantModel> rest = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject tmp = data.getJSONObject(i);
                int id = tmp.getInt("restaurant_id");
                String name = tmp.getString("restaurant_name");
                int likes = tmp.getInt("total_likes");
                rest.add(new RestaurantModel(id, name, likes));
            }
            if(!rest.isEmpty()){
                RestaurantListAdapter adapter = new RestaurantListAdapter(TopRestaurantsActivity.this, R.layout.friend_list_row, rest);
                friendList.setAdapter(adapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class NetworkTask extends AsyncTask<Void, Void, Void>{

        boolean isOk = true;
        JSONArray objects;

        @Override
        protected Void doInBackground(Void... params) {
            try{
                int id = Integer.parseInt(UserPreferences.getPreference(TopRestaurantsActivity.this, UserPreferences.USER_ID));
                objects = UserNetworkController.getFreinds(Connections.liveServerURL + "restaurantRanking", new JSONObject().put("", id).toString());
            }
            catch (Exception e){
                e.printStackTrace();
                isOk = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(isOk){
                if(objects != null){
                    loadList(objects);
                }
            }
        }
    }
}
