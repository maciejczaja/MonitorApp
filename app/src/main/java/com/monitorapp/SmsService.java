package com.monitorapp;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Telephony;

import androidx.annotation.Nullable;


public class SmsService extends Service {

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
        filter.addAction(Telephony.Sms.Intents.SMS_DELIVER_ACTION);
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