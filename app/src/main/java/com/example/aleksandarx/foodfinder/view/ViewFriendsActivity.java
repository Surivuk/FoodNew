package com.example.aleksandarx.foodfinder.view;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.aleksandarx.foodfinder.R;
import com.example.aleksandarx.foodfinder.data.model.FoodModel;
import com.example.aleksandarx.foodfinder.network.controller.UserNetworkController;
import com.example.aleksandarx.foodfinder.settings.Connections;
import com.example.aleksandarx.foodfinder.share.UserPreferences;
import com.example.aleksandarx.foodfinder.view.model.FriendModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class ViewFriendsActivity extends AppCompatActivity {

    private ListView friendList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friends);

        friendList = (ListView) findViewById(R.id.friend_list);

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ViewFriendsActivity.this, ProfileViewActivity.class);
                int fId = ((FriendModel) parent.getAdapter().getItem(position)).id;
                System.out.println("friend: " + fId);
                i.putExtra("friend", fId);
                startActivity(i);
            }
        });

        new NetworkTask().execute();
    }

    private void loadList(JSONArray data){
        try {
            List<FriendModel> mFriend = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject tmp = data.getJSONObject(i);
                int id = tmp.getInt("id");
                String firstName = tmp.getString("userName");
                String lastName = tmp.getString("lastName");
                mFriend.add(new FriendModel(id, firstName, lastName));
            }
            if(!mFriend.isEmpty()){
                FriendListAdapter adapter = new FriendListAdapter(ViewFriendsActivity.this, R.layout.friend_list_row, mFriend);
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
                int id = Integer.parseInt(UserPreferences.getPreference(ViewFriendsActivity.this, UserPreferences.USER_ID));
                objects = UserNetworkController.getFreinds(Connections.liveServerURL + "myFriends", new JSONObject().put("requestingUser", id).toString());
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
