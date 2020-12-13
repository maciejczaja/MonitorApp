package com.monitorapp.services;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.provider.Telephony;
import android.util.Log;

import androidx.annotation.Nullable;

import com.monitorapp.db_utils.DatabaseHelper;
import com.monitorapp.db_utils.UserIDStore;
import com.monitorapp.utils.BroadcastReceiverClass;

import java.sql.Date;
import java.text.SimpleDateFormat;

import static com.monitorapp.utils.NotificationUtils.myNotification;


public class SmsService extends Service {

    private BroadcastReceiverClass mSmsReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mSmsReceiver = new BroadcastReceiverClass();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(mSmsReceiver, filter);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startForeground(1, myNotification(getApplicationContext()));
        else
            startForeground(1, new Notification());
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
