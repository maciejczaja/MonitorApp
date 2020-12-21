package com.monitorapp.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.monitorapp.utils.BroadcastReceiverClass;


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
    }

    @Override
    public void onDestroy() {
        try {
            if (mBatteryReceiver != null) {
                unregisterReceiver(mBatteryReceiver);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}