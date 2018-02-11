/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import java.io.File;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;

public class SetDetailsSizeTextTask extends AsyncTask<FileInfo, Integer, String> {
    private TextView mTextView;
    private FileInfo mDetailfileInfo;
    private Context mContext;

    public SetDetailsSizeTextTask(TextView textView, FileInfo fileInfo, Context context) {
        mTextView = textView;
        mDetailfileInfo = fileInfo;
        mContext = context;
    }

    @Override
    protected String doInBackground(FileInfo... inFileInfo) {
        String sizeString = null;
        mDetailfileInfo = inFileInfo[0];
        if (!mDetailfileInfo.isDirectory()) {
            sizeString = FileUtils.sizeToString(mContext, mDetailfileInfo.getFileSize());
        }

        return sizeString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        mTextView.setText(result);
    }

    public static long getSize(File file) {
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

    public static long sizeOfDirectory(File directory) {
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
