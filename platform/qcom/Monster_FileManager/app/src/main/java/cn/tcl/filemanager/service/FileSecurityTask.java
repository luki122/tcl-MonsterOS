/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import android.content.Context;
import android.os.RemoteException;

import com.xdja.sks.IEncDecListener;

import java.io.File;
import java.util.List;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.adapter.FileInfoAdapter;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.utils.FileDecryptManagerHelper;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.SafeUtils;

public abstract class FileSecurityTask extends BaseAsyncTask {

    protected MediaStoreHelper mMediaProviderHelper;

    public static boolean stopEncrypt = false;
    public static boolean stopDecrypt = false;

    public FileSecurityTask(FileInfoManager fileInfoManager,
                            FileManagerService.OperationEventListener operationEvent, Context context) {
        super(context, fileInfoManager, operationEvent);
        if (context == null) {
            throw new IllegalArgumentException();
        } else {
            mMediaProviderHelper = new MediaStoreHelper(context);
        }
    }


    static class EncryptFilesTask extends FileSecurityTask {
        private FileInfoAdapter mFileInfoAdapter;
        private Context mContext;
        private IEncDecListener mEncryptListener;
        //modify by liaoah
        private String mTargetPath;
        //modify end

        private int encryptIndex = 0;


        public EncryptFilesTask(FileInfoManager fileInfoManager,
                                FileManagerService.OperationEventListener operationEvent, IEncDecListener encryptListener, Context context, FileInfoAdapter adapter, String Target) {
            super(fileInfoManager, operationEvent, context);
            mFileInfoAdapter = adapter;
            mContext = context;
            mEncryptListener = encryptListener;
            mTargetPath = Target;
            stopEncrypt = false;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            encryptFile(mFileInfoAdapter.getItemEditFileInfoList());
            try {
                mEncryptListener.onOperComplete(0);
                return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
        }

        /**
         * Recursive call when fileinfos array
         */
        private void encryptFile(List<FileInfo> encryptFileInfos) {
            if (null == encryptFileInfos) {
                return;
            }
            int size = encryptFileInfos.size();
            for (int i = 0; i < size; i++) {
                FileInfo fileInfo = encryptFileInfos.get(i);
                if (stopEncrypt) {
                    LogUtils.e(this.getClass().getName(), "encryptIndex:" + encryptIndex + ",size:" + size);
                    for (; encryptIndex < size; encryptIndex++) {
                        mFileInfoAdapter.removeCheck(encryptFileInfos.get(encryptIndex));
                    }
                    return;
                } else {
                    LogUtils.i(this.getClass().getName(), "stopEncrypt:" + stopEncrypt);
                    encryptIndex++;
                    boolean flag = fileInfo.getFileName().startsWith(".");
                    if (!flag) {
                        /** picture category */
                    //path error
                        if (mTargetPath == null || mTargetPath.length() == 0) mTargetPath = SafeUtils.getEncryptRootPath(mContext);
                        if (!mTargetPath.startsWith(SafeUtils.getEncryptRootPath(mContext))) return;

                        if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                            if (fileInfo.isDirectory()) {
                                encryptFilePath(fileInfo.getSubFileInfo());
                            } else {
                                if (SafeUtils.encryptFile(mContext, fileInfo.getFileAbsolutePath(), mTargetPath + File.separator + fileInfo.getFileName(), mEncryptListener) == true) {
                                    mFileInfoAdapter.addEncryptedFileList(fileInfo);
                                } else {
                                    mFileInfoAdapter.removeCheck(fileInfo);
                                }
                            }
                        } else {
                            /** other category */
                            if (fileInfo.isDirectory()) {
                                encryptFile(fileInfo.getFile().listFiles());
                            } else {
                                if (SafeUtils.encryptFile(mContext, fileInfo.getFileAbsolutePath(), mTargetPath + File.separator + fileInfo.getFileName(), mEncryptListener) == true) {
                                    mFileInfoAdapter.addEncryptedFileList(fileInfo);
                                } else {
                                    mFileInfoAdapter.removeCheck(fileInfo);
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Recursive call when fileinfos
         */
        private void encryptFilePath(List<FileInfo> encryptFileInfos) {
            if (null == encryptFileInfos) {
                return;
            }
            for (FileInfo fileInfo : encryptFileInfos) {
                LogUtils.i(this.getClass().getName(), "encryptFilePath stopEncrypt:" + stopEncrypt);
                if (stopEncrypt) {
                    return;
                }
                if (!fileInfo.getFileName().startsWith(".")) {
                    if (fileInfo.isDirectory()) {
                        encryptFilePath(fileInfo.getSubFileInfo());
                    } else {
                        SafeUtils.encryptFile(mContext, fileInfo.getFileAbsolutePath(), SafeUtils.createDirInRootPathByPath(fileInfo.getFileParentPath(), mContext) + File.separator + fileInfo.getFileName(), mEncryptListener);
                        mFileInfoAdapter.addEncryptedFileList(fileInfo);
                    }
                }
            }
        }

        /**
         * Recursive call when files
         */
        private void encryptFile(File[] files) {
            if (null == files) {
                return;
            }
            for (File file : files) {
                LogUtils.i(this.getClass().getName(), "encryptFile stopEncrypt:" + stopEncrypt);
                if (stopEncrypt) {
                    return;
                }
                if (!file.getName().startsWith(".")) {
                    if (file.isDirectory()) {
                        encryptFile(file.listFiles());
                    } else {
                        SafeUtils.encryptFile(mContext, file.getAbsolutePath(), SafeUtils.createDirInRootPathByPath(file.getParent(), mContext) + File.separator + file.getName(), mEncryptListener);
                        mFileInfoAdapter.addEncryptedFileList(new FileInfo(mContext, file));
                    }
                }
            }
        }
    }

    /**
     * decrypt files task
     */
    static class DecryptFilesTask extends FileSecurityTask {
        private FileInfo mDecryptFileInfo;
        private Context mContext;
        private IEncDecListener mEncryptListener;
        private FileInfoAdapter mFileInfoAdapter;
        private int decryptIndex;

        public DecryptFilesTask(FileInfoManager fileInfoManager,
                                FileManagerService.OperationEventListener operationEvent, IEncDecListener encryptListener, Context context, FileInfo fileInfo) {
            super(fileInfoManager, operationEvent, context);
            mDecryptFileInfo = fileInfo;
            mContext = context;
            mEncryptListener = encryptListener;
        }

        public DecryptFilesTask(FileInfoManager fileInfoManager,
                                FileManagerService.OperationEventListener operationEvent, IEncDecListener encryptListener, Context context, FileInfoAdapter adapter) {
            super(fileInfoManager, operationEvent, context);
            mFileInfoAdapter = adapter;
            mContext = context;
            mEncryptListener = encryptListener;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (null != mFileInfoAdapter){
                decryptFiles(mFileInfoAdapter.getNeedDecryptFileList());
            } else {
                SafeUtils.decryptFile(mContext, mDecryptFileInfo.getFileAbsolutePath(), SafeUtils.getDecryptRootPath(mContext) + File.separator + mDecryptFileInfo.getShowName(), mEncryptListener);
                FileDecryptManagerHelper fileDecryptManagerHelper = new FileDecryptManagerHelper(mContext);
                fileDecryptManagerHelper.insertDecryptValue(mDecryptFileInfo.getFileAbsolutePath(), SafeUtils.getDecryptRootPath(mContext) + File.separator + mDecryptFileInfo.getShowName());
            }
            return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        }

        public void decryptFiles(List<FileInfo> decryptFileInfos) {
            if (null == decryptFileInfos) {
                return;
            }
            int size = decryptFileInfos.size();
            for (int i = 0; i < size; i++) {
                FileInfo fileInfo = decryptFileInfos.get(i);
                LogUtils.i(this.getClass().getName(), "stopDecrypt:" + stopDecrypt);
                if (stopDecrypt) {
                    for (; decryptIndex < size; decryptIndex++) {
                        mFileInfoAdapter.removeNeedDecryptFileList(decryptFileInfos.get(decryptIndex));
                    }
                    return;
                } else {
                    boolean flag = fileInfo.getFileName().startsWith(".");
                    if (!flag) {
                        decryptIndex++;
                        /** other category */
                        if (fileInfo.isDirectory()) {
                            decryptFile(fileInfo.getFile().listFiles());
                        } else {
                            SafeUtils.decryptFile(mContext, fileInfo.getFileAbsolutePath(), ((FileManagerApplication) mContext.getApplicationContext()).mCurrentPath + File.separator + fileInfo.getFileName(), mEncryptListener);
                        }
                    }
                }
            }
        }

        /**
         * Recursive call when files
         */
        private void decryptFile(File[] files) {
            if (null == files) {
                return;
            }
            for (File file : files) {
                LogUtils.i(this.getClass().getName(), "decryptFile stopDecrypt:" + stopDecrypt);
                if (stopDecrypt) {
                    return;
                }
                if (!file.getName().startsWith(".")) {
                    if (file.isDirectory()) {
                        decryptFile(file.listFiles());
                    } else {
                        SafeUtils.decryptFile(mContext, file.getAbsolutePath(), SafeUtils.createDirForDecryptByPath(mContext, file.getParent()) + File.separator + file.getName(), mEncryptListener);
                    }
                }
            }
        }
    }
}
