package com.monitorapp;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class NoiseDetector extends Service {

    private MediaRecorder mRecorder = null;
    public static float dbCount = 40; //start value in dB
    Thread tmic;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
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

            tmic = new Thread() {
                public void run() {

                    while (true) {
                        try {
                            int volume = mRecorder.getMaxAmplitude();
                            if (volume > 0) {
                                dbCount = 20 * (float) (Math.log10(volume));
                            }

                            Log.d("MICROPHONE", String.valueOf(volume) + " " + String
                                    .valueOf(dbCount));

                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }

                }
            };
            tmic.start();

        }
        return START_STICKY;
    }

    public void stop() {
        tmic.interrupt();
        if (mRecorder != null) {
            mRecorder.stop();
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