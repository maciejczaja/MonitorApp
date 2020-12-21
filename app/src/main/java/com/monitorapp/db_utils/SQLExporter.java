package com.monitorapp.db_utils;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import com.monitorapp.BuildConfig;
import com.opencsv.CSVWriter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.monitorapp.utils.StorageUtils.csvDirectory;

public class SQLExporter extends Service {

    private static final String TAG = "SQL Exporter";

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Service on create");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Service onStartCommand");
        }

        new Thread(() -> {
            DatabaseHelper dbHelper = DatabaseHelper.getHelper(getApplicationContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            File targetDir = createDirectory(csvDirectory.toString());
            String fileName = generateFileName();
            File targetFile = new File(targetDir, fileName);
            try {
                targetFile.createNewFile();
            } catch (IOException e){
                e.printStackTrace();
            }
            List<String> tables = DatabaseHelper.getTableNames();
            try {
                writeCSV(targetFile, db, tables);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.close();
            }

            stopSelf();
        }).start();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Service onBind");
        }
        return null;
    }

    @Override
    public void onDestroy() {

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Service onDestroy");
        }
    }

    @NotNull
    public static File createDirectory(String path) {
        File dir = new File(path);
        boolean result = true;
        if (!dir.exists() ) {
            result = dir.mkdir();
        }
        System.out.println(result);
        return dir;
    }

    private static void writeSingleValue(@NotNull CSVWriter writer, String value) {
        writer.writeNext(new String[]{value});
    }

    private static void writeCSV(File file, @NotNull SQLiteDatabase db, @NotNull List<String> tables) {
        CSVWriter csvWrite = null;
        Cursor csvCursor = null;
        try {
            csvWrite = new CSVWriter(new FileWriter(file));
            writeSingleValue(csvWrite, "dbVersion = " + db.getVersion());
            for (String table: tables) {
                writeSingleValue(csvWrite, "table=" + table);
                if (table.equals("Motion_sensors") || table.equals("Data") || table.equals("Call_states") ||
                        table.equals("States_on_off") || table.equals("Types_on_off")) {
                    csvCursor = db.rawQuery("SELECT * FROM " + table, null);
                } else {
                    csvCursor = db.rawQuery("SELECT * FROM " + table + " LEFT JOIN Data ON " + table + ".id == Data.id", null);
                }
                csvWrite.writeNext(csvCursor.getColumnNames());
                while(csvCursor.moveToNext()) {
                    int columns = csvCursor.getColumnCount();
                    String[] columnArr = new String[columns];
                    for (int i = 0; i < columns; i++) {
                        columnArr[i] = csvCursor.getString(i);
                    }
                    csvWrite.writeNext(columnArr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (csvWrite != null) {
                try {
                    csvWrite.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (csvCursor != null) {
                csvCursor.close();
            }
        }
    }

    @NotNull
    public static String generateFileName() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
        Date date = new Date(System.currentTimeMillis());
        return "database_export_" + formatter.format(date) + ".csv";
    }
}
