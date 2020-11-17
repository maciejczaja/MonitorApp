package com.monitorapp.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;

public class CallService extends Service {

    private CallReceiver mCallCallReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mCallCallReceiver = new CallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(mCallCallReceiver, filter);
    }

    @Override
    public void onDestroy() {
        try {
            if (mCallCallReceiver != null) {
                unregisterReceiver(mCallCallReceiver);
            }
        } catch (IllegalArgumentException e) {
            Log.e("Call", e.toString());
        }
    }
}

class CallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getExtras().getString(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            getCallLog(context);
        }
    }

    private void getCallLog(Context context) {
        Cursor cursor = context.getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null,
                null, CallLog.Calls.DATE + " DESC");
        if (cursor.getCount() > 0) {
            try {
                cursor.moveToFirst();
                Long epoch = Long.parseLong(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)));
                String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss z").format(new java.util.Date(epoch));
                Log.d("DATE", date);
                Log.d("TYPE", cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)));
                Log.d("DURATION", cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION)));
            } catch (Exception e) {
                Log.e("Exception", e.toString());
            }
        }
    }
}