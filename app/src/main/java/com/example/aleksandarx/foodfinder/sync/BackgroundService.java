package com.example.aleksandarx.foodfinder.sync;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import com.example.aleksandarx.foodfinder.bluetooth.BluetoothServer;
import com.example.aleksandarx.foodfinder.network.HttpHelper;
import com.example.aleksandarx.foodfinder.network.controller.UserNetworkController;
import com.example.aleksandarx.foodfinder.settings.Connections;
import com.example.aleksandarx.foodfinder.share.UserPreferences;
import com.example.aleksandarx.foodfinder.view.DialogComponent;
import com.example.aleksandarx.foodfinder.view.logger.Log;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.util.Arrays;

/**
 * Created by Darko on 03.07.2016.
 */
public class BackgroundService extends Service {

    public boolean isRunning;
    private Context context;
    private IBinder mBinder = new LocalBinder();
    private BluetoothServer bServer;
    private Handler handler;
    private Intent friendIntent;

    public class LocalBinder extends Binder {
        public BackgroundService getServerInstance() {
            return BackgroundService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private BroadcastReceiver mFriendRequestAnswerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            char flag = intent.getExtras().getChar("Answer");
            int mId = intent.getExtras().getInt("mId");
            int fId = intent.getExtras().getInt("fId");
            if(flag == 'Y'){
                new NetworkTask().execute(new Integer[]{mId, fId});
            }

            if(flag == 'N'){
                if(bServer != null)
                    bServer.cancel();
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;
        this.isRunning = false;
        this.handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                super.handleMessage(inputMessage);
                byte[] buffer = (byte[]) inputMessage.obj;
                handleBluetoothMessage(buffer);
            }
        };
        bServer = new BluetoothServer(handler);
        LocalBroadcastManager.getInstance(this).registerReceiver(mFriendRequestAnswerReceiver, new IntentFilter("friend-req-answer"));
    }

    private void handleBluetoothMessage(byte[] data){
        char flag = (char) data[0];

        if(flag == 'Y'){
            if(bServer != null)
                bServer.cancel();
        }

        if (flag == 'N') {
            if(bServer != null)
                bServer.cancel();
        }

        if (flag == 'F') {
            friendIntent = new Intent("friend-req");
            int userID =  ByteBuffer.wrap(new byte[]{data[1], data[2], data[3], data[4]}).getInt();
            System.out.println("HandlerMessage: " + flag + ", " + userID);
            friendIntent.putExtra("ID", userID);
            new NetworkTask().execute(new Integer[]{userID});
        }
    }

    private class NetworkTask extends AsyncTask<Integer, Void, Void>{

        boolean isRequest = false;
        boolean isOk = true;
        String username;

        @Override
        protected Void doInBackground(Integer... params) {

            if(params.length == 1){
                isRequest = true;
                int id = params[0];
                try {
                    username = UserNetworkController.getUsername(Connections.liveServerURL + "getUsername", new JSONObject().put("id", id).toString());
                }
                catch (Exception e){
                    e.printStackTrace();
                    isOk = false;
                }
            }

            if(params.length == 2){
                isRequest = false;
                int mId = params[0];
                int fId = params[1];
                try {
                    JSONObject json =  new JSONObject();
                    json.put("requestingUser", mId);
                    json.put("targetUser", fId);

                    boolean isValid = true;
                    if(!(UserNetworkController.sendFriendRequest(Connections.liveServerURL + "addFriend", json.toString())))
                        isValid = false;
                    friendIntent = new Intent("friend-req");
                    friendIntent.putExtra("valid", isValid);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(friendIntent);
                }
                catch (Exception e){
                    e.printStackTrace();
                    isOk = false;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(isOk){
                if(isRequest) {
                    friendIntent.putExtra("username", username);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(friendIntent);
                }
                else{
                    if(bServer != null)
                        bServer.cancel();
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("BACKGROUND SERVICE DESTROYED");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFriendRequestAnswerReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("BACKGROUND SERVICE STARTED");
        if(bServer != null)
            bServer.start();

        return START_STICKY;
    }

}
