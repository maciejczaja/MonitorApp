package com.monitorapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Telephony;
import android.util.Log;

import androidx.annotation.Nullable;


public class Sms extends Service {

    private SmsReceiver mSmsReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mSmsReceiver = new SmsReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(mSmsReceiver, filter);
    }

    @Override
    public void onDestroy() {
        try {
            if (mSmsReceiver != null) {
                unregisterReceiver(mSmsReceiver);
            }
        } catch (IllegalArgumentException e) {
        }
    }
}

class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = SmsReceiver.class.getSimpleName();
    public static final String SMS_CONTENT = "sms_content";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Sms recieved.");

    }
}