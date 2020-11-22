package com.monitorapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

import static android.app.Activity.RESULT_OK;

public class ZipUtils {

    private static final String TAG = "ZipUtils";
    private static String password = "";

    public static void makeZipFile(@NotNull final Activity activity, final Context context, final String path) {
        Callable<File> cZipFile = () -> {
            File zipFile = null;
            try {
                if(password == "") {
                    zipFile = zipNoPassword(activity, path);
                }
                else {
                    zipFile = zipWithPassword(activity, path, password.toCharArray());
                }
            } catch (ZipException e) {
                Log.d(TAG, "Exception caused by ZIP file: " + zipFile.toString());
                Toast.makeText(activity, "Making ZIP file failed!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            Log.d(TAG, "ZIP file made in location: " + zipFile.toString());
            Toast.makeText(activity, "Making ZIP file " + zipFile.toString() + " finished successfully!", Toast.LENGTH_LONG).show();
            activity.finish();
            return zipFile;
        };

        getPasswordFromDialog(context, cZipFile);
    }

    private static File zipWithPassword(@NotNull final Activity activity, final String file, final char[] password) throws ZipException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        Date date = new Date();
        String dateString = simpleDateFormat.format(date);

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

        String defaultLocation = activity.getFilesDir().getParent();

        ZipFile zipFile = new ZipFile(defaultLocation + "/" + dateString + ".zip", password);
        zipFile.addFile(new File(file), zipParameters);

        return zipFile.getFile();
    }

    private static File zipNoPassword(@NotNull final Activity activity, final String file) throws ZipException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        Date date = new Date();
        String dateString = simpleDateFormat.format(date);

        String defaultLocation = activity.getFilesDir().getParent();

        ZipFile zipFile = new ZipFile(defaultLocation + "/" + dateString + ".zip");
        zipFile.addFile(new File(file));

        return zipFile.getFile();
    }

    public static void getPasswordFromDialog(Context context, final Callable<File> zipFunction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Type in your password:");
        final EditText input = new EditText(context);
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
