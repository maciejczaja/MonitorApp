package com.monitorapp.services;

import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.monitorapp.db_utils.DatabaseHelper;
import com.monitorapp.db_utils.UserIDStore;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.monitorapp.view.MainActivity.PACKAGE_NAME;
import static com.monitorapp.view.MainActivity.isAppRunning;

public class ForegroundAppService extends Service {

    private static final String TAG = "ForegroundAppService";
    private long delay;
    DatabaseHelper dbHelper;

    private Thread foregroundAppServiceThread = new Thread(() -> {
        while (true) {
            try {
                final String foregroundProcessName = getForegroundProcessName();
                Date currentDateTime = Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
                Log.d(TAG, foregroundProcessName + " registered at " + currentDateTime.toString());
                dbHelper.addRecordAppData(foregroundProcessName, sdf.format(date), UserIDStore.id(getApplicationContext()));
                Thread.sleep(delay);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        /* TODO: idk? */
        Log.d(TAG, ": created");
        super.onCreate();
        dbHelper = DatabaseHelper.getHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(@NotNull Intent intent, int flags, int startId) {
        delay = intent.getLongExtra("DELAY", 5);
        delay *= 1000; //to milliseconds

        foregroundAppServiceThread.start();
        Log.d(TAG, ": thread started");

        /* restart Service with null passed when terminated by OS */
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        foregroundAppServiceThread.interrupt();
        super.onDestroy();
        Log.d(TAG, ": destroyed, thread interrupted");
    }

    private String getForegroundProcessName() {

        String foregroundApp = null;
        UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService(Service.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();

        UsageEvents usageEvents = usageStatsManager.queryEvents(time - 1000 * 3600, time);
        UsageEvents.Event event = new UsageEvents.Event();
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                foregroundApp = event.getPackageName();
            }
        }

        return foregroundApp;
    }
}
