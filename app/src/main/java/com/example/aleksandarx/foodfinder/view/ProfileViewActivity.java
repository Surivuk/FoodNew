package com.example.aleksandarx.foodfinder.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aleksandarx.foodfinder.R;
import com.example.aleksandarx.foodfinder.bluetooth.BluetoothMainActivity;
import com.example.aleksandarx.foodfinder.bluetooth.BluetoothModel;
import com.example.aleksandarx.foodfinder.network.controller.UserNetworkController;
import com.example.aleksandarx.foodfinder.share.UserPreferences;
import com.example.aleksandarx.foodfinder.socket.SocketService;
import com.example.aleksandarx.foodfinder.view.logger.Log;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileViewActivity extends AppCompatActivity {

    private JSONObject data;
    private boolean toogle;

    private TextView email;
    private TextView password;
    private TextView firstName;
    private TextView lastName;
    private TextView phone;
    private ImageView img;
    private boolean isFriend = false;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        if(getIntent().hasExtra("friend")){
            id = getIntent().getExtras().getInt("friend");
            isFriend = true;
        }
        else
            id = Integer.parseInt(UserPreferences.getPreference(ProfileViewActivity.this, UserPreferences.USER_ID));

        email = (TextView) findViewById(R.id.profile_view_username_view_text);
        password = (TextView) findViewById(R.id.profile_view_password_view_text);
        firstName = (TextView) findViewById(R.id.profile_view_first_name_view_text);
        lastName = (TextView) findViewById(R.id.profile_view_last_name_view_text);
        phone = (TextView) findViewById(R.id.profile_view_phone_number_view_text);
        img = (ImageView) findViewById(R.id.profile_view_profile_image);

        if(isFriend){
            password.setVisibility(View.INVISIBLE);
        }
        else
            password.setVisibility(View.VISIBLE);

        Picasso.with(ProfileViewActivity.this)
                .load("https://food-finder-app.herokuapp.com/profileImage?id=" + id)
                .placeholder(R.drawable.ic_cached_black_24dp)
                .error(R.drawable.ic_do_not_disturb_black_24dp)
                .into(img);

        new MyThread().execute();

        toogle = false;
        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toogle) {
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    toogle = false;
                }
                else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    toogle = true;
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if(isFriend) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.friend_profile_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.view_friend_articles_menu:
                Intent i = new Intent(ProfileViewActivity.this, FoodArticlesActivity.class);
                i.putExtra("friend", id);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class MyThread extends AsyncTask<Void, Void, Void>{

        JSONObject inputJson;

        @Override
        protected void onPreExecute() {
            try {
                inputJson = new JSONObject();
                inputJson.put("id", id);
            } catch (JSONException e) {
                e.printStackTrace();
                inputJson = null;
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if(inputJson != null)
                    data = UserNetworkController.getProfileInfo("https://food-finder-app.herokuapp.com/profile", inputJson.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                data = null;
            } catch (IOException e) {
                e.printStackTrace();
                data = null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(data != null){
                try {
                    email.setText(data.getString("user_username"));
                    if(!isFriend)
                        password.setText(data.getString("user_password"));
                    firstName.setText(data.getString("user_first_name"));
                    lastName.setText(data.getString("user_last_name"));
                    phone.setText(data.getString("user_phone_number"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                Log.d("NETWORK", "data is NULL");
            }
        }
    }
}
