package com.monitorapp.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.monitorapp.utils.BroadcastReceiverClass;


public class AirplaneModeService extends Service {

    private BroadcastReceiverClass mAirplaneModeReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mAirplaneModeReceiver = new BroadcastReceiverClass();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);

        registerReceiver(mAirplaneModeReceiver, filter);
    }

    @Override
    public void onDestroy() {
        try {
            if (mAirplaneModeReceiver != null) {
                unregisterReceiver(mAirplaneModeReceiver);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}