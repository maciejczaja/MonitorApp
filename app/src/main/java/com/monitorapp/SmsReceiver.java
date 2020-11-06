package com.monitorapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = SmsReceiver.class.getSimpleName();
    public static final String SMS_CONTENT = "sms_content";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Sms recieved.");

    }
}