package com.monitorapp.view;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.monitorapp.R;
import com.monitorapp.services.AirplaneModeService;
import com.monitorapp.enums.AppRunState;
import com.monitorapp.services.BatteryService;
import com.monitorapp.services.CallService;
import com.monitorapp.services.ForegroundAppService;
import com.monitorapp.services.NetworkService;
import com.monitorapp.services.NoiseDetectorService;
import com.monitorapp.services.ScreenOnOffService;
import com.monitorapp.services.SensorsService;
import com.monitorapp.services.SmsService;

import static com.monitorapp.enums.AppRunState.STATE_CHECK_PERMISSION;
import static com.monitorapp.enums.AppRunState.STATE_START;
import static com.monitorapp.enums.AppRunState.STATE_STOP;
import static com.monitorapp.enums.SensorType.TYPE_ACCELEROMETER;
import static com.monitorapp.enums.SensorType.TYPE_GRAVITY;
import static com.monitorapp.enums.SensorType.TYPE_GYROSCOPE;
import static com.monitorapp.enums.SensorType.TYPE_LIGHT;
import static com.monitorapp.enums.SensorType.TYPE_MAGNETIC_FIELD;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS_ALL = 101;
    private static final int REQUEST_CODE_ZIP_ACTIVITY = 102;

    private static final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private SwitchCompat switchSoundLevelMeter;
    private SwitchCompat switchGyroscope;
    private SwitchCompat switchAccelerometer;
    private SwitchCompat switchGravity;
    private SwitchCompat switchLight;
    private SwitchCompat switchMagneticField;
    private SwitchCompat switchScreenOnOff;
    private SwitchCompat switchSms;
    private SwitchCompat switchCall;
    private SwitchCompat switchBattery;
    private SwitchCompat switchAirplaneMode;
    private SwitchCompat switchNetwork;
    private SwitchCompat switchForegroundApp;

    private EditText editTextDelay;

    private Button buttonSend;
    private Button buttonStartStop;
    private boolean buttonStartStopStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUIComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasPermissions()) {
            return;
        }
    }

    private void initUIComponents() {
        switchSoundLevelMeter = findViewById(R.id.Sound_level_meter);
        switchGyroscope = findViewById(R.id.Gyroscope);
        switchAccelerometer = findViewById(R.id.Accelerometer);
        switchGravity = findViewById(R.id.Gravity);
        switchLight = findViewById(R.id.Light);
        switchMagneticField = findViewById(R.id.Magnetic_field);
        switchScreenOnOff = findViewById(R.id.Screen_On_Off);
        switchSms = findViewById(R.id.Sms);
        switchCall = findViewById(R.id.Call);
        switchBattery = findViewById(R.id.Battery);
        switchAirplaneMode = findViewById(R.id.Airplane_mode);
        switchNetwork = findViewById(R.id.Network);
        switchForegroundApp = findViewById(R.id.Foreground_app);

        editTextDelay = findViewById(R.id.Foreground_app_delay);
        TextWatcher delayInputTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String delayInput = editTextDelay.getText().toString().trim();
                if (delayInput.isEmpty()) {
                    switchForegroundApp.setChecked(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String delayInput = editTextDelay.getText().toString().trim();
                switchForegroundApp.setEnabled(!delayInput.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String delayInput = editTextDelay.getText().toString().trim();
                if (delayInput.isEmpty()) {
                    switchForegroundApp.setChecked(false);
                }
            }
        };
        editTextDelay.addTextChangedListener(delayInputTextWatcher);
        if (editTextDelay.getText().toString().isEmpty()) {
            switchForegroundApp.setEnabled(false);
        }

        buttonSend = findViewById(R.id.Button_send);
        buttonSend.setOnClickListener(view -> startActivityForResult(new Intent(this, ZipActivity.class), REQUEST_CODE_ZIP_ACTIVITY));

        buttonStartStop = findViewById(R.id.Button_start_stop);
        buttonStartStop.setText("Start");
        buttonStartStop.setOnClickListener(view -> {
            if (!buttonStartStopStatus)
                action(STATE_START);
            else
                action(STATE_STOP);
        });
    }

    private void requestPermissions() {
        if (!hasSensorPermissions(PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE_PERMISSIONS_ALL);
        } else {
            action(STATE_CHECK_PERMISSION);
        }

        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission();
        }
    }

    private void requestUsageStatsPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    private boolean hasPermissions() {
        return hasSensorPermissions(PERMISSIONS) && hasUsageStatsPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS_ALL)
            action(STATE_CHECK_PERMISSION);
    }

    public void changeButtonText() {

        if (!buttonStartStopStatus) {
            buttonStartStop.setText("Stop");
            buttonStartStopStatus = true;
        } else {
            buttonStartStop.setText("Start");
            buttonStartStopStatus = false;
        }
    }

    private boolean hasSensorPermissions(@NonNull String[] perms) {

        for (String permission : perms) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasUsageStatsPermission() {
        final AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());

        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }

        appOpsManager.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS, getApplicationContext().getPackageName(), new AppOpsManager.OnOpChangedListener() {
            @Override
            public void onOpChanged(String s, String s1) {
                int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
                if (mode != AppOpsManager.MODE_ALLOWED) {
                    return;
                }
                appOpsManager.stopWatchingMode(this);
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
        });
        requestUsageStatsPermission();
        return false;
    }

    public void action(AppRunState state) {

        if (state != STATE_CHECK_PERMISSION)
            changeButtonText();

        /* SOUND LEVEL */
        if (state == STATE_CHECK_PERMISSION) {
            if (!hasSensorPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}))
                switchSoundLevelMeter.setEnabled(false);
            else
                switchSoundLevelMeter.setEnabled(true);
        } else if (state == STATE_START) {
            if (switchSoundLevelMeter.isChecked())
                startService(new Intent(getApplicationContext(), NoiseDetectorService.class));
            switchSoundLevelMeter.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchSoundLevelMeter.isChecked())
                stopService(new Intent(getApplicationContext(), NoiseDetectorService.class));
            switchSoundLevelMeter.setClickable(true);
        }

        /* GYROSCOPE */
        if (state == STATE_START) {
            if (switchGyroscope.isChecked()) {
                startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_GYROSCOPE));
                switchGyroscope.setClickable(false);
            }
        } else if (state == STATE_STOP) {
            if (switchGyroscope.isChecked())
                stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_GYROSCOPE));
            switchGyroscope.setClickable(true);
        }

        /* ACCELEROMETER */
        if (state == STATE_START) {
            if (switchAccelerometer.isChecked())
                startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_ACCELEROMETER));
            switchAccelerometer.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchAccelerometer.isChecked())
                stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_ACCELEROMETER));
            switchAccelerometer.setClickable(true);
        }

        /* GRAVITY */
        if (state == STATE_START) {
            if (switchGravity.isChecked())
                startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_GRAVITY));
            switchGravity.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchGravity.isChecked())
                stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_GRAVITY));
            switchGravity.setClickable(true);
        }

        /* LIGHT METER */
        if (state == STATE_START) {
            if (switchLight.isChecked())
                startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_LIGHT));
            switchLight.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchLight.isChecked())
                stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_LIGHT));
            switchLight.setClickable(true);
        }

        /* MAGNETIC FIELD */
        if (state == STATE_START) {
            if (switchMagneticField.isChecked())
                startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_MAGNETIC_FIELD));
            switchMagneticField.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchMagneticField.isChecked())
                stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_MAGNETIC_FIELD));
            switchMagneticField.setClickable(true);
        }

        /* SCREEN ON/OFF */
        if (state == STATE_START) {
            if (switchScreenOnOff.isChecked())
                startService(new Intent(getApplicationContext(), ScreenOnOffService.class));
            switchScreenOnOff.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchScreenOnOff.isChecked())
                stopService(new Intent(getApplicationContext(), ScreenOnOffService.class));
            switchScreenOnOff.setClickable(true);
        }

        /* SMS */
        if (state == STATE_CHECK_PERMISSION) {
            if (!hasSensorPermissions(new String[]{Manifest.permission.RECEIVE_SMS}))
                switchSms.setEnabled(false);
            else
                switchSms.setEnabled(true);
        } else if (state == STATE_START) {
            if (switchSms.isChecked())
                startService(new Intent(getApplicationContext(), SmsService.class));
            switchSms.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchSms.isChecked())
                stopService(new Intent(getApplicationContext(), SmsService.class));
            switchSms.setClickable(true);
        }

        /* CALL */
        if (state == STATE_CHECK_PERMISSION) {
            if (!hasSensorPermissions(new String[]{Manifest.permission.RECEIVE_SMS}))
                switchCall.setEnabled(false);
            else
                switchCall.setEnabled(true);
        } else if (state == STATE_START) {
            if (switchCall.isChecked())
                startService(new Intent(getApplicationContext(), CallService.class));
            switchCall.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchCall.isChecked())
                stopService(new Intent(getApplicationContext(), CallService.class));
            switchCall.setClickable(true);
        }

        /* BATTERY */
        if (state == STATE_START) {
            if (switchBattery.isChecked())
                startService(new Intent(getApplicationContext(), BatteryService.class));
            switchBattery.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchBattery.isChecked())
                stopService(new Intent(getApplicationContext(), BatteryService.class));
            switchBattery.setClickable(true);
        }

        /* AIRPLANE MODE */
        if (state == STATE_START) {
            if (switchAirplaneMode.isChecked())
                startService(new Intent(getApplicationContext(), AirplaneModeService.class));
            switchAirplaneMode.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchAirplaneMode.isChecked())
                stopService(new Intent(getApplicationContext(), AirplaneModeService.class));
            switchAirplaneMode.setClickable(true);
        }

        /* NETWORK */
        if (state == STATE_START) {
            if (switchNetwork.isChecked())
                startService(new Intent(getApplicationContext(), NetworkService.class));
            switchNetwork.setClickable(false);

        } else if (state == STATE_STOP) {
            if (switchNetwork.isChecked())
                stopService(new Intent(getApplicationContext(), NetworkService.class));
            switchNetwork.setClickable(true);
        }

        /* FOREGROUND APP */
        String delayString = editTextDelay.getText().toString();
        if (delayString.isEmpty()) {
            switchForegroundApp.setEnabled(false);
        }

        if (state == STATE_START) {
            if (switchForegroundApp.isChecked()) {
                long delay;
                if (delayString.isEmpty()) {
                    Toast.makeText(this, "You haven't specified the delay - app check started with default value of 5 seconds.", Toast.LENGTH_LONG).show();
                    startService(new Intent(getApplicationContext(), ForegroundAppService.class));
                } else {
                    delay = Long.parseLong(delayString);
                    startService(new Intent(getApplicationContext(), ForegroundAppService.class).putExtra("DELAY", delay));
                }
            }
            switchForegroundApp.setClickable(false);
        } else if (state == STATE_STOP) {
            if (switchForegroundApp.isChecked())
                stopService(new Intent(getApplicationContext(), ForegroundAppService.class));
            switchForegroundApp.setClickable(true);
        }
    }
}