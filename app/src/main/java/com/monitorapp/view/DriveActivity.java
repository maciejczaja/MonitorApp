package com.monitorapp.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.monitorapp.utils.DriveUtils;

import java.util.Collections;

public class DriveActivity extends Activity {

    private static final String TAG = "DriveActivity";

    private static final int REQUEST_CODE_SIGN_IN_INTENT = 401;

    private static DriveUtils driveUtils;

    public static DriveUtils getDriveUtils() {
        return driveUtils;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " : Started");

        requestGoogleSignIn();
    }

    private void requestGoogleSignIn() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, ": result code = " + resultCode);
        if (requestCode == REQUEST_CODE_SIGN_IN_INTENT && resultCode == RESULT_OK) {
            handleSignInIntent(data);
            startActivity(new Intent(this, FileUploadActivity.class));
        } else if (requestCode == REQUEST_CODE_SIGN_IN_INTENT && resultCode == RESULT_CANCELED) {
            runOnUiThread(() -> Toast.makeText(DriveActivity.this, "Failed to connect with Google services.", Toast.LENGTH_LONG).show());
        }
    }

    private void handleSignInIntent(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(googleSignInAccount -> {
                    GoogleAccountCredential googleAccountCredential = GoogleAccountCredential
                            .usingOAuth2(DriveActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));

                    googleAccountCredential.setSelectedAccount(googleSignInAccount.getAccount());
                    Drive googleDriveService = new Drive.Builder(
                            new NetHttpTransport(),
                            new GsonFactory(),
                            googleAccountCredential)
                            .setApplicationName("MonitorApp")
                            .build();

                    driveUtils = new DriveUtils(googleDriveService);
                    this.finish();
                })
                .addOnFailureListener(e -> {
                    this.finish();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, " : Destroyed");
    }
}
