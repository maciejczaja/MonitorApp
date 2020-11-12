package com.monitorapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.jetbrains.annotations.NotNull;

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

    //fields used for foreground app check delay input
    private Switch appCheckSwitch;
    private EditText delayEditText;
    private static final long DEFAULT_DELAY = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btn);

        String[] PERMISSIONS = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS
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

        Intent intent = new Intent(this, ScreenOnOffService.class);
        startService(intent);
    }

    private boolean checkPermissions(@NotNull String[] perms) {

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


        /* read delay from text field and run app checking service */
        delayEditText = (EditText) findViewById(R.id.AppReadDelayEditText);
        String delayString = delayEditText.getText().toString();
        appCheckSwitch = findViewById(R.id.AppReadSwitch);
        delayEditText.addTextChangedListener(delayInputTextWatcher);

        if (state == STATE_START) {
            if (appCheckSwitch.isChecked()) {
                long delay;
                if (delayString.isEmpty()) {
                    Toast.makeText(this, "You haven't specified the delay - app check started with default value of 5 seconds.", Toast.LENGTH_LONG).show();
                    startService(new Intent(getApplicationContext(), ForegroundAppService.class));
                } else {
                    delay = Long.parseLong(delayString);
                    startService(new Intent(getApplicationContext(), ForegroundAppService.class).putExtra("DELAY", delay));
                }
            }
            appCheckSwitch.setClickable(false);
        } else if (state == STATE_STOP) {
            if (appCheckSwitch.isChecked())
                stopService(new Intent(getApplicationContext(), ForegroundAppService.class));
            appCheckSwitch.setClickable(true);
        }
    }

    private TextWatcher delayInputTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String delayInput = delayEditText.getText().toString().trim();
            if (delayInput.isEmpty()) {
                appCheckSwitch.setChecked(false);
            }
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String delayInput = delayEditText.getText().toString().trim();
            appCheckSwitch.setEnabled(!delayInput.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String delayInput = delayEditText.getText().toString().trim();
            if (delayInput.isEmpty()) {
                appCheckSwitch.setChecked(false);
            }
        }
    };
}

