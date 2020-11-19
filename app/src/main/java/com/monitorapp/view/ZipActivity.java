package com.monitorapp.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.monitorapp.R;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.util.regex.Pattern;

import static com.monitorapp.utils.ZipUtils.makeZipFile;

public class ZipActivity extends Activity {

    private static final String TAG = "ZipActivity";
    private static final int REQUEST_CODE_ZIP_FILE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showFilePicker();
    }

    public void showFilePicker() {
        new MaterialFilePicker()
                .withActivity(this)
                .withCloseMenu(true)
                .withFilter(Pattern.compile(".*\\.(csv)$"))
                .withFilterDirectories(false)
                .withTitle("Select a CSV file.")
                .withRequestCode(REQUEST_CODE_ZIP_FILE)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ZIP_FILE && resultCode == RESULT_OK) {
            String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            makeZipFile(this, this, path);
        }
    }
}
