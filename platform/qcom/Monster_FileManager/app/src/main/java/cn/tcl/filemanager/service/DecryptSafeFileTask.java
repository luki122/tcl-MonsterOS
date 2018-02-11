/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import android.content.Context;

import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.SafeManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.SafeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by user on 16-3-12.
 */
public class DecryptSafeFileTask extends FileOperationTask {
    private static final String TAG = DecryptSafeFileTask.class.getSimpleName();
    private FileInfo mFileInfo;
    /*MODIFIED-BEGIN by wenjing.ni, 2016-04-19,BUG-802835*/
    private  MultiMediaStoreHelper.DeleteMediaStoreHelper deleteMediaStoreHelper; // MODIFIED by haifeng.tang, 2016-04-23,BUG-1956936

    public DecryptSafeFileTask(FileInfoManager fileInfoManager,
                               FileManagerService.OperationEventListener operationEvent, Context context, FileInfo fileInfo) {
        super(fileInfoManager, operationEvent, context);
        mFileInfo = fileInfo;
        deleteMediaStoreHelper = new MultiMediaStoreHelper.DeleteMediaStoreHelper(mMediaProviderHelper);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int ret = FileManagerService.OperationEventListener.ERROR_CODE_DECRYPT_SUCCESS;
        File targetFile = null;
        if (mFileInfo == null) {
            ret = FileManagerService.OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS;
            /*MODIFIED-END by wenjing.ni,BUG-802835*/
            return FileManagerService.OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS;
        }
        String mCurrentSafePath = SafeUtils.getCurrentSafePath(mContext);
        String dstFolder = mCurrentSafePath + "temp";
        File file = new File(dstFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        List<FileInfo> mSrcList = new ArrayList<>();
        mSrcList.add(mFileInfo);
        int resultCode = canOperate(dstFolder, mSrcList);
        if (resultCode < 0) {
            return resultCode;
        }


        try {
            SecretKeySpec skeySpec = new SecretKeySpec(SafeManager.getKey(mContext), "AES");
            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            String targetFilepath = mCurrentSafePath + "temp" + File.separator + mFileInfo.getFileName();
            targetFile = new File(targetFilepath); //MODIFIED by wenjing.ni, 2016-04-19,BUG-802835
//
            if (targetFile.exists()) {
                targetFile.delete();
            }

            targetFile.createNewFile();

            long readfilelen = 0;
            FileInputStream fileInputStream = new FileInputStream(mFileInfo.getFileAbsolutePath());
            readfilelen = fileInputStream.available();
            LogUtils.i(TAG, "readfilelen->" + readfilelen);
            UpdateInfo updateInfo = new UpdateInfo();
            updateInfo.updateTotal(readfilelen);
            ProgressInfo prepareProgressInfo = new ProgressInfo(mFileInfo.getFileName(), (int) updateInfo.getProgress(), updateInfo
                    .getTotal());
            prepareProgressInfo.setUnitStyle(ProgressInfo.M_MODE);
            publishProgress(prepareProgressInfo);
            FileOutputStream fileOutputStream = new FileOutputStream(targetFile);

            byte buf[] = new byte[PrivateFileOperationTask.BUFFER_SIZE];
            int read;
            long readTotal = 0;
            while ((read = fileInputStream.read(buf)) != -1) {
                /*MODIFIED-BEGIN by wenjing.ni, 2016-04-19,BUG-802835*/
                if (isCancelled()) {
                    ret = FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                    return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                }
                /*MODIFIED-END by wenjing.ni,BUG-802835*/
                readTotal += read;
                LogUtils.i(TAG, "readTotal->" + readTotal + "  read->" + read + "       total---->>>>>>>>" + updateInfo.getTotal());
                byte[] decData = cipher.doFinal(buf);
                fileOutputStream.write(decData);
                updateInfo.updateProgress(read);
                ProgressInfo progressInfo = new ProgressInfo(mFileInfo.getFileName(), (int) updateInfo.getProgress(), updateInfo
                        .getTotal());
                progressInfo.setUnitStyle(ProgressInfo.M_MODE);
                publishProgress(progressInfo);
            }

            fileOutputStream.close();
            fileInputStream.close();
            mFileInfoManager.setSafeFileInfo(new FileInfo(mContext, targetFilepath));


        } catch (IOException e) {
            ret = FileManagerService.OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION; //MODIFIED by wenjing.ni, 2016-04-19,BUG-802835
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-19,BUG-802835*/
        } finally {
            if (ret < 0 && targetFile != null && targetFile.exists()) {
                deleteMediaStoreHelper.addRecord(targetFile.getAbsolutePath());
                targetFile.delete();
                deleteMediaStoreHelper.updateRecords();
            }
        }

        return ret;
        /*MODIFIED-END by wenjing.ni,BUG-802835*/

    }


}
