/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StatFs;
import android.view.View;
import android.widget.TextView;

import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.R;

public class SetSizeTextTask extends AsyncTask<FileInfo, Integer, String> {
    private TextView mTextView;
    private FileInfo mFileInfo;
    private Context mContext;
    private boolean mDetails;

    public SetSizeTextTask(TextView textView, FileInfo fileInfo, Context context, boolean details) {
        mTextView = textView;
        mFileInfo = fileInfo;
        mContext = context;
        mDetails = details;
    }

    @Override
    protected String doInBackground(FileInfo... inFileInfo) {
        String doString = null;
        long blocSize = 0;
        long availaBlock = 0;
        mFileInfo = inFileInfo[0];
        if (mFileInfo.isDirectory()) {
            if (MountManager.getInstance().isMountPoint(mFileInfo.getFileAbsolutePath())) {
                String freeSpaceString = null;
                String totalSpaces = null;
                StringBuilder sb = new StringBuilder();
//                try {
//                    StatFs statfs = new StatFs(mFileInfo.getFileAbsolutePath());
//                    try {
//                        blocSize = statfs.getBlockSizeLong();
//                        availaBlock = statfs.getAvailableBlocksLong();
//                    } catch (NoSuchMethodError e) {
//                        blocSize = statfs.getBlockSizeLong();
//                        availaBlock = statfs.getAvailableBlocksLong();
//                    }
//                    freeSpaceString = FileUtils.sizeToString(mContext, availaBlock * blocSize);
//                    long totalSpace = mFileInfo.getFile().getTotalSpace();
//                    totalSpaces = FileUtils.sizeToString(mContext, totalSpace);
//                } catch (Exception e) {
//                    freeSpaceString = mContext.getResources().getString(
//                            R.string.unknown);
//                    totalSpaces = mContext.getResources().getString(
//                            R.string.unknown);
//                }
//                sb.append(
//                        mContext.getResources().getString(R.string.free_space))
//                        .append(" ");
//                sb.append(freeSpaceString).append(" \n");
//                sb.append(
//                        mContext.getResources().getString(R.string.total_space))
//                        .append(" ");
				StatFs statfs = new StatFs(mFileInfo.getFileAbsolutePath());
				try {
					blocSize = statfs.getBlockSizeLong();
					availaBlock = statfs.getAvailableBlocksLong();
				} catch (NoSuchMethodError e) {
					blocSize = statfs.getBlockSizeLong();
					availaBlock = statfs.getAvailableBlocksLong();
				}
				freeSpaceString = FileUtils.sizeToString(mContext, availaBlock * blocSize);
				long totalSpace = mFileInfo.getFile().getTotalSpace();
				totalSpaces = FileUtils.sizeToString(mContext, totalSpace);
				sb.append(mContext.getResources().getString(R.string.free_space)).append(" ");
				sb.append(freeSpaceString).append(" \n");
				sb.append(mContext.getResources().getString(R.string.total_space)).append(" ");
                sb.append(totalSpaces).append(" ");
            }
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(mContext.getResources().getString(R.string.size)).append(" ");
            sb.append(mFileInfo.getFileSizeStr());
        }
        return doString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        mTextView.setText(result);
        if (mDetails) {
            mTextView.setVisibility(View.VISIBLE);
        } else {
            mTextView.setVisibility(View.GONE);
        }
    }
}
