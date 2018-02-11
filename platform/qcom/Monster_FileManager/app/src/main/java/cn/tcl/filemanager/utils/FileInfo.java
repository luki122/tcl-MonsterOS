/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.filemanager.drm.DrmManager;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.service.FileManagerService;

//import FavoriteManager;

public class FileInfo {

    public static final String MIMETYPE_EXTENSION_NULL = "unknown_ext_null_mimeType";
    public static final String MIMETYPE_EXTENSION_UNKONW = "unknown_ext_mimeType";
    public static final String MIMETYPE_EXTENSION_BAD = "bad mime type";
    public static final String MIMETYPE_3GPP_VIDEO = "video/3gpp";
    public static final String MIMETYPE_3GPP2_VIDEO = "video/3gpp2";
    public static final String MIMETYPE_3GPP_UNKONW = "unknown_3gpp_mimeType";
    public static final String MIMETYPE_APK = "application/vnd.android.package-archive";
    public static final String MIMETYPE_UNRECOGNIZED = "application/zip";

    public static final String MIME_HAED_IMAGE = "image/";
    public static final String MIME_HEAD_VIDEO = "video/";
    public static final String MIME_HEAD_AUDIO = "audio/";

    public static final String FILE_PROVIDER = "cn.tcl.filemanager.fileProvider";

    public static final String FILE_APK = "apk";

    public static final String FILE_URI_HEAD = "file://";

    public static final String FILE_OTG_HEAD = "file:///storage/usbotg";

    public static final String FILE_SD_HEAD = "file:///storage/sdcard1";

    /**
     * image min size is 20480 = 20KB;
     */
    public static final int IMAGE_MIN_SIZE = 20480;

    /**
     * image min size is 51200 = 50KB;
     */
    public static final int MUSIC_MIN_SIZE = 51200;

    /**
     * File name's max length for hide chars
     */
    public static int FILENAME_HIDE_MAX_LENGTH = 18;

    /**
     * File name's max length
     */
    //[BUGFIX]-Mod-BEGIN by TSNJ,qinglian.zhang,10/10/2014,PR-803563
    public static final int FILENAME_MAX_LENGTH = 255;
    //[BUGFIX]-Mod-END by TSNJ,qinglian.zhang,
    private File mFile;// MODIFIED by jian.xu, 2016-03-18, BUG-1697006
    private String mParentPath;
    private String mMimeType;
    private String mName;
    private String mFolderName;
    private final String mAbsolutePath;
    private String mFileSizeStr;
    private boolean mIsDir;// MODIFIED by jian.xu, 2016-03-22,BUG-1845873
    private long mLastModifiedTime = -1;
    private long mSize = -1;

    private String mEncryptFilePath;

    private int mFileStatus;

    private long mTotalSize;

    private long mBytesSoFar;

    //private boolean mIsFavorite;
    //add for PR845930 by yane.wang@jrdcom.com 20141127 begin
    public static boolean mountReceiver;
    public static boolean scanFinishReceiver;
    //add for PR845930 by yane.wang@jrdcom.com 20141127 end
//    private static FileManagerService mService;//add for PR928303 by yane.wang@jrdcom.com 20150210

    private HideFileFilter mHideFileFilter;

    private List<FileInfo> mSubFileInfo;

