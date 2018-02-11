/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.manager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import cn.tcl.filemanager.utils.SharedPreferenceUtils;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by user on 16-3-9.
 */
public class SafeCategoryCountManager {

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(3);
    private HashMap<String, Long> mSizeMap = new HashMap<String, Long>();//add for PR961285 by yane.wang@jrdcom.com 20150511
    private static SafeCategoryCountManager sInstance = new SafeCategoryCountManager();

    private SafeCategoryCountManager() {
    }

    public static SafeCategoryCountManager getInstance() {
        return sInstance;
    }

    public void loadCategoryCountText(final int postion, final Context context, final CountTextCallback callback) {
//		if (mExecutorService.isShutdown()) {
//			return;
//		}
        mExecutorService.execute(new Runnable() {

            @Override
            public void run() {
                callback.countTextCallback(doInBackground(postion, context));
            }
        });
    }

//    public void  shutDown() {
//		if (mExecutorService != null && !mExecutorService.isShutdown()) {
//			mExecutorService.shutdownNow();
//		}
//    }

    public void putMap(String key, long value) {
        synchronized (mSizeMap) {
            mSizeMap.put(key, value);
        }
    }

    public void clearMap() {
        synchronized (mSizeMap) {
            mSizeMap.clear();
        }
    }

    protected String doInBackground(int category, Context context) {
        String sizeString = null;
        int count = 0;
        switch (category) {
            case CategoryManager.SAFE_CATEGORY_FILES:
            case CategoryManager.SAFE_CATEGORY_MUISC:
            case CategoryManager.SAFE_CATEGORY_PICTURES:
            case CategoryManager.SAFE_CATEGORY_VEDIO:
                count = getCountFromMedia(category, context);
                break;
            default:
                break;
        }
//        if(category != CategoryManager.CATEGORY_SAFE) {
            sizeString = "(" + count + ")";
//        } else {
//            sizeString = "";
//        }

        return sizeString;
    }

    private int getCountFromMedia(int position, Context context) {

        String currentSafeRoot = SharedPreferenceUtils.getCurrentSafeRoot(context);
        String currentSafeName = SharedPreferenceUtils.getCurrentSafeName(context);
        PrivateHelper mPrivateHelper = new PrivateHelper(context,currentSafeRoot+ File.separator +currentSafeName+File.separator);
        SQLiteDatabase db = mPrivateHelper.getWritableDatabase();
        Uri uri = null;
        String[] projection = {
                PrivateHelper.FILE_FIELD_TP,
        };
        int count = 0;


        StringBuilder sb = new StringBuilder();


        if (CategoryManager.SAFE_CATEGORY_FILES == position) {
            sb.append(PrivateHelper.FILE_FIELD_FT + " = 0");
        } else if (CategoryManager.SAFE_CATEGORY_MUISC == position) {
            sb.append(PrivateHelper.FILE_FIELD_FT + " = 1");
        } else if (CategoryManager.SAFE_CATEGORY_PICTURES == position) {
            sb.append(PrivateHelper.FILE_FIELD_FT + " = 2");
        } else if (CategoryManager.SAFE_CATEGORY_VEDIO == position) {
            sb.append(PrivateHelper.FILE_FIELD_FT + " = 3");
        }

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
            if(cursor != null) {
                Log.d("DDW", "this is selection --222----" + cursor.getCount());
            }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
//                	while(cursor.moveToNext()){
//                    	LogUtils.d("DAT","Modify data is "+cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)));
//                    }
                count = cursor.getCount();
                cursor.close();
            }
            if(db !=null){
                db.close();
            }
        }

        return count;
    }


    public interface CountTextCallback {
        public void countTextCallback(String countText);
    }
}
