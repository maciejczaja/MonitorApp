package com.monitorapp.services;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.monitorapp.R;
import com.monitorapp.db_utils.SQLExporter;
import com.monitorapp.db_utils.UserIDStore;
import com.monitorapp.view.MainActivity;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.monitorapp.enums.SensorType.TYPE_ACCELEROMETER;
import static com.monitorapp.enums.SensorType.TYPE_GRAVITY;
import static com.monitorapp.enums.SensorType.TYPE_GYROSCOPE;
import static com.monitorapp.enums.SensorType.TYPE_LIGHT;
import static com.monitorapp.enums.SensorType.TYPE_MAGNETIC_FIELD;
import static com.monitorapp.view.MainActivity.PACKAGE_NAME;
import static com.monitorapp.view.MainActivity.editTextDelay;
import static com.monitorapp.view.MainActivity.isAppRunning;
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

public class MonitoringNotificationService extends Service {

    private static final int NOTIFICATION_ID = 999;

    public static final String ACTION_START_SERVICE = PACKAGE_NAME + ".ACTION_START_SERVICE";
    public static final String ACTION_STOP_SERVICE = PACKAGE_NAME + ".ACTION_STOP_SERVICE";
    private static final String ACTION_MAIN = PACKAGE_NAME + ".ACTION_MAIN";
    private static final String TAG = "MonitoringNotifService";

    public static boolean isServiceRunning = false;

    private boolean ifSoundMonitoring;
    private boolean ifGyroMonitoring;
    private boolean ifAccelMonitoring;
    private boolean ifGravityMonitoring;
    private boolean ifLightMonitoring;
    private boolean ifMagnMonitoring;
    private boolean ifScreenMonitoring;
    private boolean ifSmsMonitoring;
    private boolean ifCallMonitoring;
    private boolean ifBattMonitoring;
    private boolean ifAirplMonitoring;
    private boolean ifNetwMonitoring;
    private boolean ifAppMonitoring;
    private boolean isAppRunning;
    private long delay;
    private String delayString;

    @Override
    public void onCreate() {
        super.onCreate();
        loadUiState(this);
        startServiceWithNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction().equals(ACTION_START_SERVICE)) {
            startServiceWithNotification();
        } else stopMyService();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        isServiceRunning = false;
        Log.d(TAG, ": destroyed");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startServiceWithNotification() {
        if (isServiceRunning) {
            return;
        }

        isServiceRunning = true;
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setAction(ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setTicker(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.notification_string))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(contentPendingIntent)
                .setOngoing(true)
                .build();

        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
        onMonitoringStart();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void stopMyService() {
        onMonitoringStop();
        stopForeground(true);
        stopSelf();
        isServiceRunning = false;
    }

    private void onMonitoringStart() {
        Log.d(TAG, ": onMonitoringStart");

        isAppRunning = isAppRunning(this);

        /* SOUND LEVEL */
        if (ifSoundMonitoring) {
            startService(new Intent(getApplicationContext(), NoiseDetectorService.class));
            if (isAppRunning) {
                switchSoundLevelMeter.setClickable(false);
            }
        }

        /* GYROSCOPE */
        if (ifGyroMonitoring) {
            startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_GYROSCOPE.getValue()));
            if (isAppRunning) {
                switchGyroscope.setClickable(false);
            }
        }

