/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment; // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329
import android.text.TextUtils;

import cn.tcl.filemanager.drm.DrmManager;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;

public class ListFileTask extends BaseAsyncTask {

    private final String mPath;
    private final int mFilterType;
    private final boolean mOnlyShowDir;
    private String mFileCategory = "all";
    public static final int LEVEL_NONE = -2;
    public static final int LEVEL_FL = 1;
    public static final int LEVEL_SD = 2;
    public static final int LEVEL_ALL = 4;

    private int mDrmLevel = LEVEL_ALL;
    private int mListMode; //MODIFIED by jian.xu, 2016-04-18,BUG-1868328 // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329

    /* MODIFIED-BEGIN by songlin.qi, 2016-05-27,BUG-2202845*/
    // use to hide the hiden files when choose file from safe box
    private boolean mHideHiddenForSafeSelect = false;

    public ListFileTask(Context context, FileInfoManager fileInfoManager, FileManagerService.OperationEventListener operationEvent,
                        String path, int filterType, boolean onlyShowDir, String fileCategory, int drm_sd,int listMode) {
        this(context, fileInfoManager, operationEvent, path, filterType, onlyShowDir, fileCategory, drm_sd, listMode, false);
    }
    /* MODIFIED-END by songlin.qi,BUG-2202845*/

    /**
     * Constructor for ListFileTask, construct a ListFileTask with certain
     * parameters
     *
     * @param fileInfoManager a instance of FileInfoManager, which manages
     *                        information of files in FileManager.
     * @param operationEvent  a instance of OperationEventListener, which is a
     *                        interface doing things before/in/after the task.
     * @param path            ListView will list files included in this path.
     * @param filterType      to determine which files will be listed.
     */
    public ListFileTask(Context context, FileInfoManager fileInfoManager, FileManagerService.OperationEventListener operationEvent,
                        String path, int filterType, boolean onlyShowDir, String fileCategory, int drm_sd,int listMode, boolean hide) { // MODIFIED by songlin.qi, 2016-05-27,BUG-2202845
        super(context, fileInfoManager, operationEvent);
        mPath = path;
        mFilterType = filterType;
        mOnlyShowDir = onlyShowDir;
        if (fileCategory != null && !fileCategory.equals("*/*")) {
            mFileCategory = fileCategory;
        }
        mDrmLevel = drm_sd;
        /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
        mListMode = listMode;
        mHideHiddenForSafeSelect = hide; // MODIFIED by songlin.qi, 2016-05-27,BUG-2202845
    }