    private Context mContext;
    private final String[][] MIME_MapTable = {
            {".mp3", "audio/mp3"},
            {".wav", "audio/*"},
            {".ogg", "audio/ogg"},
            {".mid", "audio/*"},
            {".spm", "audio/*"},
            {".wma", "audio/x-ms-wma"},
            {".amr", "audio/*"},
            {".aac", "audio/*"},
            {".m4a", "audio/*"},
            {".midi", "audio/*"},
            {".awb", "audio/amr-wb"},
            {".mpga", "audio/mpeg"},
            {".xmf", "audio/xmf"},
            {".flac", "audio/*"},
            {".imy", "audio/*"},
            {".diff", "audio/*"},
            {".gsm", "audio/*"},
            {".avi", "video/*"},
            {".wmv", "video/*"},
            {".mov", "video/*"},
            {".rmvb", "video/*"},
            {".mp4", "video/*"},
            {".mpeg", "video/*"},
            {".3gp", "video/*"},
            {".3g2", "video/*"},
            {".flv", "video/*"},
            {".m4v", "video/*"},
            {".mkv", "video/*"},
            {".mpg", "video/*"},
            {".3gpp", "video/*"},
            {".sdp", "application/sdp"},
            {".jar", "application/java-archive"},
            {".jad", "application/java-archive"},
            {".zip", "application/zip"},
            {".rar", "application/x-rar-compressed"},
            {".tar", "application/x-tar"},
            {".7z", "application/x-7z-compressed"},
            {".gz", "application/x-gzip"},
            {".apk", "application/vnd.android.package-archive"},
            {".pdf", "application/pdf"},
            {".doc", "application/msword"},
            {".xls", "application/vnd.ms-excel"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".docx", "application/msword"},
            {".xlsx", "application/vnd.ms-excel"},
            {".pptx", "application/vnd.ms-powerpoint"},
            {".eml", "application/eml"},
            {".xlsm", "application/vnd.ms-excel"},
            {".rtf", "application/msword"},
            {".keynote", "application/vnd.ms-powerpoint"},
            {".numbers", "application/vnd.ms-powerpoint"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".xml", "text/html"},
            {".php", "application/vnd.wap.xhtml+xml"},
            {".url", "text/html"},
            {".png", "image/*"},
            {".jpg", "image/*"},
            {".gif", "image/*"},
            {".bmp", "image/*"},
            {".jpeg", "image/*"},
            {".dm", "image/*"},
            {".dcf", "image/*"},
            {".wbmp", "image/*"},
            {".webp", "image/*"},
            {".rc", "text/plain"},
            {".txt", "text/plain"},
            {".sh", "text/plain"},
            {".vcf", "text/x-vcard"},
            {".vcs", "text/x-vcalendar"},
            {".ics", "text/calendar"},
            {".ICZ", "text/calendar"},
            {".ape", "audio/x-ape"},
            {"", "*/*"}
    };


    /**
     * 49
     * only one of	50
     * {@link CategoryManager#SAFE_CATEGORY_FILES}	51
     * {@link CategoryManager#SAFE_CATEGORY_MUISC}	52
     * {@link CategoryManager#SAFE_CATEGORY_PICTURES}	53
     * {@link CategoryManager#SAFE_CATEGORY_VEDIO}	54
     */
    private int mFileType;

    /**
     * Constructor of FileInfo, which restore details of a file.
     *
     * @param file the file associate with the instance of FileInfo.
     * @throws IllegalArgumentException when the parameter file is null, will
     *                                  throw the Exception.
     */
    public FileInfo(Context context, File file) {
        mContext = context.getApplicationContext(); // MODIFIED by haifeng.tang, 2016-04-23,BUG-1956936
        mFile = file;
        mAbsolutePath = mFile.getAbsolutePath();
        mLastModifiedTime = mFile.lastModified();
        mIsDir = mFile.isDirectory();
        // mIsFavorite = isFavourite(mAbsolutePath);
        if (!mIsDir) {
            mSize = mFile.length();
        }
        mHideFileFilter = new HideFileFilter();
        mEncryptFilePath = SafeUtils.getEncryptRootPath(context);
    }

    /**
     * Constructor of FileInfo, which restore details of a file.
     *
     * @param absPath the absolute path of a file which associated with the
     *                instance of FileInfo.
     */
    public FileInfo(Context context, String absPath) {
//        if (absPath == null) {
//            throw new IllegalArgumentException();
//        }
        mContext = context;
        mAbsolutePath = absPath;
        mFile = new File(absPath);
        mLastModifiedTime = mFile.lastModified();
        mIsDir = mFile.isDirectory();
        //mIsFavorite = isFavourite(mAbsolutePath);
        if (!mIsDir) {
            mSize = mFile.length();
        } else {
            mSubFileInfo = new ArrayList<FileInfo>();
        }
        mHideFileFilter = new HideFileFilter();
        mEncryptFilePath = SafeUtils.getEncryptRootPath(context);
    }

    public FileInfo(Context context, File file, boolean isDir, String parentPath, String absPath, boolean isDrm) {
        mContext = context;
        mFile = file;
        mAbsolutePath = absPath;
        mIsDir = isDir;
        mParentPath = parentPath;
        if (!mIsDir) {
            mSize = mFile.length();
        }
        mHideFileFilter = new HideFileFilter();
        mEncryptFilePath = SafeUtils.getEncryptRootPath(context);
    }
    /* MODIFIED-BEGIN by jian.xu, 2016-03-18, BUG-1697006 */
    public FileInfo(Context context, boolean isDir, String parentPath, String absPath) {
        mContext = context;
        mAbsolutePath = absPath;
        mIsDir = isDir;
        mParentPath = parentPath;
        mHideFileFilter = new HideFileFilter();
        mEncryptFilePath = SafeUtils.getEncryptRootPath(context);
    }

