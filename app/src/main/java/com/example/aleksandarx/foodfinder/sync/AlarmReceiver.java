package com.example.aleksandarx.foodfinder.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Darko on 03.07.2016.
 */
public class AlarmReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent background = new Intent(context,BackgroundService.class);
        context.startService(background);
    }
}
