package com.monitorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final int TYPE_GYROSCOPE = 4;
    private static final int TYPE_ACCELEROMETER = 1;
    private static final int TYPE_GRAVITY = 9;
    private static final int TYPE_LIGHT = 5;
    private static final int TYPE_MAGNETIC_FIELD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Run sensors
        startService(new Intent(this, Sensors.class).putExtra("SENSOR_TYPE", TYPE_GYROSCOPE));
        startService(new Intent(this, Sensors.class).putExtra("SENSOR_TYPE", TYPE_ACCELEROMETER));
        startService(new Intent(this, Sensors.class).putExtra("SENSOR_TYPE", TYPE_GRAVITY));
        startService(new Intent(this, Sensors.class).putExtra("SENSOR_TYPE", TYPE_LIGHT));
        startService(new Intent(this, Sensors.class).putExtra("SENSOR_TYPE", TYPE_MAGNETIC_FIELD));
    }
}