    private void checkFile() {
        if(mFile == null) {
            mFile = new File(mAbsolutePath);
            mLastModifiedTime = mFile.lastModified();
            if (!mIsDir) {
                mSize = mFile.length();
            }
        }
    }

    /* MODIFIED-BEGIN by jian.xu, 2016-03-22,BUG-1845873 */
    public void updateSizeAndLastModifiedTime(File file) {
        if(file != null && file.exists()) {
            mIsDir = file.isDirectory();
            mLastModifiedTime = file.lastModified();
            if (!mIsDir) {
                mSize = file.length();
            }
        }
    }

    public void updateSizeAndLastModifiedTime(long size, long modifiedTime) {
        mSize = size;
        mLastModifiedTime = modifiedTime;
    }
    /* MODIFIED-END by jian.xu,BUG-1845873 */

//	public long getLastModifiedTime() {
//		if (mFile != null && mLastModifiedTime == -1) {
//			mLastModifiedTime = mFile.lastModified();
//		}
//		return mLastModifiedTime;
//	}

//    public boolean isFavourite(String path) {
//        List<String> array = FavoriteManager.getFavoriteArray();
//        return array.contains(path);
//    }

    /**
     * This method gets a file's parent path
     *
     * @return file's parent path.
     */
    public String getFileParentPath() {
        if (mParentPath == null) {
            mParentPath = FileUtils.getFilePath(mAbsolutePath);
        }
        return mParentPath;
    }

    /**
     * This method only use by PICTURE CATEGORY
     * @return
     */
    public List<FileInfo> getSubFileInfo() {
        return mSubFileInfo;
    }

    public void setSubFileInfo(List<FileInfo> mSubFileInfo) {
        this.mSubFileInfo = mSubFileInfo;
    }

    /**
     * This method gets a file's parent path's description, which will be shown
     * on the NavigationBar.
     *
     * @return the path's parent path's description path.
     */
//    public String getShowParentPath() {
//        return MountManager.getInstance().getDescriptionPath(getFileParentPath());
//    }

    /**
     * This method gets a file's description path, which will be shown on the
     * NavigationBar.
     *
     * @return the path's description path.
     */
    public String getShowPath() {
        return MountManager.getInstance().getDescriptionPath(getFileAbsolutePath());
    }

    /**
     * This method gets a file's real name.
     *
     * @return file's name on FileSystem.
     */
    public String getFileName() {
        if (mName == null) {
            mName = FileUtils.getFileName(mAbsolutePath);
        }
        return mName;
    }

    /**
     * This method gets a file's folder name.
     *
     * @return file's folder name on FileSystem.
     */
    public String getFolderName() {
        if (TextUtils.isEmpty(mFolderName)) {
            mFolderName = getShowPath();
            mFolderName = mFolderName.substring(mFolderName.lastIndexOf(File.separator) + 1, mFolderName.length());
        }
        return mFolderName;
    }

    /**
     * This method gets the file's description name.
     *
     * @return file's description name for show.
     */
    public String getShowName() {
        if (mName == null) {
            return FileUtils.getFileName(getShowPath());
        }
        return mName;
    }

    /**
     * This method gets the file's size(including its contains).
     *
     * @return file's size in long format.
     */
    public long getFileSize() {
        return mSize;
    }

    /**
     * This method gets transform the file's size from long to String.
     *
     * @return file's size in String format.
     */
    public String getFileSizeStr() {
        if (mFileSizeStr == null) {
            mFileSizeStr = FileUtils.sizeToString(mContext, mSize);
        }
        return mFileSizeStr;
    }

    /**
     * This method check the file is directory, or not.
     *
     * @return true for directory, false for not directory.
     */
    public boolean isDirectory() {
        return mIsDir;
    }

