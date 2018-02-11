/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Created on 16-9-12.
 */
public class MultiPicCopyUtil {

    private static ExecutorService exec = Executors.newSingleThreadExecutor();

    private static final String TAG = MultiPicCopyUtil.class.getSimpleName();

    /**
     * copy pics to pathDir
     * @param context
     * @param data
     * @param pathDir
     */
    public static void addPicToDir(Context context, Intent data,String pathDir,
                                  @Nullable OnCopiedAPicListener listener) {
        File file = new File(pathDir);
        if(!file.exists()){
            file.mkdir();
        }
        ArrayList<Uri> uriList = getDataFromIntent(data);
        for (Uri uri : uriList) {
            UriToFileTask addTask = new UriToFileTask(context,pathDir,listener);
            addTask.executeOnExecutor(exec, uri);
        }
        if(uriList.size() >= 1){
            MeetingStaticInfo.updateCurrentTime(context);
        }
    }

    private static ArrayList<Uri> getDataFromIntent(Intent intent) {
        ArrayList<Uri> uriList = new ArrayList<>();
        Uri uri = intent.getData();
        if (uri == null) {
            uriList = intent.getParcelableArrayListExtra("data");
        } else {
            uriList.add(uri);
        }
        if (MeetingLog.DEBUG) {
            for (Uri uriTemp : uriList) {
                MeetingLog.d(TAG, "get uri=" + uriTemp);
            }
        }
        return uriList;
    }


    /**
     * start a task that copy uri img to note file.
     */
    static class UriToFileTask extends AsyncTask<Uri, Void, String> {
        private final String LOWER_SPACE = "lowerSpace";

        private Context mContext;
        private String mDirPath;
        private OnCopiedAPicListener mOnCopiedAPicListener;

        UriToFileTask(Context context,String dirPath,@Nullable OnCopiedAPicListener listener){
            mContext = context;
            mDirPath = dirPath;
            mOnCopiedAPicListener = listener;
        }

        @Override
        protected String doInBackground(Uri... params) {
            ContentResolver contentResolver = mContext.getContentResolver();
            InputStream is;
             try {
                is = contentResolver.openInputStream(params[0]);
                String filePath = getFilePath(mDirPath);
                if (filePath == null) {
                    return null;
                }
                int result = FileUtils.copyToFile(is, new File(filePath));
                if (result == 0) {
                    return filePath;
                } else {
                    if (result == -2) {
                        return LOWER_SPACE;
                    }
                    return null;
                }
            } catch (FileNotFoundException e) {
                MeetingLog.e(TAG, "image uri can not find", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String picName) {
            if(null != mOnCopiedAPicListener){
                mOnCopiedAPicListener.onCopied(picName);
            }

        }



    }


    private synchronized static String getFilePath(String dirPath){
        String filePath = dirPath + File.separator + "meetingImage"+ System.currentTimeMillis()+".jpg";
        // avoid two thread get the same path
        try {
            Thread.currentThread().sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public interface OnCopiedAPicListener{
        void onCopied(String picName);
    }

}
