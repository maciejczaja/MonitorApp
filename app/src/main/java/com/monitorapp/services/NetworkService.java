package com.monitorapp.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.monitorapp.utils.BroadcastReceiverClass;

import static com.monitorapp.utils.NotificationUtils.myNotification;


public class NetworkService extends Service {

    private BroadcastReceiverClass mNetworkReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mNetworkReceiver = new BroadcastReceiverClass();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        registerReceiver(mNetworkReceiver, filter);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startForeground(1, myNotification(getApplicationContext()));
        else
            startForeground(1, new Notification());
    }

    @Override
    public void onDestroy() {
        try {
            if (mNetworkReceiver != null) {
                unregisterReceiver(mNetworkReceiver);
            }
        } catch (IllegalArgumentException e) {
            Log.e("Error", "Exception");
        }
    }
}