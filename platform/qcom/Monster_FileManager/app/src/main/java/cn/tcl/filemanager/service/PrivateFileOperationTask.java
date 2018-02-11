/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.filemanager.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.manager.PrivateHelper;
import cn.tcl.filemanager.manager.SafeManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//import FavoriteManager;
//import FavoriteHelper;

public abstract class PrivateFileOperationTask extends FileOperationTask {

    public static final String SAFE_KEY = "0123456789ABCDEF0123456789ABCDEF";
    static String DESFILE_PATH = "/sdcard/safe1";
    public static final int BUFFER_SIZE = 256 * 1024;

    public static final long SAFE_SIZE_LIMITED = 2147483648l; //MODIFIED by wenjing.ni, 2016-04-16,BUG-1951763


    protected PrivateHelper mPrivateHelper;
    protected SQLiteDatabase db;
    protected MountManager mMountPointManager;
    protected String dstFolder;
    protected MediaStoreHelper mMediaProviderHelper;
    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1956936*/
    protected MultiMediaStoreHelper.DeleteMediaStoreHelper deleteMediaStoreHelper;
    private String currentSafeRoot = null;
    private String currentSafeName;
    protected  String mStorageRootPath;
    /* MODIFIED-END by haifeng.tang,BUG-1956936*/
    public static int mAddSuccessCount = 0;


    public PrivateFileOperationTask(FileInfoManager fileInfoManager,
                                    FileManagerService.OperationEventListener operationEvent, Context context) {
        super(fileInfoManager, operationEvent, context);
        if (context == null) {
            throw new IllegalArgumentException();
        } else {
            currentSafeRoot = SharedPreferenceUtils.getCurrentSafeRoot(context);
            currentSafeName = SharedPreferenceUtils.getCurrentSafeName(context);
            mPrivateHelper = new PrivateHelper(context, currentSafeRoot + File.separator + currentSafeName + File.separator);
            mMountPointManager = MountManager.getInstance();
            dstFolder = currentSafeRoot + File.separator + currentSafeName + File.separator + "file" + File.separator;
            mMediaProviderHelper = new MediaStoreHelper(context);
            mStorageRootPath = SafeUtils.getSafeRootName(mContext, mMountPointManager, currentSafeRoot);
            // desFile = new DesFileEncrypt();
        }
    }

