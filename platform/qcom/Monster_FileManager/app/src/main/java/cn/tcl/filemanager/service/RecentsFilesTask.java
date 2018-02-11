/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.provider.MediaStore;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.manager.FileInfoComparator;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.MediaFile;
import cn.tcl.filemanager.utils.MediaFile.MediaFileType;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;

public class RecentsFilesTask extends BaseAsyncTask {

    private final ContentResolver mContentResolver;
    private final int mCategory;

    /**
     * Constructor for SearchTask
     *
     * @param fileInfoManager a instance of FileInfoManager, which manages information of
     *                        files in FileManager.
     * @param operationEvent  a instance of OperationEventListener, which is a interface
     *                        doing things before/in/after the task.
     * @param contentResolver the contentResolver for query(search).
     */
    public RecentsFilesTask(FileInfoManager fileInfoManager,
                            FileManagerService.OperationEventListener operationEvent, int category,
                            ContentResolver contentResolver, Context context) {
        super(context, fileInfoManager, operationEvent);
        mContentResolver = contentResolver;
        mCategory = category;
        mContext = context;
    }

    int dirprogress = 0;

    @Override
    protected Integer doInBackground(Void... params) {
        publishProgress(new ProgressInfo("", -1, -1));
        mStartOperationTime = System.currentTimeMillis();
        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        int progress = 0;
        Uri uri = MediaStore.Files.getContentUri("external");

        String[] projection = {MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MIME_TYPE};

        StringBuilder sb = new StringBuilder();
        sb.append(MediaStore.Files.FileColumns.TITLE + " not like ");
        DatabaseUtils.appendEscapedSQLString(sb, ".%");
        sb.append(" and ");
        /** Shield Hidden files */
        sb.append(MediaStore.Files.FileColumns.DATA + " not like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%/.%/%");
        sb.append(" and ");

        sb.append(MediaStore.Files.FileColumns.DATA + " not like ");
        DatabaseUtils.appendEscapedSQLString(sb, "null");

        sb.append(" and ");
        sb.append(MediaStore.Files.FileColumns.MEDIA_TYPE + " not like ");
        DatabaseUtils.appendEscapedSQLString(sb, "null");

        FileUtils.getALLTypeSql(sb);

        String selection = sb.toString();
        Cursor cursor = mContentResolver.query(uri, projection, selection,
                null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC LIMIT 2");

        if (cursor == null) {
            return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
        }
        int total = cursor.getCount();
        boolean firstItem = true;

        try {
            while (cursor.moveToNext()) {
                progress++;
                if (needUpdate() || progress == total) {
                    publishProgress(new ProgressInfo("", progress, total));
                }
                if (mCancelled) {
                    cancel(true);
                    onCancelled();
                    return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                }
                if (isCancelled()) {
                    ret = FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                    break;
                }
                String name = (String) cursor.getString(cursor
                        .getColumnIndex(MediaStore.Files.FileColumns.DATA));
                String mMimeType = (String) cursor
                        .getString(cursor
                                .getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE));
                FileInfo fileInfo = new FileInfo(mContext, name);
                if (mMimeType == null) {
                    MediaFileType fileType = MediaFile
                            .getFileType(fileInfo.getFileAbsolutePath());
                    if (fileType == null) {
                        mMimeType = null;
                    } else {
                        mMimeType = fileType.mimeType;
                    }
                }
                fileInfo.setFileMimeType(mMimeType);

                mFileInfoManager.addItem(fileInfo, firstItem);
                firstItem = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
            cursor.close();
        }

        if (ret != FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL) {
            mFileInfoManager.loadAllFileInfoList();
        }

        return ret;
    }

}
