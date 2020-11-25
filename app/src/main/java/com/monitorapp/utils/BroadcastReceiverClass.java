package com.monitorapp.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.util.Log;

import com.monitorapp.db_utils.DatabaseHelper;
import com.monitorapp.db_utils.UserIDStore;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class BroadcastReceiverClass extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());

        switch (intent.getAction()) {
            case Intent.ACTION_BATTERY_CHANGED:

                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float) scale;
                Log.d("BATTERY", "change battery:" + batteryPct);
                dbHelper.addRecordBatteryData(sdf.format(date), UserIDStore.id(context), batteryPct);
                break;

            case Intent.ACTION_AIRPLANE_MODE_CHANGED:

                Log.d("AIRPLANEMODE", "wlaczenie/wylaczenie");
                dbHelper.addRecordOnOffData("Airplane mode", "On", UserIDStore.id(context), sdf.format(date));
                break;

            case Intent.ACTION_POWER_CONNECTED:

                Log.d("BATTERY", "Podłączenie ładowarki");
                dbHelper.addRecordOnOffData("Power", "On", UserIDStore.id(context), sdf.format(date));
                break;

            case Intent.ACTION_POWER_DISCONNECTED:

                Log.d("BATTERY", "Odłączenie ładowarki");
                dbHelper.addRecordOnOffData("Power", "Off", UserIDStore.id(context), sdf.format(date));
                break;

            case Intent.ACTION_SCREEN_ON:

                Log.d("SCREEN", "Screen On");
                dbHelper.addRecordOnOffData("Screen", "On", UserIDStore.id(context), sdf.format(date));
                break;

            case Intent.ACTION_SCREEN_OFF:

                Log.d("SCREEN", "Screen Off");
                dbHelper.addRecordOnOffData("Screen", "Off", UserIDStore.id(context), sdf.format(date));
                break;

            case ConnectivityManager.CONNECTIVITY_ACTION:

                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                Log.d("WIFI", "Type: " + info);
                dbHelper.addRecordNetworkData(sdf.format(date), UserIDStore.id(context), info.toString());
                break;

            default:

                Log.e("err", intent.getAction());
                break;

        }
    }
}