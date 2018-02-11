/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.xdja.sks.IEncDecListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import cn.tcl.filemanager.adapter.FileInfoAdapter;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.service.FileOperationTask.CopyPasteFilesTask;
import cn.tcl.filemanager.service.FileOperationTask.CreateFolderTask;
import cn.tcl.filemanager.service.FileOperationTask.CutPasteFilesTask;
import cn.tcl.filemanager.service.FileOperationTask.DeleteFilesTask;
import cn.tcl.filemanager.service.FileOperationTask.RenameTask;
import cn.tcl.filemanager.service.FileSecurityTask.EncryptFilesTask;
import cn.tcl.filemanager.service.FileSecurityTask.DecryptFilesTask;
import cn.tcl.filemanager.utils.FileDecryptManagerHelper;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.view.PathProgressLayout;
import cn.tcl.filemanager.view.PathProgressThirdLayout;
import cn.tcl.filemanager.view.PathProgressTwoFirstLayout;
import cn.tcl.filemanager.view.PathProgressTwoSecondLayout;

public class FileManagerService extends Service {

    public static final int FILE_FILTER_TYPE_UNKOWN = -1;
    public static final int FILE_FILTER_TYPE_DEFAULT = 0;
    public static final int FILE_FILTER_TYPE_FOLDER = 1;
    public static final int FILE_FILTER_TYPE_ALL = 2;
    public static final int FILE_FILTER_TYPE_FILES = 3;

    private static final String TAG = FileManagerService.class.getSimpleName();
    private final HashMap<String, FileManagerActivityInfo> mActivityMap =
            new HashMap<String, FileManagerActivityInfo>();
    private ServiceBinder mBinder;

    /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
    /*MODIFIED-BEGIN by jian.xu, 2016-04-18,BUG-1868328*/
    private List<BaseAsyncTask> mDelayTask = new ArrayList<BaseAsyncTask>(1);
    private String mDelayActivityName;
    /*MODIFIED-END by jian.xu,BUG-1868328*/
    /* MODIFIED-END by haifeng.tang,BUG-1987329*/

    private TimeTickRegister mTimeTickRegister;
    private FileDecryptManagerHelper mFileDecryptManagerHelper;

    private static class FileManagerActivityInfo {
        private BaseAsyncTask mTask = null;
        private FileInfoManager mFileInfoManager = null;
        private CategoryManager mCagegoryManager = null;
        private int mFilterType = FILE_FILTER_TYPE_DEFAULT;

        public void setTask(BaseAsyncTask task) {
//add for PR959312 by long.tang@tcl.com on 2015.03.31 start
            if (mTask != null && !mTask.isCancelled()) {
                mTask.cancel(true);
                mTask.onCancelled();
            }
            //add for PR959312 by long.tang@tcl.com on 2015.03.31 start
            this.mTask = task;
        }

        public void setFileInfoManager(FileInfoManager fileInfoManager) {
            this.mFileInfoManager = fileInfoManager;
        }

        public void setCategoryManager(CategoryManager categoryManager) {
            this.mCagegoryManager = categoryManager;
        }

        public void setFilterType(int filterType) {
            LogUtils.d("FOL", "this is setFilterType" + filterType);
            this.mFilterType = filterType;
        }

        BaseAsyncTask getTask() {
            return mTask;
        }

        FileInfoManager getFileInfoManager() {
            return mFileInfoManager;
        }

        CategoryManager getCategoryManager() {
            return mCagegoryManager;
        }

        int getFilterType() {
            return mFilterType;
        }
    }

    public interface OperationEventListener {
        int ERROR_CODE_NAME_VALID = 100;
        int ERROR_CODE_SUCCESS = 0;
        int ERROR_CODE_DECRYPT_SUCCESS = 1;
        int ERROR_CODE_UNSUCCESS = -1;
        int ERROR_CODE_NAME_EMPTY = -2;
        int ERROR_CODE_NAME_TOO_LONG = -3;
        int ERROR_CODE_FILE_EXIST = -4;
        int ERROR_CODE_NOT_ENOUGH_SPACE = -5;
        int ERROR_CODE_DELETE_FAILS = -6;
        int ERROR_CODE_USER_CANCEL = -7;
        int ERROR_CODE_PASTE_TO_SUB = -8;
        int ERROR_CODE_UNKOWN = -9;
        int ERROR_CODE_COPY_NO_PERMISSION = -10;
        int ERROR_CODE_MKDIR_UNSUCCESS = -11;
        int ERROR_CODE_CUT_SAME_PATH = -12;
        int ERROR_CODE_BUSY = -100;
        int ERROR_CODE_DELETE_UNSUCCESS = -13;
        int ERROR_CODE_PASTE_UNSUCCESS = -14;
        int ERROR_CODE_DELETE_NO_PERMISSION = -15;
        int ERROR_CODE_FAVORITE_UNSUCESS = -16;
        int ERROR_CODE_ENCRYPT_UNSUCCESS = -17;
        //[BUGFIX]-Mod-BEGIN by TSNJ,qinglian.zhang,09/03/2014,PR-776005
        int ERROR_INVALID_CHAR = -17;
        //[BUGFIX]-Mod-END by TSNJ,qinglian.zhang

        int ERROR_SAFE_SIZE_LIMTED = -18;
        int ERROR_SAFE_DRM_LIMTED = -19;

