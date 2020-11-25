package com.monitorapp.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.util.regex.Pattern;

import static com.monitorapp.utils.FilePickerUtils.showFilePicker;
import static com.monitorapp.utils.StorageUtils.getZipStoragePath;
import static com.monitorapp.view.DriveActivity.getDriveUtils;

public class FileUploadActivity extends Activity {

    private static final int REQUEST_CODE_FILE_UPLOAD = 301;
    private static final String TAG = "FileUploadActivity";

    private String filePath = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " : Started");

        showFilePicker(this, getZipStoragePath(this), Pattern.compile(".*\\.(zip)$"), "Select ZIP file for upload.", REQUEST_CODE_FILE_UPLOAD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FILE_UPLOAD && resultCode == RESULT_OK) {
            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            uploadZipFile();
        } else {
            onBackPressed();
            this.finish();
        }
    }

    public void uploadZipFile() {
        getDriveUtils().createZipFileForDrive(filePath).addOnSuccessListener(s -> {
            Toast.makeText(this, "Upload successful.", Toast.LENGTH_SHORT).show();
            this.finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Upload failed.", Toast.LENGTH_SHORT).show();
            this.finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, " : Destroyed");
    }
}