        /* ACCELEROMETER */
        if (ifAccelMonitoring) {
            startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_ACCELEROMETER.getValue()));
            if (isAppRunning) {
                switchAccelerometer.setClickable(false);
            }
        }

        /* GRAVITY */
        if (ifGravityMonitoring) {
            startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_GRAVITY.getValue()));
            if (isAppRunning) {
                switchGravity.setClickable(false);
            }
        }

        /* LIGHT METER */
        if (ifLightMonitoring) {
            startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_LIGHT.getValue()));
            if (isAppRunning) {
                switchLight.setClickable(false);
            }
        }

        /* MAGNETIC FIELD */
        if (ifMagnMonitoring) {
            startService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_MAGNETIC_FIELD.getValue()));
            if (isAppRunning) {
                switchMagneticField.setClickable(false);
            }
        }

        /* SCREEN ON/OFF */
        if (ifScreenMonitoring) {
            startService(new Intent(getApplicationContext(), ScreenOnOffService.class));
            if (isAppRunning) {
                switchScreenOnOff.setClickable(false);
            }
        }

        /* SMS */
        if (ifSmsMonitoring) {
            startService(new Intent(getApplicationContext(), SmsService.class));
            if (isAppRunning) {
                switchSms.setClickable(false);
            }
        }

        /* CALL */
        if (ifCallMonitoring) {
            startService(new Intent(getApplicationContext(), CallService.class));
            if (isAppRunning) {
                switchCall.setClickable(false);
            }
        }

        /* BATTERY */
        if (ifBattMonitoring) {
            startService(new Intent(getApplicationContext(), BatteryService.class));
            if (isAppRunning) {
                switchBattery.setClickable(false);
            }
        }

        /* AIRPLANE MODE */
        if (ifAirplMonitoring) {
            startService(new Intent(getApplicationContext(), AirplaneModeService.class));
            if (isAppRunning) {
                switchAirplaneMode.setClickable(false);
            }
        }

        /* NETWORK */
        if (ifNetwMonitoring) {
            startService(new Intent(getApplicationContext(), NetworkService.class));
            if (isAppRunning) {
                switchNetwork.setClickable(false);
            }
        }

        /* FOREGROUND APP */
        if (!hasUsageStatsPermission() && isAppRunning) {
            switchForegroundApp.setEnabled(false);
        }

        if (ifAppMonitoring) {
            if (delayString.isEmpty()) {
//                Toast.makeText(this, "Empty delay field: app check started with delay default value of 5 seconds.", Toast.LENGTH_LONG).show();
                startService(new Intent(getApplicationContext(), ForegroundAppService.class));
            } else if (delayString.startsWith("-")) {
//                Toast.makeText(this, "Negative delay specified: app check started with delay default value of 5 seconds.", Toast.LENGTH_LONG).show();
                startService(new Intent(getApplicationContext(), ForegroundAppService.class));
            } else if (delayString.startsWith("+")) {
//                delay = Long.parseLong(delayString.substring(1));
//                Toast.makeText(this, "Redundant plus character: app check started with delay value of " + delayString.substring(1) + " seconds.", Toast.LENGTH_LONG).show();
                startService(new Intent(getApplicationContext(), ForegroundAppService.class).putExtra("DELAY", delay));
            } else if (delayString.equals("0") || delayString.equals("00")) {
//                Toast.makeText(this, "Delay equals zero: app check started with delay default value of 5 seconds.", Toast.LENGTH_LONG).show();
                startService(new Intent(getApplicationContext(), ForegroundAppService.class));
            } else if (delayString.startsWith("0")) {
//                delay = Long.parseLong(delayString);
//                Toast.makeText(this, "App check started with delay value of " + delayString.substring(1) + " seconds.", Toast.LENGTH_LONG).show();
                startService(new Intent(getApplicationContext(), ForegroundAppService.class).putExtra("DELAY", delay));
            } else {
//                delay = Long.parseLong(delayString);
//                Toast.makeText(this, "App check started with delay value of " + delayString + " seconds.", Toast.LENGTH_LONG).show();
                startService(new Intent(getApplicationContext(), ForegroundAppService.class).putExtra("DELAY", delay));
            }
        }
        if (isAppRunning) {
            switchForegroundApp.setClickable(false);
        }
    }

