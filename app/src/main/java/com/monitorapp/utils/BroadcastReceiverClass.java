package com.monitorapp.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.provider.Telephony;
import android.util.Log;

import com.monitorapp.BuildConfig;
import com.monitorapp.db_utils.DatabaseHelper;
import com.monitorapp.db_utils.UserIDStore;

import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class BroadcastReceiverClass extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, @NotNull Intent intent) {

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());

        switch (Objects.requireNonNull(intent.getAction())) {
            case Intent.ACTION_BATTERY_CHANGED:

                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float) scale;
                if (BuildConfig.DEBUG) {
                    Log.d("BATTERY", "change battery:" + batteryPct);
                }
                dbHelper.addRecordBatteryData(sdf.format(date), UserIDStore.id(context), batteryPct);
                break;

            case Intent.ACTION_AIRPLANE_MODE_CHANGED:

                if (BuildConfig.DEBUG) {
                    Log.d("AIRPLANEMODE", "wlaczenie/wylaczenie");
                }
                dbHelper.addRecordOnOffData("Airplane mode", "On", UserIDStore.id(context), sdf.format(date));
                break;

            case Intent.ACTION_POWER_CONNECTED:

                if (BuildConfig.DEBUG) {
                    Log.d("BATTERY", "Podłączenie ładowarki");
                }
                dbHelper.addRecordOnOffData("Power", "On", UserIDStore.id(context), sdf.format(date));
                break;

            case Intent.ACTION_POWER_DISCONNECTED:

                if (BuildConfig.DEBUG) {
                    Log.d("BATTERY", "Odłączenie ładowarki");
                }
                dbHelper.addRecordOnOffData("Power", "Off", UserIDStore.id(context), sdf.format(date));
                break;

            case Intent.ACTION_SCREEN_ON:

                if (BuildConfig.DEBUG) {
                    Log.d("SCREEN", "Screen On");
                }
                dbHelper.addRecordOnOffData("Screen", "On", UserIDStore.id(context), sdf.format(date));
                break;

            case Intent.ACTION_SCREEN_OFF:

                if (BuildConfig.DEBUG) {
                    Log.d("SCREEN", "Screen Off");
                }
                dbHelper.addRecordOnOffData("Screen", "Off", UserIDStore.id(context), sdf.format(date));
                break;

            case Telephony.Sms.Intents.SMS_RECEIVED_ACTION:

                if (BuildConfig.DEBUG) {
                    Log.d("SMS", "Sms recieved.");
                }
                dbHelper.addRecordTextMessageData(UserIDStore.id(context), sdf.format(date));
                break;

            case ConnectivityManager.CONNECTIVITY_ACTION:

                /*TODO: NetworkInfo & CONNECTIVITY_ACTION deprecated */
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                if (BuildConfig.DEBUG) {
                    Log.d("WIFI", "Type: " + info);
                }

                /*TODO: NetworkInfo#toString() deprecated */
                assert info != null;
                dbHelper.addRecordNetworkData(sdf.format(date), UserIDStore.id(context), info.toString());
                break;

            default:

                if (BuildConfig.DEBUG) {
                    Log.e("err", intent.getAction());
                }
                break;
        }
    }
}