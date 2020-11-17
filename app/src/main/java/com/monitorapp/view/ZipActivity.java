package com.monitorapp.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import com.monitorapp.R;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class ZipActivity extends Activity {

    private static final int REQUEST_CODE_ZIP_FILE = 201;
    private static String password = "";

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
            makeZipFile(path);
        }

        finish();
    }

    public void makeZipFile(final String path) {
        Callable<File> cZipFile = () -> {
            File zipFile = null;
            try {
                if (password == "") {
                    zipFile = zipNoPassword(path);
                } else {
                    zipFile = zipWithPassword(path, password.toCharArray());
                }
            } catch (ZipException e) {
                e.printStackTrace();
            }
            return zipFile;
        };

        getPasswordThenZip(cZipFile);
    }

    private File zipWithPassword(final String file, final char[] password) throws ZipException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        Date date = new Date();
        String dateString = simpleDateFormat.format(date);

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

        String defaultLocation = this.getFilesDir().getParent();

        ZipFile zipFile = new ZipFile(defaultLocation + "/" + dateString + ".zip", password);
        zipFile.addFile(new File(file), zipParameters);

        return zipFile.getFile();
    }

    private File zipNoPassword(final String file) throws ZipException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        Date date = new Date();
        String dateString = simpleDateFormat.format(date);

        String defaultLocation = this.getFilesDir().getParent();

        ZipFile zipFile = new ZipFile(defaultLocation + "/" + dateString + ".zip");
        zipFile.addFile(new File(file));

        return zipFile.getFile();
    }

    public void getPasswordThenZip(final Callable<File> zipFunction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type in your password:");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                password = input.getText().toString();
                try {
                    zipFunction.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }
}
