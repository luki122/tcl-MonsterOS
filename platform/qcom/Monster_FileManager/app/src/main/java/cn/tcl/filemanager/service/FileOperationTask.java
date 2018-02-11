/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Downloads;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.adapter.ListFileInfoAdapter;
import cn.tcl.filemanager.drm.DrmManager;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.IconManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.manager.SafeManager;
import cn.tcl.filemanager.service.MultiMediaStoreHelper.CopyMediaStoreHelper;
import cn.tcl.filemanager.service.MultiMediaStoreHelper.DeleteMediaStoreHelper;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;

public abstract class FileOperationTask extends BaseAsyncTask {
    protected static final int BUFFER_SIZE = 256 * 1024;
    protected static final int TOTAL = 100;

    protected MediaStoreHelper mMediaProviderHelper;

    private static final String TAG = "FileOperationTask";
    protected long mFileCurrentSize = 0;
    protected long mPasteHaveSize = 0;

    public FileOperationTask(FileInfoManager fileInfoManager,
            FileManagerService.OperationEventListener operationEvent, Context context) {
        super(context, fileInfoManager, operationEvent);
        if (context == null) {
            throw new IllegalArgumentException();
        } else {
            mMediaProviderHelper = new MediaStoreHelper(context);
        }
    }

    protected boolean deleteFile(File file) {
        if (file == null) {
            publishProgress(new ProgressInfo(
                    FileManagerService.OperationEventListener.ERROR_CODE_DELETE_NO_PERMISSION, true));
        } else {
            // ADD START FOR PR1106804 BY Wenjing.ni 20151103
            if ( file.canWrite() && file.getName().equals("DCIM")) {
                final File to = new File(file.getAbsolutePath() + System.currentTimeMillis());
                if (file.renameTo(to)) {
                    return to.delete();
                } else {
                    publishProgress(new ProgressInfo(
                            FileManagerService.OperationEventListener.ERROR_CODE_DELETE_NO_PERMISSION, true));
                }
            } else if (file.canWrite() && file.delete()) {
                // final File to = new File(file.getAbsolutePath() +
                // System.currentTimeMillis());
                // if (file.renameTo(to)) {
                // return to.delete();
                // }
                return true;
                // ADD END FOR PR1106804 BY Wenjing.ni 20151103 // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329
            } else if (!file.exists()) {
                return true;
            } else {
                publishProgress(new ProgressInfo(
                        FileManagerService.OperationEventListener.ERROR_CODE_DELETE_NO_PERMISSION, true));
            }
        }
        // ADD END FOR PR1097928 BY HONGBIN.CHEN 20150925
        return false;
    }

    /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
    protected File getDstFile(HashMap<String, String> pathMap, File file, String defPath) {

        String curPath = pathMap.get(file.getParent());
        if (curPath == null) {
            curPath = defPath;
        }
        File dstFile = new File(curPath, file.getName());
        return checkFileNameAndRename(dstFile);
    }
    /* MODIFIED-END by haifeng.tang,BUG-1987329*/

    protected boolean mkdir(HashMap<String, String> pathMap, File srcFile, File dstFile) {
        if (srcFile.exists() && srcFile.canRead() && dstFile.mkdirs()) {
            pathMap.put(srcFile.getAbsolutePath(), dstFile.getAbsolutePath());
            return true;
        } else {
            publishProgress(new ProgressInfo(FileManagerService.OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS,
                    true));
            return false;
        }
    }

    private long calcNeedSpace(List<File> fileList) {
        long need = 0;
        for (File file : fileList) {
            need += file.length();
        }
        return need;
    }

    protected boolean isEnoughSpace(List<File> fileList, String dstFolder) {
        try {
            long needSpace = calcNeedSpace(fileList);
            File file = new File(dstFolder);
            long freeSpace = file.getFreeSpace();
            if (needSpace > freeSpace) {
                return false;
            }
        } catch (Exception e) {

        }
        return true;
    }

    protected int getAllDeleteFile(File deleteFile, List<File> deleteList) {
        if (isCancelled()) {
            return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
        }
        if (deleteFile.isDirectory()) {
            deleteList.add(0, deleteFile);
            if (deleteFile.canWrite()) {
                File[] files = deleteFile.listFiles();
                if (files == null) {
                    return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
                }
                for (File file : files) {
                    getAllDeleteFile(file, deleteList);
                }
            }
        } else {
            deleteList.add(0, deleteFile);
        }
        return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
    }

    protected int getAllDeleteFiles(List<FileInfo> fileInfoList, List<File> deleteList) {

        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        for (FileInfo fileInfo : fileInfoList) {
            ret = getAllDeleteFile(fileInfo.getFile(), deleteList);
            if (ret < 0) {
                break;
            }
        }
        return ret;
    }

    protected Integer canOperate(String dstFolder, List<FileInfo> fileInfoList) {
        List<File> fileList = new ArrayList<>();
        UpdateInfo updateInfo = new UpdateInfo();
        int ret = getAllFileList(fileInfoList, fileList, updateInfo);
        if (ret < 0) {
            return ret;
        }

        if (!isEnoughSpace(fileList, dstFolder)) {
            return FileManagerService.OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE;
        }
        return ret;
    }

