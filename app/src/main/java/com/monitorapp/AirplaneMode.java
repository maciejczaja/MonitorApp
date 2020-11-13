package com.monitorapp;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;


public class AirplaneMode extends Service {

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
            Log.e("Error", "Exception");
        }
    }
}