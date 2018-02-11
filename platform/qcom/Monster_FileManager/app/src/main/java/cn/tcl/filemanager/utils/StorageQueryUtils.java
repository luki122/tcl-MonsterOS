/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;

import java.io.File;

import cn.tcl.filemanager.manager.MountManager;

public class StorageQueryUtils {
    private MountManager mMountManager;
    private Context mContext;

    private long sdBlockSize = 0;
    private long sdAvailableBlock = 0;
    private long sdAvailableSize = 0;
    private long sdTotalSize = 0;
    private long sdBlockCount = 0;
    private long sdUsedSize = 0;
    private long otgBlockSize = 0;
    private long otgAvailableBlock = 0;
    private long otgAvailableSize = 0;
    private long otgTotalSize = 0;
    private long otgBlockCount = 0;
    private long otgUsedSize = 0;

    private String mPhonePath = MountManager.getInstance().getPhonePath();
    private String mSDCardPath = MountManager.getInstance().getSDCardPath();
    private String mUsbOtgPath = MountManager.getInstance().getUsbOtgPath();

    public StorageQueryUtils(Context context) {
        mContext = context;
    }

    /**
     * Query mobile system storage
     */
    public long getSystemSize() {
        long size = 0;
        String path = Environment.getRootDirectory().getPath();//path is /system
        File file = new File(path);
        size = getFolderSize(file);
        return size;
    }

