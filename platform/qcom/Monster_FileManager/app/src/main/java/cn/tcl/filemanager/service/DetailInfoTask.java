/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import java.io.File;

import android.content.Context;

import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.utils.FileInfo;

public class DetailInfoTask extends BaseAsyncTask {

    private final FileInfo mDetailfileInfo;

    /**
     * Constructor of DetailInfoTask
     *
     * @param fileInfoManager a instance of FileInfoManager, which manages
     *            information of files in FileManager.
     * @param operationEvent a instance of OperationEventListener, which is a
     *            interface doing things before/in/after the task.
     * @param file a instance of FileInfo, which contains all data about a file.
     */
    public DetailInfoTask(Context context, FileInfoManager fileInfoManager, FileManagerService.OperationEventListener operationEvent,
            FileInfo file) {
        super(context, fileInfoManager, operationEvent);
        mDetailfileInfo = file;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        if (mDetailfileInfo.isDirectory()) {
            publishProgress(new ProgressInfo("", 0, getSize(mDetailfileInfo.getFile())));
        } else {
            publishProgress(new ProgressInfo("", 0, mDetailfileInfo.getFileSize()));
        }
        return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
    }

    private static long getSize(File file) {
        long folderSize = 0;
        try {
            if (!file.exists()) {
                String message = file + " does not exist";
                throw new IllegalArgumentException(message);
            }
            if (file.isDirectory()) {
                folderSize = sizeOfDirectory(file);
            } else
                folderSize = file.length();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return folderSize;
    }

    private static long sizeOfDirectory(File directory) {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        long size = 0;

        File[] files = directory.listFiles();
        if (files == null) {
            return 0L;
        }
        //add by long.tang@tcl.com
        int len = files.length;
        for (int i = 0; i < len; i++) {
            File file = files[i];

            if (file.isDirectory()) {
                size += sizeOfDirectory(file);
            } else {
                size += file.length();
            }
        }
        return size;
    }
}
