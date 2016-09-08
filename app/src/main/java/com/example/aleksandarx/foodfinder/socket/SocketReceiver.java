package com.example.aleksandarx.foodfinder.socket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Darko on 28.08.2016.
 */
public class SocketReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int datapassed = intent.getIntExtra("DATAPASSED", 0);

        Toast.makeText(context,
                "Triggered by Service!\n"
                        + "Data passed: " + String.valueOf(datapassed),
                Toast.LENGTH_LONG).show();
    }
}
