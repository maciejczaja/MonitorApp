package com.monitorapp.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.monitorapp.db_utils.DatabaseHelper;
import com.monitorapp.db_utils.UserIDStore;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import static com.monitorapp.utils.NotificationUtils.myNotification;

public class SensorsService extends Service {
    SensorManager sm = null;
    List list;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startForeground(1, myNotification(getApplicationContext()));
        else
            startForeground(1, new Notification());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    SensorEventListener sensorListener = new SensorEventListener() {

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            DatabaseHelper dbHelper = DatabaseHelper.getHelper(getApplicationContext());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());

            if (values.length == 1) {
                Log.d(String.valueOf(event.sensor.getName()), "x: " + values[0]);
                dbHelper.addRecordMotionSensorReadings(UserIDStore.id(getApplicationContext()), sdf.format(date), values[0], null, null, event.sensor.getName());
            }

            if (values.length == 2) {
                Log.d(String.valueOf(event.sensor.getName()), "x: " + values[0] + ", y: ");
                dbHelper.addRecordMotionSensorReadings(UserIDStore.id(getApplicationContext()), sdf.format(date), values[0], values[1], null, event.sensor.getName());
            }

            if (values.length == 3) {
                Log.d(String.valueOf(event.sensor.getName()), "x: " + values[0] + ", y: " + values[1] + ", z: " + values[2]);
                dbHelper.addRecordMotionSensorReadings(UserIDStore.id(getApplicationContext()), sdf.format(date), values[0], values[1], values[2], event.sensor.getName());
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int type_sensor = intent.getIntExtra("SENSOR_TYPE", -1);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);

        list = sm.getSensorList(type_sensor);
        if (list.size() > 0) {
            sm.registerListener(sensorListener, (Sensor) list.get(0), SensorManager.SENSOR_DELAY_UI);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (list.size() > 0) {
            sm.unregisterListener(sensorListener);
        }
        super.onDestroy();
    }
}