        int ERROR_CODE_DELETE_CANCEL = -20;
        int ERROR_CODE_COPY_CANCEL = -21;
        int ERROR_CODE_CUT_CANCEL = -22;

        /**
         * This method will be implemented, and called in onPreExecute of
         * asynctask
         */
        void onTaskPrepare();

        /**
         * This method will be implemented, and called in onProgressUpdate
         * function of asynctask
         *
         * @param progressInfo information of ProgressInfo, which will be
         *                     updated on UI
         */
        void onTaskProgress(ProgressInfo progressInfo);

        /**
         * This method will be implemented, and called in onPostExecute of
         * asynctask
         *
         * @param result the result of asynctask's doInBackground()
         */
        void onTaskResult(int result);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new ServiceBinder();
        //DrmManager.getInstance().init(this);
        // AsyncTask.setDefaultExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mTimeTickRegister = new TimeTickRegister();
        mFileDecryptManagerHelper = new FileDecryptManagerHelper(getApplicationContext());
        registerTimeTickReceiver();
    }

    private void registerTimeTickReceiver() {
        LogUtils.i(TAG, "registerTimeTickReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mTimeTickRegister, filter);
    }

    /**
     * time tick broadcast receiver
     */
    class TimeTickRegister extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                String path = SafeUtils.getDecryptRootPath(getApplicationContext());
                deleteFileByPath(path);
            }
        }
    }

    /**
     * encrypt file again and delete decrypt file
     * @param file decrypt file
     * @return is delete
     */
    private boolean encryptFileAgainByPath(File file) {
        String path = file.getAbsolutePath();
        long recodeModifyTime = mFileDecryptManagerHelper.queryModifyTimeByDecryptPath(path);
        LogUtils.i(TAG, "encryptFileAgainByPath recode modify time : " + recodeModifyTime);
        if (recodeModifyTime != -1 && recodeModifyTime < file.lastModified()) {
            LogUtils.i(TAG, "encrypt file again,file path:" + file.getAbsolutePath());
            SafeUtils.encryptFile(getApplicationContext(), path, path.replace(SafeUtils.DECRYPT_TEMP_ROOT_DIR, SafeUtils.SAFE_ROOT_DIR), new IEncDecListener() {
                @Override
                public void onOperStart() throws RemoteException {

                }

                @Override
                public void onOperProgress(long l, long l1) throws RemoteException {

                }

                @Override
                public void onOperComplete(int i) throws RemoteException {

                }

                @Override
                public IBinder asBinder() {
                    return null;
                }
            });
            file.delete();
            mFileDecryptManagerHelper.deleValueByDecryptPath(file.getAbsolutePath());
            return true;
        }
        return false;
    }

    /**
     * delete file when decrypt file time > 24hours
     * @param path decrypt file path
     */
    private void deleteFileByPath(String path) {
        File file = new File(path);
        LogUtils.i(TAG, "path:"+path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (null != files) {
                    for (File subFile : files) {
                        deleteFileByPath(subFile.getAbsolutePath());
                    }
                }
            } else {
                if (!encryptFileAgainByPath(file)) {
                    if (System.currentTimeMillis() - file.lastModified() > 24 * 60 * 60 * 1000) {
                        LogUtils.i(TAG, "delete file path:" + file.getAbsolutePath());
                        file.delete();
                        mFileDecryptManagerHelper.deleValueByDecryptPath(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (null != mTimeTickRegister) {
            LogUtils.i(TAG, "unregisterTimeTickReceiver");
            unregisterReceiver(mTimeTickRegister);
        }
    }

    public class ServiceBinder extends Binder {
        /**
         * This method gets instance of FileManagerService
         *
         * @return instance of FileManagerService
         */
        public FileManagerService getServiceInstance() {
            return FileManagerService.this;
        }
    }

    /**
     * This method initializes FileInfoManager of certain activity.
     *
     * @param activityName name of activity, which the FileInfoManager attached
     *                     to
     * @return FileInforManager of certain activity
     */
    public FileInfoManager initFileInfoManager(Activity a) {
        FileManagerActivityInfo activityInfo = mActivityMap.get(a.getClass().getName());
        Log.d("filetest", "initFileInfoManager(), a=" + a.getClass().getName() + ", activityInfo-=" + activityInfo);
        //Log.d("filetest", "-----------------------------------", new Exception()); //MODIFIED by wenjing.ni, 2016-04-13,BUG-1941073
        if (activityInfo == null) {
            activityInfo = new FileManagerActivityInfo();
            activityInfo.setFileInfoManager(new FileInfoManager()); // MODIFIED by haifeng.tang, 2016-04-23,BUG-1956936
            mActivityMap.put(a.getClass().getName(), activityInfo);
        }
        return activityInfo.getFileInfoManager();
    }

    public CategoryManager initCategoryManager(Activity a) {
        FileManagerActivityInfo activityInfo = mActivityMap.get(a.getClass().getName());
        if (activityInfo == null) {
            activityInfo = new FileManagerActivityInfo();
            activityInfo.setCategoryManager(new CategoryManager(getContentResolver()));
        }
        return activityInfo.getCategoryManager();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * This method checks that weather the service is busy or not, which means
     * id any task exist for certain activity
     *
     * @param activityName name of activity, which will be checked
     * @return true for busy, false for not busy
     */
    public boolean isBusy(String activityName) {
        FileManagerActivityInfo activityInfo = mActivityMap.get(activityName);
        if (activityInfo == null) {
            return false;
        }
        BaseAsyncTask task = activityInfo.getTask();
        if (task != null
                && !task.isCancelled()
                && (task.getStatus() == AsyncTask.Status.PENDING || task
                .getStatus() == AsyncTask.Status.RUNNING)) {
            Log.i(TAG, "Task->" + task + "            is busy");
            return true;
        }else {
            Log.i(TAG, "Task->" + task + "            is  no busy");
        }
        return false;
    }

    private FileManagerActivityInfo getActivityInfo(String activityName) {
        FileManagerActivityInfo activityInfo = mActivityMap.get(activityName);
        if (activityInfo == null) {
//            throw new IllegalArgumentException("this activity not init in Service");
        }
        return activityInfo;
    }

    /**
     * This method sets list filter, which which type of items will be listed in
     * listView
     *
     * @param type         type of list filter
     * @param activityName name of activity, which operations attached to
     */
    public void setListType(int type, String activityName) {
        getActivityInfo(activityName).setFilterType(type);
    }

    /**
     * This method does create folder job by starting a new CreateFolderTask
     *
     * @param activityName name of activity, which the CreateFolderTask attached
     *                     to
     * @param destFolder   information of file, which needs to be created
     * @param listener     listener of CreateFolderTask
     */
    public void createFolder(String activityName, String destFolder,
                             OperationEventListener listener) {
        try {
            if (isBusy(activityName)) {
                listener.onTaskResult(OperationEventListener.ERROR_CODE_BUSY);
            } else {
                FileInfoManager fileInfoManager = getActivityInfo(activityName).getFileInfoManager();
                int filterType = getActivityInfo(activityName).getFilterType();
                SharedPreferences sp = getSharedPreferences("firstTimeEnterApp",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("filterType", filterType);
                editor.commit();
                if (fileInfoManager != null) {
                    BaseAsyncTask task = new CreateFolderTask(fileInfoManager, listener, this, destFolder, filterType);
                    getActivityInfo(activityName).setTask(task);
                    task.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method does rename job by starting a new RenameTask
     *
     * @param activityName  name of activity, which the operations attached to
     * @param srcFile       information of certain file, which needs to be renamed
     * @param mSearchString
     * @param dstFile       information of new file after rename
     * @param listener      listener of RenameTask
     */
    public void rename(String activityName, FileInfo srcFile, String mSearchString, FileInfo dstFile,
                       OperationEventListener listener,int mode, String path) {
        try {
            if (isBusy(activityName)) {
                listener.onTaskResult(OperationEventListener.ERROR_CODE_BUSY);
            } else {
                FileInfoManager fileInfoManager = getActivityInfo(activityName)
                        .getFileInfoManager();
                int filterType = getActivityInfo(activityName).getFilterType();
                if (fileInfoManager != null) {
                    BaseAsyncTask task = new RenameTask(fileInfoManager, listener, mSearchString,
                            this, srcFile, dstFile, filterType,mode,path);
                    getActivityInfo(activityName).setTask(task);
                    task.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCountStorageSizeTask(PathProgressLayout pathProgressLayout, String fileString, Context context) {
        try {
            CountStorageSizeTask task = new CountStorageSizeTask(pathProgressLayout, context, fileString);
            task.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCountStorageSizeTask(PathProgressTwoFirstLayout pathProgressTwoFirstLayout, String fileString, Context context) {
        try {
            CountStorageSizeTask task = new CountStorageSizeTask(pathProgressTwoFirstLayout, context, fileString);
            task.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCountStorageSizeTask(PathProgressTwoSecondLayout pathProgressTwoSecondLayout, String fileString, Context context) {
        try {
            CountStorageSizeTask task = new CountStorageSizeTask(pathProgressTwoSecondLayout, context, fileString);
            task.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCountStorageSizeTask(PathProgressThirdLayout pathProgressThirdLayout, String fileString, Context context) {
        try {
            CountStorageSizeTask task = new CountStorageSizeTask(pathProgressThirdLayout, context, fileString);
            task.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCountStorageSizeTask(TextView textView, int width, String fileString, Context context, boolean isSafe) {
        try {
            CountStorageSizeTask task = new CountStorageSizeTask(textView, width, fileString, context, isSafe);
            task.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void setDetailsSizeText(TextView textView, FileInfo fileInfo, Context context) {
//        try {
//            SetDetailsSizeTextTask task = new SetDetailsSizeTextTask(textView, fileInfo, context);
//            task.execute(fileInfo);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void setCategoryCountText(TextView textView, ContentResolver ContentResolver,
//            Context context, int position, String text) {
//        try {
//            CategoryCountTextTask task = new CategoryCountTextTask(textView, ContentResolver,
//                    context, position, text);
//            task.execute();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private int filterPasteList(List<FileInfo> fileInfoList, String destFolder) {

        int remove = 0;
        Iterator<FileInfo> iterator = fileInfoList.iterator();
        while (iterator.hasNext()) {
            FileInfo fileInfo = iterator.next();
            if (fileInfo.isDirectory()) {
                if ((destFolder + MountManager.SEPARATOR)
                        .startsWith(fileInfo.getFileAbsolutePath()
                                + MountManager.SEPARATOR)) {
                    iterator.remove();
                    remove++;
                }
            }
        }
        return remove;
    }

//    public void addFavoriteFiles(String activityName, List<FileInfo> fileInfoList,
//            OperationEventListener listener) {
//        try {
//            if (isBusy(activityName)) {
//                listener.onTaskResult(OperationEventListener.ERROR_CODE_BUSY);
//            } else {
//                FileInfoManager fileInfoManager = getActivityInfo(activityName).getFileInfoManager();
//                if (fileInfoManager != null) {
//                    FavoriteFileOperationTask task = new AddFavoriteFileTask(fileInfoManager,
//                            listener, this, fileInfoList);
//                    getActivityInfo(activityName).setTask(task);
//                    task.execute();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void cancelFavoriteFiles(String activityName, List<FileInfo> fileInfoList,
//            OperationEventListener listener) {
//        try {
//            if (isBusy(activityName)) {
//                listener.onTaskResult(OperationEventListener.ERROR_CODE_BUSY);
//            } else {
//                FileInfoManager fileInfoManager = getActivityInfo(activityName)
//                        .getFileInfoManager();
//                if (fileInfoManager != null) {
//                    FavoriteFileOperationTask task = new CancelFavoriteFileTask(fileInfoManager,
//                            listener, this, fileInfoList);
//                    getActivityInfo(activityName).setTask(task);
//                    task.execute();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * This method does delete job by starting a new DeleteFilesTask.
     *
     * @param activityName name of activity, which the operations attached to
     * @param fileInfoList list of files, which needs to be deleted
     * @param listener     listener of the DeleteFilesTask
     */
    public void deleteFiles(String activityName, int deleteMode, List<FileInfo> fileInfoList,
                            OperationEventListener listener) {
        try {
            if (isBusy(activityName)) {
                listener.onTaskResult(OperationEventListener.ERROR_CODE_BUSY);
            } else {
                FileInfoManager fileInfoManager = getActivityInfo(activityName)
                        .getFileInfoManager();
                if (fileInfoManager != null) {
                    BaseAsyncTask task = new DeleteFilesTask(fileInfoManager, deleteMode,
                            listener, this, fileInfoList);
                    getActivityInfo(activityName).setTask(task);
                    task.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * This method cancel certain task
     *
     * @param activityName name of activity, which the task attached to
     */
    public void cancel(String activityName) {
        boolean ifCancel = false;//add for PR916731 by yane.wang@jrdcom.com 20150130
        BaseAsyncTask task = getActivityInfo(activityName).getTask();
        Log.i(TAG, "cancel task->" + task + " activityName->" + activityName);
        if (task != null) {
            task.setCancel(true);
            ifCancel = task.cancel(true);
        }
        //add for PR916731 by yane.wang@jrdcom.com 20150130 begin
        if (!ifCancel && task != null) {
            task.onCancelled();
        }
        //add for PR916731 by yane.wang@jrdcom.com 20150130 end
    }

    /**
     * This method does paste job by starting a new CutPasteFilesTask or
     * CopyPasteFilesTask according to parameter of type
     *
     * @param activityName name of activity, which the task and operations
     *                     attached to
     * @param fileInfoList list of files which needs to be paste
     * @param dstFolder    destination, which the files should be paste to
     * @param type         indicate the previous operation is cut or copy
     * @param listener     listener of the started task
     */
    public void pasteFiles(String activityName, List<FileInfo> fileInfoList,
                           String dstFolder, int type, OperationEventListener listener) {
        Log.e(TAG, "pasteFiles");
        try {
            if (isBusy(activityName)) {
                //modify by haifeng.tang for PR:1274836 start 2015-12-31
                Log.i(TAG, "task is busy for activityName->" + activityName);
                cancel(activityName);
                //modify by haifeng.tang for PR:1274836 end 2015-12-31
            }
            /**  Folder cannot be copied to sub directory  */
            if (filterPasteList(fileInfoList, dstFolder) > 0) {
                listener.onTaskResult(OperationEventListener.ERROR_CODE_PASTE_TO_SUB);
            }
            FileInfoManager fileInfoManager = getActivityInfo(activityName)
                    .getFileInfoManager();
            if (fileInfoManager == null) {
                Log.e(TAG, "fileInfoManager is null activityName->" + activityName);
                listener.onTaskResult(OperationEventListener.ERROR_CODE_UNKOWN);
                return;
            }
            BaseAsyncTask task = null;
            if (fileInfoList.size() > 0) {
                switch (type) {
                    case FileInfoManager.PASTE_MODE_CUT:
                        if (isCutSamePath(fileInfoList, dstFolder)) {
                            listener.onTaskResult(OperationEventListener.ERROR_CODE_CUT_SAME_PATH);
                            return;
                        }
                        task = new CutPasteFilesTask(fileInfoManager, listener, this,
                                fileInfoList, dstFolder);
                        getActivityInfo(activityName).setTask(task);
                        task.execute();
                        break;
                    case FileInfoManager.PASTE_MODE_COPY:
                        task = new CopyPasteFilesTask(fileInfoManager, listener, this,
                                fileInfoList, dstFolder);
                        getActivityInfo(activityName).setTask(task);
                        task.execute();
                        break;
                    default:
                        listener.onTaskResult(OperationEventListener.ERROR_CODE_UNKOWN);
                        return;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void ShiftOutFiles(String activityName, List<FileInfo> fileInfoList,
                              String dstFolder, int mode, OperationEventListener listener) {

        LogUtils.i(TAG, "ShiftOutFiles activityName->" + activityName);
        try {
            if (isBusy(activityName)) {
                cancel(activityName);
            } else {
                FileInfoManager fileInfoManager = getActivityInfo(activityName).getFileInfoManager();
                if (fileInfoManager != null) {
                    ShiftOutSafeFileTask task = new ShiftOutSafeFileTask(fileInfoManager,
                            listener, dstFolder, mode, this, fileInfoList);
                    getActivityInfo(activityName).setTask(task);
                    task.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isCutSamePath(List<FileInfo> fileInfoList, String dstFolder) {
        for (FileInfo fileInfo : fileInfoList) {
            if (fileInfo.getFileParentPath().equals(dstFolder)) {
                return true;
            }
        }
        return false;
    }

    /* MODIFIED-BEGIN by songlin.qi, 2016-05-27,BUG-2202845*/
    public void listFiles(String activityName, String path,
                          OperationEventListener listener, boolean onlyShowdir, String fileCategory, int drm_sd, int listMode) {
        listFiles(activityName, path, listener, onlyShowdir, fileCategory, drm_sd, listMode, false);
    }
    /* MODIFIED-END by songlin.qi,BUG-2202845*/

    /**
     * This method lists files of certain directory by starting a new
     * ListFileTask.
     *
     * @param activityName name of activity, which the ListFileTask attached to
     * @param path         the path of certain directory
     * @param listener     listener of the ListFileTask
     */
    public void listFiles(String activityName, String path,
                          /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
                          /*MODIFIED-BEGIN by jian.xu, 2016-04-18,BUG-1868328*/
                          OperationEventListener listener, boolean onlyShowdir, String fileCategory, int drm_sd, int listMode, boolean hideHidenFile) { // MODIFIED by songlin.qi, 2016-05-27,BUG-2202845
        boolean needDelay = false;
        try {
            // ADD START FOR PR1050553 BY HONGBIN.CHEN 20150810


            if (isBusy(activityName)) {
                FileManagerActivityInfo activityInfo = mActivityMap.get(activityName);
                if (activityInfo != null) {
                    BaseAsyncTask task = activityInfo.getTask();
                    if(listMode == ListFileTask.LIST_MODE_ONCHANGE) {
                        if (task != null
                                && (task instanceof CopyPasteFilesTask || task instanceof CutPasteFilesTask || task instanceof DeleteFilesTask)) {
                            //todo:media db changes will trigger ListFileTask or CategoryTask, if current task is above, it just returns.
                            //this is not a good way, need to be improved
                            return;
                        } else if(task != null && (task instanceof ListFileTask || task instanceof CategoryTask)) {
                            //media db changes will trigger ListFileTask or CategoryTask, this can be delay
                            needDelay = true;
                        }
                    } else if(listMode == ListFileTask.LIST_MODE_SHORCUT) {
                        if (task != null
                                && (task instanceof CopyPasteFilesTask || task instanceof CutPasteFilesTask || task instanceof DeleteFilesTask)) {
                            //media db changes will trigger ListFileTask or CategoryTask, this can be delay
                            needDelay = true;
                        }
                    }
                }
                if(!needDelay) {
                    cancel(activityName);
                }
            }
            FileInfoManager fileInfoManager = getActivityInfo(activityName).getFileInfoManager();
            int filterType = getActivityInfo(activityName).getFilterType();
            if (fileInfoManager != null) {
                //[FEATURE]-Add-BEGIN by TSNJ,qinglian.zhang,09/15/2014,PR-787616,
                BaseAsyncTask task = new ListFileTask(this, fileInfoManager,
                        listener, path, filterType, onlyShowdir, fileCategory, drm_sd, listMode, hideHidenFile); // MODIFIED by songlin.qi, 2016-05-27,BUG-2202845
                if(needDelay) {
                    updateDelayTask(task, activityName);
                    return;
                }
                /*MODIFIED-END by jian.xu,BUG-1868328*/
                /* MODIFIED-END by haifeng.tang,BUG-1987329*/
                getActivityInfo(activityName).setTask(task);
/* MODIFIED-BEGIN by jian.xu, 2016-03-18, BUG-1697006 */
//                task.execute();
                //task can run faster
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                /* MODIFIED-END by jian.xu,BUG-1697006 */
            }
            // ADD END FOR PR1050553 BY HONGBIN.CHEN 20150810
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method lists files of category by starting a new ListFileTask.
     *
     * @param activityName name of activity, which the ListFileTask attached to
     * @param listener     listener of the ListFileTask
     */
    public void decryptFiles(String activityName, Context context,
                            OperationEventListener listener, IEncDecListener encryptListener, FileInfoAdapter adapter) {
        try {
            if (isBusy(activityName)) {
                cancel(activityName);
            }
            FileInfoManager fileInfoManager = getActivityInfo(activityName)
                    .getFileInfoManager();
            if (fileInfoManager != null) {
                BaseAsyncTask task = new DecryptFilesTask(fileInfoManager,
                        listener, encryptListener, context, adapter);
                getActivityInfo(activityName).setTask(task);
                task.execute();
            }
            // ADD END FOR PR1050553 BY HONGBIN.CHEN 20150810
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method lists files of category by starting a new ListFileTask.
     *
     * @param activityName name of activity, which the ListFileTask attached to
     * @param listener     listener of the ListFileTask
     */
    public void decryptFile(String activityName, Context context,
                            OperationEventListener listener, IEncDecListener encryptListener, FileInfo fileInfo) {
        try {
            if (isBusy(activityName)) {
                cancel(activityName);
            }
            FileInfoManager fileInfoManager = getActivityInfo(activityName)
                    .getFileInfoManager();
            if (fileInfoManager != null) {
                BaseAsyncTask task = new DecryptFilesTask(fileInfoManager,
                        listener, encryptListener, context, fileInfo);
                getActivityInfo(activityName).setTask(task);
                task.execute();
            }
            // ADD END FOR PR1050553 BY HONGBIN.CHEN 20150810
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method lists files of encrypt
     *
     * @param activityName name of activity, which the ListFileTask attached to
     * @param listener     listener of the ListFileTask
     */
    public void encryptFile(String activityName, Context context,
                            OperationEventListener listener, IEncDecListener encryptListener, FileInfoAdapter adapter, String target) {
        try {
            if (isBusy(activityName)) {
                cancel(activityName);
            }
            FileInfoManager fileInfoManager = getActivityInfo(activityName)
                    .getFileInfoManager();
            if (fileInfoManager != null) {
                BaseAsyncTask task = new EncryptFilesTask(fileInfoManager,
                        listener, encryptListener, context, adapter, target);
                getActivityInfo(activityName).setTask(task);
                task.execute();
            }
            // ADD END FOR PR1050553 BY HONGBIN.CHEN 20150810
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * This method lists files of category by starting a new ListFileTask.
     *
     * @param activityName name of activity, which the ListFileTask attached to
     * @param category     the category
     * @param listener     listener of the ListFileTask
     */
    public void listCategoryFiles(String activityName, int category, Context context,
                                  /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
                                  /*MODIFIED-BEGIN by jian.xu, 2016-04-18,BUG-1868328*/
                                  OperationEventListener listener, int mode) {
        try {
            boolean needDelay = false;
            if (isBusy(activityName)) {
                if(mode == CategoryTask.LIST_MODE_VIEW) {
                    cancel(activityName);
                } else if(mode == CategoryTask.LIST_MODE_ONCHANGE) {
                    FileManagerActivityInfo activityInfo = mActivityMap.get(activityName);
                    if (activityInfo != null) {
                        BaseAsyncTask task = activityInfo.getTask();
                        if(task != null && task instanceof CategoryTask) {
                            needDelay = true;
                        }
                    }
                } else {
                    return;
                }
            }
            FileInfoManager fileInfoManager = getActivityInfo(activityName).getFileInfoManager();
            if (fileInfoManager != null) {
                BaseAsyncTask task = new CategoryTask(fileInfoManager,
                        listener, category, getContentResolver(), context);
                if(needDelay) {
                    updateDelayTask(task, activityName);
                    return;
                }
                /*MODIFIED-END by jian.xu,BUG-1868328*/
                /* MODIFIED-END by haifeng.tang,BUG-1987329*/
                getActivityInfo(activityName).setTask(task);
                task.execute();
            }
            // ADD END FOR PR1050553 BY HONGBIN.CHEN 20150810
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method lists files of recents by starting a new ListFileTask.
     *
     * @param activityName name of activity, which the ListFileTask attached to
     * @param listener     listener of the ListFileTask
     */
    public void listRecentsFiles(String activityName, FileInfoManager fileInfoManager, Context context, OperationEventListener listener) {

        if (fileInfoManager != null) {
            BaseAsyncTask task = new RecentsFilesTask(fileInfoManager,
                    listener, CategoryManager.CATEGORY_RECENT, getContentResolver(), context);
            getActivityInfo(activityName).setTask(task);
            task.execute();
        }
    }

    /**
     * This method lists files that saved before search.
     *
     * @param listener listener of the ListFileTask
     */
    public void listBeforeSearchFiles(String activityName, OperationEventListener listener,
                                      List<FileInfo> filesInfoList) {
        try {
            if (isBusy(activityName)) {
                listener.onTaskResult(OperationEventListener.ERROR_CODE_BUSY);
            } else {
                FileInfoManager fileInfoManager = getActivityInfo(activityName).getFileInfoManager();
                if (fileInfoManager != null) {
                    BaseAsyncTask task = new ShowListBeforeSearch(this, fileInfoManager,
                            listener, filesInfoList);
                    getActivityInfo(activityName).setTask(task);
                    task.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method gets detail information of a file (or directory) by starting
     * a new DetailInfotask.
     *
     * @param activityName name of activity, which the task and operations
     *                     attached to
     * @param file         a certain file (or directory)
     * @param listener     listener of the DetailInfotask
     */
    public void getDetailInfo(String activityName, FileInfo file,
                              OperationEventListener listener) {
        try {
            if (isBusy(activityName)) {
                listener.onTaskResult(OperationEventListener.ERROR_CODE_BUSY);
            } else {
                FileInfoManager fileInfoManager = getActivityInfo(activityName)
                        .getFileInfoManager();
                if (fileInfoManager != null) {
                    BaseAsyncTask task = new DetailInfoTask(this, fileInfoManager,
                            listener, file);
                    getActivityInfo(activityName).setTask(task);
                    task.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method gets size information of a sd card and phone by starting a
     * new UpdatePercentageBarTask.
     *
     * @param activityName name of activity, which the task and operations
     *                     attached to
     * @param listener     listener of the DetailInfotask
     */
    public void updatePercentage(String activityName, OperationEventListener listener) {
        if (isBusy(activityName)) {
            listener.onTaskResult(OperationEventListener.ERROR_CODE_BUSY);
        } else {
            FileInfoManager fileInfoManager = getActivityInfo(activityName)
                    .getFileInfoManager();
            if (fileInfoManager != null) {
                try {
                    UpdatePercentageBarTask task = new UpdatePercentageBarTask(this, fileInfoManager,
                            listener);
                    getActivityInfo(activityName).setTask(task);
                    task.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This method removes listener from task when service disconnected.
     *
     * @param activityName name of activity, which the task attached to
     */
    public void disconnected(String activityName) {
        BaseAsyncTask task = getActivityInfo(activityName).getTask();
        if (task != null) {
            task.removeListener();
        }
    }

    /**
     * This method reconnects to the running task by setting a new listener to
     * the task, when dialog is destroyed and recreated
     *
     * @param activityName name of activity, which the task and dialog attached
     *                     to
     * @param listener     new listener for the task and dialog
     */
    public void reconnected(String activityName, OperationEventListener listener) {
        BaseAsyncTask task = getActivityInfo(activityName).getTask();
        if (task != null) {
            task.setListener(listener);
        }
    }

    /**
     * A 3gpp file could be video type or audio type. The method try to find out
     * its real MIME type from database of MediaStore.
     *
     * @param fileInfo information of a file
     * @return the file's real MIME type
     */
//    public String update3gppMimetype(FileInfo fileInfo) {
//        ContentResolver resolver = getContentResolver();
//        LogUtils.d(TAG, "get3gppOriginalMimetype resolver =" + resolver);
//
//        if (resolver != null && fileInfo != null) {
//            fileInfo.setFileMimeType(FileInfo.MIMETYPE_3GPP_VIDEO);
//            final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//            final String[] projection = new String[] {
//                    MediaStore.MediaColumns.MIME_TYPE
//            };
//            final String selection = MediaStore.MediaColumns.DATA + "=?";
//            final String[] selectionArgs = new String[] {
//                    fileInfo
//                            .getFileAbsolutePath()
//            };
//            Cursor cursor = null;
//            try {
//                cursor = resolver.query(uri, projection, selection,
//                        selectionArgs, null);
//                LogUtils.d(TAG, "get3gppOriginalMimetype cursor=" + cursor
//                        + " file:" + fileInfo.getFileAbsolutePath());
//                if (cursor != null && cursor.moveToFirst()) {
//                    String mimeType = cursor.getString(cursor
//                            .getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
//                    LogUtils.d(TAG, "get3gppOriginalMimetype mimeType: "
//                            + mimeType);
//                    if (mimeType != null) {
//                        fileInfo.setFileMimeType(mimeType);
//                        return mimeType;
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }
//        }
//
//        return FileInfo.MIMETYPE_3GPP_VIDEO;
//    }

    /**
     * This method do search job by starting a new search task
     *
     * @param activityName   name of activity which starts the search
     * @param searchName     the search target
     * @param path           the path to limit the search in
     * @param operationEvent the listener corresponds to this search task
     */
    public void search(String activityName, String searchName, String path,
                       OperationEventListener operationEvent) {
        try {
            if (isBusy(activityName)) {
                cancel(activityName);
            }
            FileInfoManager fileInfoManager = getActivityInfo(activityName).getFileInfoManager();
            fileInfoManager.removeAllItem();
            BaseAsyncTask task = new SearchTask(this, fileInfoManager, operationEvent, searchName, path, getContentResolver());
            getActivityInfo(activityName).setTask(task);
            task.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method do Globalsearch job by starting a new search task
     *
     * @param activityName   name of activity which starts the search
     * @param searchName     the search target
     * @param path           the path to limit the search in
     * @param operationEvent the listener corresponds to this search task
     */
    public void Globalsearch(String activityName, String searchName, String Phonepath, String Sdpath, String usbPath,
                             OperationEventListener operationEvent) {
        ArrayList<String> phonePathArrayList = new ArrayList<String>();
        try {
            if (isBusy(activityName)) {
                cancel(activityName);
            }
            FileInfoManager fileInfoManager = getActivityInfo(activityName).getFileInfoManager();
            fileInfoManager.removeAllItem();
            if (Phonepath != null) {
                phonePathArrayList.add(Phonepath);
//                BaseAsyncTask taskPhone = new SearchTask(this, fileInfoManager, operationEvent, searchName, Phonepath, getContentResolver());
//                 getActivityInfo(activityName).setTask(taskPhone);
//                 taskPhone.execute();
            }
            if (Sdpath != null) {
                phonePathArrayList.add(Sdpath);
//                 BaseAsyncTask taskSDCard = new SearchTask(this, fileInfoManager, operationEvent, searchName, Sdpath, getContentResolver());
//                 getActivityInfo(activityName).setTask(taskSDCard);
//                 taskSDCard.execute();
            }
            if (usbPath != null) {
                phonePathArrayList.add(usbPath);
//                   BaseAsyncTask taskUSb = new SearchTask(this, fileInfoManager, operationEvent, searchName, usbPath, getContentResolver());
//                   getActivityInfo(activityName).setTask(taskUSb);
//                   taskUSb.execute();
            }
            BaseAsyncTask taskUSb = new GlobalSearchTask(this, fileInfoManager, operationEvent, searchName, phonePathArrayList, getContentResolver());
            getActivityInfo(activityName).setTask(taskUSb);
            taskUSb.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method do search job in category mode by starting a new search task
     *
     * @param activityName   name of activity which starts the search
     * @param searchName     the search target
     * @param filesInfoList  the filesInfoList to limit the search in
     * @param operationEvent the listener corresponds to this search task
     */
    public void categorySearch(String activityName, String searchName,
                               OperationEventListener operationEvent, List<FileInfo> filesInfoList, int catagory) {
        try {
            if (isBusy(activityName)) {
                cancel(activityName);
            }
            //else {
            FileInfoManager fileInfoManager = getActivityInfo(activityName)
                    .getFileInfoManager();
            if (fileInfoManager != null) {
                BaseAsyncTask task = new CategorySearchTask(this, fileInfoManager,
                        operationEvent, searchName, filesInfoList, catagory);
                getActivityInfo(activityName).setTask(task);
                task.execute();
            }
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
    /**
     * This method lists files of category by starting a new ListFileTask.
     *
     * @param activityName name of activity, which the ListFileTask attached to
     * @param category     the category
     * @param listener     listener of the ListFileTask
     */
    public void listSafeCategoryFiles(String activityName, int category, Context context,
                                      OperationEventListener listener) {
        Log.d("DDK", "this is enter listSafeCategoryFiles");
        try {
            // ADD START FOR PR1050553 BY HONGBIN.CHEN 20150810
            if (isBusy(activityName)) {
                cancel(activityName);
            }
            FileInfoManager fileInfoManager = getActivityInfo(activityName)
                    .getFileInfoManager();
            if (fileInfoManager != null) {
                BaseAsyncTask task = new SafeCategoryTask(fileInfoManager,
                        listener, category, context);
                getActivityInfo(activityName).setTask(task);
                task.execute();
            }
            // ADD END FOR PR1050553 BY HONGBIN.CHEN 20150810
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /* MODIFIED-END by haifeng.tang,BUG-1987329*/

    public void addPrivateFiles(String activityName, List<FileInfo> fileInfoList,
                                /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2003636*/
                                OperationEventListener listener,boolean isSafeFile) {

        LogUtils.i(TAG, "addPrivateFiles activityName->" + activityName);
        try {
            if (isBusy(activityName) && !isSafeFile) {
            /* MODIFIED-END by wenjing.ni,BUG-2003636*/
                listener.onTaskResult(OperationEventListener.ERROR_CODE_BUSY);
            } else {
                FileInfoManager fileInfoManager = getActivityInfo(activityName).getFileInfoManager();
                if (fileInfoManager != null) {
                    PrivateFileOperationTask task = new AddPrivateFileTask(fileInfoManager,
                            listener, this, fileInfoList);
                    getActivityInfo(activityName).setTask(task);
                    task.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * The method try to find out its type in databases from database of
     * MediaStore.
     *
     * @param fileInfo information of a file
     * @return the file's real MIME type
     */
//    public String queryMimetype(FileInfo fileInfo) {
//        ContentResolver resolver = getContentResolver();
//        if (resolver != null && fileInfo != null) {
//            final Uri uri = MediaStore.Files.getContentUri("external");
//            final String[] projection = new String[] {
//                    MediaStore.MediaColumns.MIME_TYPE
//            };
//            final String selection = MediaStore.MediaColumns.DATA + "=?";
//            final String[] selectionArgs = new String[] {
//                    fileInfo.getFileAbsolutePath()
//            };
//            Cursor cursor = null;
//            try {
//                cursor = resolver.query(uri, projection, selection, selectionArgs, null);
//                if (cursor != null && cursor.moveToFirst()) {
//                    String mimeType = cursor.getString(cursor
//                            .getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
//                    if (mimeType != null) {
//                        fileInfo.setFileMimeType(mimeType);
//                        return mimeType;
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }
//        }
//
//        return FileInfo.MIMETYPE_EXTENSION_UNKONW;
//    }

    /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
    /*MODIFIED-BEGIN by jian.xu, 2016-04-18,BUG-1868328*/
    public synchronized void doDelayOperation() {
        for(BaseAsyncTask task:mDelayTask) {
            if(mDelayActivityName != null) {
                LogUtils.d(TAG, "running task:" + task + "   task.getStatus():" + task.getStatus());
                getActivityInfo(mDelayActivityName).setTask(task);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        mDelayTask.clear();
    }

    public void updateDelayTask(BaseAsyncTask task, String activityName) {
        mDelayTask.clear();
        mDelayTask.add(task);
        LogUtils.d(TAG, "add task:" + task + "   task.getStatus():" + task.getStatus());
        mDelayActivityName = activityName;
    }
    /*MODIFIED-END by jian.xu,BUG-1868328*/
    /* MODIFIED-END by haifeng.tang,BUG-1987329*/

}