    protected void addItem(HashMap<File, FileInfo> fileInfoMap, File file, File addFile) {
        if (fileInfoMap.containsKey(file)) {
            FileInfo fileInfo = new FileInfo(mContext, addFile);
            mFileInfoManager.addItem(fileInfo);
        }
    }



/* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1956936*/
}


 class AddPrivateFileTask extends PrivateFileOperationTask {

    private final List<FileInfo> mSrcList;

    public AddPrivateFileTask(FileInfoManager fileInfoManager,
                              FileManagerService.OperationEventListener operationEvent, Context context, List<FileInfo> src) {
        super(fileInfoManager, operationEvent, context);
        mSrcList = src;
        mAddSuccessCount = 0;
    }

     @Override // MODIFIED by wenjing.ni, 2016-05-13,BUG-2003636
    protected Integer doInBackground(Void... params) {
        if (mSrcList.isEmpty()) {
            return FileManagerService.OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS;
        }
        int resultCode = canOperate(dstFolder, mSrcList);
        if (resultCode < 0) {
            return resultCode;
        }
        return insertPrivate();
    }

    private Integer insertPrivate() {
        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        List<File> fileList = new ArrayList<File>();
        UpdateInfo updateInfo = new UpdateInfo();
        ret = getAllFileList(mSrcList, fileList, updateInfo);
        if (ret < 0) {
            return ret;
        }
        int total = mSrcList.size();
        int progress = 0;
        int fileType = 0;
        // if (total > 100) {
        publishProgress(new ProgressInfo("", 0, total));
        // }
        deleteMediaStoreHelper = new MultiMediaStoreHelper.DeleteMediaStoreHelper(mMediaProviderHelper);
        //Log.d("STORAGE","this is root path"+mStorageRootPath);
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(SafeManager.getKey(mContext), "AES");

            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");

            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            if (!isEnoughSpace(fileList, dstFolder)) {
                return FileManagerService.OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE;
            }
            for (FileInfo fileInfo : mSrcList) {
                if (isCancelled()) {
                    return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                }
                String mimeType = fileInfo.getMIMEType();
                boolean needFilter = mimeType.startsWith("application/zip") || mimeType.startsWith("application/x-rar-compressed")
                        || mimeType.startsWith("application/x-tar") || mimeType.startsWith("application/x-7z-compressed") || mimeType.startsWith("application/vnd.android.package-archive");
                String DstFileName;
                if (needFilter) {
                    DstFileName = fileInfo.getFileName();
                } else {
                    DstFileName = UUID.randomUUID().toString();
                }
                //if (total > 100) {
                publishProgress(new ProgressInfo(fileInfo, ++progress, total));
                //}

                db = mPrivateHelper.getWritableDatabase();

                ret = enCryptFile(fileInfo, dstFolder, DstFileName, cipher, needFilter,fileType);

            }
            Log.d("DDS", "this is enter privateFileOperationTask");
        } catch (Exception e) {
            e.printStackTrace();
            ret = FileManagerService.OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS;
        } finally {
            if (db != null) {
                db.close();
            }
            deleteMediaStoreHelper.updateRecords(); //MODIFIED by wenjing.ni, 2016-04-16,BUG-1951763
        }

        return ret;

    }


    /**
     * ae
     *
     * @param sourceFile
     * @param mDstFolder
     * @param fileName
     * @param cipher
     * @param needFilter filter can't file
     * @return
     */

    private int enCryptFile(FileInfo sourceFile, String mDstFolder, String fileName, Cipher cipher, boolean needFilter,int fileType) {

        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        File dstFile = new File(mDstFolder + fileName);
        File srcFile = sourceFile.getFile();
        byte[] buffer = new byte[FileOperationTask.BUFFER_SIZE];
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        File thumbnailFile = null;
        try {
            if (!srcFile.exists()) {
                    /*MODIFIED-BEGIN by wenjing.ni, 2016-04-16,BUG-1951763*/
                ret = FileManagerService.OperationEventListener.ERROR_CODE_UNKOWN;
                return FileManagerService.OperationEventListener.ERROR_CODE_UNKOWN;
            }
            if(sourceFile.isDrmFile()){
                Log.d("DES", "this is enter OperationEventListener.ERROR_SAFE_DRM_LIMTED");
                ret = FileManagerService.OperationEventListener.ERROR_SAFE_DRM_LIMTED;
                return FileManagerService.OperationEventListener.ERROR_SAFE_DRM_LIMTED;
            }
            if(srcFile.length() >= SAFE_SIZE_LIMITED){
                Log.d("DES", "this is enter OperationEventListener.BIG_SIZE_LIMITED"+srcFile.length());
                ret = FileManagerService.OperationEventListener.ERROR_SAFE_SIZE_LIMTED;
                    /*MODIFIED-END by wenjing.ni,BUG-1951763*/
                return FileManagerService.OperationEventListener.ERROR_SAFE_SIZE_LIMTED;
            }
            String path = sourceFile.getFileAbsolutePath();
            Log.d("DES", "this is enter insertPrivate" + path);
            if (sourceFile.getMimeType().startsWith("audio")) {
                fileType = 1;
            } else if (sourceFile.getMimeType().startsWith("image")) {
                fileType = 2;
                try {
                    Bitmap thumbnail = FileUtils.getImageThumbnail(mContext, sourceFile);
                    thumbnailFile = new File(dstFolder, fileName + "_temp");
                    if (thumbnailFile.exists()) {
                        thumbnailFile.delete();
                    }
                    thumbnailFile.createNewFile();
                    FileOutputStream thumbnailFileOutputStream = new FileOutputStream(thumbnailFile);
                    thumbnail.compress(Bitmap.CompressFormat.PNG, 100, thumbnailFileOutputStream);
                    thumbnailFileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else if (sourceFile.getMimeType().startsWith("video")) {
                fileType = 3;
                try {
                    Bitmap thumbnail = FileUtils.getVideoThumbnail(mContext, sourceFile);
                    thumbnailFile = new File(dstFolder, fileName + "_temp");
                    if (thumbnailFile.exists()) {
                        thumbnailFile.delete();
                    }
                    thumbnailFile.createNewFile();
                    FileOutputStream thumbnailFileOutputStream = new FileOutputStream(thumbnailFile);
                    thumbnail.compress(Bitmap.CompressFormat.PNG, 100, thumbnailFileOutputStream);
                    thumbnailFileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                fileType = 0;
            }

            fileInputStream = new FileInputStream(srcFile);
            long readfilelen = fileInputStream.available();
            File desFilePath = new File(DESFILE_PATH);
            if (!desFilePath.exists()) {
                desFilePath.mkdirs();
            }
            fileOutputStream = new FileOutputStream(mDstFolder + File.separator + fileName);
            byte buf[] = new byte[BUFFER_SIZE];
            long startTime = System.currentTimeMillis();


            if (needFilter) {
                int length =0;
                while ((length =fileInputStream.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, length);
                }
            } else {
                while (fileInputStream.read(buf) != -1) {
                    byte[] encData = cipher.doFinal(buf);
                    fileOutputStream.write(encData);
                }
            }
            ContentValues values = new ContentValues();
            values.put(PrivateHelper.FILE_FIELD_FT, fileType);
            values.put(PrivateHelper.FILE_FIELD_WT, 0);
            values.put(PrivateHelper.FILE_FIELD_SP, sourceFile.getFileAbsolutePath());
            values.put(PrivateHelper.FILE_FIELD_DP, dstFolder + fileName);
            values.put(PrivateHelper.FILE_FIELD_DN, sourceFile.getFileName());
            values.put(PrivateHelper.FILE_FIELD_SS, sourceFile.getFileSize());
            values.put(PrivateHelper.FILE_FIELD_DS, new File(dstFolder + sourceFile.getFileName()).length());
            values.put(PrivateHelper.FILE_FIELD_TP, dstFolder + fileName);
            values.put(PrivateHelper.FILE_FIELD_PT, 0);
            values.put(PrivateHelper.FILE_FIELD_FS, 0);
            values.put(PrivateHelper.FILE_FIELD_CT, 0);
            values.put(PrivateHelper.FILE_FIELD_LM, 0);
            values.put(PrivateHelper.FILE_FIELD_UT, "");
            values.put(PrivateHelper.FILE_FIELD_CU, "");
            values.put(PrivateHelper.FILE_FIELD_CD, mStorageRootPath);
            db.insert(PrivateHelper.FILE_TABLE_NAME, null, values);

            fileOutputStream.close();
            fileInputStream.close();


        } catch (IOException e) {
            e.printStackTrace();
            ret = FileManagerService.OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION;
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            ret = FileManagerService.OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION;
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (ret >=0 && deleteFile(srcFile)) {
                    mAddSuccessCount++;
                    deleteMediaStoreHelper.addRecord(srcFile.getAbsolutePath());
                } else {
                    ret = FileManagerService.OperationEventListener.ERROR_CODE_DELETE_FAILS;
                    if(dstFile != null && dstFile.exists()) {
                        dstFile.delete();
                    }
                    if(thumbnailFile != null && thumbnailFile.exists()){
                        thumbnailFile.delete();
                    }
                    SafeManager.deleteSafeFileRecord(mContext, mDstFolder + fileName); //MODIFIED by wenjing.ni, 2016-04-16,BUG-1951763
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ret;
        /* MODIFIED-END by haifeng.tang,BUG-1956936*/

    }

}
