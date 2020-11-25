package com.monitorapp.utils;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveUtils {

    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private Drive mDriveService;

    public DriveUtils(Drive mDriveService) {
        this.mDriveService = mDriveService;
    }

    public Task<String> createZipFileForDrive(String filePath) {
        return Tasks.call(mExecutor, () -> {
            File fileMetaData = new File();

            String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
            fileMetaData.setName(filename.replaceFirst("[.][^.]+$", ""));

            java.io.File file = new java.io.File(filePath);

            FileContent mediaContent = new FileContent("application/zip", file);
            File myFile = null;

            try {
                myFile = mDriveService.files().create(fileMetaData, mediaContent).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (myFile == null) {
                throw new IOException("File creation resulted with null.");
            }

            return myFile.getId();
        });
    }
}
