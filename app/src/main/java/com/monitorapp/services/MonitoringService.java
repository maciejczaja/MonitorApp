package com.monitorapp.services;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.monitorapp.db_utils.SQLExporter;
import com.monitorapp.view.MainActivity;

import org.jetbrains.annotations.NotNull;

import static com.monitorapp.enums.SensorType.TYPE_ACCELEROMETER;
import static com.monitorapp.enums.SensorType.TYPE_GRAVITY;
import static com.monitorapp.enums.SensorType.TYPE_GYROSCOPE;
import static com.monitorapp.enums.SensorType.TYPE_LIGHT;
import static com.monitorapp.enums.SensorType.TYPE_MAGNETIC_FIELD;
import static com.monitorapp.view.MainActivity.PACKAGE_NAME;
import static com.monitorapp.view.MainActivity.editTextDelay;
import static com.monitorapp.view.MainActivity.isAppRunning;
import static com.monitorapp.view.MainActivity.loadUiState;
import static com.monitorapp.view.MainActivity.switchAccelerometer;
import static com.monitorapp.view.MainActivity.switchAirplaneMode;
import static com.monitorapp.view.MainActivity.switchBattery;
import static com.monitorapp.view.MainActivity.switchCall;
import static com.monitorapp.view.MainActivity.switchForegroundApp;
import static com.monitorapp.view.MainActivity.switchGravity;
import static com.monitorapp.view.MainActivity.switchGyroscope;
import static com.monitorapp.view.MainActivity.switchLight;
import static com.monitorapp.view.MainActivity.switchMagneticField;
import static com.monitorapp.view.MainActivity.switchNetwork;
import static com.monitorapp.view.MainActivity.switchScreenOnOff;
import static com.monitorapp.view.MainActivity.switchSms;
import static com.monitorapp.view.MainActivity.switchSoundLevelMeter;

public class MonitoringService extends JobService {

    private static final String TAG = "MonitoringService";
    private static String monitoringType;

    @Override
    public boolean onStartJob(@NotNull JobParameters params) {
        monitoringType = params.getExtras().getString("monitoringType");
        Log.d(TAG, ": ON START JOB");

        if (!isAppRunning(this)) {
            //loadUiState(this);
        }

        switch (monitoringType) {
            case "START":
                Log.d(TAG, ": START");
                onMonitoringStart();
                break;

            case "STOP":
                Log.d(TAG, ": STOP");
                onMonitoringStop();
                return false;

            case "CHECK":
                Log.d(TAG, ": CHECK");
                onMonitoringPermissionsCheck();
                return false;

            default:
                throw new IllegalStateException("Unexpected value: " + monitoringType);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, ": ON STOP JOB");
        return false;
    }

