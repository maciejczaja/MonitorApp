package com.monitorapp.view;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.monitorapp.db_utils.DatabaseHelper;
import com.monitorapp.R;
import com.monitorapp.db_utils.SQLExporter;
import com.monitorapp.enums.AppRunState;
import com.monitorapp.services.AirplaneModeService;
import com.monitorapp.services.BatteryService;
import com.monitorapp.services.CallService;
import com.monitorapp.services.ForegroundAppService;
import com.monitorapp.services.MonitoringService;
import com.monitorapp.services.NetworkService;
import com.monitorapp.services.NoiseDetectorService;
import com.monitorapp.services.ScreenOnOffService;
import com.monitorapp.services.SensorsService;
import com.monitorapp.services.SmsService;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static com.monitorapp.enums.AppRunState.STATE_CHECK_PERMISSION;
import static com.monitorapp.enums.AppRunState.STATE_START;
import static com.monitorapp.enums.AppRunState.STATE_STOP;
import static com.monitorapp.enums.SensorType.TYPE_ACCELEROMETER;
import static com.monitorapp.enums.SensorType.TYPE_GRAVITY;
import static com.monitorapp.enums.SensorType.TYPE_GYROSCOPE;
import static com.monitorapp.enums.SensorType.TYPE_LIGHT;
import static com.monitorapp.enums.SensorType.TYPE_MAGNETIC_FIELD;
import static com.monitorapp.utils.StorageUtils.csvMkdir;
import static com.monitorapp.utils.StorageUtils.getExternalStoragePath;
import static com.monitorapp.utils.StorageUtils.isCsvStorageReadable;
import static com.monitorapp.utils.StorageUtils.isCsvStorageWritable;
import static com.monitorapp.utils.StorageUtils.isZipStorageReadable;
import static com.monitorapp.utils.StorageUtils.isZipStorageWritable;
import static com.monitorapp.utils.StorageUtils.zipMkdir;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS_ALL = 101;

    private static final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final String TAG = "MainActivity";

    public static SwitchCompat switchSoundLevelMeter;
    public static SwitchCompat switchGyroscope;
    public static SwitchCompat switchAccelerometer;
    public static SwitchCompat switchGravity;
    public static SwitchCompat switchLight;
    public static SwitchCompat switchMagneticField;
    public static SwitchCompat switchScreenOnOff;
    public static SwitchCompat switchSms;
    public static SwitchCompat switchCall;
    public static SwitchCompat switchBattery;
    public static SwitchCompat switchAirplaneMode;
    public static SwitchCompat switchNetwork;
    public static SwitchCompat switchForegroundApp;

    public static EditText editTextDelay;

    private Button buttonZip;
    private static Button buttonStartStop;
    private Button buttonSend;

    private static boolean buttonStartStopStatus = false;

    public static String PACKAGE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PACKAGE_NAME = getApplicationContext().getPackageName();

        /* Create folders for CSV and ZIP files respectively if non-existing */
        csvMkdir(this);
        zipMkdir(this);

        initUIComponents();
        loadUiState(this);
        Log.d(TAG, "editTextDelay: " + editTextDelay.toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            batteryOptimizations(getApplicationContext());
        }
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
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String delayInput = editTextDelay.getText().toString().trim();
                if (delayInput.startsWith("-") || delayInput.startsWith("+") || delayInput.equals("0") || delayInput.equals("00")) {
                    switchForegroundApp.setEnabled(false);
                } else {
                    switchForegroundApp.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        editTextDelay.addTextChangedListener(delayInputTextWatcher);

        buttonZip = findViewById(R.id.Button_zip);
        buttonZip.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ZipActivity.class)));

        buttonStartStop = findViewById(R.id.Button_start_stop);
        buttonStartStop.setText(R.string.button_start);
        buttonStartStop.setOnClickListener(view -> {
            if (!buttonStartStopStatus) {
                //startService(new Intent(getApplicationContext(), MonitoringService.class).putExtra("monitoringType", "START"));
                JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                PersistableBundle bundle = new PersistableBundle();
                bundle.putString("monitoringType", "START");

                JobInfo jobInfo = new JobInfo.Builder(11, new ComponentName(this, MonitoringService.class))
                        .setExtras(bundle)
                        .setOverrideDeadline(0)
                        .build();

                jobScheduler.schedule(jobInfo);
            } else {
                if (isMyServiceRunning(MonitoringService.class)) {
                    stopService(new Intent(this, MonitoringService.class));
                }
                //startService(new Intent(getApplicationContext(), MonitoringService.class).putExtra("monitoringType", "STOP"));
                JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                PersistableBundle bundle = new PersistableBundle();
                bundle.putString("monitoringType", "STOP");

                JobInfo jobInfo = new JobInfo.Builder(11, new ComponentName(this, MonitoringService.class))
                        .setExtras(bundle)
                        .setOverrideDeadline(0)
                        .build();

                jobScheduler.schedule(jobInfo);
            }
            changeButtonText();
        });

        buttonSend = findViewById(R.id.Button_send);
        buttonSend.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), DriveActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void batteryOptimizations(Context context) {
        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        }
    }

    private void requestPermissions() {

        if (!hasSensorPermissions(PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE_PERMISSIONS_ALL);
        } else {
            //startService(new Intent(getApplicationContext(), MonitoringService.class).putExtra("monitoringType", "CHECK"));
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            PersistableBundle bundle = new PersistableBundle();
            bundle.putString("monitoringType", "CHECK");

            JobInfo jobInfo = new JobInfo.Builder(11, new ComponentName(this, MonitoringService.class))
                    .setExtras(bundle)
                    .setOverrideDeadline(0)
                    .build();

            jobScheduler.schedule(jobInfo);
        }

        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission();
        }

        if (!hasReadWriteStorageAccess()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cannot read/write data");
            builder.setMessage("Cannot read/write data from " + getExternalStoragePath(this)
                    + " which may cause problems with functioning of this app. Please contact app supplier for further help.");

            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            });
            builder.show();
        }
    }

    private boolean hasReadWriteStorageAccess() {
        return isCsvStorageReadable()
                && isCsvStorageWritable()
                && isZipStorageReadable()
                && isZipStorageWritable();
    }

    private void requestUsageStatsPermission() {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

    private boolean hasPermissions() {
        return hasSensorPermissions(PERMISSIONS) && hasUsageStatsPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS_ALL) {
            //startService(new Intent(getApplicationContext(), MonitoringService.class).putExtra("monitoringType", "CHECK"));
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            PersistableBundle bundle = new PersistableBundle();
            bundle.putString("monitoringType", "CHECK");

            JobInfo jobInfo = new JobInfo.Builder(11, new ComponentName(this, MonitoringService.class))
                    .setExtras(bundle)
                    .setOverrideDeadline(0)
                    .build();

            jobScheduler.schedule(jobInfo);
        }
    }

    public void changeButtonText() {

        if (!buttonStartStopStatus) {
            buttonStartStop.setText(R.string.button_stop);
            buttonStartStopStatus = true;
        } else {
            buttonStartStop.setText(R.string.button_start);
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

    @Override
    protected void onPause() {
        super.onPause();
        saveUiState(this);
    }

    public static void saveUiState(@NotNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("switchSoundLevelMeter", switchSoundLevelMeter.isChecked());
        editor.putBoolean("switchGyroscope", switchGyroscope.isChecked());
        editor.putBoolean("switchAccelerometer", switchAccelerometer.isChecked());
        editor.putBoolean("switchGravity", switchGravity.isChecked());
        editor.putBoolean("switchLight", switchLight.isChecked());
        editor.putBoolean("switchMagneticField", switchMagneticField.isChecked());
        editor.putBoolean("switchScreenOnOff", switchScreenOnOff.isChecked());
        editor.putBoolean("switchSms", switchSms.isChecked());
        editor.putBoolean("switchCall", switchCall.isChecked());
        editor.putBoolean("switchBattery", switchBattery.isChecked());
        editor.putBoolean("switchAirplaneMode", switchAirplaneMode.isChecked());
        editor.putBoolean("switchNetwork", switchNetwork.isChecked());
        editor.putBoolean("switchForegroundApp", switchForegroundApp.isChecked());
        editor.putString("editTextDelay", editTextDelay.getText().toString());
        editor.putString("buttonStartStop", buttonStartStop.getText().toString());
        editor.putBoolean("buttonStartStopStatus", buttonStartStopStatus);
        editor.apply();
    }

    public static void loadUiState(@NotNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        switchSoundLevelMeter.setChecked(sharedPreferences.getBoolean("switchSoundLevelMeter", false));
        switchGyroscope.setChecked(sharedPreferences.getBoolean("switchGyroscope", false));
        switchAccelerometer.setChecked(sharedPreferences.getBoolean("switchAccelerometer", false));
        switchGravity.setChecked(sharedPreferences.getBoolean("switchGravity", false));
        switchLight.setChecked(sharedPreferences.getBoolean("switchLight", false));
        switchMagneticField.setChecked(sharedPreferences.getBoolean("switchMagneticField", false));
        switchScreenOnOff.setChecked(sharedPreferences.getBoolean("switchScreenOnOff", false));
        switchSms.setChecked(sharedPreferences.getBoolean("switchSms", false));
        switchCall.setChecked(sharedPreferences.getBoolean("switchCall", false));
        switchBattery.setChecked(sharedPreferences.getBoolean("switchBattery", false));
        switchAirplaneMode.setChecked(sharedPreferences.getBoolean("switchAirplaneMode", false));
        switchNetwork.setChecked(sharedPreferences.getBoolean("switchNetwork", false));
        switchForegroundApp.setChecked(sharedPreferences.getBoolean("switchForegroundApp", false));
        editTextDelay.setText(sharedPreferences.getString("editTextDelay", ""));
        buttonStartStop.setText(sharedPreferences.getString("buttonStartStop", "START"));
        buttonStartStopStatus = sharedPreferences.getBoolean("buttonStartStopStatus", false);
    }

    public static boolean isAppRunning(@NotNull Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (context.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }
        return false;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}