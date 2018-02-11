/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import android.content.Context;
import android.os.StatFs;

import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.utils.CommonUtils;

import java.io.File;

public class UpdatePercentageBarTask extends BaseAsyncTask {
    private long mAllSpace;
    private long mPhoneUsedSpace;
    private long mSDUsedSpace;

    public UpdatePercentageBarTask(Context context, FileInfoManager fileInfoManager, FileManagerService.OperationEventListener operationEvent) {
        super(context, fileInfoManager, operationEvent);
        mAllSpace = 0;
        mPhoneUsedSpace = 0;
        mSDUsedSpace = 0;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        MountManager mMountManager = MountManager.getInstance();
        long totalSpace = 0;
        long blocSize = 0;
        long availaBlock = 0;
        long blockCount = 0;
        long freeSpace = 0;
        try {
        	String filePath = mMountManager.getPhonePath();
            if (filePath!=null) {/*PR 1308449 zibin.wang add 2016.01.08*/
                StatFs statfs = new StatFs(filePath);
                try {
                    blocSize = statfs.getBlockSizeLong();
                    if(CommonUtils.hasM()) {
                        availaBlock = new File(filePath).getFreeSpace();
                    } else {
                        availaBlock = statfs.getAvailableBlocksLong();
                    }
                    blockCount = statfs.getBlockCountLong();
                } catch (NoSuchMethodError e) {
                    blocSize = statfs.getBlockSizeLong();
                    if(CommonUtils.hasM()) {
                        availaBlock = new File(filePath).getFreeSpace();
                    } else {
                        availaBlock = statfs.getAvailableBlocksLong();
                    }
                    blockCount = statfs.getBlockCountLong();
                }
                if(!CommonUtils.hasM()) {
                    freeSpace = availaBlock * blocSize;
                }
                totalSpace = blocSize * blockCount;
                if(CommonUtils.hasM()) {
                    mPhoneUsedSpace = totalSpace - availaBlock;
                } else {
                    mPhoneUsedSpace = totalSpace - freeSpace;
                }
                publishProgress(new ProgressInfo("phoneUsedSpace", 0, mPhoneUsedSpace));
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        mAllSpace += totalSpace;

        if (mMountManager.isSDCardMounted()) {
            try {
                long sdfreeSpace =0; //added by bin.song@tcl.com for sdcard size display 0B
            	long sdBlockCount = 0;
            	String sdPath = mMountManager.getSDCardPath();
				do {
					StatFs statfs = new StatFs(sdPath);// (mFile.getAbsolutePath());
					try {
						blocSize = statfs.getBlockSizeLong();
                        if(CommonUtils.hasM()) {
                            availaBlock = new File(sdPath).getFreeSpace();
                        } else {
                            availaBlock = statfs.getAvailableBlocksLong();
                        }
						sdBlockCount = statfs.getBlockCountLong();
					} catch (NoSuchMethodError e) {
						blocSize = statfs.getBlockSizeLong();
                        if(CommonUtils.hasM()) {
                            availaBlock = new File(sdPath).getFreeSpace();
                        } else {
                            availaBlock = statfs.getAvailableBlocksLong();
                        }
						sdBlockCount = statfs.getBlockCountLong();
					}
                    if(!CommonUtils.hasM()) {
                        sdfreeSpace = availaBlock * blocSize;
                    }
					totalSpace = blocSize * sdBlockCount;// mFile.getTotalSpace();
					if ((totalSpace <= 0)) {
						Thread.sleep(50);
					}
					// added by bin.song@tcl.com for sdcard size display 0B
				} while (((totalSpace <= 0)) && MountManager.getInstance().isMountPoint(sdPath));
                if(CommonUtils.hasM()) {
                    mSDUsedSpace = totalSpace - availaBlock;
                } else {
                    mSDUsedSpace = totalSpace - sdfreeSpace;
                }
                //mSDUsedSpace = totalSpace - sdfreeSpace;
                publishProgress(new ProgressInfo("sdUsedSpace", 0, mSDUsedSpace));
            } catch (Exception e) {
            }
            mAllSpace += totalSpace;
        }
        publishProgress(new ProgressInfo("allSpace", 0, mAllSpace));

        return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
    }
}