    protected int getAllFileList(List<FileInfo> srcList, List<File> resultList,
            UpdateInfo updateInfo) {

        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        for (FileInfo fileInfo : srcList) {
            ret = getAllFile(fileInfo.getFile(), resultList, updateInfo);
            if (ret < 0) {
                break;
            }
        }
        return ret;
    }

    protected int getAllFile(File srcFile, List<File> fileList, UpdateInfo updateInfo) {
        if (isCancelled()) {
            return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
        }
        fileList.add(srcFile);
        if (srcFile.canRead()) {
            if (srcFile.isDirectory()) {
                File[] files = srcFile.listFiles();
                if (files == null) {
                    return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
                }
                for (File file : files) {
                    int ret = getAllFile(file, fileList, updateInfo);
                    if (ret < 0) {
                        return ret;
                    }
                }
            } else {
                updateInfo.updateTotal(srcFile.length());
            }
        }
        return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
    }

    protected int copyFile(byte[] buffer, File srcFile, File dstFile, UpdateInfo updateInfo) {
        FileInputStream in = null;
        FileOutputStream out = null;
        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        try {
            if (!dstFile.createNewFile()) {
                return FileManagerService.OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
            }
            if (!srcFile.exists()) {
                return FileManagerService.OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
            }
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(dstFile);

            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                // Copy data from in stream to out stream
//                modify by haifeng.tang@tcl.com for PR:1201757 start 2015-12-23
                if (isCancelled() || mFileInfoManager.getPasteStatus() == FileInfoManager.PASTE_MODE_CANCEL) {
                    LogUtils.e(TAG,"CopyFile cancelled by user....");
                    if (!dstFile.delete()) {
                        LogUtils.e(this.getClass().getName(), "delete fail in copyFile()");
                    }
                    return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                }
//              modify by haifeng.tang@tcl.com for PR:1201757 end 2015-12-23
                out.write(buffer, 0, len);
                if (!dstFile.isDirectory()) {
                    mFileCurrentSize = dstFile.length();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ret = FileManagerService.OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
        } finally {
        	try {
	        	if (in != null) {
	        		in.close();
	        	}
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        	try {
	        	if (out != null) {
	        		out.close();
	        	}
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        }
        return ret;
    }

    public File checkFileNameAndRename(File conflictFile) {
        File retFile = conflictFile;
        while (true) {
            if (isCancelled()) {
                return null;
            }
            if (!retFile.exists()) {
                return retFile;
            }
            retFile = FileUtils.genrateNextNewName(retFile);
            if (retFile == null) {
                return null;
            }
        }
    }

    protected void updateProgressWithTime(UpdateInfo updateInfo, File file) {
        if (updateInfo.needUpdate()) {
            int progress = (int) (updateInfo.getProgress() * TOTAL / updateInfo.getTotal());
            publishProgress(new ProgressInfo(file.getName(), progress, TOTAL));
        }
    }

    protected void addItem(HashMap<File, FileInfo> fileInfoMap, File file, File addFile) {
        if (fileInfoMap.containsKey(file)) {
            FileInfo fileInfo = new FileInfo(mContext, addFile);
            mFileInfoManager.addItem(fileInfo);
        }
    }

    protected void addItemWithMimeType(HashMap<File, FileInfo> fileInfoMap, File file,
            File addFile, FileManagerService mService) {
        if (fileInfoMap.containsKey(file)) {
            FileInfo fileInfo = new FileInfo(mContext, addFile);
            fileInfo.setFileMimeType(fileInfoMap.get(file).getMimeType());
            mFileInfoManager.addItem(fileInfo);
        }
    }

    protected void removeItem(HashMap<File, FileInfo> fileInfoMap, File file, File removeFile) {
        if (fileInfoMap.containsKey(file)) {
            mFileInfoManager.removeItem(fileInfoMap.get(removeFile));
        }
    }

    static class DeleteFilesTask extends FileOperationTask {
        private List<FileInfo> mDeletedFilesInfo;
        private int mDeleteMode = 0;
        private Context mContext;

        public DeleteFilesTask(FileInfoManager fileInfoManager,int deleteMode,
                FileManagerService.OperationEventListener operationEvent, Context context, List<FileInfo> fileInfoList) {
            super(fileInfoManager, operationEvent, context);
            mDeletedFilesInfo = fileInfoList;
            mDeleteMode = deleteMode;
            mContext = context;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            List<File> deletefileList = new ArrayList<File>();
            UpdateInfo updateInfo = new UpdateInfo();
            if(mDeleteMode == SafeManager.SAFE_DESTORY_DELETE_MODE){
                mDeletedFilesInfo = SafeManager.querySafeAllFileInfo(mContext);
            }

            List<FileInfo> selectInfo = new ArrayList<>();
            selectInfo.addAll(mDeletedFilesInfo);
            int size = selectInfo.size();
            FileManagerApplication application = (FileManagerApplication) mContext.getApplicationContext();

            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES && mDeleteMode != SafeManager.DELETE_ALBUM_MODE) {
                deletefileList.clear();
                mDeletedFilesInfo.clear();
                for (int i = 0; i < size; i++) {
                    if (selectInfo.get(i).getSubFileInfo() != null && selectInfo.get(i).getSubFileInfo().size() > 0) {
                        mDeletedFilesInfo.addAll(selectInfo.get(i).getSubFileInfo());
                    } else {
                        mDeletedFilesInfo.add(selectInfo.get(i));
                    }
                }
            }
            int ret = getAllDeleteFiles(mDeletedFilesInfo, deletefileList);
            if (ret < 0) {
                return ret;
            }

            /** delete temp data by floder or file*/
            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                int index = -1;
                for (int i = 0; i < size; i++) {
                    if (selectInfo.get(i).getSubFileInfo() != null) {
                        boolean flag = application.mFileInfoList.remove(selectInfo.get(i));
                        LogUtils.i(TAG, "remove floder flag:" + flag);
                    } else {
                        /** location current file index in temp list */
                        if (index == -1) {
                            int temp_size = application.mFileInfoList.size();
                            for (int m = 0; m < temp_size; m++) {
                                if (application.mFileInfoList.get(m).getSubFileInfo().remove(selectInfo.get(i))) {
                                    index = m;
                                    LogUtils.i(TAG, "remove floder index:" + index);
                                    removeTempFolder(index, application, i, selectInfo);
                                    temp_size = application.mFileInfoList.size();
                                }
                            }
                        } else {
                            removeTempFolder(index, application, i, selectInfo);
                        }
                    }
                }
            }
            selectInfo.clear();

            DeleteMediaStoreHelper deleteMediaStoreHelper = new DeleteMediaStoreHelper(
                    mMediaProviderHelper);

            HashMap<File, FileInfo> deleteFileInfoMap = new HashMap<File, FileInfo>();
            for (FileInfo fileInfo : mDeletedFilesInfo) {
                deleteFileInfoMap.put(fileInfo.getFile(), fileInfo);
            }

            publishProgress(new ProgressInfo("", (int) updateInfo.getProgress(), deletefileList.size()));
            boolean isSafeBoxFile = false;
            if(mDeleteMode == SafeManager.SAFE_DELETE_MODE || mDeleteMode == SafeManager.SAFE_DESTORY_DELETE_MODE){
                isSafeBoxFile = true;
            }
            try {
	            for (File file : deletefileList) {
                    if (mFileInfoManager.getDeleteStatus() == FileInfoManager.DELETE_MODE_CANCEL) {
                        publishProgress(new ProgressInfo(
                                FileManagerService.OperationEventListener.ERROR_CODE_DELETE_CANCEL, true));
                        break;
                    }
                    if (isCancelled()) {
	                    return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
	                }
                    if (deleteFile(file)) {
                        deleteMediaStoreHelper.addRecord(file.getAbsolutePath());
	                    removeItem(deleteFileInfoMap, file, file);
	                    // ADD START FOR PR58533 BY HONGBIN.CHEN 20150914
	                    IconManager.getInstance().removeCache(file.getAbsolutePath());
	                    // ADD END FOR PR58533 BY HONGBIN.CHEN 20150914
	                    //add for PR933897 by yane.wang@jrdcom.com 20150216 begin
						if (file.isHidden() && mDeleteMode != SafeManager.SAFE_DESTORY_DELETE_MODE) {
	                        mFileInfoManager.removeHideItem(new FileInfo(mContext, file));
	                    }
                        if(isSafeBoxFile) {
                            SafeManager.deleteSafeFileRecord(mContext, file.getAbsolutePath());
                        }
	                  //add for PR933897 by yane.wang@jrdcom.com 20150216 end
	                }else {
                        mFileInfoManager.addFailFiles(file);
                    }
                    updateInfo.updateProgress(1);
                    if (updateInfo.needUpdate()) {
                        publishProgress(new ProgressInfo(file.getName(),
                                (int) updateInfo.getProgress(), deletefileList.size()));
                    }
                    DataOutputStream dos = null;
                    try {
                        dos = new DataOutputStream(new FileOutputStream(file));
                        dos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(file);
                    intent.setData(uri);
                    mContext.sendBroadcast(intent);

                    deleContentProviderData(file.getAbsolutePath());

                    file.delete();
                }
            } finally {
                deleteMediaStoreHelper.updateRecords(); // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329
            }
          //add for PR203541 by yane.wang@jrdcom.com 20150505 begin
            DrmManager.getInstance(mContext.getApplicationContext()).restoreWallpaper();
            //add for PR203541 by yane.wang@jrdcom.com 20150505 end
            mFileInfoManager.setDeleteStatus(FileInfoManager.DELETE_MODE_GOING);
            return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        }

        /**
         * delete contentprovider data by file path
         * @param path file path
         */
        private void deleContentProviderData(String path) {
            int flagByCategory = -1;
            Uri uri = MediaStore.Files.getContentUri("external");
            LogUtils.i(this.getClass().getName(), "path:" + path + ",uri:" + uri);
            int flagByFiles = mContext.getContentResolver().delete(uri, MediaStore.Files.FileColumns.DATA + " = \"" + path + "\"", null);
            if (CategoryManager.CATEGORY_PICTURES == CategoryManager.mCurrentCagegory) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                flagByCategory = mContext.getContentResolver().delete(uri, MediaStore.Images.ImageColumns.DATA + " = \"" + path + "\"", null);
            } else if (CategoryManager.CATEGORY_VEDIOS == CategoryManager.mCurrentCagegory) {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                flagByCategory = mContext.getContentResolver().delete(uri, MediaStore.Video.VideoColumns.DATA + " = \"" + path + "\"", null);
            } else if (CategoryManager.CATEGORY_MUSIC == CategoryManager.mCurrentCagegory) {
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                flagByCategory = mContext.getContentResolver().delete(uri, MediaStore.Audio.AudioColumns.DATA + " = \"" + path + "\"", null);
            } else if (CategoryManager.CATEGORY_DOWNLOAD == CategoryManager.mCurrentCagegory) {
                uri = Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI;
                flagByCategory = mContext.getContentResolver().delete(uri, Downloads.Impl._DATA + " = '" + path + "'", null);
            }
            LogUtils.i(this.getClass().getName(), "flagByCategory:" + flagByCategory + ",flagByFiles:" + flagByFiles);
        }

        /**
         * remove temp list file by fileinfo
         * @param index temp list location
         * @param application
         * @param i index
         * @param selectInfo  file list
         */
        private void removeTempFolder(int index, FileManagerApplication application, int i, List<FileInfo> selectInfo) {
            List<FileInfo> list = application.mFileInfoList.get(index).getSubFileInfo();
            boolean flag = list.remove(selectInfo.get(i));
            if (list == null || list.size() < 1) {
                application.mFileInfoList.remove(index);
                LogUtils.i(TAG, "remove file!!");
            }
            LogUtils.i(TAG, "remove floder flag:" + flag);
        }
    }

    public static class UpdateInfo {
        protected static final int NEED_UPDATE_TIME = 200;
        private long mStartOperationTime = 0;
        private long mTotalCount = 0;
        private long mCurrentCount = 0;
        private long mTotalSize = 0;
        private long mCurrentSize = 0;

        public UpdateInfo() {
            mStartOperationTime = System.currentTimeMillis();
        }

        public long getTotalCount() {
            return mTotalCount;
        }

        public long getProgress() {
            return mCurrentCount;
        }

        public long getTotal() {
            return mTotalSize;
        }

        public long getCurrent() {
            return mCurrentSize;
        }

        public void updateTotalCount(long count) {
            mTotalCount += count;
        }

        public void updateProgress(long addSize) {
            mCurrentCount += addSize;
        }

        public void updateTotal(long addSize) {
            mTotalSize += addSize;
        }

        public void updateCurrent(long addSize) {
            mCurrentSize = addSize;
        }

        public boolean needUpdate() {
            long operationTime = System.currentTimeMillis() - mStartOperationTime;
            if (operationTime > NEED_UPDATE_TIME) {
                mStartOperationTime = System.currentTimeMillis();
                return true;
            }
            return false;
        }

    }

    static class CutPasteFilesTask extends FileOperationTask {
        private final List<FileInfo> mSrcList;
        private final String mDstFolder;
        private FileManagerService mService;

        public CutPasteFilesTask(FileInfoManager fileInfoManager,
                FileManagerService.OperationEventListener operationEvent, Context context, List<FileInfo> src,
                String destFolder) {
            super(fileInfoManager, operationEvent, context);
            mSrcList = src;
            mDstFolder = destFolder;
            mService = (FileManagerService) context;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (mSrcList.isEmpty()) {
                return FileManagerService.OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
            }

          //add by yane.wang@jrdcom.com 20150303 begin
//            if (isSameRoot(mSrcList.get(0).getFileAbsolutePath(), mDstFolder)) {
//                return cutPasteInSameCard();
//            } else {
                return cutPasteInDiffCard();
//            } // PR-1175531 Nicky Ni -001 20151219
          //add by yane.wang@jrdcom.com 20150303 end
        }

      /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
      //add by yane.wang@jrdcom.com 20150303 begin
        private boolean isSameRoot(String srcPath, String dstPath) {
            MountManager mpm = MountManager.getInstance();
            String srcMountPoint = mpm.getRealMountPointPath(srcPath);
            String dstMountPoint = mpm.getRealMountPointPath(dstPath);
            if (srcMountPoint != null && dstMountPoint != null
                    && srcMountPoint.equals(dstMountPoint)) {
                return true;
            }
            return false;
        }

        private Integer cutPasteInSameCard() {
            UpdateInfo updateInfo = new UpdateInfo();
            int total = mSrcList.size();

            updateInfo.updateTotal(total);
            publishProgress(new ProgressInfo("", 0, total));

            CopyMediaStoreHelper pasteMediaStoreHelper = new CopyMediaStoreHelper(mMediaProviderHelper);
            DeleteMediaStoreHelper deleteMediaStoreHelper = new DeleteMediaStoreHelper(mMediaProviderHelper);

            // Set dstFolder so we can scan folder instead of scanning each file one by one.
            pasteMediaStoreHelper.setDstFolder(mDstFolder);

            boolean showHidden = SharedPreferenceUtils.isShowHidden(mService);

            try {
	            for (FileInfo fileInfo : mSrcList) {
	                File newFile = new File(mDstFolder + MountManager.SEPARATOR + fileInfo.getFileName());
	                newFile = checkFileNameAndRename(newFile);
	                if (isCancelled()) {
	                    pasteMediaStoreHelper.updateRecords();
	                    deleteMediaStoreHelper.updateRecords();
	                    return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
	                }

	                if (newFile == null) {
	                    publishProgress(new ProgressInfo(FileManagerService.OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS, true));
	                    continue;
	                }

	                if (fileInfo.getFile().renameTo(newFile)) {
	                    updateInfo.updateProgress(1);
	                    FileInfo newFileInfo = new FileInfo(mContext, newFile);
	                    if (showHidden || !newFileInfo.isHideFile()) {
	                    	mFileInfoManager.addItem(newFileInfo);
	                    }
	                    if (newFile.isHidden()) {
	                        mFileInfoManager.addHideItem(newFileInfo);
	                    }
	                    deleteMediaStoreHelper.addRecord(fileInfo.getFileAbsolutePath());
	                    pasteMediaStoreHelper.addRecord(newFile.getAbsolutePath());
	                } else {
	                    publishProgress(new ProgressInfo(FileManagerService.OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS, true));
	                }

	                if (updateInfo.needUpdate()) {
	                    publishProgress(new ProgressInfo(fileInfo.getFile().getName(),
	                            (int) updateInfo.getProgress(), total));
	                }
	            }
            } finally {
            	pasteMediaStoreHelper.updateRecords();
                deleteMediaStoreHelper.updateRecords();
            }

            publishProgress(new ProgressInfo("", (int) updateInfo.getProgress(), updateInfo.getTotal()));

            return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        }
      //add by yane.wang@jrdcom.com 20150303 end
      /* MODIFIED-END by haifeng.tang,BUG-1987329*/


        private Integer cutPasteInDiffCard() {
            int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
            final List<File> fileList = new ArrayList<File>();
            final UpdateInfo updateInfo = new UpdateInfo();
            Timer timer = new Timer();
            ret = getAllFileList(mSrcList, fileList, updateInfo);
            if (ret < 0) {
                return ret;
            }
            if (!isEnoughSpace(fileList, mDstFolder)) {
                return FileManagerService.OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE;
            }

            List<File> romoveFolderFiles = new LinkedList<File>();
//            updateInfo.updateTotal(fileList.size());
            publishProgress(new ProgressInfo("", 0, fileList.size(), updateInfo.getCurrent(), updateInfo.getTotal()));
            byte[] buffer = new byte[BUFFER_SIZE];
            HashMap<String, String> pathMap = new HashMap<String, String>();
            if (!fileList.isEmpty()) {
                pathMap.put(fileList.get(0).getParent(), mDstFolder);
            }

            CopyMediaStoreHelper copyMediaStoreHelper = new CopyMediaStoreHelper(mMediaProviderHelper);
            DeleteMediaStoreHelper deleteMediaStoreHelper = new DeleteMediaStoreHelper(mMediaProviderHelper);
            // Set dstFolder so we can scan folder instead of scanning each file one by one.
            copyMediaStoreHelper.setDstFolder(mDstFolder);
            HashMap<File, FileInfo> cutFileInfoMap = new HashMap<File, FileInfo>();
            for (FileInfo fileInfo : mSrcList) {
                cutFileInfoMap.put(fileInfo.getFile(), fileInfo);
            }

            MountManager mount = MountManager.getInstance();//add for PR908671 by yane.wang@jrdcom.com 20150127
            try {
                for (final File file : fileList) {
                    if (mFileInfoManager.getPasteStatus() == FileInfoManager.PASTE_MODE_CANCEL) {
                        publishProgress(new ProgressInfo(
                                FileManagerService.OperationEventListener.ERROR_CODE_CUT_CANCEL, true));
                        break;
                    }
                    File dstFile = getDstFile(pathMap, file, mDstFolder);
                    if (isCancelled()) {
                        return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                    }
                    if (dstFile == null || file == null) {
                        publishProgress(new ProgressInfo(FileManagerService.OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS, true));
                        continue;
                    }

                    //add for PR908671,917059 by yane.wang@jrdcom.com 20150127 begin
                    if (mount != null && !file.getAbsolutePath().startsWith(SafeUtils.getEncryptRootPath(mContext))) {
                        String dst = mount.getRealMountPointPath(dstFile.getAbsolutePath());
                        String sour = mount.getRealMountPointPath(file.getAbsolutePath());
                        if (!mount.isMounted(dst) || !mount.isMounted(sour)) {
                            copyMediaStoreHelper.updateRecords();
                            return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                        }
                    }
                    //add for PR908671,917059 by yane.wang@jrdcom.com 20150127 end

                    if (file.isDirectory()) {
                        if (mkdir(pathMap, file, dstFile)) {
                            copyMediaStoreHelper.addRecord(dstFile.getAbsolutePath());
                            addItem(cutFileInfoMap, file, dstFile);
                            romoveFolderFiles.add(0, file);
                        }
                    } else {
                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    updateInfo.updateCurrent(mPasteHaveSize + mFileCurrentSize);
                                    if (updateInfo.needUpdate()) {
                                        publishProgress(new ProgressInfo(file.getName(),
                                                (int) updateInfo.getProgress(), fileList.size(),
                                                updateInfo.getCurrent(), updateInfo.getTotal()));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        timer.schedule(timerTask, 0, 500);

                        ret = copyFile(buffer, file, dstFile, updateInfo);
                        if (ret == FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL) {
                            return ret;
                        } else if (ret < 0) {
                            publishProgress(new ProgressInfo(FileManagerService.OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS, true));
                            mFileInfoManager.addFailFiles(file);
                        } else {
                            copyMediaStoreHelper.addRecord(dstFile.getAbsolutePath());
                            addItemWithMimeType(cutFileInfoMap, file, dstFile, mService);
                            if (deleteFile(file)) {
                                deleteMediaStoreHelper.addRecord(file.getAbsolutePath());
                            }
                        }
                    }
                    //add for PR933897 by yane.wang@jrdcom.com 20150216 begin
                    if (dstFile.isHidden()) {
                        mFileInfoManager.addHideItem(new FileInfo(mContext, dstFile));
                    }
                    //add for PR933897 by yane.wang@jrdcom.com 20150216 end
                    updateInfo.updateProgress(1);
                    if (updateInfo.needUpdate()) {
                        publishProgress(new ProgressInfo(file.getName(), (int) updateInfo.getProgress(), fileList.size(),
                                updateInfo.getCurrent(), updateInfo.getTotal()));
                    }
                    if (!dstFile.isDirectory()) {
                        mPasteHaveSize = mPasteHaveSize + dstFile.length();
                    }
                    mFileCurrentSize = 0;
                }

                for (File file : romoveFolderFiles) {
                    if (file.delete()) {
                        deleteMediaStoreHelper.addRecord(file.getAbsolutePath());
                    }
                }
            } finally {
                copyMediaStoreHelper.updateRecords();
                deleteMediaStoreHelper.updateRecords();
            }
            timer.cancel();
            mPasteHaveSize = 0;
            mFileInfoManager.setPasteStatus(FileInfoManager.PASTE_MODE_GOING);
            return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        }
    }

    static class CopyPasteFilesTask extends FileOperationTask {

        List<FileInfo> mSrcList = null;
        String mDstFolder = null;
        private FileManagerService mService;
        /** Buffer size for data read and write. */
        public static final int BUFFER_SIZE = 4 * 1024;

        public CopyPasteFilesTask(FileInfoManager fileInfoManager,
                FileManagerService.OperationEventListener operationEvent, Context context, List<FileInfo> src,
                String destFolder) {
            super(fileInfoManager, operationEvent, context);
            mSrcList = src;
            mDstFolder = destFolder;
            mService = (FileManagerService) context;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            final List<File> fileList = new ArrayList<File>();
            List<FileInfo> tempFileInfo = new ArrayList<>();
            final UpdateInfo updateInfo = new UpdateInfo();
            Timer timer = new Timer();
            int size = mSrcList.size();
            for (int i = 0; i < size; i++) {
                FileInfo fileInfo = mSrcList.get(i);
                if (fileInfo.isDirectory() && MountManager.getInstance().isSdOrPhonePath(fileInfo.getFileAbsolutePath())) {
                    mSrcList.remove(fileInfo);
                    tempFileInfo.addAll(fileInfo.getSubFileInfo());
                    i = (i == 0) ? 0 : i - 1;
                    size = mSrcList.size();
                }
            }
            mSrcList.addAll(tempFileInfo);
            tempFileInfo.clear();
            int ret = getAllFileList(mSrcList, fileList, updateInfo);
            if (ret < 0) {
                return ret;
            }

            CopyMediaStoreHelper copyMediaStoreHelper = new CopyMediaStoreHelper(mMediaProviderHelper);
            copyMediaStoreHelper.setDstFolder(mDstFolder);

            HashMap<File, FileInfo> copyFileInfoMap = new HashMap<File, FileInfo>();
            for (FileInfo fileInfo : mSrcList) {
                copyFileInfoMap.put(fileInfo.getFile(), fileInfo);
            }

            if (!isEnoughSpace(fileList, mDstFolder)) {
                return FileManagerService.OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE;
            }
//            updateInfo.updateTotal(fileList.size());
            publishProgress(new ProgressInfo("", 0, fileList.size(),
                    updateInfo.getCurrent(), updateInfo.getTotal()));
            byte[] buffer = new byte[BUFFER_SIZE];
            HashMap<String, String> pathMap = new HashMap<String, String>();
            if (!fileList.isEmpty()) {
                pathMap.put(fileList.get(0).getParent(), mDstFolder);
            }
            MountManager mount = MountManager.getInstance();//add for PR908671 by yane.wang@jrdcom.com 20150127
            //int count = 0;
            try {
                for (final File file : fileList) {
                    if (mFileInfoManager.getPasteStatus() == FileInfoManager.PASTE_MODE_CANCEL) {
                        publishProgress(new ProgressInfo(
                                FileManagerService.OperationEventListener.ERROR_CODE_COPY_CANCEL, true));
                        break;
                    }
                    File dstFile = getDstFile(pathMap, file, mDstFolder);
                    if (isCancelled()) {
                        return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                    }
                    if (dstFile == null || file == null) {
                        publishProgress(new ProgressInfo(FileManagerService.OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS, true));
                        continue;
                    }
                    //add for PR908671,917059 by yane.wang@jrdcom.com 20150127 begin
                    if (mount != null && !file.getAbsolutePath().startsWith(SafeUtils.getEncryptRootPath(mContext))) {
                        String dst = mount.getRealMountPointPath(dstFile.getAbsolutePath());
                        String sour = mount.getRealMountPointPath(file.getAbsolutePath());
                        if (!mount.isMounted(dst) || !mount.isMounted(sour)) {
                            return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                        }
                    }
                    //add for PR908671,917059 by yane.wang@jrdcom.com 20150127 end

                    boolean showHidden = SharedPreferenceUtils.isShowHidden(mService);

                    if (file.isDirectory()) {
                        if (mkdir(pathMap, file, dstFile)) {
                            copyMediaStoreHelper.addRecord(dstFile.getAbsolutePath());
                            if (showHidden || !dstFile.isHidden()) {
                                addItem(copyFileInfoMap, file, dstFile);
                            }
                        }
                    } else {
                        if (DrmManager.isDrmFileExt(file.getName()) || !file.canRead()) {
                            publishProgress(new ProgressInfo(FileManagerService.OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION, true));
                            continue;
                        }

                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    updateInfo.updateCurrent(mPasteHaveSize + mFileCurrentSize);
                                    if (updateInfo.needUpdate()) {
                                        publishProgress(new ProgressInfo(file.getName(),
                                                (int) updateInfo.getProgress(), fileList.size(), updateInfo.getCurrent(), updateInfo.getTotal()));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        timer.schedule(timerTask, 0, 500);

                        ret = copyFile(buffer, file, dstFile, updateInfo);
                        if (ret == FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL) {
                            return ret;
                        } else if (ret < 0) {
                            publishProgress(new ProgressInfo(ret, true));
                            mFileInfoManager.addFailFiles(file); // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329
                        } else {
                            copyMediaStoreHelper.addRecord(dstFile.getAbsolutePath());
                            if (showHidden || !dstFile.isHidden()) {
                                addItemWithMimeType(copyFileInfoMap, file, dstFile, mService);
                            }
                        }
                    }

                    if (dstFile.isHidden()) {
                        mFileInfoManager.addHideItem(new FileInfo(mContext, dstFile));
                    }
                    updateInfo.updateProgress(1);
                    if (updateInfo.needUpdate()) {
                        publishProgress(new ProgressInfo(file.getName(),
                                (int) updateInfo.getProgress(), fileList.size(), updateInfo.getCurrent(), updateInfo.getTotal()));
                    }
                    if (!dstFile.isDirectory()) {
                        mPasteHaveSize = mPasteHaveSize + dstFile.length();
                    }
                    mFileCurrentSize = 0;
                }
            } finally {
                copyMediaStoreHelper.updateRecords();
            }
            //  copyMediaStoreHelper.updateRecords();
            timer.cancel();
            mPasteHaveSize = 0;
            mFileInfoManager.setPasteStatus(FileInfoManager.PASTE_MODE_GOING);
            return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        }
    }

    static class CreateFolderTask extends FileOperationTask {
        public static final String TAG = "CreateFolderTask";
        private final String mDstFolder;
        int mFilterType;

        public CreateFolderTask(FileInfoManager fileInfoManager,
                FileManagerService.OperationEventListener operationEvent, Context context, String dstFolder,
                int filterType) {
            super(fileInfoManager, operationEvent, context);
            mDstFolder = dstFolder;
            mFilterType = filterType;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int ret = FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;

            ret = FileUtils.checkFileName(FileUtils.getFileName(mDstFolder));
            if (ret < 0) {
                return ret;
            }

            File dir = new File(mDstFolder.trim());
            if (dir.exists()) {
                return FileManagerService.OperationEventListener.ERROR_CODE_FILE_EXIST;
            }
            File path = new File(FileUtils.getFilePath(mDstFolder));
            if (path.getFreeSpace() <= 0) {
                return FileManagerService.OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE;
            }
            if (dir.mkdirs()) {
                FileInfo fileInfo = new FileInfo(mContext, dir);
              //add for PR933897 by yane.wang@jrdcom.com 20150216 begin
                boolean hide = fileInfo.isHideFile();

				if (!hide || mFilterType == FileManagerService.FILE_FILTER_TYPE_ALL) {
					mFileInfoManager.addItem(fileInfo);
				}
				if (hide) {
					mFileInfoManager.addHideItem(fileInfo);
				}
              //add for PR933897 by yane.wang@jrdcom.com 20150216 end
                mMediaProviderHelper.scanPathforMediaStore(fileInfo.getFileAbsolutePath());
                return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
            } else {
                return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
            }
        }
    }

    static class RenameTask extends FileOperationTask {
        public static final String TAG = "RenameTask";
        private final FileInfo mDstFileInfo;
        private final FileInfo mSrcFileInfo;
        private String mSearchTextString;
        int mFilterType = 0;
        private String mCurrentPath;
        private int mCurrentMode;

        public RenameTask(FileInfoManager fileInfoManager, FileManagerService.OperationEventListener operationEvent,
                String mSearchString, Context context, FileInfo srcFile, FileInfo dstFile, int filterType,int mode,String path) {
            super(fileInfoManager, operationEvent, context);
            mDstFileInfo = dstFile;
            mSrcFileInfo = srcFile;
            mFilterType = filterType;
            mSearchTextString=mSearchString;
            mCurrentPath = path;
            mCurrentMode = mode;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            LogUtils.e(this.getClass().getName(), "Rename operation begin");
            int ret = FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;

            String dstFile = mDstFileInfo.getFileAbsolutePath();
            boolean isFolder = true;//add for PR926130 by yane.wang@jrdcom.com 20150209
            dstFile = dstFile.trim();
            ret = FileUtils.checkFileName(FileUtils.getFileName(dstFile));
            if (ret < 0) {
                return ret;
            }

            File newFile = mDstFileInfo.getFile();
            File oldFile = mSrcFileInfo.getFile();

            if (newFile.exists()) {
                LogUtils.e(this.getClass().getName(), "File exist when renaming.");
                return FileManagerService.OperationEventListener.ERROR_CODE_FILE_EXIST;
            } else if (dstFile.endsWith(".")) {
                //[BUGFIX]-Mod-BEGIN by TSNJ,qinglian.zhang,09/03/2014,PR-776005
                return FileManagerService.OperationEventListener.ERROR_INVALID_CHAR;
                //[BUGFIX]-Mod-END by TSNJ,qinglian.zhang
            }

            if (oldFile.renameTo(newFile)) {
                // ADD START FOR PR659307 BY HONGBIN.CHEN 20150925
                IconManager.getInstance().removeCache(oldFile.getAbsolutePath());
                // ADD END FOR PR659307 BY HONGBIN.CHEN 20150925
                FileInfo newFileInfo = new FileInfo(mContext, newFile);//added by bin.song@tcl.com for PR922479
                mFileInfoManager.removeItem(mSrcFileInfo);
                boolean isHideFile = newFileInfo.isHideFile();
                LogUtils.e(TAG,"Rename task is HideFile " + isHideFile);
                if (!isHideFile || mFilterType == FileManagerService.FILE_FILTER_TYPE_ALL) {
                    if (!TextUtils.isEmpty(mSearchTextString)) {
                        if(newFileInfo.getFileName().contains(mSearchTextString)) {
                            mFileInfoManager.getSearchFileList().add(newFileInfo);
                            mFileInfoManager.addItem(newFileInfo);
                        } else if(mCurrentMode == ListFileInfoAdapter.MODE_SEARCH && mCurrentPath != null){
                            mFileInfoManager.addItem(newFileInfo);
                        }
                    } else {
                        LogUtils.e(TAG,"Rename task FileInfoManager add item NewFileInfo " + newFileInfo.getFileName());
                        mFileInfoManager.addItem(newFileInfo); //MODIFIED by jian.xu, 2016-04-11,BUG-1915927
                        LogUtils.e(TAG,"Rename task FileInfoManager size " + mFileInfoManager.getAddFilesInfoList().size());
                    }
                    LogUtils.e(TAG,"Rename task Category add item NewFileInfo " + newFileInfo.getFileName());
                    mFileInfoManager.getCategoryFileList().add(newFileInfo);
                    LogUtils.e(TAG,"Rename task Category size " + mFileInfoManager.getCategoryFileList().size());
                }
				if (isHideFile) {
					mFileInfoManager.addHideItem(newFileInfo);
				}
				if (mSrcFileInfo.isHideFile()) {
					mFileInfoManager.removeHideItem(mSrcFileInfo);
				}
              //add for PR926130 by yane.wang@jrdcom.com 20150209
				if (newFile.isDirectory()) {
					isFolder = true;
				} else {
					isFolder = false;
				}
              //add for PR926130 by yane.wang@jrdcom.com 20150209
                //add for PR815918,PR856877 by yane.wang@jrdcom.com 20141107 begin
				mMediaProviderHelper.updateInMediaStore(newFileInfo.getFileAbsolutePath(),
						mSrcFileInfo.getFileAbsolutePath(), isFolder);
                //mMediaProviderHelper.scanMedia(newFile, oldFile);
              //add for PR815918,PR856877 by yane.wang@jrdcom.com 20141107 end
                return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
            } else {
                return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
            }
        }
    }
}
