/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.manager;

import android.content.ContentResolver;

import java.util.ArrayList;

import cn.tcl.filemanager.R;

public class CategoryManager {
    public static final int CATEGORY_RECENT = 0;
    public static final int CATEGORY_PICTURES = 1;
    public static final int CATEGORY_VEDIOS = 2;
    public static final int CATEGORY_MUSIC = 3;
    public static final int CATEGORY_APKS = 4;
    public static final int CATEGORY_DOWNLOAD = 5;
    public static final int CATEGORY_BLUETOOTH = 6;
    public static final int CATEGORY_DOCS = 7;
    //public static final int CATEGORY_PHOTOS = 5;
    public static final int CATEGORY_SAFE = 8;

    public static final int SAFE_CATEGORY_FILES = 0;
    public static final int SAFE_CATEGORY_MUISC = 1;
    public static final int SAFE_CATEGORY_PICTURES = 2;
    public static final int SAFE_CATEGORY_VEDIO = 3;

    // public static final int CATEGORY_ARCHIVES = 5;
    // public static final int CATEGORY_FAVORITE = 7;
    // public static final int CATEGORY_RINGTONES = 9;
    // public static final int CATEGORY_RECORDINGS = 10;
    // public static final int CATEGORY_SAFEBOX = 11;
    public static final long CATEGORY_MODE = 0x01;
    public static final long PATH_MODE = 0X02;
    public static boolean isSafeCategory = false;

    public static long mCurrentMode;
    public static int mCurrentCagegory = -1;
    public static int mLastCagegory = -1;

    public static final String OTG_ROOT_PATH = "storage/usbotg";

    public static int mCurrentSafeCategory = -1; // MODIFIED by songlin.qi, 2016-06-15,BUG-2227088

    private static final String[] mRecorder = new String[]{"/myrecorder",
            "/SoundRecorder/CallRecorder", "/SoundRecorder/Recorder",
            "/PhoneRecord", "/Recording"};// add for PR820197 by
    // yane.wang@jrdcom.com 20141105

    public CategoryManager(ContentResolver contentResolver) {
        mCurrentMode = CategoryManager.CATEGORY_MODE;
    }

    public static void setCurrentMode(long mode) {
        mCurrentMode = mode;
    }

//	public static int getCategoryCount(int tag) {
//		int count = 0;
//		switch (tag) {
//		case CATEGORY_RECENT:
//			count = CATEGORY_RECENT_COUNT;
//			break;
//		case CATEGORY_APKS:
//			count = CATEGORY_APKS_COUNT;
//			break;
//		case CATEGORY_BLUETOOTH:
//			count = CATEGORY_BLUETOOTH_COUNT;
//			break;
//		case CATEGORY_DOCS:
//			count = CATEGORY_DOCS_COUNT;
//			break;
//		case CATEGORY_DOWNLOAD:
//			count = CATEGORY_DOWNLOAD_COUNT;
//			break;
//		case CATEGORY_MUSIC:
//			count = CATEGORY_MUSIC_COUNT;
//			break;
//		case CATEGORY_PHOTOS:
//			count = CATEGORY_PHOTOS_COUNT;
//			break;
//		case CATEGORY_PICTURES:
//			count = CATEGORY_PICTURES_COUNT;
//			break;
//		case CATEGORY_VEDIOS:
//			count = CATEGORY_VEDIOS_COUNT;
//			break;
    // case CATEGORY_ARCHIVES:
    // count = CATEGORY_ARCHIVES_COUNT;
    // break;
    // case CATEGORY_RINGTONES:
    // count = CATEGORY_RINGTONES_COUNT;
    // break;
    // case CATEGORY_RECORDINGS:
    // count = CATEGORY_RECORDINGS_COUNT;
    // break;
    // case CATEGORY_FAVORITE:
    // count = CATEGORY_FAVORITE_COUNT;
    // break;
    // case CATEGORY_SAFEBOX:
    // count = CATEGORY_SAFEBOX_COUNT;
    // break;
//		default:
//			break;
//		}
//		return count;
//	}
//
//	public static void setCategoryCount(int tag, int count) {
//		switch (tag) {
//		case CATEGORY_RECENT:
//			CATEGORY_RECENT_COUNT = count;
//			break;
//		case CATEGORY_APKS:
//			CATEGORY_APKS_COUNT = count;
//			break;
//		case CATEGORY_BLUETOOTH:
//			CATEGORY_BLUETOOTH_COUNT = count;
//			break;
//		case CATEGORY_DOCS:
//			CATEGORY_DOCS_COUNT = count;
//			break;
//		case CATEGORY_DOWNLOAD:
//			CATEGORY_DOWNLOAD_COUNT = count;
//			break;
//		case CATEGORY_MUSIC:
//			CATEGORY_MUSIC_COUNT = count;
//			break;
//		case CATEGORY_PHOTOS:
//			CATEGORY_PHOTOS_COUNT = count;
//			break;
//		case CATEGORY_PICTURES:
//			CATEGORY_PICTURES_COUNT = count;
//			break;
//		case CATEGORY_VEDIOS:
//			CATEGORY_VEDIOS_COUNT = count;
//			break;
    // case CATEGORY_ARCHIVES:
    // CATEGORY_ARCHIVES_COUNT = count;
    // break;
    // case CATEGORY_RINGTONES:
    // CATEGORY_RINGTONES_COUNT = count;
    // break;
    // case CATEGORY_RECORDINGS:
    // CATEGORY_RECORDINGS_COUNT = count;
    // break;
    // case CATEGORY_FAVORITE:
    // CATEGORY_FAVORITE_COUNT = count;
    // break;
    // case CATEGORY_SAFEBOX:
    // CATEGORY_SAFEBOX_COUNT = count;
    // break;
//		default:
//			break;
//		}
//	}

//	public static String getCategoryPath(String rootPath, int mCategory) {
//		String path = null;
//		if (CategoryManager.CATEGORY_DOWNLOAD == mCategory) {
//			if (rootPath != null) {
//				path = rootPath + "/Download";
//			}
//		} else if (CategoryManager.CATEGORY_BLUETOOTH == mCategory) {
//			if (rootPath != null) {
//				path = rootPath + "/bluetooth";
//			}
//		}else if (CategoryManager.CATEGORY_PHOTOS==mCategory)
//		{
//			if (rootPath!=null)
//			{
//				path=rootPath+"/DCIM/Camera/";
//			}
//		}
//		// else if (CategoryManager.CATEGORY_RINGTONES == mCategory) {
//		// if (rootPath != null) {
//		// path = rootPath + "/Ringtones";
//		// }
//		// } else if (CategoryManager.CATEGORY_RECORDINGS == mCategory) {
//		// if (rootPath != null) {
//		// path = rootPath + "/myrecorder";
//		// }
//		// }
//		return path;
//	}

