package com.monitorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final int TYPE_GYROSCOPE = 4;
    private static final int TYPE_ACCELEROMETER = 1;
    private static final int TYPE_GRAVITY = 9;
    private static final int TYPE_LIGHT = 5;
    private static final int TYPE_MAGNETIC_FIELD = 2;

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Permission for NoiseDetector
        if (checkPermissions(Manifest.permission.RECORD_AUDIO, PERMISSION_REQUEST_RECORD_AUDIO))
            startService(new Intent(getApplicationContext(), NoiseDetector.class));
        else
            requestPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQUEST_RECORD_AUDIO);

        //Run sensors
        startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_GYROSCOPE));
        startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_ACCELEROMETER));
        startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_GRAVITY));
        startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_LIGHT));
        startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_MAGNETIC_FIELD));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Run noiseDetector
                startService(new Intent(getApplicationContext(), NoiseDetector.class));
            }
        }
    }

    private boolean checkPermissions(String perm, int requestCode) {

        return (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) ? false : true;

    }

    private void requestPermission(String perm, int requestCod) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{perm},
                    requestCod);
        }
    }
}

