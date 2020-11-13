package com.monitorapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.util.Log;

import java.util.concurrent.CopyOnWriteArraySet;

public class BroadcastReceiverClass extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()) {
            case Intent.ACTION_BATTERY_CHANGED:

                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float) scale;
                Log.d("BATTERY", "change battery:" + batteryPct);
                break;

            case Intent.ACTION_AIRPLANE_MODE_CHANGED:

                Log.d("AIRPLANEMODE", "wlaczenie/wylaczenie");

            case Intent.ACTION_POWER_CONNECTED:

                Log.d("BATTERY", "Podłączenie ładowarki");
                break;

            case Intent.ACTION_POWER_DISCONNECTED:

                Log.d("BATTERY", "Odłączenie ładowarki");
                break;

            case Intent.ACTION_SCREEN_ON:

                Log.d("SCREEN", "Screen On");
                break;

            case Intent.ACTION_SCREEN_OFF:

                Log.d("SCREEN", "Screen Off");
                break;

            case ConnectivityManager.CONNECTIVITY_ACTION:

                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                Log.d("WIFI", "Type: " + info);
                break;

            default:

                Log.e("err", intent.getAction());
                break;

        }
    }
}