    /**
     * Query mobile total storage
     */
    public Long getPhoneTolSize() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long blockSize = statFs.getBlockSize();
        long totalBlocks = statFs.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * Query mobile available storage
     */
    public Long getPhoneAvailableSize() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long blockSize = statFs.getBlockSize();
        long availableBlocks = statFs.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * Query mobile audio storage
     */
    public long getPhoneAudioSize() { // Including music and audio
        long size = 0L;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                if (mPhonePath != null && cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)).contains(mPhonePath)) {
                    size += cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                }
            }
        } catch (Exception e) {
            LogUtils.e(this.getClass().getName(), e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }

    /**
     * Query mobile picture storage
     */
    public long getPhonePictureSize() {
        long size = 0L;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                if (mPhonePath != null && cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)).contains(mPhonePath)) {
                    size += cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                }
            }
        } catch (Exception e) {
            LogUtils.e(this.getClass().getName(), e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }

    /**
     * Query mobile video storage
     */
    public long getPhoneVideoSize() {
        long size = 0L;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                if (mPhonePath != null && cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)).contains(mPhonePath)) {
                    size += cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                }
            }
        } catch (Exception e) {
            LogUtils.e(this.getClass().getName(), e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }

    /**
     * Query mobile apk storage
     */
    public long getPhoneApkSize() {
        long size = 0;
        String[] projection = new String[]{MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE};
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection,
                MediaStore.Files.FileColumns.DATA + " like ?",
                new String[]{"%.apk"},
                null);
        try {
            while (cursor != null && cursor.moveToNext()) {
                if (mPhonePath != null && cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)).contains(mPhonePath)) {
                    size += cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }

    /**
     * Query mobile other storage
     */
    public long getPhoneOtherSize() {
        long size = getPhoneTolSize() - getPhonePictureSize() - getPhoneAudioSize()
                - getPhoneVideoSize() - getPhoneApkSize() - getPhoneAvailableSize();
        return size;
    }

    /**
     * Query mobile phone used storage
     */
    public long getUsedSize() {
        long size = getPhoneAudioSize() + getPhonePictureSize() + getPhoneVideoSize() + getPhoneApkSize() + getPhoneOtherSize();
        return size;
    }

    /**
     * Query mobile sd total storage
     */
    public Long getSdTolSize() {
        countStorageSize();
        long size = sdTotalSize;
        return size;
    }

    /**
     * Query mobile sd available storage
     */
    public Long getSdAvailableSize() {
        countStorageSize();
        long size = sdAvailableSize;
        return size;
    }

    /**
     * Query mobile sd audio storage
     */
    public long getSdAudioSize() {
        long size = 0L;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                if (mSDCardPath != null && cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)).contains(mSDCardPath)) {
                    size += cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                }
            }
        } catch (Exception e) {
            LogUtils.e(this.getClass().getName(), e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }

    /**
     * Query mobile sd picture storage
     */
    public long getSdPictureSize() {
        long size = 0L;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                if (mSDCardPath != null && cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)).contains(mSDCardPath)) {
                    size += cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                }
            }
        } catch (Exception e) {
            LogUtils.e(this.getClass().getName(), e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }

    /**
     * Query mobile sd video storage
     */
    public long getSdVideoSize() {
        long size = 0L;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                if (mSDCardPath != null && cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)).contains(mSDCardPath)) {
                    size += cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                }
            }
        } catch (Exception e) {
            LogUtils.e(this.getClass().getName(), e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }

    /**
     * Query mobile sd apk storage
     */
    public long getSdApkSize() {
        long size = 0;
        String[] projection = new String[]{MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE};
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection,
                MediaStore.Files.FileColumns.DATA + " like ?",
                new String[]{"%.apk"},
                null);
        try {
            while (cursor != null && cursor.moveToNext()) {
                if (mSDCardPath != null && cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)).contains(mSDCardPath)) {
                    size += cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }

    /**
     * Query mobile sd other storage
     */
    public long getSdOtherSize() {
        long size = getSdUsedSize() - getSdPictureSize() - getSdAudioSize() - getSdVideoSize() - getSdApkSize();
        return size;
    }

    /**
     * Query mobile sd used storage
     */
    public long getSdUsedSize() {
        countStorageSize();
        long size = sdUsedSize;
        return size;
    }

    /**
     * Query mobile usbOtg total storage
     */
    public Long getOtgTolSize() {
        countStorageSize();
        long size = otgTotalSize;
        return size;
    }

    /**
     * Query mobile usbOtg available storage
     */
    public Long getOtgAvailableSize() {
        countStorageSize();
        long size = otgAvailableSize;
        return size;
    }

    /**
     * Query mobile usbOtg audio storage
     */
    public long getOtgAudioSize() {
        long size = 0L;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                if (mUsbOtgPath != null && cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)).contains(mUsbOtgPath)) {
                    size += cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                }
            }
        } catch (Exception e) {
            LogUtils.e(this.getClass().getName(), e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }

    /**
     * Query mobile usbOtg picture storage
     */
    public long getOtgPictureSize() {
        long size = 0L;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                if (mUsbOtgPath != null && cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)).contains(mUsbOtgPath)) {
                    size += cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                }
            }
        } catch (Exception e) {
            LogUtils.e(this.getClass().getName(), e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }

    /**
     * Query mobile usbOtg video storage
     */
    public long getOtgVideoSize() {
        long size = 0L;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                if (mUsbOtgPath != null && cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)).contains(mUsbOtgPath)) {
                    size += cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                }
            }
        } catch (Exception e) {
            LogUtils.e(this.getClass().getName(), e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }

    /**
     * Query mobile usbOtg apk storage
     */
    public long getOtgApkSize() {
        long size = 0;
        String[] projection = new String[]{MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE};
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection,
                MediaStore.Files.FileColumns.DATA + " like ?",
                new String[]{"%.apk"},
                null);
        try {
            while (cursor != null && cursor.moveToNext()) {
                if (mUsbOtgPath != null && cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)).contains(mUsbOtgPath)) {
                    size += cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }

    /**
     * Query mobile usbOtg other storage
     */
    public long getOtgOtherSize() {
        long size = getOtgUsedSize() - getOtgPictureSize() - getOtgAudioSize() - getOtgVideoSize() - getOtgApkSize();
        return size;
    }


    /**
     * Query mobile usbOtg used storage
     */
    public long getOtgUsedSize() {
        countStorageSize();
        long size = otgUsedSize;
        return size;
    }

    /**
     * Query mobile folder size
     */
    public static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            if (null == fileList) {
                return 0;
            }
            int length = fileList.length;
            for (int i = 0; i < length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * Query mobile sd storage or usbOtg storage
     */
    public void countStorageSize() {
        mMountManager = MountManager.getInstance();
        if (mMountManager.isSDCardMounted()) {
            try {
                String sdFilePath = mMountManager.getSDCardPath();
                StatFs statfs = new StatFs(sdFilePath);
                try {
                    sdBlockSize = statfs.getBlockSizeLong();
                    if (CommonUtils.hasM()) {
                        sdAvailableBlock = new File(sdFilePath).getFreeSpace();
                    } else {
                        sdAvailableBlock = statfs.getAvailableBlocksLong();
                    }
                    sdBlockCount = statfs.getBlockCountLong();
                } catch (NoSuchMethodError e) {
                    sdBlockSize = statfs.getBlockSizeLong();
                    if (CommonUtils.hasM()) {
                        sdAvailableBlock = new File(sdFilePath).getFreeSpace();
                    } else {
                        sdAvailableBlock = statfs.getAvailableBlocksLong();
                    }
                    sdBlockCount = statfs.getBlockCountLong();
                }
                if (!CommonUtils.hasM()) {
                    sdAvailableSize = sdAvailableBlock * sdBlockSize;
                } else {
                    sdAvailableSize = sdAvailableBlock;
                }
                sdTotalSize = sdBlockSize * sdBlockCount;
                if (CommonUtils.hasM()) {
                    sdUsedSize = sdTotalSize - sdAvailableBlock;
                } else {
                    sdUsedSize = sdTotalSize - sdAvailableSize;
                }
            } catch (Exception e) {
                sdTotalSize = 0;
                sdAvailableSize = 0;
                sdUsedSize = 0;
                e.printStackTrace();
            }
        }
        if (mMountManager.isOtgMounted()) {
            try {
                String otgFilePath = mMountManager.getUsbOtgPath();
                StatFs statfs = new StatFs(otgFilePath);
                try {
                    otgBlockSize = statfs.getBlockSizeLong();
                    if (CommonUtils.hasM()) {
                        otgAvailableBlock = new File(otgFilePath).getFreeSpace();
                    } else {
                        otgAvailableBlock = statfs.getAvailableBlocksLong();
                    }
                    otgBlockCount = statfs.getBlockCountLong();
                } catch (NoSuchMethodError e) {
                    otgBlockSize = statfs.getBlockSizeLong();
                    if (CommonUtils.hasM()) {
                        otgAvailableBlock = new File(otgFilePath).getFreeSpace();
                    } else {
                        otgAvailableBlock = statfs.getAvailableBlocksLong();
                    }
                    otgBlockCount = statfs.getBlockCountLong();
                }
                if (!CommonUtils.hasM()) {
                    otgAvailableSize = otgAvailableBlock * otgBlockSize;
                } else {
                    otgAvailableSize = otgAvailableBlock;
                }
                otgTotalSize = otgBlockSize * otgBlockCount;
                if (CommonUtils.hasM()) {
                    otgUsedSize = otgTotalSize - otgAvailableBlock;
                } else {
                    otgUsedSize = otgTotalSize - otgAvailableSize;
                }
            } catch (Exception e) {
                otgTotalSize = 0;
                otgAvailableSize = 0;
                otgUsedSize = 0;
                e.printStackTrace();
            }
        }
    }
}