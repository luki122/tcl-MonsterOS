/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.manager;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.utils.LogUtils;

public final class MountManager {
    private static final String TAG = "MountManager";

    public static final String SEPARATOR = File.separator;
    public static final String HOME = "Home";
    public static final String ROOT_PATH = "Root Path";

    private String mRootPath = "Root Path";
    private static MountManager sInstance = new MountManager();

    private StorageManager mStorageManager;
    private final ArrayList<MountPoint> mMountPathList = new ArrayList<MountPoint>(4);
    private Context mContext;

    public MountManager() {}

    /**
     * This method initializes MountPointManager.
     *
     * @param context Context to use
     */
    public void init(Context context) {
        mContext = context;
		if (mStorageManager == null) {
			mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
		}
        if (!TextUtils.isEmpty(getDefaultPath())) {
            mRootPath = FileUtils.getFilePath(getDefaultPath());
        }
        mMountPathList.clear();
        // check media availability to init mMountPathList
        StorageVolume[] storageVolumeList = mStorageManager.getVolumeList();
        if (storageVolumeList != null) {
            for (StorageVolume volume : storageVolumeList) {
                MountPoint mountPoint = new MountPoint();
                mountPoint.mPath = volume.getPath();
                mountPoint.mIsMounted = isMounted(volume.getPath());
                mountPoint.mIsExternal = volume.isRemovable();
                getMountPointDescription(mountPoint, context);
                //Only show the mounted volume
                if (mountPoint.mIsMounted) {
                    mMountPathList.add(mountPoint);
                }
            }
        }
        IconManager.getInstance().init(context, getDefaultPath() + SEPARATOR);
    }

    /**
     * This method gets instance of MountPointManager. Before calling this
     * method, must call init().
     *
     * @return instance of MountPointManager
     */
    public static MountManager getInstance() {
        return sInstance;
    }

    private static class MountPoint {
        String mDescription;
        String mPath;
        boolean mIsExternal;
        boolean mIsMounted;
    }

    /**
     * This method checks weather certain path is root path.
     *
     * @param path certain path to be checked
     * @return true for root path, and false for not root path
     */
    public boolean isRootPath(String path) {
        return mRootPath.equals(path);
    }

    /**
     * This method gets root path
     *
     * @return root path
     */
    public String getRootPath() {
        return mRootPath;
    }

    /**
     * This method gets informations of file of mount point path
     *
     * @return fileInfos of mount point path
     */
    public List<FileInfo> getMountPointFileInfo() {
        List<FileInfo> fileInfos = new ArrayList<FileInfo>(0);
        /* MODIFIED-BEGIN by zibin.wang, 2016-05-11,BUG-2125562*/
        FileInfo internal = null, sdcard =null,external = null;
        for (MountPoint mp : mMountPathList) {
            if (!mp.mIsExternal) {
                internal = new FileInfo(mContext, mp.mPath);
            } else if (mp.mIsExternal && mp.mIsMounted && !mp.mPath.equals("/storage/usbotg")) {
                sdcard = new FileInfo(mContext, mp.mPath);
            } else if (mp.mIsExternal && mp.mIsMounted && mp.mPath.equals("/storage/usbotg")) {
                external = new FileInfo(mContext, mp.mPath);
            }
        }
        if (internal != null) {
            fileInfos.add(internal);
        }
        if (sdcard != null) {
            fileInfos.add(sdcard);
        }
        /* MODIFIED-END by zibin.wang,BUG-2125562*/
        if (external != null) {
            fileInfos.add(external);
        }
        return fileInfos;
    }
    /**
     * This method gets the description of mountPoints.
     *
     * @param mp the mount point that should be checked
     */
    public void getMountPointDescription(MountPoint mp, Context context) {
        if (!mp.mIsExternal) {
            /* MODIFIED-BEGIN by zibin.wang, 2016-04-26,BUG-1996427*/
            //mp.mDescription = context.getResources().getString(R.string.draw_left_phone_storage_m);
            mp.mDescription = context.getResources().getString(R.string.draw_left_phone_storage); // MODIFIED by wenjing.ni, 2016-05-07,BUG-802835
            /* MODIFIED-END by zibin.wang,BUG-1996427*/
        } else {
			if (mp.mPath.equals("/storage/usbotg")) {
                mp.mDescription = context.getResources().getString(R.string.usbotg_m);
            }else{
                mp.mDescription = context.getResources().getString(R.string.draw_left_sd_card);
            }
        }
    }

