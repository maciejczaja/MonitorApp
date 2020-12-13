package com.monitorapp.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.monitorapp.utils.BroadcastReceiverClass;

import static com.monitorapp.utils.NotificationUtils.myNotification;


public class BatteryService extends Service {

    private BroadcastReceiverClass mBatteryReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mBatteryReceiver = new BroadcastReceiverClass();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);

        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        registerReceiver(mBatteryReceiver, filter);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startForeground(1, myNotification(getApplicationContext()));
        else
            startForeground(1, new Notification());
    }

    @Override
    public void onDestroy() {
        try {
            if (mBatteryReceiver != null) {
                unregisterReceiver(mBatteryReceiver);
            }
        } catch (IllegalArgumentException e) {
            Log.e("Error", "Exception");
        }
    }
}