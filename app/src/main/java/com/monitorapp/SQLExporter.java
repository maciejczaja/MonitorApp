package com.monitorapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.util.List;
import com.opencsv.CSVWriter;

public class SQLExporter {

    public static boolean isExternalStorageWritable() {
        //check if external storage is writable
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static File createDirectory(String path) {
        File dir = new File(path);
        boolean result = true;
        if (!dir.exists() ) {
            result = dir.mkdir();
        }
        System.out.println(result);
        return dir;
    }

    public static Cursor getAllData(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("select * from my_stats", null);
        return cursor;
    }

    public static String getExternalStorage() {
        //if (android.os.Build.VERSION.SDK_INT < 30)
            return Environment.getExternalStorageDirectory().toString();
        //else
            //return getApplicationContext().getExternalFilesDir();
    }

    private static void writeSingleValue(CSVWriter writer, String value) {
        writer.writeNext(new String[]{value});
    }

    private static void writeCSV(File file, SQLiteDatabase db, List<String> tables) {
        CSVWriter csvWrite = null;
        Cursor csvCursor = null;
        try {
            csvWrite = new CSVWriter(new FileWriter(file));
            writeSingleValue(csvWrite, "dbVersion = " + db.getVersion());
            for (String table: tables) {
                writeSingleValue(csvWrite, "table=" + table);
                csvCursor = db.rawQuery("SELECT * FROM " + table, null);
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

    public static String generateFileName() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String name = "database_export_" + formatter.format(date) + ".csv";
        return name;
    }

    public static String export(SQLiteDatabase db) throws IOException {
        if (!isExternalStorageWritable()) {
            throw new IOException("Cannot write to external storage");
        }
        File targetDir = createDirectory(Environment.getExternalStorageDirectory().toString());
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

        return targetFile.getAbsolutePath();
    }
}
