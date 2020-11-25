package com.monitorapp.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.monitorapp.utils.BroadcastReceiverClass;


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