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
import android.util.Log;

import cn.tcl.filemanager.manager.FileInfoComparator;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;

public class SearchTask extends BaseAsyncTask {

    private final String mSearchName;
    private final String mPath;
    private final ContentResolver mContentResolver;
    private List<FileInfo> mSearchResult;

    public SearchTask(Context context, FileInfoManager fileInfoManager, FileManagerService.OperationEventListener operationEvent,
            String searchName, String path, ContentResolver contentResolver) {
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

		String[] projection = { MediaStore.Files.FileColumns.DATA,
				MediaStore.Files.FileColumns.MIME_TYPE };

        StringBuilder sb = new StringBuilder();

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

        String path = mPath;
        String separator = File.separator;

        if (!(searchText.toString().trim()).equals(mSearchName.trim())) {
        	path = mPath.replace("/", "//");
        	separator = "//";
        }

        String data = path + separator + "%" + searchText.toString() + "%";

        sb.append(MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, data);

        if (!(searchText.toString().trim()).equals(mSearchName.trim())) {
        	sb.append(" escape '/'");
        }

		// ADD START FOR PR431716 BY HONGBIN.CHEN 20150721
        boolean isShowHidden = SharedPreferenceUtils.isShowHidden(mContext);
        if (!isShowHidden) {
	        sb.append(" and ").append(MediaStore.Files.FileColumns.DATA + " not like ");
	        String hideString = mPath + "%" + File.separator + ".%";
	        DatabaseUtils.appendEscapedSQLString(sb, hideString);
        }
		// ADD END FOR PR431716 BY HONGBIN.CHEN 20150721

        String selection = sb.toString();

        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(uri, projection, selection, null, null);
        } catch (Exception e) {
            Log.d("SearchTask", "Exception occurred when do query in doInBackground: ", e);
        }

		if (cursor == null) {
			return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
		}

		int total = cursor.getCount();
		publishProgress(new ProgressInfo("", 0, total));
		int progress = 0;
		try {
			int dataIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
			int mimeTypeIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);

            List<FileInfo> searchList = new ArrayList<FileInfo>();

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
				String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);

				if (fileName.toLowerCase().contains(mSearchName.toLowerCase())) {
					info.setFileMimeType(mMimeType);
					searchList.add(info);
				}

				publishProgress(new ProgressInfo(info, ++progress, total));
			}

            for (FileInfo fi : searchList) {
                mSearchResult.add(fi);
            }

			if (mSearchResult.size() > 0) {
				mFileInfoManager.addItemList(mSearchResult);
                mFileInfoManager
                        .updateSearchList(FileInfoComparator.SORT_BY_TYPE);
			}
		} catch (Exception e) {
            Log.d("SearchTask", "Exception occurred when handle search result in doInBackground: ", e);
        } finally {
            if (cursor!=null) {
                cursor.close();
            }
		}
        return ret;
    }

}
