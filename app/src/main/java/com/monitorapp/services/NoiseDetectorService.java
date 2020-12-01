package com.monitorapp.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import com.monitorapp.db_utils.DatabaseHelper;
import com.monitorapp.db_utils.UserIDStore;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class NoiseDetectorService extends Service {

    private MediaRecorder mRecorder = null;
    public static float dbCount = 40; //start value in dB
    Thread tMic;
    DatabaseHelper dbHelper;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        dbHelper = DatabaseHelper.getHelper(getApplicationContext());
        if (mRecorder == null) {
            try {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile("/dev/null/");
                mRecorder.prepare();
                mRecorder.start();

            } catch (IOException exception) {
                stop();
                exception.printStackTrace();
            }

            tMic = new Thread() {
                public void run() {

                    while (true) {
                        try {
                            int volume = mRecorder.getMaxAmplitude();
                            if (volume > 0)
                                dbCount = 20 * (float) (Math.log10(volume));

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                            Date date = new Date(System.currentTimeMillis());

                            Log.d("MICROPHONE", String.valueOf(volume) + " " + String
                                    .valueOf(dbCount));
                            dbHelper.addRecordNoiseDetectorData(sdf.format(date), UserIDStore.id(getApplicationContext()), volume, dbCount);

                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            };
            tMic.start();

        }
        return START_STICKY;
    }

    public void stop() {
        tMic.interrupt();
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    @Override
    public void onDestroy() {
        stop();
        super.onDestroy();
    }
}