//    private void onMonitoringStart() {
//        Log.d(TAG, ": monitoring start");
//    }

    private void onMonitoringStop() {
        Log.d(TAG, ": onMonitoringStop");

        /* SOUND LEVEL */
        if (ifSoundMonitoring) {
            stopService(new Intent(getApplicationContext(), NoiseDetectorService.class));
            if (isAppRunning) {
                switchSoundLevelMeter.setClickable(true);
            }
        }

        /* GYROSCOPE */
        if (ifGyroMonitoring) {
            stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_GYROSCOPE.getValue()));
            if (isAppRunning) {
                switchGyroscope.setClickable(true);
            }
        }

        /* ACCELEROMETER */
        if (ifAccelMonitoring) {
            stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_ACCELEROMETER.getValue()));
            if (isAppRunning) {
                switchAccelerometer.setClickable(true);
            }
        }

        /* GRAVITY */
        if (ifGravityMonitoring) {
            stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_GRAVITY.getValue()));
            if (isAppRunning) {
                switchGravity.setClickable(true);
            }
        }

        /* LIGHT METER */
        if (ifLightMonitoring) {
            stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_LIGHT.getValue()));
            if (isAppRunning) {
                switchLight.setClickable(true);
            }
        }

        /* MAGNETIC FIELD */
        if (ifMagnMonitoring) {
            stopService(new Intent(getApplicationContext(), SensorsService.class).putExtra("SENSOR_TYPE", TYPE_MAGNETIC_FIELD.getValue()));
            if (isAppRunning) {
                switchMagneticField.setClickable(true);
            }
        }

        /* SCREEN ON/OFF */
        if (ifScreenMonitoring) {
            stopService(new Intent(getApplicationContext(), ScreenOnOffService.class));
            if (isAppRunning) {
                switchScreenOnOff.setClickable(true);
            }
        }

        /* SMS */
        if (ifSmsMonitoring) {
            stopService(new Intent(getApplicationContext(), SmsService.class));
            if (isAppRunning) {
                switchSms.setClickable(true);
            }
        }

        /* CALL */
        if (ifCallMonitoring) {
            stopService(new Intent(getApplicationContext(), CallService.class));
            if (isAppRunning) {
                switchCall.setClickable(true);
            }
        }

        /* BATTERY */
        if (ifBattMonitoring) {
            stopService(new Intent(getApplicationContext(), BatteryService.class));
            if (isAppRunning) {
                switchBattery.setClickable(true);
            }
        }

        /* AIRPLANE MODE */
        if (ifAirplMonitoring) {
            stopService(new Intent(getApplicationContext(), AirplaneModeService.class));
            if (isAppRunning) {
                switchAirplaneMode.setClickable(true);
            }
        }

        /* NETWORK */
        if (ifNetwMonitoring) {
            stopService(new Intent(getApplicationContext(), NetworkService.class));
            if (isAppRunning) {
                switchNetwork.setClickable(true);
            }
        }

        /* FOREGROUND APP */
        if (!hasUsageStatsPermission()&&isAppRunning) {
            switchForegroundApp.setEnabled(false);
        }

        if (ifAppMonitoring) {
            stopService(new Intent(getApplicationContext(), ForegroundAppService.class));
            if (isAppRunning) {
                switchForegroundApp.setClickable(true);
            }
        }

        /* EXPORT TO CSV */
        try {
            Intent intent = new Intent(this, SQLExporter.class);
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void loadUiState(@NotNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        ifSoundMonitoring = sharedPreferences.getBoolean("switchSoundLevelMeter", false);
        ifGyroMonitoring = sharedPreferences.getBoolean("switchGyroscope", false);
        ifAccelMonitoring = sharedPreferences.getBoolean("switchAccelerometer", false);
        ifGravityMonitoring = sharedPreferences.getBoolean("switchGravity", false);
        ifLightMonitoring = sharedPreferences.getBoolean("switchLight", false);
        ifMagnMonitoring = sharedPreferences.getBoolean("switchMagneticField", false);
        ifScreenMonitoring = sharedPreferences.getBoolean("switchScreenOnOff", false);
        ifSmsMonitoring = sharedPreferences.getBoolean("switchSms", false);
        ifCallMonitoring = sharedPreferences.getBoolean("switchCall", false);
        ifBattMonitoring = sharedPreferences.getBoolean("switchBattery", false);
        ifAirplMonitoring = sharedPreferences.getBoolean("switchAirplaneMode", false);
        ifNetwMonitoring = sharedPreferences.getBoolean("switchNetwork", false);
        ifAppMonitoring = sharedPreferences.getBoolean("switchForegroundApp", false);
        delay = Long.parseLong((sharedPreferences.getString("editTextDelay", "")));
        delayString = sharedPreferences.getString("editTextDelay", "");
    }
}