    public static String getCategoryPath(String rootPath, int category) {
        String path = null;
        if (CategoryManager.CATEGORY_DOWNLOAD == category) {
            if (rootPath != null) {
                path = rootPath + "/Download";
            }
        } else if (CategoryManager.CATEGORY_BLUETOOTH == category) {
            if (rootPath != null) {
                path = rootPath + "/bluetooth";
            }
        }
//		else if (CategoryManager.CATEGORY_PHOTOS == category) {
//            if (rootPath != null) {
//                path = rootPath + "/DCIM/Camera";
//            }
//        }
//        else if (CategoryManager.CATEGORY_RINGTONES == category) {
//            if (rootPath != null) {
//                path = rootPath + "/Ringtones";
//            }
//        } else if (CategoryManager.CATEGORY_RECORDINGS == category) {
//            if (rootPath != null) {
//                path = rootPath + "/myrecorder";
//            }
//        }
        return path;
    }

    public static String[] getRecorderPaths(String rootPath) {
        if (rootPath != null) {
            ArrayList<String> paths = new ArrayList<String>(mRecorder.length);
            int len = mRecorder.length;
            for (int i = 0; i < len; i++) {
                String path = rootPath + mRecorder[i];
                if (path != null) {
                    paths.add(path);
                }
            }
            return (String[]) paths.toArray(new String[paths.size()]);
        }
        return null;
    }

    public static String getSDRootPath() {
        if (MountManager.getInstance().isSDCardMounted()) {
            return MountManager.getInstance().getSDCardPath();
        }
        return null;
    }

    public static String getPhoneRootPath() {
        return MountManager.getInstance().getPhonePath();
    }

    public static int getCategoryString(int category) {
        int str;
        switch (category) {
            case CATEGORY_RECENT:
                str = R.string.main_recents_cn;
                break;
            case CATEGORY_APKS:
                str = R.string.main_installers;
                break;
            case CATEGORY_BLUETOOTH:
                str = R.string.category_bluetooth;
                break;
            case CATEGORY_DOCS:
                str = R.string.category_files;
                break;
            case CATEGORY_DOWNLOAD:
                str = R.string.category_download;
                break;
            case CATEGORY_MUSIC:
                str = R.string.category_music;
                break;
//		case CATEGORY_PHOTOS:
//			str = R.string.main_photo;
//			break;
            case CATEGORY_PICTURES:
                str = R.string.category_pictures;
                break;
            case CATEGORY_VEDIOS:
                str = R.string.category_vedios;
                break;
            case CATEGORY_SAFE:
                str = R.string.category_safe;
                break;
            // case CATEGORY_ARCHIVES:
            // str = R.string.category_archives;
            // break;

            // case CATEGORY_FAVORITE:
            // str = R.string.category_favorite;
            // break;

            // case CATEGORY_RINGTONES:
            // str = R.string.category_ringtones;
            // break;
            // case CATEGORY_RECORDINGS:
            // str = R.string.category_recordings;
            // break;
            // case CATEGORY_SAFEBOX:
            // str = R.string.category_safebox;
            // break;
            default:
                str = 0;
                break;

        }
        return str;
    }
}
