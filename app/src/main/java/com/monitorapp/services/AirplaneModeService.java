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

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startForeground(1, myNotification(getApplicationContext()));
        else
            startForeground(1, new Notification());
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