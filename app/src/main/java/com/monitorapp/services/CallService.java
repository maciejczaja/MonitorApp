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

import com.monitorapp.BuildConfig;
import com.monitorapp.db_utils.DatabaseHelper;
import com.monitorapp.db_utils.UserIDStore;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
            e.printStackTrace();
        }
    }
}

class CallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, @NotNull Intent intent) {

        if (Objects.equals(Objects.requireNonNull(intent.getExtras())
                .getString(TelephonyManager.EXTRA_STATE), TelephonyManager.EXTRA_STATE_IDLE)) {
            getCallLog(context);
        }
    }

    private void getCallLog(@NotNull Context context) {

        /*TODO: This Cursor should be freed up after use with #close() */
        Cursor cursor = context.getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null,
                null, CallLog.Calls.DATE + " DESC");
        DatabaseHelper dbHelper = DatabaseHelper.getHelper(context);

        assert cursor != null;
        if (cursor.getCount() > 0) {
            try {
                cursor.moveToFirst();
                long epoch = Long.parseLong(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)));
                String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss z").format(new java.util.Date(epoch));
                if (BuildConfig.DEBUG) {
                    Log.d("DATE", date);
                    Log.d("TYPE", cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)));
                    Log.d("DURATION", cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION)));
                }
                dbHelper.addRecordCallData(cursor.getColumnIndex(CallLog.Calls.TYPE), UserIDStore.id(context.getApplicationContext()), date, cursor.getColumnIndex(CallLog.Calls.DURATION));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}