    // ADD START FOR PR1045781 BY HONGBIN.CHEN 20150721
    // Please use this method when get mimetype
    public String getMimeType() {
        if (TextUtils.isEmpty(mMimeType) && !isDirectory()) {
            if (isDrmFile()) {
                mMimeType = DrmManager.getInstance(mContext.getApplicationContext()).getOriginalMimeType(getFileAbsolutePath());
                if (!TextUtils.isEmpty(mMimeType)) {
                    return mMimeType;
                }
            }

            ContentResolver resolver = mContext.getContentResolver();
            final Uri uri = MediaStore.Files.getContentUri("external");
            final String[] projection = new String[]{MediaStore.Files.FileColumns.MIME_TYPE};
            final String selection = MediaStore.Files.FileColumns.DATA + "=?";
            final String[] selectionArgs = new String[]{getFileAbsolutePath()};
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mMimeType = cursor.getString(0);
                    if (!TextUtils.isEmpty(mMimeType)) {
                        return mMimeType;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            mMimeType = FileUtils.getMimeTypeByExt(mAbsolutePath);
            if (TextUtils.isEmpty(mMimeType)) {
                mMimeType = MIMETYPE_EXTENSION_UNKONW;
            }
        }
        return mMimeType;
    }
    // ADD END FOR PR1045781 BY HONGBIN.CHEN 20150721

    /**
     * This method check the file is directory, or not.
     */
    public String getMIMEType() { /*PR 1469018 zibin.wang add 2016.01.26*/
        if (!TextUtils.isEmpty(mMimeType)) {
            return mMimeType;
        }
        String type="*/*";
        String fName=getFileName();
        int dotIndex = fName.lastIndexOf(".");
        if(dotIndex < 0){
            if (isDrmFile()) {
                type = DrmManager.getInstance(mContext.getApplicationContext()).getOriginalMimeType(getFileAbsolutePath());
                if (!TextUtils.isEmpty(type)) {
                    return type;
                }
            }else{
                ContentResolver resolver = mContext.getContentResolver();
                final Uri uri = MediaStore.Files.getContentUri("external");
                final String[] projection = new String[] { MediaStore.Files.FileColumns.MIME_TYPE };
                final String selection = MediaStore.Files.FileColumns.DATA + "=?";
                final String[] selectionArgs = new String[] { getFileAbsolutePath() };
                Cursor cursor = null;
                try {
                    cursor = resolver.query(uri, projection, selection, selectionArgs, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        type = cursor.getString(0);
                        if (!TextUtils.isEmpty(type)) {
                            return type;
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            return type;
        }
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") return type;
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    public String getShareMimeType() {
        if (TextUtils.isEmpty(mMimeType) && !isDirectory()) {
            if (isDrmFile()) {
                mMimeType = DrmManager.getInstance(mContext.getApplicationContext()).getOriginalMimeType(getFileAbsolutePath());
                if (!TextUtils.isEmpty(mMimeType)) {
                    return mMimeType;
                }
            }

            ContentResolver resolver = mContext.getContentResolver();
            final Uri uri = MediaStore.Files.getContentUri("external");
            final String[] projection = new String[]{
                    MediaStore.Files.FileColumns.MIME_TYPE
            };
            final String selection = MediaStore.Files.FileColumns.DATA + "=?";
            final String[] selectionArgs = new String[]{
                    getFileAbsolutePath()
            };
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mMimeType = cursor.getString(0);
                    if (!TextUtils.isEmpty(mMimeType)) {
                        return mMimeType;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            mMimeType = FileUtils.getMimeTypeByExt(mAbsolutePath);
            if (TextUtils.isEmpty(mMimeType)) {
                mMimeType = MIMETYPE_EXTENSION_UNKONW;
            }

            // check for special mimetype
            if (mMimeType.equals("application/ogg") || mMimeType.equals("application/x-ogg")) {
                mMimeType = "audio/ogg";
            }
        } else if (!TextUtils.isEmpty(mMimeType) && !isDirectory()) { //PR-1650994 Nicky Ni -001 20160222
            if (mMimeType.equals("application/ogg") || mMimeType.equals("application/x-ogg")) {
                mMimeType = "audio/ogg";
            }
        }
        return mMimeType;
    }
    /**
     * This method get the file's MIME type.
     *
     * @param service the FileManager Service for update the 3gpp File
     * @return the file's MIME type.
     */
//    public String getFileMimeType(FileManagerService service) {
//        if (!isDirectory()) {
//            mMimeType = FileInfo.MIMETYPE_EXTENSION_UNKONW;
//            if (isDrmFile()) {
//                mMimeType = DrmManager.getInstance().getOriginalMimeType(this.getFileAbsolutePath());
//                if (!TextUtils.isEmpty(mMimeType)) {
//                    return mMimeType;
//                }
//            }
//            String mimeTypeByName = FileUtils.getMimeTypeByExt(mAbsolutePath);
//            if (TextUtils.isEmpty(mimeTypeByName)) {
//                // query the data from databases,if return
//                // FileInfo.MIMETYPE_EXTENSION_UNKONW
//                // get the mimetype by the file's extension
//                mMimeType = service.queryMimetype(this);
//                if (mMimeType == FileInfo.MIMETYPE_EXTENSION_UNKONW)
//                    mMimeType = getMimeType(mFile);
//            } else {
//                mMimeType = mimeTypeByName;
//            }
//        }
//        if (mMimeType == FileInfo.MIMETYPE_3GPP_UNKONW) {
//            mMimeType = service.update3gppMimetype(this);
//        }
//        return mMimeType;
//    }

    /**
     * This method gets the MIME type based on the extension of a file
     *
     * @param file the target file
     * @return the MIME type of the file
     */
//    public String getMimeType(File file) {
//		if (file.isDirectory()) {
//			return null;
//		}
//        //add for PR928303 by yane.wang@jrdcom.com 20150210 begin
//        FileInfo fileInfo = new FileInfo(mContext, file);
//		if (mService != null) {
//			mMimeType = mService.queryMimetype(fileInfo);
//		}
//        //add for PR928303 by yane.wang@jrdcom.com 20150210 end
//		if (!TextUtils.isEmpty(mMimeType) && !FileInfo.MIMETYPE_EXTENSION_UNKONW.equals(mMimeType) && !FileInfo.MIMETYPE_EXTENSION_BAD.equals(mMimeType)) {
//			return mMimeType;
//		}
//
//        MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(file.getAbsolutePath());
//        if (mediaFileType == null) {
//            return MIMETYPE_EXTENSION_UNKONW;
//        }
//
//        String mimeType = mediaFileType.mimeType;
//        if (mimeType == null) {
//            return MIMETYPE_EXTENSION_UNKONW;
//        }
//
//        // special solution for checking 3gpp original mimetype
//        // 3gpp extension could be video/3gpp or audio/3gpp
//        if (mimeType.equalsIgnoreCase(FileInfo.MIMETYPE_3GPP_VIDEO)
//                || mimeType.equalsIgnoreCase(FileInfo.MIMETYPE_3GPP2_VIDEO)) {
//            return MIMETYPE_3GPP_UNKONW;
//        }
//        mMimeType = mimeType;//add for PR982001 by yane.wang@jrdcom.com 20150424
//        return mMimeType;
//    }

    /**
     * The method check the file is DRM file, or not.
     *
     * @return true for DRM file, false for not DRM file.
     */

    public boolean isDrmFile() {
        if (mIsDir) {
            return false;
        }
        if (TextUtils.isEmpty(mAbsolutePath)) {
            return false;
        }
        // ADD START FOR PR1063468 BY HONGBIN.CHEN 20150810
        return DrmManager.getInstance(mContext.getApplicationContext()).isDrm(mAbsolutePath) || DrmManager.isDrmFileExt(mAbsolutePath);
        // ADD END FOR PR1063468 BY HONGBIN.CHEN 20150810
    }

    /**
     * This method sets the MIME type of a file.
     *
     * @param mimeType MIME type which will be set
     */
    public void setFileMimeType(String mimeType) {
        mMimeType = mimeType;
    }

    public void setFileName(String fileName) {
        mName = fileName;
    }

    public void setFolderName(String folderName){
        mFolderName = folderName;
    }
    //add by long.tang@tcl.com for better speed on 2015.03.24 start
    /**
     * This method gets the MIME type of a file
     * add by long.tang@tcl.com
     * @return
     */
//    public String getFileMimeType() {
//        return mMimeType;
//    }
    //add by long.tang@tcl.com for better speed on 2015.03.24 end

    /**
     * This method gets last modified time of the file.
     *
     * @return last modified time of the file.
     */
    public long getFileLastModifiedTime() {
        return mLastModifiedTime;
    }

    /**
     * This method update mLastModifiedTime(the file's last modified time).
     *
     * @return updated mLastModifiedTime(the file's last modified time).
     */
//    public long getNewModifiedTime() {
//        mLastModifiedTime = mFile.lastModified();
//        return mLastModifiedTime;
//    }

    /**
     * This method gets the file's absolute path.
     *
     * @return the file's absolute path.
     */
    public String getFileAbsolutePath() {
        return mAbsolutePath;
    }

    public String getEncryptFilePath() {
        return mEncryptFilePath;
    }

    public void setEncryptFilePath(String encryptFilePath) {
        this.mEncryptFilePath = encryptFilePath;
    }

    /**
     * This method gets the file packaged in FileInfo.
     *
     * @return the file packaged in FileInfo.
     */
    public File getFile() {
        checkFile();
        return mFile;
    }

    public int getFileStatus() {
        return mFileStatus;
    }

    /**
     * set file status
     * @param FileStatus DownloadManager.STATUS_XX
     */
    public void setFileStatus(int FileStatus) {
        this.mFileStatus = FileStatus;
    }

    public long getTotalBytes() {
        return mTotalSize;
    }

    public void setTotalBytes(long TotalSize) {
        this.mTotalSize = TotalSize;
    }

    public long getCurrentBytes() {
        return mBytesSoFar;
    }

    public void setCurrentBytes(long BytesSoFar) {
        this.mBytesSoFar = BytesSoFar;
    }

    /**
     * This method gets the file packaged in FileInfo.
     *
     * @return the file packaged in FileInfo.
     */
    public Uri getUri() {
        /* MODIFIED-BEGIN by jian.xu, 2016-03-18, BUG-1697006 */
        checkFile();
        return Uri.fromFile(mFile);
    }

    public Uri getContentUri(FileManagerService service) {
        checkFile();

        Uri uri = Uri.fromFile(mFile);
        if (!TextUtils.isEmpty(mFile.getAbsolutePath())) {
            uri = queryMedia(mFile.getAbsolutePath(), service);
        }

        if (uri == null || TextUtils.isEmpty(uri.toString())) {
            uri = Uri.fromFile(mFile);
        }
        LogUtils.i(this.getClass().getName(), "uri:" + uri);
        return uri;
    }

    private Uri queryMedia(String mediaPath, FileManagerService service) {
        ContentResolver mContentResolver = service.getApplicationContext().getContentResolver();

        String searchString = mediaPath;
        Cursor c = null;
        try {
            searchString = Uri.decode(searchString).trim().replace("'", "''");

            c = mContentResolver.query(MediaStore.Files.getContentUri("external"), new String[]{
                    "_id"
            }, "_data='" + searchString + "'", null, null);
            if (c != null && c.moveToNext()) {
                int id = c.getInt(0);
                return Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), String.valueOf(id));
            }
        } catch (Exception e) {
            LogUtils.e("queryMedia", e.toString());
        }finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return getFileAbsolutePath().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            return true;
        } else {
            if (o instanceof FileInfo) {
                if (((FileInfo) o).getFileAbsolutePath().equals(getFileAbsolutePath())) {
                    return true;
                }
            }
            return false;
        }
    }

    public int getFileType() {
        return mFileType;
    }

    public int getSubFileNum() {
        mFile = new File(this.getFileAbsolutePath());
        if (mFile != null && mFile.isDirectory()) {
            File[] files = mFile.listFiles(mHideFileFilter);
            if (files != null) {
                return files.length;
            }
        }
        return 0;
    }

    private class HideFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            if (pathname.getName().indexOf(".") != 0) {
                return true;
            }
            return false;
        }
    }

    public void setFileType(int fileType) {
        this.mFileType = fileType;
    }

//    public boolean isFavorite() {
//        return mIsFavorite;
//    }
//
//    public void setFavorite(boolean favorite) {
//        mIsFavorite = favorite;
//    }

    /**
     * This method checks that weather the file is hide file, or not.
     *
     * @return true for hide file, and false for not hide file
     */
    public boolean isHideFile() {
        return getFileName().startsWith(".");
    }

//    public int getChildrenCount() {
//        File dir = new File(mAbsolutePath);
//        int count = 0;
//        File[] files = dir.listFiles();
//        if (files != null) {
//            for (File file : files) {
//                if (!file.getName().startsWith(".")) {
//                    count++;
//                }
//            }
//        }
//        return count;
//    }

//	public static void setService(FileManagerService service) {
//		mService = service;
//	}
}
