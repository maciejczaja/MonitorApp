package com.monitorapp.services;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.monitorapp.db_utils.DatabaseHelper;
import com.monitorapp.db_utils.UserIDStore;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ForegroundAppService extends Service {

    private static final String TAG = "ForegroundAppService";
    private long delay;
    DatabaseHelper dbHelper;

    private Thread foregroundAppServiceThread = new Thread(() -> {
        while (true) {
            try {
//                final String foregroundProcessName = getForegroundProcessName();
                final String foregroundProcessName = getForegroundProcessNameNew();
                Date currentDateTime = Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
                Log.d(TAG, foregroundProcessName + " registered at " + currentDateTime.toString());
                dbHelper.addRecordAppData(foregroundProcessName, sdf.format(date), UserIDStore.id(getApplicationContext()));
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
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
        super.onCreate();
        dbHelper = new DatabaseHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(@NotNull Intent intent, int flags, int startId) {
        delay = intent.getLongExtra("DELAY", 5);
        delay *= 1000; //to milliseconds
        foregroundAppServiceThread.start();

        /* restart Service with null passed when terminated by OS */
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        foregroundAppServiceThread.interrupt();
        super.onDestroy();
    }

    /*
    foreground app check logic
    TODO: refactoring
    */
    private String getForegroundProcessName() {

        final int PROCESS_STATE_TOP = 2;

        ActivityManager.RunningAppProcessInfo currentInfo = null;
        Field field = null;
        try {
            field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
        } catch (Exception ignored) {
        }
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo app : appList) {
            if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && app.importanceReasonCode == ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN) {
                Integer state = null;
                try {
                    state = field.getInt(app);
                } catch (Exception e) {
                }
                if (state != null && state == PROCESS_STATE_TOP) {
                    currentInfo = app;
                    break;
                }
            }
        }
        return currentInfo.processName;
    }

    private String getForegroundProcessNameNew() {

        String foregroundApp = null;
        UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService(Service.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();

        UsageEvents usageEvents = usageStatsManager.queryEvents(time - 1000 * 3600, time);
        UsageEvents.Event event = new UsageEvents.Event();
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                foregroundApp = event.getPackageName();
            }
        }

        return foregroundApp;
    }
}
