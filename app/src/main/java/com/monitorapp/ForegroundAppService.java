package com.monitorapp;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ForegroundAppService extends Service {

    private static final String TAG = "ForegroundAppService";
    private long delay;

    private Thread foregroundAppServiceThread = new Thread(() -> {
        while (true) {
            try {
                final String foregroundProcessName = getForegroundProcessName();
                Date currentDateTime = Calendar.getInstance().getTime();
                Log.d(TAG, foregroundProcessName + " registered at " + currentDateTime.toString());
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
}