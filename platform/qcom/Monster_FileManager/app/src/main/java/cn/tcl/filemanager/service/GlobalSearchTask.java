/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.filemanager.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.MediaStore;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.manager.FileInfoComparator;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;

public class GlobalSearchTask extends BaseAsyncTask {

    private final String mSearchName;
    private final ArrayList<String> mPath;
    private final ContentResolver mContentResolver;
    private List<FileInfo> mSearchResult;

    public GlobalSearchTask(Context context, FileInfoManager fileInfoManager,
            FileManagerService.OperationEventListener operationEvent,
            String searchName, ArrayList<String> path, ContentResolver contentResolver) {
        super(context, fileInfoManager, operationEvent);
        mContentResolver = contentResolver;
        mPath = path;
        mSearchName = searchName;
        mSearchResult = new ArrayList<FileInfo>();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;

        Uri uri = MediaStore.Files.getContentUri("external");

        String[] projection = {
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MIME_TYPE
        };
        StringBuilder searchText = new StringBuilder();
        int len = mSearchName.length();
        for (int i = 0; i < len; i++) {
            char c = mSearchName.charAt(i);
            if ((c == '_') || (c == '%')) {
                searchText.append('/');
                searchText.append(c);
            } else {
                searchText.append(c);
            }
        }

        if (null != mSearchResult) {
            mSearchResult.clear();
        }

        for (int i = 0; i < mPath.size(); i++)
        {
            StringBuilder sb = new StringBuilder();
            String path = mPath.get(i);
            String separator = File.separator;
            if (!(searchText.toString().trim()).equals(mSearchName.trim())) {
                path = mPath.get(i).replace("/", "//");
                separator = "//";
            }
            String data = path + separator + "%" + searchText.toString() + "%";
            sb.append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, data);
            if (!(searchText.toString().trim()).equals(mSearchName.trim())) {
                sb.append(" escape '/'");
            }
            sb.append(" and ");
            sb.append(MediaStore.Files.FileColumns.TITLE + " not like ");
            DatabaseUtils.appendEscapedSQLString(sb, ".%");
            sb.append(" and ");
            /** Shield Hidden files */
            sb.append(MediaStore.Files.FileColumns.DATA + " not like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%/.%/%");
            sb.append(" and ");

            sb.append(MediaStore.Files.FileColumns.DATA + " not like ");
            DatabaseUtils.appendEscapedSQLString(sb, "null");

            String selection = sb.toString();
            Cursor cursor = mContentResolver.query(uri, projection, selection, null, MediaStore.Files.FileColumns.MEDIA_TYPE + " ASC ");
            if (cursor == null) {
                return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
            }
            int total = cursor.getCount();
            publishProgress(new ProgressInfo("", 0, total));
            int progress = 0;
            try {
                int dataIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int mimeTypeIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);

                while (cursor.moveToNext()) {
                    if (mCancelled) {
                        cancel(true);
                        onCancelled();
                        return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                    }
                    if (isCancelled()) {
                        return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                    }
                    String filePath = cursor.getString(dataIdx);
                    String mMimeType = cursor.getString(mimeTypeIdx);

                    FileInfo info = new FileInfo(mContext, filePath);
                    String fileName = info.getShowName();

                    if (fileName.toLowerCase().contains(mSearchName.toLowerCase())) {
                        info.setFileMimeType(mMimeType);
                        if (mMimeType != null && !FileInfo.MIMETYPE_EXTENSION_UNKONW.equals(mMimeType) &&
                                (mMimeType.contains(FileInfo.MIME_HAED_IMAGE) ||
                                        mMimeType.contains(FileInfo.MIME_HEAD_AUDIO) ||
                                        mMimeType.contains(FileInfo.MIME_HEAD_VIDEO) ||
                                        mMimeType.contains(FileInfo.MIMETYPE_APK))) {
                            mSearchResult.add(0, info);
                        } else {
                            mSearchResult.add(info);
                        }
                    }
                    publishProgress(new ProgressInfo(info, ++progress, total));
                }

            } finally {
                if (cursor!=null) {
                    cursor.close();
                }
            }
        }
        if (mSearchResult.size() > 0) {
            ((FileManagerApplication)mContext.getApplicationContext()).mSearchResultList = mSearchResult;
            mFileInfoManager.addItemList(mSearchResult);
            mFileInfoManager
                    .updateSearchList(FileInfoComparator.SORT_BY_TYPE);
        }
        return ret;
    }
}
