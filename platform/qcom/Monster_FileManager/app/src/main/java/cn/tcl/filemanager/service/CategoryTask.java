/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.mtp.MtpConstants; // MODIFIED by songlin.qi, 2016-06-14,BUG-2128837
import android.net.Uri;
import android.provider.Downloads;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.text.TextUtils;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.FileInfoComparator;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.MediaFile;
import cn.tcl.filemanager.utils.MediaFile.MediaFileType;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;

public class CategoryTask extends BaseAsyncTask {

    private final ContentResolver mContentResolver;
    private final int mCategory;
    private final static String TAG = "cn.tcl.filemanager.service";

    /**
     * Constructor for SearchTask
     *
     * @param fileInfoManager a instance of FileInfoManager, which manages information of
     *                        files in FileManager.
     * @param operationEvent  a instance of OperationEventListener, which is a interface
     *                        doing things before/in/after the task.
     * @param searchName      the String, which need search
     * @param path            the limitation, which limit the search just in the file
     *                        represented by the path
     * @param contentResolver the contentResolver for query(search).
     */
    public CategoryTask(FileInfoManager fileInfoManager,
                        FileManagerService.OperationEventListener operationEvent, int category,
                        ContentResolver contentResolver, Context context) {
        super(context, fileInfoManager, operationEvent);
        mContentResolver = contentResolver;
        mCategory = category;
        mContext = context;
    }

    int dirprogress = 0;

