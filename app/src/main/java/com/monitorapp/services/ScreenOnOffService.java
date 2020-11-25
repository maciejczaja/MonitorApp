package com.monitorapp.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.monitorapp.utils.BroadcastReceiverClass;


public class ScreenOnOffService extends Service {

    private BroadcastReceiverClass mScreenReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mScreenReceiver = new BroadcastReceiverClass();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenReceiver, filter);
    }

    @Override
    public void onDestroy() {
        try {
            if (mScreenReceiver != null) {
                unregisterReceiver(mScreenReceiver);
            }
        } catch (IllegalArgumentException e) {
            Log.e("E", "Exception");
        }
    }
}