package com.monitorapp.utils;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class StorageUtils {

    private static final String TAG = "StorageUtils";
    public static File csvDirectory;
    public static File zipDirectory;

    public static File csvMkdir(Context context) {
        csvDirectory = new File(getCsvStoragePath(context));
        if (!csvDirectory.exists()) {
            csvDirectory.mkdirs();
            Log.d(TAG, " : Default csv directory created.");
        }
        return csvDirectory;
    }

    public static File zipMkdir(Context context) {
        zipDirectory = new File(getZipStoragePath(context));
        if (!zipDirectory.exists()) {
            zipDirectory.mkdirs();
            Log.d(TAG, " : Default zip directory created.");
        }
        return zipDirectory;
    }

    @NotNull
    public static String getExternalStoragePath(@NotNull Context context) {
        return context.getExternalFilesDir(null).toString();
    }

    @NotNull
    public static String getCsvStoragePath(Context context) {
//        return getExternalStoragePath(context) + "/Android/data/" + PACKAGE_NAME + "/csv/";
        return getExternalStoragePath(context) + "/csv/";
    }

    @NotNull
    public static String getZipStoragePath(Context context) {
//        return getExternalStoragePath(context) + "/Android/data/" + PACKAGE_NAME + "/zip/";
        return getExternalStoragePath(context) + "/zip/";
    }

    public static boolean isCsvStorageWritable() {
        return csvDirectory.canWrite();
    }

    public static boolean isZipStorageWritable() {
        return zipDirectory.canWrite();
    }

    public static boolean isCsvStorageReadable() {
        return csvDirectory.canRead();
    }

    public static boolean isZipStorageReadable() {
        return zipDirectory.canRead();
    }
}