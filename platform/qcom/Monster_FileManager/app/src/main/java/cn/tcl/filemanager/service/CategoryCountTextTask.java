/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import java.io.File;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.widget.TextView;

import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.utils.CommonUtils;
//import FavoriteHelper;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;

public class CategoryCountTextTask extends AsyncTask<FileInfo, Integer, String> {
    private static String TAG = "CategoryCountTextTask";
    private TextView mTextView;
    private ContentResolver mContentResolver;
    private Context mContext;
    private int mPosition;
    private String mCurrentText;

    public CategoryCountTextTask(TextView textView,
                                 ContentResolver contentResolver, Context context, int position,
                                 String currentText) {
        mTextView = textView;
        mContentResolver = contentResolver;
        mContext = context;
        mPosition = position;
        mCurrentText = currentText;
    }

    @Override
    protected String doInBackground(FileInfo... inFileInfo) {
        String sizeString = null;
        int count = 0;

        switch (mPosition) {
//		case CategoryManager.CATEGORY_PHOTOS:
		case CategoryManager.CATEGORY_PICTURES:
		case CategoryManager.CATEGORY_VEDIOS:
		case CategoryManager.CATEGORY_MUSIC:
		case CategoryManager.CATEGORY_DOCS:
		case CategoryManager.CATEGORY_APKS:
			// case CategoryManager.CATEGORY_ARCHIVES:
			count = getCountFromMedia();
			break;
		case CategoryManager.CATEGORY_DOWNLOAD:
		case CategoryManager.CATEGORY_BLUETOOTH:
			// case CategoryManager.CATEGORY_RINGTONES:
			count = getCountFromFiles();
			break;
		// case CategoryManager.CATEGORY_RECORDINGS:
		// count = getRecorderCount();
		// break;
		// case CategoryManager.CATEGORY_FAVORITE:
		// count = getFavoriteCount();
		// break;
		// case CategoryManager.CATEGORY_SAFEBOX:
		// count = 0;
		default:
			break;
		}
		//CategoryManager.setCategoryCount(mPosition, count);
		sizeString = "(" + count + ")";
		return sizeString;
	}

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (!mCurrentText.equals(result)) {
            LogUtils.i(TAG, "result != mCurrentText : result = " + result
                    + ", mCurrentText = " + mCurrentText + ", mPosition = "
                    + mPosition);
            mTextView.setText(result);
        }
    }

    private int getCountFromMedia() {
        Uri uri = null;
        String[] projection = {MediaStore.Files.FileColumns.DATA,};
        int count = 0;

        if (CategoryManager.CATEGORY_PICTURES == mPosition) {
            uri = Images.Media.EXTERNAL_CONTENT_URI;
        } else if (CategoryManager.CATEGORY_VEDIOS == mPosition) {
            uri = Video.Media.EXTERNAL_CONTENT_URI;
        } else if (CategoryManager.CATEGORY_MUSIC == mPosition) {
            uri = Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            uri = MediaStore.Files.getContentUri("external");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(MediaStore.Files.FileColumns.TITLE + " not like ");
        DatabaseUtils.appendEscapedSQLString(sb, ".%");
        sb.append(" and ").append(
                MediaStore.Files.FileColumns.DISPLAY_NAME + " not like ");
        DatabaseUtils.appendEscapedSQLString(sb, ".%");
        // add for PR821018 by yane.wang@jrdcom.com 20141029 begin
        sb.append(" and ").append(
                MediaStore.Files.FileColumns.DATA + " not like ");
        DatabaseUtils.appendEscapedSQLString(sb, "null");
        // add for PR821018 by yane.wang@jrdcom.com 20141029 end
        // add for PR835073 by yane.wang@jrdcom.com 20141110 begin
//		if (CategoryManager.CATEGORY_MUSIC == mPosition) {
//			sb.append(" and ").append(
//					MediaStore.Audio.AudioColumns.IS_MUSIC + " like ");
//			DatabaseUtils.appendEscapedSQLString(sb, "1");
//		}
        // add for PR835073 by yane.wang@jrdcom.com 20141110 end
        String selection0 = sb.toString();
        if (CategoryManager.CATEGORY_DOCS == mPosition) {
            sb.append(" and ").append(
                    MediaStore.Files.FileColumns.MIME_TYPE + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "text/%");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.doc");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xls");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.ppt");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.docx");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsm");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pptx");
            // add for PR815660 by yane.wang@jrdcom.com 20141022 begin
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pdf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcs");
            // add for PR815660 by yane.wang@jrdcom.com 20141022 end
        } else if (CategoryManager.CATEGORY_RECENT == mPosition) {
            sb.append(" and ").append(
                    MediaStore.Files.FileColumns.MIME_TYPE + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "text/%");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.doc");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xls");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.ppt");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.docx");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsm");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pptx");
            // add for PR815660 by yane.wang@jrdcom.com 20141022 begin
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pdf");
            DatabaseUtils.appendEscapedSQLString(sb, "%.jpg");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.jpeg");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.png");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.bmp");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.mp3");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.wav");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.mp4");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.avi");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.mov");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.zip");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.rar");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.apk");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcs");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.m4a");
            sb.append(") and ").append(MediaStore.Files.FileColumns.DATE_MODIFIED + " > " + CommonUtils.getYesterdayTime());
            //sb.append(")");
        }
        // else if (CategoryManager.CATEGORY_ARCHIVES == mPosition) {
        // sb.append(" and ").append(MediaStore.Files.FileColumns.MIME_TYPE +
        // " like ");
        // DatabaseUtils.appendEscapedSQLString(sb, "application/zip");
        // //add for PR849363 by yane.wang@jrdcom.com 20141124 begin
        // sb.append(" or ").append(MediaStore.Files.FileColumns.DATA +
        // " like ");
        // DatabaseUtils.appendEscapedSQLString(sb, "%.rar");
        // //add for PR849363 by yane.wang@jrdcom.com 20141124 end
        // }

        String selection = sb.toString();

        try {
            if (uri != null) {
                Cursor cursor = null;
                if (CategoryManager.CATEGORY_PICTURES == mPosition
                        || CategoryManager.CATEGORY_VEDIOS == mPosition
                        || CategoryManager.CATEGORY_MUSIC == mPosition) {
                    LogUtils.d(TAG, "count.selection1 = " + selection0);
                    cursor = mContentResolver.query(uri, projection,
                            selection0, null, null);

                } else if (CategoryManager.CATEGORY_DOCS == mPosition
                        || CategoryManager.CATEGORY_RECENT == mPosition
                        ) {
                    LogUtils.d(TAG, "count.selection2 = " + selection);
                    cursor = mContentResolver.query(uri, projection, selection,
                            null, null);

                }

                if (cursor != null) {
                    count = cursor.getCount();
                    LogUtils.v("wye", "count=" + count);
                    cursor.close();
                }

            }
        } catch (Exception e) {
            LogUtils.v("wye", "Exception");
            e.printStackTrace();
        }

        return count;
    }

    private int getCountFromPath(String path) {
        File dir = new File(path);
        File[] files = null;
        int count = 0;
        if (dir.exists()) {
            files = dir.listFiles();
            if (files != null) {
                int len = files.length;
                for (int i = 0; i < len; i++) {
                    if (isCancelled()) {
                        return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
                    }

                    if (!files[i].getName().startsWith(".")
                            && !files[i].isDirectory()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private int getRecorderCount() {
        String path1 = CategoryManager.getPhoneRootPath();
        String path2 = CategoryManager.getSDRootPath();
        int count = 0;
        if (path1 != null) {
            String[] paths = CategoryManager.getRecorderPaths(path1);
            int len = paths.length;
            for (int i = 0; i < len; i++) {
                String path = paths[i];
                count += getCountFromPath(path);
            }
        }
        if (path2 != null) {
            String[] paths = CategoryManager.getRecorderPaths(path2);
            int len = paths.length;
            for (int i = 0; i < len; i++) {
                String path = paths[i];
                count += getCountFromPath(path);
            }
        }
        return count;
    }

    private int getCountFromFiles() {
        String path1 = CategoryManager.getPhoneRootPath();
        String path2 = CategoryManager.getSDRootPath();
        int count = 0;
        if (path1 != null) {
            path1 = CategoryManager.getCategoryPath(path1, mPosition);
            count += getCountFromPath(path1);
        }
        if (path2 != null) {
            path2 = CategoryManager.getCategoryPath(path2, mPosition);
            count += getCountFromPath(path2);
        }

        return count;
    }

//	private int getFavoriteCount() {
//		int count = 0;
//		FavoriteHelper mFavoriteHelper = new FavoriteHelper(mContext);
//		// add for PR969817 by yane.wang@jrdcom.com 20150408 begin
//		if (mFavoriteHelper.hasCalledOnOpen()) {
//			return count;
//		}
//		// add for PR969817 by yane.wang@jrdcom.com 20150408 end
//		SQLiteDatabase db = mFavoriteHelper.getWritableDatabase();
//		// add for PR866227,PR866863 by yane.wang@jrdcom.com 20141209 begin
//		// String sql = "select _id from " + FavoriteHelper.TABLE_NAME;
//		String sql = "select * from " + FavoriteHelper.TABLE_NAME;
//		// add for PR866227,PR866863 by yane.wang@jrdcom.com 20141209 begin
//		Cursor cursor = db.rawQuery(sql, null);
//		if (cursor != null) {
//			// add for PR866227,PR866863 by yane.wang@jrdcom.com 20141209 begin
//			// count = cursor.getCount();
//			// db.close();
//			// cursor.close();
//			cursor.moveToFirst();
//			try {
//				while (!cursor.isAfterLast()) {
//					String name = (String) cursor.getString(cursor
//							.getColumnIndex(FavoriteHelper.FILE_PATH));
//					File file = new File(name);
//					if (file.exists()) {
//						count++;
//					}
//                    cursor.moveToNext();
//                }
//            } catch (Exception e) {
//            	e.printStackTrace();
//            } finally {
//            	mFavoriteHelper.resetStatus();//add for PR969817 by yane.wang@jrdcom.com 20150408
//                db.close();
//                cursor.close();
//            }
//          //add for PR866227,PR866863 by yane.wang@jrdcom.com 20141209 end
//        }
//        return count;
//    }
}
