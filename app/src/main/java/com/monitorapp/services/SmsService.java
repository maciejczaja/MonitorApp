package com.monitorapp.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Telephony;
import android.util.Log;

import androidx.annotation.Nullable;

import com.monitorapp.db_utils.DatabaseHelper;
import com.monitorapp.db_utils.UserIDStore;

import java.sql.Date;
import java.text.SimpleDateFormat;


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

    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHelper dbHelper = DatabaseHelper.getHelper(context);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        Log.i(TAG, "Sms recieved.");
        dbHelper.addRecordTextMessageData(UserIDStore.id(context), sdf.format(date));
    }
}