    @Override
    protected Integer doInBackground(Void... params) {
        publishProgress(new ProgressInfo("", -1, -1));
        mStartOperationTime = System.currentTimeMillis();
        /*MODIFIED-END by jian.xu,BUG-1868328*/
        /* MODIFIED-END by haifeng.tang,BUG-1987329*/
        File[] files = null;
        int progress = 0;
        int total = 0;
        if (TextUtils.isEmpty(mPath)) {
            return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
        }

//		mFileInfoManager.removeAllItem();
//        mFileInfoManager.clearHideItem();//add for PR910227 by yane.wang@jrdcom.com 20150123

        File dir = new File(mPath);
        /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
        /* MODIFIED-BEGIN by jian.xu, 2016-03-18, BUG-1697006 */
        final String originPath = dir.getAbsolutePath();
        String internalPath = null;
        if (dir.exists()) {
            File dir0 = Environment.maybeTranslateEmulatedPathToInternal(dir);
            internalPath = dir0.getAbsolutePath();
            files = dir0.listFiles();
            /* MODIFIED-END by jian.xu,BUG-1697006 */
            /* MODIFIED-BEGIN by zibin.wang, 2016-05-03,BUG-1992587*/
            /* When attempting to use the maybeTranslateEmulatedPathToInternal method to convert the path to failure,
            the original path conversion method is used.*/
            if (files == null || files.length == 0) {
                files = dir.listFiles();
            }
            /* MODIFIED-END by zibin.wang,BUG-1992587*/
            /* MODIFIED-END by haifeng.tang,BUG-1987329*/
//            if (files == null) {
//                return OperationEventListener.ERROR_CODE_UNSUCCESS;
//            }
//        } else {
//            return OperationEventListener.ERROR_CODE_UNSUCCESS;
        }

        List<FileInfo> addList = new ArrayList<FileInfo>();
        List<FileInfo> hiddenList = new ArrayList<FileInfo>();

        total = files == null ? 0 : files.length;
        for (int i = 0; i < total; i++) {
            //PR-984826 Nicky Ni -001 201512012 start
            /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
            progress++;
            if (needUpdate() || progress == total) {
                publishProgress(new ProgressInfo("", progress, total));
			                /*MODIFIED-END by jian.xu,BUG-1868328*/
            }
        //PR-984826 Nicky Ni -001 201512012 end
        if (isCancelled()) {
            return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
        }
        String name = files[i].getName();
        boolean directory = files[i].isDirectory();
        /* MODIFIED-END by haifeng.tang,BUG-1987329*/
            String mimeType = FileUtils.getMimeTypeByExt(name);
            if (mOnlyShowDir) {
                if (mFilterType == FileManagerService.FILE_FILTER_TYPE_DEFAULT) {
                    if (name.startsWith(".")) {
                        continue;
                    }
                }
                if (directory) {
                    addList.add(createFileInfo(files[i], internalPath, originPath));
                }
            } else {
                if (!"all".equals(mFileCategory) && !directory) {
                    String extensionName = FileUtils.getFileExtension(name);
                    if (extensionName != null && !"".equals(extensionName)) {
                        if (mFileCategory.equals("audio/*") && (extensionName.equals("mp3") || (extensionName.equals("mp4") &&
                                new FileInfo(mContext, files[i]).getMimeType() != null &&
                                new FileInfo(mContext, files[i]).getMimeType().equals("audio/mp4")))) {
                            addList.add(createFileInfo(files[i], internalPath, originPath));
                            continue;
                        }
                    }
                    try {
                        String path = files[i].getAbsolutePath();
                        String drmMimetype = DrmManager.getInstance(mContext.getApplicationContext()).getOriginalMimeType(path);
                        int schema = DrmManager.getInstance(mContext.getApplicationContext()).getDrmScheme(path);
                        if (!TextUtils.isEmpty(drmMimetype)) {
                            if ((mDrmLevel == LEVEL_SD
                                    && schema == DrmManager.METHOD_SD
                                    || mDrmLevel == LEVEL_FL
                                    && schema == DrmManager.METHOD_FL || mDrmLevel == LEVEL_ALL)
                                    && (mFileCategory.startsWith("audio/")
                                    && drmMimetype.startsWith("audio/")
                                    || mFileCategory.startsWith("video/")
                                    && drmMimetype.startsWith("video/") || mFileCategory.startsWith("image/")
                                        && drmMimetype.startsWith("image/"))) {
                                addList.add(createFileInfo(files[i], internalPath, originPath));
                            }
                            continue;
                        }

                        if (mimeType != null && !mFileCategory.equals(mimeType)) {//PR-1496957 Nicky Ni -001 20160114
                            // PR-1398562,1489079 Nicky Ni -001 20160114 start
                            if (mFileCategory != null) {
                                if (mFileCategory.equals("audio/*") && (mimeType.equals("audio/amr-wb")
                                        || mimeType.equals("audio/x-ms-wma"))) {

                                } else {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                            // PR-1398562,1489079 Nicky Ni -001 20160114 end
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                boolean hide = name.startsWith(".");
                if (hide) {
                    hiddenList.add(createFileInfo(files[i], internalPath, originPath));
                }
                if (mFilterType == FileManagerService.FILE_FILTER_TYPE_DEFAULT) {
                    if (hide) {
                        continue;
                    }
                }

                if (mFilterType == FileManagerService.FILE_FILTER_TYPE_FOLDER) {
                    if (!directory) {
                        continue;
                    }
                }

                if (mFilterType == FileManagerService.FILE_FILTER_TYPE_FILES) {
                    // MODIFIED-BEGIN by songlin.qi, 2016-05-27, BUG-2202845
                    if (mHideHiddenForSafeSelect && hide) {
                        continue;
                    }
                    if (directory) {
                        addList.add(createFileInfo(files[i], internalPath, originPath));
                        // MODIFIED-END by songlin.qi, BUG-2202845
                    } else if (!TextUtils.isEmpty(mimeType)) {
                        if (!mimeType.startsWith("application/ogg")
                                && !mimeType.startsWith("audio/ogg")
                                && !mimeType.startsWith("audio/amr")
                                && !mimeType.startsWith("audio/")
                                && !mimeType.startsWith("image/")
                                && !mimeType.startsWith("video/")
                                && !mimeType.startsWith("application/sdp")) {
                            addList.add(createFileInfo(files[i], internalPath, originPath));
                        }
                    }else {
                        addList.add(createFileInfo(files[i], internalPath, originPath));
                    }
                    continue;
                }

                addList.add(createFileInfo(files[i], internalPath, originPath));
            }
        }

        mFileInfoManager.removeAllItem();
        mFileInfoManager.clearHideItem();

        for (FileInfo fi : addList) {
            mFileInfoManager.addItem(fi);
        }
        for (FileInfo fi : hiddenList) {
            mFileInfoManager.addHideItem(fi);
        }

        return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
    }

    // MODIFIED-BEGIN by haifeng.tang, 2016-05-05, BUG-1987329
    // MODIFIED-BEGIN by jian.xu, 2016-03-18, BUG-1697006
    private FileInfo createFileInfo(File file, String internalPath, String originPath) {
        FileInfo fileInfo = new FileInfo(mContext, file.isDirectory(), originPath, file.getAbsolutePath().replace(internalPath, originPath));
        fileInfo.updateSizeAndLastModifiedTime(file);// MODIFIED by jian.xu, 2016-03-22,BUG-1845873
        return fileInfo;
    }
    // MODIFIED-END by jian.xu,BUG-1697006}

    //MODIFIED-BEGIN by jian.xu, 2016-04-18, BUG-1868328
    //the params is used to check where the listTasks is from
    public static final int LIST_MODE_VIEW = 0;
    public static final int LIST_MODE_SHORCUT = 1;
    public static final int LIST_MODE_ONCHANGE = 2;
    public static final int LIST_MODE_FILE_SELECT = 3;
    public static final int LIST_MODE_PATH_SELECT = 4;
    //MODIFIED-END by jian.xu, BUG-1868328
    // MODIFIED-END by haifeng.tang, BUG-1987329
}
