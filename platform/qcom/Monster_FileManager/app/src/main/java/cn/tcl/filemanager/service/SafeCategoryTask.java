/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.PrivateHelper;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;

import java.io.File;

/**
 * Created by user on 16-3-11.
 */
public class SafeCategoryTask extends BaseAsyncTask{
     private String dbPath;
     private PrivateHelper mPrivateHelper;
     private SQLiteDatabase db;
     private final int mCategory;

    /**
     * Constructor of BaseAsyncTask
     *
     * @param context
     * @param fileInfoManager a instance of FileInfoManager, which manages
     *                        information of files in FileManager.
     */
    public SafeCategoryTask(FileInfoManager fileInfoManager,
                            FileManagerService.OperationEventListener operationEvent, int category, Context context) {
        super(context, fileInfoManager, operationEvent);
        mCategory = category;
        dbPath = SharedPreferenceUtils.getCurrentSafeRoot(context)+ File.separator +SharedPreferenceUtils.getCurrentSafeName(context)+File.separator;
        mPrivateHelper = new PrivateHelper(context,dbPath);
        db = mPrivateHelper.getWritableDatabase();
    }

    @Override
    protected Integer doInBackground(Void... voids) {

        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        int progress = 0;

        String[] projection = { PrivateHelper.FILE_FIELD_TP,PrivateHelper.FILE_FIELD_DN};
        int count = 0;

        Log.d("DDK","this is enter SafeCategoryTask"+mCategory);
        StringBuilder sb = new StringBuilder();


        if (CategoryManager.SAFE_CATEGORY_FILES == mCategory) {
            sb.append(PrivateHelper.FILE_FIELD_FT + " = 0");
        } else if (CategoryManager.SAFE_CATEGORY_MUISC == mCategory) {
            sb.append(PrivateHelper.FILE_FIELD_FT + " = 1");
        } else if (CategoryManager.SAFE_CATEGORY_PICTURES == mCategory) {
            sb.append(PrivateHelper.FILE_FIELD_FT + " = 2");
        } else if (CategoryManager.SAFE_CATEGORY_VEDIO == mCategory) {
            sb.append(PrivateHelper.FILE_FIELD_FT + " = 3");
        }
        Log.d("DDK","this is enter dbpath"+dbPath);
        String selection = sb.toString();
        Log.d("DDW", "this is selection" + selection);

        Cursor cursor = null;
        try {
//            if (CategoryManager.CATEGORY_PICTURES == position ||
//                    CategoryManager.CATEGORY_VEDIOS == position ||
//                    CategoryManager.CATEGORY_MUSIC == position) {
//                cursor = context.getContentResolver().query(uri, projection, selection0, null, null);
//            } else if (CategoryManager.CATEGORY_DOCS == position
//                    || CategoryManager.CATEGORY_APKS == position
//                    || CategoryManager.CATEGORY_RECENT == position
//                //||CategoryManager.CATEGORY_ARCHIVES == position
//                    ) {
            cursor = db.query(PrivateHelper.FILE_TABLE_NAME,projection,selection,null,null,null,null);
            if (cursor == null) {
                return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
            }
            if(cursor != null) {
                Log.d("DDW", "this is selection --222----" + cursor.getCount());
            }
//
            int total = cursor.getCount();
            boolean firstItem = true;
            try {
                Log.d("DDK","this is enter Total"+total);
                while (cursor.moveToNext()) {
                    //PR-984826 Nicky Ni -001 201512012 start
                    //if (total > 500) {
                    publishProgress(new ProgressInfo("", progress++, total));
                    //}//PR-984826 Nicky Ni -001 201512012 end
                    if (mCancelled) {
                        cancel(true);
                        onCancelled();
                        return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                    }
                    if (isCancelled()) {
                        ret = FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                        break;
                    }
                    String name = (String) cursor.getString(0);
                    String mFilename = (String) cursor.getString(1);
                    Log.d("DDK","this is enter Total"+name);
                    Log.d("DDK", "this is enter Total--11--" + mFilename);
                    FileInfo mFileInfo = new FileInfo(mContext, name);
                    mFileInfo.setFileName(mFilename);
                    mFileInfo.setFileType(mCategory);

//                    if (mMimeType == null) {
//                        MediaFile.MediaFileType fileType = MediaFile
//                                .getFileType(mFileInfo.getFileAbsolutePath());
//                        if (fileType == null) {
//                            mMimeType = null;
//                        } else {
//                            mMimeType = fileType.mimeType;
//                        }
//                    }
//                    mFileInfo.setFileMimeType(mMimeType);
                    /* PR 1002001 zibin.wang modify 12/23/2015 Start */
                    if (mFileInfo.getFile().exists())
                    {   //PR-1001441 Nicky Ni -001 20160104 start
                        mFileInfoManager.addItem(mFileInfo,firstItem);
                        firstItem = false;
                        //PR-1001441 Nicky Ni -001 20160104 end
                    }
                    /* PR 1002001 zibin.wang modify 12/23/2015 */
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
                cursor.close();
            }


        if (ret != FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL) {
            FileManagerApplication application = (FileManagerApplication) mContext
                    .getApplicationContext();
            application.mFileInfoManager
                    .updateCategoryList(application.mSortType);
        }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
            if(cursor!=null) {
                cursor.close();
            }
            if(db !=null){
                db.close();
            }
        }

        return count;


    }
}
