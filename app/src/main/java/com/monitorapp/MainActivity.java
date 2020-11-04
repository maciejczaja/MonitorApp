package com.monitorapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private static final int TYPE_ACCELEROMETER = 1;
    private static final int TYPE_MAGNETIC_FIELD = 2;
    private static final int TYPE_GYROSCOPE = 4;
    private static final int TYPE_LIGHT = 5;
    private static final int TYPE_GRAVITY = 9;

    private static final int PERMISSION_ALL = 1;
    private Button btn;
    private boolean btnRunStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btn);
        String[] PERMISSIONS = {
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.READ_SMS,
        };

        if (!checkPermissions(PERMISSIONS, PERMISSION_ALL)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        btn.setText("Start");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTextButton();
                if (btnRunStatus)
                    start();
                else
                    stop();
            }
        });

        Intent intent = new Intent(this, ScreenOnOff.class);
        startService(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_ALL) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Run noiseDetector
                startService(new Intent(getApplicationContext(), NoiseDetector.class));
            }
        }
    }

    private boolean checkPermissions(String[] perms, int requestCode) {

        for (String permission : perms) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void changeTextButton() {
        if (btn.getText().toString() == "Start") {
            btn.setText("Stop");
            btnRunStatus = true;
        } else {
            btn.setText("Start");
            btnRunStatus = false;
        }
    }

    public void start() {

        if (((Switch) findViewById(R.id.Sound_level_meter)).isChecked())
            startService(new Intent(getApplicationContext(), NoiseDetector.class));

        if (((Switch) findViewById(R.id.Gyroscope)).isChecked())
            startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_GYROSCOPE));

        if (((Switch) findViewById(R.id.Accelerometr)).isChecked())
            startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_ACCELEROMETER));

        if (((Switch) findViewById(R.id.Gravity)).isChecked())
            startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_GRAVITY));

        if (((Switch) findViewById(R.id.Light)).isChecked())
            startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_LIGHT));

        if (((Switch) findViewById(R.id.Magnetic_field)).isChecked())
            startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_MAGNETIC_FIELD));
    }

    public void stop() {

        if (((Switch) findViewById(R.id.Sound_level_meter)).isChecked())
            stopService(new Intent(getApplicationContext(), NoiseDetector.class));

        if (((Switch) findViewById(R.id.Gyroscope)).isChecked())
            stopService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_GYROSCOPE));

        if (((Switch) findViewById(R.id.Accelerometr)).isChecked())
            stopService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_ACCELEROMETER));

        if (((Switch) findViewById(R.id.Gravity)).isChecked())
            stopService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_GRAVITY));

        if (((Switch) findViewById(R.id.Light)).isChecked())
            stopService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_LIGHT));

        if (((Switch) findViewById(R.id.Magnetic_field)).isChecked())
            stopService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_MAGNETIC_FIELD));
    }
}