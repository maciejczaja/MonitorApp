package com.monitorapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int STATE_START = 1;
    private static final int STATE_STOP = 2;
    private static final int STATE_CHECK_PERMISSION = 3;

    private static final int TYPE_ACCELEROMETER = 1;
    private static final int TYPE_MAGNETIC_FIELD = 2;
    private static final int TYPE_GYROSCOPE = 4;
    private static final int TYPE_LIGHT = 5;
    private static final int TYPE_GRAVITY = 9;

    private static final int PERMISSION_ALL = 1;
    private Button btn;
    private boolean btnRunStatus = false;

    DatabaseHelper dbHelper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btn);

        String[] PERMISSIONS = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        if (!checkPermissions(PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else
            action(STATE_CHECK_PERMISSION);

        btn.setText("Start");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!btnRunStatus)
                    action(STATE_START);
                else
                    action(STATE_STOP);
            }
        });

        //add motion sensors - in database helper maybe?

        //call state

        Intent intent = new Intent(this, ScreenOnOffService.class);
        startService(intent);
    }

    private boolean checkPermissions(String[] perms) {

        for (String permission : perms) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_ALL)
            action(STATE_CHECK_PERMISSION);
    }

    public void changeTextButton() {

        if (!btnRunStatus) {
            btn.setText("Stop");
            btnRunStatus = true;
        } else {
            btn.setText("Start");
            btnRunStatus = false;
        }
    }

    public void action(int state) {

        SQLExporter exporter = new SQLExporter();

        if (state != STATE_CHECK_PERMISSION)
            changeTextButton();

        Switch switchTemp = findViewById(R.id.Sound_level_meter);
        if (state == STATE_CHECK_PERMISSION) {
            if (!checkPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}))
                switchTemp.setEnabled(false);
            else
                switchTemp.setEnabled(true);
        } else if (state == STATE_START) {
            if (switchTemp.isChecked())
                startService(new Intent(getApplicationContext(), NoiseDetector.class));
            switchTemp.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchTemp.isChecked())
                stopService(new Intent(getApplicationContext(), NoiseDetector.class));
            switchTemp.setClickable(true);
        }

        switchTemp = (Switch) findViewById(R.id.Gyroscope);
        if (state == STATE_START) {
            if (switchTemp.isChecked()) {
                startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_GYROSCOPE));
                switchTemp.setClickable(false);
            }
        } else if (state == STATE_STOP) {
            if (switchTemp.isChecked())
                stopService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_GYROSCOPE));
            switchTemp.setClickable(true);
        }

        switchTemp = (Switch) findViewById(R.id.Accelerometr);
        if (state == STATE_START) {
            if (switchTemp.isChecked())
                startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_ACCELEROMETER));
            switchTemp.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchTemp.isChecked())
                stopService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_ACCELEROMETER));
            switchTemp.setClickable(true);
        }

        switchTemp = (Switch) findViewById(R.id.Gravity);
        if (state == STATE_START) {
            if (switchTemp.isChecked())
                startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_GRAVITY));
            switchTemp.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchTemp.isChecked())
                stopService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_GRAVITY));
            switchTemp.setClickable(true);
        }

        switchTemp = (Switch) findViewById(R.id.Light);
        if (state == STATE_START) {
            if (switchTemp.isChecked())
                startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_LIGHT));
            switchTemp.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchTemp.isChecked())
                stopService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_LIGHT));
            switchTemp.setClickable(true);
        }

        switchTemp = (Switch) findViewById(R.id.Magnetic_field);
        if (state == STATE_START) {
            if (switchTemp.isChecked())
                startService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_MAGNETIC_FIELD));
            switchTemp.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchTemp.isChecked())
                stopService(new Intent(getApplicationContext(), Sensors.class).putExtra("SENSOR_TYPE", TYPE_MAGNETIC_FIELD));
            switchTemp.setClickable(true);
        }

        switchTemp = (Switch) findViewById(R.id.ScreenOnOff);
        if (state == STATE_START) {
            if (switchTemp.isChecked())
                startService(new Intent(getApplicationContext(), ScreenOnOffService.class));
            switchTemp.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchTemp.isChecked())
                stopService(new Intent(getApplicationContext(), ScreenOnOffService.class));
            switchTemp.setClickable(true);
        }

        switchTemp = findViewById(R.id.Sms);
        if (state == STATE_CHECK_PERMISSION) {
            if (!checkPermissions(new String[]{Manifest.permission.RECEIVE_SMS}))
                switchTemp.setEnabled(false);
            else
                switchTemp.setEnabled(true);
        } else if (state == STATE_START) {
            if (switchTemp.isChecked())
                startService(new Intent(getApplicationContext(), SmsService.class));
            switchTemp.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchTemp.isChecked())
                stopService(new Intent(getApplicationContext(), SmsService.class));
            switchTemp.setClickable(true);
        }

        switchTemp = findViewById(R.id.Call);
        if (state == STATE_CHECK_PERMISSION) {
            if (!checkPermissions(new String[]{Manifest.permission.RECEIVE_SMS}))
                switchTemp.setEnabled(false);
            else
                switchTemp.setEnabled(true);
        } else if (state == STATE_START) {
            if (switchTemp.isChecked())
                startService(new Intent(getApplicationContext(), CallService.class));
            switchTemp.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchTemp.isChecked())
                stopService(new Intent(getApplicationContext(), CallService.class));
            switchTemp.setClickable(true);
        }

        if (state == STATE_STOP) {
            try {
                exporter.export(dbHelper.getDatabase());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}