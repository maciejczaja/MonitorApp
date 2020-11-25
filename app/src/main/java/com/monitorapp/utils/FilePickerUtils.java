package com.monitorapp.utils;

import android.app.Activity;

import com.nbsp.materialfilepicker.MaterialFilePicker;

import java.util.regex.Pattern;

public class FilePickerUtils {

    public static void showFilePicker(Activity activity, String startPath, Pattern pattern, String title, int requestCode) {
        new MaterialFilePicker()
                .withActivity(activity)
                .withCloseMenu(true)
                .withPath(startPath)
                .withFilter(pattern)
                .withFilterDirectories(false)
                .withTitle(title)
                .withRequestCode(requestCode)
                .start();
    }
}
