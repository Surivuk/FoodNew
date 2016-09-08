package com.example.aleksandarx.foodfinder.socket;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.example.aleksandarx.foodfinder.network.FriendModel;
import com.example.aleksandarx.foodfinder.network.PersonModel;
import com.example.aleksandarx.foodfinder.settings.Connections;
import com.example.aleksandarx.foodfinder.share.UserPreferences;
import com.example.aleksandarx.foodfinder.sync.LocationController;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class SocketService extends Service {


    public static final String MY_ACTION = "MY_ACTION";
    public static final String MY_COMMAND = "test";
    //String liveServer = "http://192.168.1.15:8081/";//"https://food-finder-app.herokuapp.com/";

    public boolean socketConnected;
    private Socket mSocket;

    public static boolean isRunning = false;
    private SocketReceiver receiver;
    private BroadcastReceiver locationReceiver;
    public static String connectionID = "";

    private LocationController locationController;

    private Emitter.Listener onPlace = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONArray people = (JSONArray) args[0];

            Intent intent = new Intent();
            intent.setAction(MY_ACTION);
            intent.putExtra("type", 0);
            intent.putExtra("people", people.toString());

            sendBroadcast(intent);
        }
    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Intent intent = new Intent();
            intent.setAction(MY_ACTION);
            intent.putExtra("type", 1);
            connectionID = (String) args[0];
            intent.putExtra("ID", (String) args[0]);
            sendBroadcast(intent);
        }
    };
    private Emitter.Listener onFriendsUpdate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            Intent intent = new Intent();
            intent.setAction(MY_ACTION);
            intent.putExtra("type",4);
            try{
                JSONArray friends = (JSONArray) args[0];
                ArrayList<FriendModel> friendModels = new ArrayList<>();

                for(int i = 0 ; i < friends.length(); i++)
                    {
                    JSONObject friendJson = friends.getJSONObject(i);

                    FriendModel friendModel = new FriendModel(friendJson.getInt("ID"),friendJson.getDouble("lat"),friendJson.getDouble("lng"),friendJson.getString("username"));

                    friendModels.add(friendModel);

                }
                intent.putExtra("friends",friendModels);
                System.out.println(friends);
                sendBroadcast(intent);
            }
            catch(Exception ex)
            {
                System.out.println(ex);
            }



        }
    };
    @Override
    public void onCreate() {
        super.onCreate();

        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                double lat = intent.getDoubleExtra("lat",0);
                double lng = intent.getDoubleExtra("lng",0);
                int ID = intent.getIntExtra("ID",0);
                String username = intent.getStringExtra("username");

                String json = "{\"lat\":"+String.valueOf(lat)+",\"lng\":"+String.valueOf(lng)+",\"ID\":"+String.valueOf(ID)+",\"username\":\""+username+"\"}";

                mSocket.emit("userLocationUpdate", json, new Ack() {
                    @Override
                    public void call(Object... args) {
                        //response from location update list of friends and ID
                        try{
                            JSONArray friends = (JSONArray) args[0];
                            if(friends.length() == 0) return;

                            ArrayList<PersonModel> friendsModels = new ArrayList<PersonModel>();
                            for(int i = 0; i < friends.length(); i++)
                            {

                                JSONObject friend = friends.getJSONObject(i);
                                PersonModel friendModel = new PersonModel(friend.getString("username"),"NOTSET",friend.getDouble("lat"),friend.getDouble("lng"),friend.getInt("ID"));
                                friendsModels.add(friendModel);

                            }
                            Intent intent = new Intent();
                            intent.setAction(MY_ACTION);
                            intent.putExtra("type", 3);

                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList("friends",friendsModels);
                            intent.putExtras(bundle);
                            sendBroadcast(intent);
                        }
                        catch (Exception ex)
                        {
                            System.out.println(ex.getMessage());
                        }



                    }
                });
            }
        };
        locationController = LocationController.getLocationController(SocketService.this);

        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver,
                new IntentFilter("location-change"));

    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        locationController.startLocationController();
        if(!isRunning) {
            isRunning = true;

            receiver = new SocketReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Bundle bundle = intent.getExtras();
                    int code = bundle.getInt("type");
                    PersonModel data = bundle.getParcelable("person");
                    // rewriting it because activity can get disposed and lose this
                    if(data != null)
                        data.connectionID = connectionID;
                    switch(code) {
                        case 0:
                            //got person, should emit group leave.
                            mSocket.emit("leaveGroup", data, new Ack(){

                                @Override
                                public void call(Object... args) {
                                    Intent intent = new Intent();
                                    intent.setAction(MY_ACTION);
                                    intent.putExtra("type", 2);

                                    String response = (String) args[0];
                                    intent.putExtra("response", response);
                                    sendBroadcast(intent);
                                }
                            });
                            break;
                        case 1:
                            //got person should emit group join
                            mSocket.emit("joinGroup", data,new Ack(){

                                @Override
                                public void call(Object... args) {
                                    Intent intent = new Intent();
                                    intent.setAction(MY_ACTION);
                                    intent.putExtra("type", 2);
                                    String response = (String) args[0];
                                    intent.putExtra("response", response);

                                    sendBroadcast(intent);
                                }
                            });
                            break;

                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SocketService.MY_COMMAND);
            registerReceiver(receiver, intentFilter);

            try {
                mSocket = IO.socket(Connections.liveServerURL);
                socketConnected = true;
            }
            catch (URISyntaxException e) {
                mSocket = null;
                socketConnected = false;
            }

            if(mSocket != null) {
                mSocket.connect();
                mSocket.on("message", onMessage);
                mSocket.on("foodFriends", onPlace);
                String test = UserPreferences.getPreference(SocketService.this,UserPreferences.USER_USERNAME);
                mSocket.on(test,onFriendsUpdate);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        isRunning = false;
        mSocket.disconnect();

        mSocket.off("foodFriends",onPlace);
        mSocket.off("message", onMessage);
        locationController.stopLocationController();
    }

}
