package com.monitorapp;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class Sensors extends Service {
    SensorManager sm = null;
    List list;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    SensorEventListener sensorListener = new SensorEventListener() {

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;

            if (values.length == 1)
                Log.d(String.valueOf(event.sensor.getName()), "x: " + values[0]);

            if (values.length == 2)
                Log.d(String.valueOf(event.sensor.getName()), "x: " + values[0] + ", y: ");

            if (values.length == 3)
                Log.d(String.valueOf(event.sensor.getName()), "x: " + values[0] + ", y: " + values[1] + ", z: " + values[2]);
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