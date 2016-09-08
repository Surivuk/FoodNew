package com.example.aleksandarx.foodfinder.view;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.aleksandarx.foodfinder.R;
import com.example.aleksandarx.foodfinder.bluetooth.BluetoothClient;
import com.example.aleksandarx.foodfinder.share.UserPreferences;
import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class BluetoothActivity extends AppCompatActivity {

    private ListView list;
    private Intent receiver;
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> devices;
    Timer timer;
    private Handler handler;
    private ProgressBar loading;
    private boolean isRegistred = false;
    private boolean first;
    private BluetoothClient bClient;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device != null) {
                    devices.add(device);
                }
            }
        }
    };

    private BroadcastReceiver errorRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            System.out.println("errorRec is recive it");

            if(bClient != null) {
                bClient.cancel();
                bClient.interrupt();
                bClient = null;
            }

            LocalBroadcastManager.getInstance(BluetoothActivity.this).unregisterReceiver(errorRec);
            if(mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.cancelDiscovery();
            if(isRegistred) {
                isRegistred = false;
                unregisterReceiver(mReceiver);
                receiver = null;
            }
            Toast.makeText(BluetoothActivity.this, "Bluetooth error. Try again!", Toast.LENGTH_SHORT).show();
            finish();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth2);

        list = (ListView) findViewById(R.id.bluetooth_friend_list);
        loading = (ProgressBar) findViewById(R.id.loading);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        devices = new ArrayList<>();
        handler = new Handler();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mBluetoothAdapter.isDiscovering())
                    mBluetoothAdapter.cancelDiscovery();
                BluetoothDevice d = ((BluetoothListAdapter)parent.getAdapter()).getItem(position);
                bClient = new BluetoothClient(d, handler, BluetoothActivity.this);
                int userID = Integer.parseInt(UserPreferences.getPreference(BluetoothActivity.this, UserPreferences.USER_ID));
                bClient.setCommand('F', userID);
                bClient.start();
            }
        });

    }

    private void updateGUI(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(!first) {
                    list.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.INVISIBLE);
                    if (devices.isEmpty())
                        Toast.makeText(BluetoothActivity.this, "No Device found!", Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(BluetoothActivity.this, "List", Toast.LENGTH_SHORT).show();
                        list.setAdapter(new BluetoothListAdapter(BluetoothActivity.this, R.layout.list_view_row, devices));
                    }
                    if (mBluetoothAdapter.isDiscovering())
                        mBluetoothAdapter.cancelDiscovery();
                    if (isRegistred) {
                        isRegistred = false;
                        unregisterReceiver(mReceiver);
                        receiver = null;
                    }
                    timer.cancel();
                }
                else
                    first = false;
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver(errorRec, new IntentFilter("bluetooth-error"));
        list.setVisibility(View.INVISIBLE);
        loading.setVisibility(View.VISIBLE);
        if(receiver == null){
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            receiver = registerReceiver(mReceiver, filter);
            isRegistred = true;
        }
        mBluetoothAdapter.startDiscovery();
        timer = new Timer(true);
        first = true;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("TICK");
                updateGUI();

            }
        }, 0, 10000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(errorRec);
        timer.cancel();
        if(mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
        if(isRegistred) {
            isRegistred = false;
            unregisterReceiver(mReceiver);
            receiver = null;
        }
    }
}
