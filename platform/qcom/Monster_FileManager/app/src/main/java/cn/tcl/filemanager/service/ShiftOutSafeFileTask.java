/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import android.content.Context;
import android.util.Log;

import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.SafeManager;
import cn.tcl.filemanager.utils.FileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by user on 16-3-12.
 */
public class ShiftOutSafeFileTask extends FileOperationTask {
    private List<FileInfo> mSrcList;
    private final String mDstFolder;
    private final int mMode;
    private Context mContext;
    private static MultiMediaStoreHelper.DeleteMediaStoreHelper deleteMediaStoreHelper;

    private static MultiMediaStoreHelper.CopyMediaStoreHelper mCopyMediaStoreHelper;

    public ShiftOutSafeFileTask(FileInfoManager fileInfoManager,
                                FileManagerService.OperationEventListener operationEvent, String dstFolder, int mode, Context context, List<FileInfo> src) {
        super(fileInfoManager, operationEvent, context);
        mSrcList = src;
        mDstFolder = dstFolder;
        mMode = mode;
        mContext = context;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        int progress = 0;
        if (mMode == SafeManager.DESTORY_SHIFT_OUT_MODE) {
            mSrcList = SafeManager.querySafeAllFileInfo(mContext);
        }
        int total = mSrcList.size();
        if (mSrcList.isEmpty()) {
            return FileManagerService.OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS;
        }
        List<String> sourcePath;
        Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--111--" + mMode + "total is " + total);
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(SafeManager.getKey(mContext), "AES");
            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            List<File> fileList = new ArrayList<File>();
            UpdateInfo updateInfo = new UpdateInfo();
            ret = getAllFileList(mSrcList, fileList, updateInfo);
            Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--333--" + mMode + "ret is" + ret);
            if (ret < 0) {
                return ret;
            }
            if (!isEnoughSpace(fileList, mDstFolder)) {
                return FileManagerService.OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE;
            }
            Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--333--" + mMode + "ret is" + ret);
            publishProgress(new ProgressInfo("", 0, mSrcList.size()));
            deleteMediaStoreHelper = new MultiMediaStoreHelper.DeleteMediaStoreHelper(mMediaProviderHelper);
            mCopyMediaStoreHelper = new MultiMediaStoreHelper.CopyMediaStoreHelper(mMediaProviderHelper);
            Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--222--" + mMode + "mSrcList size is" + mSrcList.size());
            List<File> romoveFolderFiles = new LinkedList<File>();
            updateInfo.updateTotal(fileList.size());
            Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--777--" + mMode);
            byte[] buffer = new byte[BUFFER_SIZE];
            sourcePath = SafeManager.queryShiftOutSourcePath(mContext, mSrcList);
            mCopyMediaStoreHelper.setDstFolder(mDstFolder);
            if (mMode == FileInfoManager.SHIFT_OUT_SOURCE_MODE || mMode == SafeManager.DESTORY_SHIFT_OUT_MODE) {


                Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--888--" + mSrcList.size());
                for (int i = 0; i < mSrcList.size(); i++) {
                    if (isCancelled()) {
                        return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                    }
                    FileInfo info = mSrcList.get(i);
                    Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--999--" + sourcePath.get(i));
                    File souceFile = new File(sourcePath.get(i));
                    ret = deCryptFile(info, cipher, mDstFolder + souceFile.getName());
                    publishProgress(new ProgressInfo(info, ++progress, total));
//                    updateInfo.updateProgress(1);
//                    if (updateInfo.needUpdate()) {
//                        publishProgress(new ProgressInfo(info.getFileName(),
//                                (int) updateInfo.getProgress(), updateInfo.getTotal()));
//                    }
                }


            } else if (mMode == FileInfoManager.SHIFT_OUT_TARGET_MODE) {
                for (int i = 0; i < mSrcList.size(); i++) {
                    if (isCancelled()) {
                        return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                    }
                    FileInfo info = mSrcList.get(i);
                    String mSourcePath = mDstFolder + File.separator + new File(sourcePath.get(i)).getName();
                    Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--999--" + sourcePath.get(i));
                    ret = deCryptFile(info, cipher, mSourcePath);
                    publishProgress(new ProgressInfo(info, ++progress, total));
//                    updateInfo.updateProgress(1);
//                    if (updateInfo.needUpdate()) {
//                        publishProgress(new ProgressInfo(info.getFileName(),
//                                (int) updateInfo.getProgress(), updateInfo.getTotal()));
//                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            ret = FileManagerService.OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION;
        } finally {
            mCopyMediaStoreHelper.updateRecords();
        }
        return ret;

    }


    private int deCryptFile(FileInfo fileInfo, Cipher cipher, String targetPath) {
        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--11111--" + mMode);
        String sourcePath = fileInfo.getFileAbsolutePath();
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        File sourceFile = new File(sourcePath);
        Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--333--" + targetPath);
        Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--444--" + sourcePath);
        if (!sourceFile.exists()) {
            return FileManagerService.OperationEventListener.ERROR_CODE_UNKOWN;
        }

        try {
//            String targetFileFolderPath = "/sdcard/";
//            String targetFileName = "AAAAAAAAA.mp4";
            File targetFile = new File(targetPath);

            File targetFileFolder = new File(targetFile.getParent());
            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs();
            }

            if (targetFile.exists()) {
                targetFile.delete();
            }

            targetFile.createNewFile();

            Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--555--" + sourcePath);
            long readfilelen = 0;
            fileInputStream = new FileInputStream(sourceFile);
            readfilelen = fileInputStream.available();
            fileOutputStream = new FileOutputStream(targetFile);

            byte buf[] = new byte[PrivateFileOperationTask.BUFFER_SIZE];
            long start = System.currentTimeMillis();

            String mimeType = fileInfo.getMIMEType();
            boolean noNeedDecrpyt = mimeType.startsWith("application/zip")
                    || mimeType.startsWith("application/x-rar-compressed")
                    || mimeType.startsWith("application/x-tar")
                    || mimeType.startsWith("application/x-7z-compressed")
                    || mimeType.startsWith("application/vnd.android.package-archive");

            int readcount;
            if (!noNeedDecrpyt) {
                double count = 0;
                while ((readcount = fileInputStream.read(buf)) != -1) {
                    count += readcount;
                    byte[] decData = cipher.doFinal(buf);
                    fileOutputStream.write(decData);
                }

            } else {
                while ((readcount = fileInputStream.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, readcount);
                }
            }


            SafeManager.deleteSafeFileRecord(mContext, sourcePath);
            mCopyMediaStoreHelper.addRecord(targetPath);
            Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--666--" + sourcePath);
        } catch (IOException e) {
            e.printStackTrace();
            ret = FileManagerService.OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            ret = FileManagerService.OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION;
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                // if (srcFile == null || !srcFile.exists()) {
                // // return true;
                // } else {
                // if (srcFile.canWrite()) {
                // final File to = new File(srcFile.getAbsolutePath() +
                // System.currentTimeMillis());
                // if (srcFile.renameTo(to)) {
                // to.delete();
                // }
                // }
                // }
                File tempFile = new File(sourceFile.getAbsolutePath() + "_temp");


                if (deleteFile(sourceFile)) {
                    Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--777--" + sourcePath);
                    deleteMediaStoreHelper.addRecord(sourceFile.getAbsolutePath());
                }
                if (tempFile.exists() && deleteFile(tempFile)) {
                    deleteMediaStoreHelper.addRecord(tempFile.getAbsolutePath());
                }
                ;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return ret;
    }


}
