package com.example.aleksandarx.foodfinder.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.aleksandarx.foodfinder.R;
import com.example.aleksandarx.foodfinder.bluetooth.BluetoothMainActivity;
import com.example.aleksandarx.foodfinder.bluetooth.BluetoothModel;
import com.example.aleksandarx.foodfinder.data.model.PlaceModel;
import com.example.aleksandarx.foodfinder.network.FriendModel;
import com.example.aleksandarx.foodfinder.network.PersonModel;
import com.example.aleksandarx.foodfinder.share.UserPreferences;
import com.example.aleksandarx.foodfinder.socket.SocketReceiver;
import com.example.aleksandarx.foodfinder.socket.SocketService;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private MapClass myMap;
    private Context context;
    private HashMap<Integer,PersonModel> friends;
    private BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            double lat = intent.getExtras().getDouble("lat");
            double lng = intent.getExtras().getDouble("lng");

            System.out.println("Got message: " + lat + ", " + lng);
            if(myMap != null)
                myMap.changeMyPin(new LatLng(lat, lng));
        }
    };

    private Intent btServicel;

    private BroadcastReceiver mFriendReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if(intent.hasExtra("ID")){
                final int id = intent.getExtras().getInt("ID");
                String username = intent.getExtras().getString("username");
                System.out.println("Got message: " + username + ", " + id);
                Runnable yes = new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent("friend-req-answer");
                        i.putExtra("Answer", 'Y');
                        i.putExtra("mId", Integer.parseInt(UserPreferences.getPreference(MainActivity.this, UserPreferences.USER_ID)));
                        i.putExtra("fId", id);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(i);
                    }
                };

                Runnable no = new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent("friend-req-answer");
                        i.putExtra("Answer", 'N');
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(i);
                    }
                };
                DialogComponent.permissionDialog(MainActivity.this, "Friend request", username + " what to be friend with you.", "Yes", "No", yes, no).show();
            }

            if(intent.hasExtra("valid")){
                boolean valid = intent.getExtras().getBoolean("valid");
                String toasText;
                if(valid)
                    toasText = "Successfully added friend!";
                else
                    toasText = "No added friend!";
                Toast.makeText(MainActivity.this, toasText, Toast.LENGTH_SHORT).show();
            }

        }
    };

    private SocketReceiver SocketReceiver = new SocketReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type",-7);
            Bundle bundle = intent.getExtras();
            if(type == 3)
            {
                ArrayList<PersonModel> data = bundle.getParcelableArrayList("friends");
                if(data == null)
                {
                    System.out.println("Received friends update but its empty!");
                    return;
                }
                for(int i = 0 ; i < data.size(); i++)
                {
                    PersonModel friend = data.get(i);
                    friends.put(friend.ID,friend);
                    myMap.tryAddFriend(friend.ID,friend.connectionID,friend.latitude,friend.longitude);
                    System.out.println(friend.connectionID);

                }
            }
            // friends update!!!
            else if(type == 4)
            {
                ArrayList<FriendModel> friends = bundle.getParcelableArrayList("friends");
                for(int i = 0; i < friends.size(); i++)
                {
                    FriendModel currentModel = friends.get(i);
                    myMap.tryAddFriend(currentModel.ID, currentModel.username, currentModel.lat, currentModel.lng);
                }
            }
            //restaurant update
            else if(type == 5)
            {
                ArrayList<PlaceModel> places = bundle.getParcelableArrayList("restaurants");
                if(places == null)
                {
                    Log.i("MainActivity","BCAST type 5! Received restaurants update but its empty!");
                    return;
                }

                for(int i = 0; i < places.size(); i++)
                {
                        PlaceModel place = places.get(i);
                        myMap.tryAddplace(place.id,place.name+"\n"+place.address,place.latitude,place.longitude);
                }



            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;

        friends = new HashMap<>();

        myMap = new MapClass(R.id.map, MainActivity.this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(fab != null)
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, BluetoothActivity.class));
                }
            });

        Intent invokingIntent = getIntent();
        Bundle personBundle = invokingIntent.getExtras();
        if(personBundle != null)
        {

            String personID = personBundle.getString("personsID");
            if(personID == null)
            {
                int type = personBundle.getInt("type");
                if(type == -1)
                {
                    //restaurants
                    /*ArrayList<PlaceModel> places = personBundle.getParcelableArrayList("restaurants");
                    for(int i = 0; i < places.size(); i++)
                    {
                        PlaceModel place = places.get(i);
                        myMap.tryAddplace(place.id,place.name+"\n"+place.address,place.latitude,place.longitude);
                    }*/

                }
            }
            else{
                double personLat = personBundle.getDouble("personsLat");
                double personLng = personBundle.getDouble("personsLng");

                myMap.addPersonMarker(personID,personLat,personLng);
            }

        }
        //btServicel = new Intent(MainActivity.this, BackgroundService.class);
    }

    @Override
    protected void onStart() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationReceiver, new IntentFilter("location-change"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mFriendReceiver, new IntentFilter("friend-req"));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SocketService.MY_ACTION);
        registerReceiver(SocketReceiver, intentFilter);
        startService(btServicel);
        super.onStart();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFriendReceiver);
        unregisterReceiver(SocketReceiver);
        stopService(btServicel);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();

                builder.setView(inflater.inflate(R.layout.about_us_dialog, null))
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder.show();
                return true;
            case R.id.menu_places:
                Intent i = new Intent(MainActivity.this, FoodArticlesActivity.class);
                startActivity(i);
                return true;
            case R.id.menu_sign_out:
                UserPreferences.removePreference(MainActivity.this, UserPreferences.USER_USERNAME);
                UserPreferences.removePreference(MainActivity.this, UserPreferences.USER_ID);
                stopService(new Intent(MainActivity.this,SocketService.class));
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return true;
            case R.id.menu_bluetooth:
                Intent intent = new Intent(MainActivity.this,BluetoothMainActivity.class);
                intent.putExtra("food",new BluetoothModel(43.2,23.1,"Na cose","ItalianFood"));
                startActivity(intent);
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.menu_find_people:
                startActivity(new Intent(MainActivity.this, SocketActivity.class));
                return true;
            case R.id.menu_view_profile:
                startActivity(new Intent(MainActivity.this, ProfileViewActivity.class));
                return true;
            case R.id.menu_friend_list:
                startActivity(new Intent(MainActivity.this, ViewFriendsActivity.class));
                return true;
            case R.id.menu_top_restaurant:
                startActivity(new Intent(MainActivity.this, TopRestaurantsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