    @Override
    protected Integer doInBackground(Void... params) {
        publishProgress(new ProgressInfo("", -1, -1));
        mStartOperationTime = System.currentTimeMillis();

        FileManagerApplication application = (FileManagerApplication) mContext
                .getApplicationContext();

        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        int progress = 0;
        if (CategoryManager.CATEGORY_DOWNLOAD == mCategory) {
            queryDownLoadInfo();
        } else {
            Uri uri = MediaStore.Files.getContentUri("external");

            String[] projection = {MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE};

            StringBuilder sb = new StringBuilder();
            sb.append(MediaStore.Files.FileColumns.TITLE + " not like ");
            DatabaseUtils.appendEscapedSQLString(sb, ".%");
            sb.append(" and ");
            /** Shield Hidden files */
            sb.append(MediaStore.Files.FileColumns.DATA + " not like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%/.%/%");
            sb.append(" and ");

            sb.append(MediaStore.Files.FileColumns.DATA + " not like ");
            DatabaseUtils.appendEscapedSQLString(sb, "null");

            String selection0 = sb.toString();
            if (CategoryManager.CATEGORY_DOCS == mCategory) {
                sb.append(" and ");
                sb.append(MediaStore.Files.FileColumns.FORMAT + "!=");
                DatabaseUtils.appendEscapedSQLString(sb, MtpConstants.FORMAT_ASSOCIATION + "");
                sb.append(" and (").append(
                        MediaStore.Files.FileColumns.MIME_TYPE + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "text/%");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.doc");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.xls");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.ppt");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.docx");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.xlsx");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.xlsm");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.pptx");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.pdf");
                sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.vcf");
                sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.vcs");
                sb.append(")");
            } else if (CategoryManager.CATEGORY_APKS == mCategory) {
                application.mSortType = FileInfoComparator.SORT_BY_NAME;

                sb.append(" and ");
                sb.append(MediaStore.Files.FileColumns.FORMAT + "!=");
                DatabaseUtils.appendEscapedSQLString(sb, MtpConstants.FORMAT_ASSOCIATION + "");
                sb.append(" and (").append(
                        MediaStore.Files.FileColumns.MIME_TYPE + " like ");
                DatabaseUtils.appendEscapedSQLString(sb,
                        "application/vnd.android.package-archive");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.apk");
                sb.append(")");
            } else if (CategoryManager.CATEGORY_RECENT == mCategory) {
                /* MODIFIED-BEGIN by songlin.qi, 2016-06-14,BUG-2128837*/
                //avoid to get folders like xx.mp4,xx.mp3, xx.apk
                application.mSortType = FileInfoComparator.SORT_BY_TIME;

                sb.append(" and ");
                sb.append(MediaStore.Files.FileColumns.MEDIA_TYPE + " not like ");
                DatabaseUtils.appendEscapedSQLString(sb, "null");

                FileUtils.getALLTypeSql(sb);

				/* MODIFIED-END by songlin.qi,BUG-2128837*/
            }

            String selection = sb.toString();
            Cursor cursor = null;
            if (CategoryManager.CATEGORY_PICTURES == mCategory) {
                sb.append(" and (" + MediaStore.Files.FileColumns.DATA + " not in (select " + MediaStore.Files.FileColumns.DATA + " from files where "
                        + MediaStore.Files.FileColumns.FORMAT + "==" + MtpConstants.FORMAT_ASSOCIATION + "))");
                sb.append(" and " + Images.Media.SIZE + " >= " + FileInfo.IMAGE_MIN_SIZE);
                sb.append(" and " + Images.Media.MIME_TYPE + " in('image/jpeg', 'image/png', 'image/gif', 'image/bmp','image/x-ms-bmp') ");
                sb.append(" and " + Images.Media.DATA + " not like ");
                DatabaseUtils.appendEscapedSQLString(sb, "/%/emulated/%/%/%/%/%");
                sb.append(" and " + Images.Media.DATA + " not like ");
                DatabaseUtils.appendEscapedSQLString(sb, "/%/sdcard1/%/%/%/%");
                sb.append(" and " + Images.Media.DATA + " not like ");
                DatabaseUtils.appendEscapedSQLString(sb, "/%/usbotg/%/%/%/%");
                selection0 = sb.toString();
                LogUtils.i(this.getClass().getName(), "selection0:" + selection0);
                cursor = mContentResolver.query(uri, projection, selection0,
                        null, Images.ImageColumns.BUCKET_DISPLAY_NAME + "," + Images.Media.DATE_MODIFIED + " DESC ");
            } else if (CategoryManager.CATEGORY_VEDIOS == mCategory) {
                application.mSortType = FileInfoComparator.SORT_BY_NAME;
                sb.append(" and (" + MediaStore.Files.FileColumns.DATA + " not in (select " + MediaStore.Files.FileColumns.DATA + " from files where "
                        + MediaStore.Files.FileColumns.FORMAT + "==" + MtpConstants.FORMAT_ASSOCIATION + "))");
                sb.append(" and " + MediaStore.Files.FileColumns.MIME_TYPE + " in('video/avi', 'video/mp2p', 'video/x-ms-wmv', 'video/mpeg', 'video/flv', 'video/x-matroska', 'video/3gpp', 'video/x-ms-asf', 'video/x-msvideo', 'video/mp4', 'video/vnd.rn-realvideo', 'video/rmvb')");
                sb.append(" or " + MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.rmvb");
                selection0 = sb.toString();
                LogUtils.i(this.getClass().getName(), "selection0:" + selection0);
                cursor = mContentResolver.query(uri, projection, selection0,
                        null, MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC ");
            } else if (CategoryManager.CATEGORY_MUSIC == mCategory
                    ) {
                application.mSortType = FileInfoComparator.SORT_BY_NAME;

                sb.append(" and (" + MediaStore.Files.FileColumns.DATA + " not in (select " + MediaStore.Files.FileColumns.DATA + " from files where "
                        + MediaStore.Files.FileColumns.FORMAT + "==" + MtpConstants.FORMAT_ASSOCIATION + "))");
                sb.append(" and " + MediaStore.Files.FileColumns.SIZE + " >= " + FileInfo.MUSIC_MIN_SIZE);
                sb.append(" and " + MediaStore.Files.FileColumns.MIME_TYPE + " in( 'audio/mpeg','audio/wav','audio/x-ms-wma','audio/x-wav', 'audio/mp4a-latm', 'audio/flac', 'application/vnd.americandynamics.acc', 'audio/x-ape')");
                selection0 = sb.toString();
                LogUtils.i(this.getClass().getName(), "selection0:" + selection0);
                cursor = mContentResolver.query(uri, projection, selection0,
                        null, MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC ");
            } else if (CategoryManager.CATEGORY_DOCS == mCategory
                    // ||CategoryManager.CATEGORY_ARCHIVES == mCategory
                    || CategoryManager.CATEGORY_APKS == mCategory
                    ) {
                cursor = mContentResolver.query(uri, projection, selection,
                        null, MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC ");
            } else if (CategoryManager.CATEGORY_RECENT == mCategory) {
                cursor = mContentResolver.query(uri, projection, selection,
                        null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC LIMIT 50");
            }

            boolean firstItem = true;
            if (CategoryManager.CATEGORY_SAFE == mCategory) {
                if (null == application.mCurrentPath) {
                    application.mCurrentPath = SafeUtils.getEncryptRootPath(mContext);
                }
                File file = new File(application.mCurrentPath);
                File[] files = file.listFiles();
                if (null != files) {
                    for (File subFile : files) {
                        FileInfo fileInfo = new FileInfo(mContext, subFile);
                        mFileInfoManager.addItem(fileInfo, firstItem);
                        firstItem = false;
                    }
                } else {
                    return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
                }
            } else {
                if (cursor == null) {
                    return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
                }
                int total = cursor.getCount();
                FileInfo mFileInfo = null;

                // Backup data
                if (null == application.mFileInfoList) {
                    application.mFileInfoList = new ArrayList<FileInfo>();
                } else {
                    application.mFileInfoList.clear();
                }
                try {
                    while (cursor.moveToNext()) {
                        progress++;
                        if (needUpdate() || progress == total) {
                            publishProgress(new ProgressInfo("", progress, total));
                        }
                        if (mCancelled) {
                            cancel(true);
                            onCancelled();
                            return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                        }
                        if (isCancelled()) {
                            ret = FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
                            break;
                        }
                        String name = (String) cursor.getString(cursor
                                .getColumnIndex(MediaStore.Files.FileColumns.DATA));
                        String mMimeType = (String) cursor
                                .getString(cursor
                                        .getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE));
                        FileInfo fileInfo = new FileInfo(mContext, name);
                        if (mMimeType == null) {
                            MediaFileType fileType = MediaFile
                                    .getFileType(fileInfo.getFileAbsolutePath());
                            if (fileType == null) {
                                mMimeType = null;
                            } else {
                                mMimeType = fileType.mimeType;
                            }
                        }
                        fileInfo.setFileMimeType(mMimeType);

                        if (CategoryManager.CATEGORY_PICTURES == mCategory) {

                            LogUtils.i(TAG, "fileInfo.Path:" + fileInfo.getFileParentPath());
                            LogUtils.i(TAG, "application.mCurrentPath:" + application.mCurrentPath);
                            if (null != application.mCurrentPath && application.mCurrentPath.equals(fileInfo.getFileParentPath())) {
                                mFileInfoManager.addItem(fileInfo, firstItem);
                            }
                            if (null != mFileInfo) {
                                if (mFileInfo.getFileAbsolutePath().equals(fileInfo.getFileParentPath())) {
                                    mFileInfo.getSubFileInfo().add(fileInfo);
                                    continue;
                                }
                            }
                            mFileInfo = new FileInfo(mContext, name.substring(0, name.lastIndexOf(File.separator)));
                            mFileInfo.getSubFileInfo().add(fileInfo);
                            if (firstItem && null != application.mFileInfoList) {
                                application.mFileInfoList.clear();
                            }

                            /** file sort by rules where picture category */
                            if (MountManager.getInstance().getCameraPath().equals(mFileInfo.getFileAbsolutePath())) {
                                application.mFileInfoList.add(0, mFileInfo);
                            } else if (MountManager.getInstance().getScreenShotPath().equals(mFileInfo.getFileAbsolutePath())) {
                                if (application.mFileInfoList.size() < 1) {
                                    application.mFileInfoList.add(mFileInfo);
                                } else {
                                    if (MountManager.getInstance().getCameraPath().equals(application.mFileInfoList.get(0).getFileAbsolutePath())) {
                                        application.mFileInfoList.add(1, mFileInfo);
                                    } else {
                                        application.mFileInfoList.add(0, mFileInfo);
                                    }
                                }
                            } else {
                                application.mFileInfoList.add(mFileInfo);
                            }
                        } else {
                            mFileInfoManager.addItem(fileInfo, firstItem);
                        }
                        firstItem = false;
                    }
                    ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
                if (mFileInfoManager.getAddFilesInfoList() == null || mFileInfoManager.getAddFilesInfoList().size() < 1) {
                    mFileInfoManager.addItemList(application.mFileInfoList);
                }
            }
        }

        if (ret != FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL) {
            application.mFileInfoManager
                    .updateCategoryList(application.mSortType);
        }

        return ret;
    }

    /**
     * query download file info
     */
    private void queryDownLoadInfo() {
        try {
            Cursor cursor = mContext.getContentResolver().query(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, null, null, null,
                    Downloads.Impl.COLUMN_LAST_MODIFICATION + " DESC ");
            if (null != cursor) {
                FileInfo fileInfo = null;
                while (cursor.moveToNext()) {
                    String title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                    String column_uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
                    String path = cursor.getString(cursor.getColumnIndex(Downloads.Impl._DATA));

                    long total_bytes = cursor.getLong(cursor.getColumnIndex(Downloads.Impl.COLUMN_TOTAL_BYTES));
                    long current_bytes = cursor.getLong(cursor.getColumnIndex(Downloads.Impl.COLUMN_CURRENT_BYTES));

                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (!TextUtils.isEmpty(path)) {
                        fileInfo = new FileInfo(mContext, path);
                        fileInfo.setFileStatus(status);
                        fileInfo.setTotalBytes(total_bytes);
                        fileInfo.setCurrentBytes(current_bytes);
                        mFileInfoManager.addItem(fileInfo);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private int categoryFromFolder(String path, int total, int progress) {
        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        File dir = new File(path);
        File[] files;
        if (dir.exists()) {
            files = dir.listFiles();
            if (files == null) {
                ret = FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
            } else {
                List<String> datas = new ArrayList<String>();
                int len = files.length;
                for (int i = 0; i < len; i++) {
                    //PR-984826 Nicky Ni -001 201512012 start
/* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
/*MODIFIED-BEGIN by jian.xu, 2016-04-18,BUG-1868328*/
//				    if(total > 500){
//				        publishProgress(new ProgressInfo("", dirprogress++, total));
//				    }
                    dirprogress++;
                    if (needUpdate() || dirprogress == total) {
                        publishProgress(new ProgressInfo("", dirprogress, total));
                    }
					/*MODIFIED-END by jian.xu,BUG-1868328*/
					/* MODIFIED-END by haifeng.tang,BUG-1987329*/
                    //PR-984826 Nicky Ni -001 201512012 end
                    if (isCancelled()) {
                        return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
                    }

                    boolean isShowHidden = SharedPreferenceUtils
                            .isShowHidden(mContext);

                    if (!files[i].isDirectory()
                            && (isShowHidden || !files[i].getName().startsWith(
                            "."))) {
                        datas.add(files[i].getAbsolutePath());
                    }
                }

                // if (mCategory == CategoryManager.CATEGORY_RINGTONES ||
                // mCategory == CategoryManager.CATEGORY_RECORDINGS) {
                // StringBuilder builder = new StringBuilder();
                // Iterator<String> iterator = datas.iterator();
                // if (iterator.hasNext()) {
                // builder.append(MediaStore.Audio.Media.DATA + "='" +
                // iterator.next() + "'");
                // while (iterator.hasNext()) {
                // builder.append(" OR ");
                // builder.append(MediaStore.Audio.Media.DATA + "='" +
                // iterator.next() + "'");
                // }
                //
                // Cursor cursor = mContext.getContentResolver().query(
                // MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
                // MediaStore.Audio.Media.DATA },
                // builder.toString(), null, null);
                // if (cursor != null) {
                // cursor.moveToFirst();
                // int dataIdx =
                // cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                // while (!cursor.isAfterLast()) {
                // mFileInfoManager.addItem(new FileInfo(mContext, new
                // File(cursor.getString(dataIdx))));
                // cursor.moveToNext();
                // }
                // cursor.close();
                // }
                // }
                // } else {
                Iterator<String> iterator = datas.iterator();
                while (iterator.hasNext()) {
                    mFileInfoManager.addItem(new FileInfo(mContext, new File(
                            iterator.next())));
                    // }
                }
            }
        } else {
            ret = FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
        }
        return ret;
    }

    private int categoryFromPhotoFolder(String path, int total, int progress) {
        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        File dir = new File(path);
        File[] files;
        if (dir.exists()) {
            files = dir.listFiles();
            if (files == null) {
                ret = FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
            } else {
                List<String> datas = new ArrayList<String>();
                int len = files.length;
                for (int i = 0; i < len; i++) {
                    //PR-984826 Nicky Ni -001 201512012 start
/* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
/*MODIFIED-BEGIN by jian.xu, 2016-04-18,BUG-1868328*/
//				    if(total > 500){
//				       publishProgress(new ProgressInfo("", dirprogress++, total));
//				    }
                    dirprogress++;
                    if (needUpdate() || dirprogress == total) {
                        publishProgress(new ProgressInfo("", dirprogress, total));
                    }
					/*MODIFIED-END by jian.xu,BUG-1868328*/
					/* MODIFIED-END by haifeng.tang,BUG-1987329*/
                    //PR-984826 Nicky Ni -001 201512012 end
                    if (isCancelled()) {
                        return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
                    }

                    boolean isShowHidden = SharedPreferenceUtils
                            .isShowHidden(mContext);

                    if (!files[i].isDirectory()
                            && (isShowHidden || !files[i].getName().startsWith(".")) && files[i].getName().endsWith(".jpg")) {
                        datas.add(files[i].getAbsolutePath());
                    }
                }
                Iterator<String> iterator = datas.iterator();

                while (iterator.hasNext()) {
                    mFileInfoManager.addItem(new FileInfo(mContext, new File(
                            iterator.next())));
                }
            }
        } else {
            ret = FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
        }
        return ret;
    }

    /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
	/*MODIFIED-BEGIN by jian.xu, 2016-04-18,BUG-1868328*/
    public static final int LIST_MODE_VIEW = 0;
    public static final int LIST_MODE_ONCHANGE = 2;
	/*MODIFIED-END by jian.xu,BUG-1868328*/
	/* MODIFIED-END by haifeng.tang,BUG-1987329*/
}