    private void onMonitoringStart() {
        Log.d(TAG, ": onMonitoringStart");

        /* SOUND LEVEL */
        if (switchSoundLevelMeter.isChecked()) {
            startService(new Intent(getApplicationContext(), NoiseDetectorService.class));
            switchSoundLevelMeter.setClickable(false);
        }

        /* GYROSCOPE */
        if (switchGyroscope.isChecked()) {
            startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_GYROSCOPE.getValue()));
            switchGyroscope.setClickable(false);
        }

        /* ACCELEROMETER */
        if (switchAccelerometer.isChecked()) {
            startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_ACCELEROMETER.getValue()));
            switchAccelerometer.setClickable(false);
        }

        /* GRAVITY */
        if (switchGravity.isChecked()) {
            startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_GRAVITY.getValue()));
            switchGravity.setClickable(false);
        }

        /* LIGHT METER */
        if (switchLight.isChecked()) {
            startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_LIGHT.getValue()));
            switchLight.setClickable(false);
        }

        /* MAGNETIC FIELD */
        if (switchMagneticField.isChecked()) {
            startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_MAGNETIC_FIELD.getValue()));
            switchMagneticField.setClickable(false);
        }

        /* SCREEN ON/OFF */
        if (switchScreenOnOff.isChecked()) {
            startService(new Intent(getApplicationContext(), ScreenOnOffService.class));
            switchScreenOnOff.setClickable(false);
        }

        /* SMS */
        if (switchSms.isChecked()) {
            startService(new Intent(getApplicationContext(), SmsService.class));
            switchSms.setClickable(false);
        }

        /* CALL */
        if (switchCall.isChecked()) {
            startService(new Intent(getApplicationContext(), CallService.class));
            switchCall.setClickable(false);
        }

        /* BATTERY */
        if (switchBattery.isChecked()) {
            startService(new Intent(getApplicationContext(), BatteryService.class));
            switchBattery.setClickable(false);
        }

        /* AIRPLANE MODE */
        if (switchAirplaneMode.isChecked()) {
            startService(new Intent(getApplicationContext(), AirplaneModeService.class));
            switchAirplaneMode.setClickable(false);
        }

        /* NETWORK */
        if (switchNetwork.isChecked()) {
            startService(new Intent(getApplicationContext(), NetworkService.class));
            switchNetwork.setClickable(false);
        }

        /* FOREGROUND APP */
        if (!hasUsageStatsPermission()) {
            switchForegroundApp.setEnabled(false);
        }

        String delayString = editTextDelay.getText().toString();
        if (switchForegroundApp.isChecked()) {
            long delay;
            if (delayString.isEmpty()) {
                Toast.makeText(this, "Empty delay field: app check started with delay default value of 5 seconds.", Toast.LENGTH_LONG).show();
                startService(new Intent(getApplicationContext(), ForegroundAppService.class));
            } else if (delayString.startsWith("-")) {
                Toast.makeText(this, "Negative delay specified: app check started with delay default value of 5 seconds.", Toast.LENGTH_LONG).show();
                startService(new Intent(getApplicationContext(), ForegroundAppService.class));
            } else if (delayString.startsWith("+")) {
                delay = Long.parseLong(delayString.substring(1));
                Toast.makeText(this, "Redundant plus character: app check started with delay value of " + delayString.substring(1) + " seconds.", Toast.LENGTH_LONG).show();
                startService(new Intent(getApplicationContext(), ForegroundAppService.class).putExtra("DELAY", delay));
            } else if (delayString.equals("0") || delayString.equals("00")) {
                Toast.makeText(this, "Delay equals zero: app check started with delay default value of 5 seconds.", Toast.LENGTH_LONG).show();
                startService(new Intent(getApplicationContext(), ForegroundAppService.class));
            } else if (delayString.startsWith("0")) {
                delay = Long.parseLong(delayString);
                Toast.makeText(this, "App check started with delay value of " + delayString.substring(1) + " seconds.", Toast.LENGTH_LONG).show();
                startService(new Intent(getApplicationContext(), ForegroundAppService.class).putExtra("DELAY", delay));
            } else {
                delay = Long.parseLong(delayString);
                Toast.makeText(this, "App check started with delay value of " + delayString + " seconds.", Toast.LENGTH_LONG).show();
                startService(new Intent(getApplicationContext(), ForegroundAppService.class).putExtra("DELAY", delay));
            }
        }
        switchForegroundApp.setClickable(false);
    }

    private void onMonitoringStop() {
        Log.d(TAG, ": onMonitoringStop");

        /* SOUND LEVEL */
        if (switchSoundLevelMeter.isChecked()) {
            stopService(new Intent(getApplicationContext(), NoiseDetectorService.class));
            switchSoundLevelMeter.setClickable(true);
        }

        /* GYROSCOPE */
        if (switchGyroscope.isChecked()) {
            stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_GYROSCOPE.getValue()));
            switchGyroscope.setClickable(true);
        }

        /* ACCELEROMETER */
        if (switchAccelerometer.isChecked()) {
            stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_ACCELEROMETER.getValue()));
            switchAccelerometer.setClickable(true);
        }

        /* GRAVITY */
        if (switchGravity.isChecked()) {
            stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_GRAVITY.getValue()));
            switchGravity.setClickable(true);
        }

        /* LIGHT METER */
        if (switchLight.isChecked()) {
            stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_LIGHT.getValue()));
            switchLight.setClickable(true);
        }

        /* MAGNETIC FIELD */
        if (switchMagneticField.isChecked()) {
            stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_MAGNETIC_FIELD.getValue()));
            switchMagneticField.setClickable(true);
        }

        /* SCREEN ON/OFF */
        if (switchScreenOnOff.isChecked()) {
            stopService(new Intent(getApplicationContext(), ScreenOnOffService.class));
            switchScreenOnOff.setClickable(true);
        }

        /* SMS */
        if (switchSms.isChecked()) {
            stopService(new Intent(getApplicationContext(), SmsService.class));
            switchSms.setClickable(true);
        }

        /* CALL */
        if (switchCall.isChecked()) {
            stopService(new Intent(getApplicationContext(), CallService.class));
            switchCall.setClickable(true);
        }

        /* BATTERY */
        if (switchBattery.isChecked()) {
            stopService(new Intent(getApplicationContext(), BatteryService.class));
            switchBattery.setClickable(true);
        }

        /* AIRPLANE MODE */
        if (switchAirplaneMode.isChecked()) {
            stopService(new Intent(getApplicationContext(), AirplaneModeService.class));
            switchAirplaneMode.setClickable(true);
        }

        /* NETWORK */
        if (switchNetwork.isChecked()) {
            stopService(new Intent(getApplicationContext(), NetworkService.class));
            switchNetwork.setClickable(true);
        }

        /* FOREGROUND APP */
        if (!hasUsageStatsPermission()) {
            switchForegroundApp.setEnabled(false);
        }

        String delayString = editTextDelay.getText().toString();
        if (switchForegroundApp.isChecked()) {
            stopService(new Intent(getApplicationContext(), ForegroundAppService.class));
            switchForegroundApp.setClickable(true);
        }

        /* EXPORT TO CSV */
        try {
            Intent intent = new Intent(this, SQLExporter.class);
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onMonitoringPermissionsCheck() {
        Log.d(TAG, ": onMonitoringPermissionsCheck");

        /* SOUND LEVEL */
        if (!hasSensorPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO})) {
            switchSoundLevelMeter.setEnabled(false);
        } else {
            switchSoundLevelMeter.setEnabled(true);
        }

        /* SMS */
        if (!hasSensorPermissions(new String[]{Manifest.permission.RECEIVE_SMS})) {
            switchSms.setEnabled(false);
        } else {
            switchSms.setEnabled(true);
        }

        /* CALL */
        if (!hasSensorPermissions(new String[]{Manifest.permission.RECEIVE_SMS})) {
            switchCall.setEnabled(false);
        } else {
            switchCall.setEnabled(true);
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

    private void requestUsageStatsPermission() {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
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
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
        });
        requestUsageStatsPermission();
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, ": destroyed");
        saveMonitoringState();
        onMonitoringStop();
    }

    private void saveMonitoringState() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("monitoringState", monitoringType);
        editor.apply();
    }

    private static String getLastMonitoringState(@NotNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        return sharedPreferences.getString("monitoringState", "");
    }
}
