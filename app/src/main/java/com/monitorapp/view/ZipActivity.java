package com.monitorapp.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.monitorapp.R;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.util.regex.Pattern;

import static com.monitorapp.utils.FilePickerUtils.showFilePicker;
import static com.monitorapp.utils.StorageUtils.getCsvStoragePath;
import static com.monitorapp.utils.ZipUtils.makeZipFile;

public class ZipActivity extends Activity {

    private static final String TAG = "ZipActivity";
    private static final int REQUEST_CODE_ZIP_FILE = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, " : Started");
        showFilePicker(this, getCsvStoragePath(this), Pattern.compile(".*\\.(csv)$"), "Select a CSV file.", REQUEST_CODE_ZIP_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ZIP_FILE && resultCode == RESULT_OK) {
            String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            makeZipFile(this, this, path);
        } else {
            onBackPressed();
            this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, " : Destroyed");
    }
}