package com.monitorapp;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;

public class CallService extends Service {

    private CallReceiver mCallReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mCallReceiver = new CallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(mCallReceiver, filter);
    }

    @Override
    public void onDestroy() {
        try {
            if (mCallReceiver != null) {
                unregisterReceiver(mCallReceiver);
            }
        } catch (IllegalArgumentException e) {
        }
    }
}
