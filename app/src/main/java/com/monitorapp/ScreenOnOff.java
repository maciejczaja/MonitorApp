package com.monitorapp;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;


public class ScreenOnOff extends Service {

    private ScreenOnOffReceiver mScreenReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        registerScreenStatusReceiver();
    }

    @Override
    public void onDestroy() {
        unregisterScreenStatusReceiver();
    }

    private void registerScreenStatusReceiver() {
        mScreenReceiver = new ScreenOnOffReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mScreenReceiver, filter);
    }

    private void unregisterScreenStatusReceiver() {
        try {
            if (mScreenReceiver != null) {
                unregisterReceiver(mScreenReceiver);
            }
        } catch (IllegalArgumentException e) {
        }
    }
}