    /**
     * This method gets count of mount, number of mount point(s)
     *
     * @return number of mount point(s)
     */
    public int getMountCount() {
        int count = 0;
        for (MountPoint mPoint : mMountPathList) {
            if (mPoint.mIsMounted) {
                count++;
            }
        }
        return count;
    }

    /**
     * This method gets default path from StorageManager
     *
     * @return default path from StorageManager
     */
    public String getDefaultPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * This method checks whether mountPoint is mounted or not
     *
     * @param mountPoint the mount point that should be checked
     * @return true if mountPhont is mounted, false otherwise
     */
    public boolean isMounted(String mountPoint) {
        if (TextUtils.isEmpty(mountPoint)) {
            return false;
        }
        String state = null;
        state = mStorageManager.getVolumeState(mountPoint);
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * This method checks whether SDCard is mounted or not
     *
     * @return true if SDCard is mounted, false otherwise
     */
    public boolean isSDCardMounted() {
        for (MountPoint mp : mMountPathList) {
            if (mp.mIsExternal && mp.mIsMounted && !mp.mPath.equals("/storage/usbotg")) {
                return true;
            }
        }
        return false;
    }

    public boolean isOtgMounted() {
        for (MountPoint mp : mMountPathList) {
            if (mp.mIsExternal && mp.mIsMounted && mp.mPath.equals("/storage/usbotg")) {
                return true;
            }
        }
        return false;
    }

  //add for PR825113 by yane.wang@jrdcom.com 20141103 begin
    /**
     * This method checks whether each of SDCard is mounted or not
     *
     * @return true if SDCard is mounted, false otherwise
     */
	public boolean isSignalSDCardMounted(int i) {
		MountPoint mp = mMountPathList.get(i);
		if (mp.mIsExternal && mp.mIsMounted) {
			return true;
		}
		return false;
	}
  //add for PR825113 by yane.wang@jrdcom.com 20141103 end

    /**
     * This method checks whether the current path is mount path.
     *
     * @param path
     * @return
     */
    public boolean isSdOrPhonePath(String path) {
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mPath.equals(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method gets real mount point path for certain path.
     *
     * @param path certain path to be checked
     * @return real mount point path for certain path, "" for path is not
     *         mounted
     */
    public String getRealMountPointPath(String path) {
        for (MountPoint mountPoint : mMountPathList) {
            if ((path + SEPARATOR).startsWith(mountPoint.mPath + SEPARATOR)) {
                return mountPoint.mPath;
            }
        }
        return "";
    }

    /**
     * This method changes mount state of mount point, if parameter path is
     * mount point.
     *
     * @param path certain path to be checked
     * @param isMounted flag to mark weather certain mount point is under
     *            mounted state
     * @return true for change success, and false for fail
     */
    public boolean changeMountState(String path, Boolean isMounted) {
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mPath.equals(path)) {
                if (mountPoint.mIsMounted == isMounted) {
                    return false;
                } else {
                    mountPoint.mIsMounted = isMounted;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method checks weather certain path is mount point.
     *
     * @param path certain path, which needs to be checked
     * @return true for mount point, and false for not mount piont
     */
    public boolean isMountPoint(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (path.equals(mountPoint.mPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks weather certain path is internal mount path.
     *
     * @param path path which needs to be checked
     * @return true for internal mount path, and false for not internal mount
     *         path
     */
    public boolean isInternalMountPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (!mountPoint.mIsExternal && mountPoint.mPath.equals(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks weather certain path is external mount path.
     *
     * @param path path which needs to be checked
     * @return true for external mount path, and false for not external mount
     *         path
     */
    public boolean isExternalMountPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mIsExternal && mountPoint.mPath.equals(path)) {
                return true;
            }
        }
        return false;
    }

    //add for restore the last state by yane.wang@jrdcom.com 20150512 begin
    public boolean isSDMountPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mIsExternal && mountPoint.mPath.equals(path)
                    && !mountPoint.mPath.equals("/storage/usbotg")) {
                return true;
            }
        }
        return false;
    }

    public boolean isUSBMountPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mIsExternal && mountPoint.mPath.equals(path)
                    && mountPoint.mPath.equals("/storage/usbotg")) {
                return true;
            }
        }
        return false;
    }
    //add for restore the last state by yane.wang@jrdcom.com 20150512 end

    /**
     * This method return the file of phone.
     *
     * @return
     */
    public File getPhoneFile() {
        for (MountPoint mountPoint : mMountPathList) {
            if (!mountPoint.mIsExternal) {
                return new File(mountPoint.mPath);
            }
        }
        return null;
    }

    /**
     * This method return the file of sd card.
     *
     * @return
     */
    public File getSDCardFile() {
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mIsExternal) {
                return new File(mountPoint.mPath);
            }
        }
        return null;
    }

    /**
     * This method return the path of phone.
     *
     * @return
     */
    public String getPhonePath() {
        for (MountPoint mountPoint : mMountPathList) {
            if (!mountPoint.mIsExternal) {
                return mountPoint.mPath;
            }
        }
        return null;
    }

    public String getCameraPath(){
        return getPhonePath() + File.separator + "DCIM" + File.separator + "Camera";
    }

    public String getScreenShotPath(){
        return getPhonePath() + File.separator + "Pictures" + File.separator + "Screenshots";
    }

    /**
     * This method return the path of sd card.
     *
     * @return
     */
    public String getSDCardPath() {
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mIsExternal && !mountPoint.mPath.equals("/storage/usbotg")) {//add for PR835938 by yane.wang@jrdcom.com 20141111
                return mountPoint.mPath;
            }
        }
        return null;
    }

  //add for PR835938 by yane.wang@jrdcom.com 20141111 begin
	public String getUsbOtgPath() {
		for (MountPoint mountPoint : mMountPathList) {
			if (mountPoint.mIsExternal && mountPoint.mPath.equals("/storage/usbotg")) {
				return mountPoint.mPath;
			}
		}
		return null;
	}

	public boolean isUsbOtg(int i) {
		MountPoint mountPoint = mMountPathList.get(i);
		if (mountPoint.mIsExternal && mountPoint.mPath.equals("/storage/usbotg")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isSdOrOtg() {
		if (getMountCount() == 1) {
			if (mMountPathList.get(0).mIsExternal && mMountPathList.get(0).mPath.equals("/storage/usbotg")) {
				return false;
			} else {
				return true;
			}
		}
		return true;
	}
  //add for PR835938 by yane.wang@jrdcom.com 20141111 end

    /**
     * This method checks weather certain file is External File.
     *
     * @param fileInfo certain file needs to be checked
     * @return true for external file, and false for not external file
     */
    public boolean isExternalFile(FileInfo fileInfo) {
        if (fileInfo != null) {
            String mountPath = getRealMountPointPath(fileInfo.getFileAbsolutePath());
            if (mountPath.equals(fileInfo.getFileAbsolutePath())) {
                return false;
            }
            if (isExternalMountPath(mountPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method gets description of certain path
     *
     * @param path certain path
     * @return description of the path
     */
    public String getDescriptionPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            for (MountPoint mountPoint : mMountPathList) {
                if ((path + SEPARATOR).startsWith(mountPoint.mPath + SEPARATOR)) {
                    return path.length() > mountPoint.mPath.length() + 1 ? mountPoint.mDescription
                            + SEPARATOR + path.substring(mountPoint.mPath.length() + 1)
                            : mountPoint.mDescription;
                }
            }
        }
        return path;
    }
}
