/* Copyright (C) 2016 Tcl Corporation Limited */
/* MODIFIED-BEGIN by songlin.qi, 2016-05-26, BUG-2202760*/
package cn.tcl.filemanager.manager;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;

//import FavoriteHelper;

public class CategoryCountManager {

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(3);
    private HashMap<String, Long> mSizeMap = new HashMap<String, Long>();//add for PR961285 by yane.wang@jrdcom.com 20150511
    private static CategoryCountManager sInstance = new CategoryCountManager();
    private static BlockingQueue<Runnable> sPoolWorkQueue = null;
// MODIFIED by jian.xu, 2016-03-22,BUG-1845873

    private CategoryCountManager() {
    }

    public static CategoryCountManager getInstance() {
        return sInstance;
    }

    /* MODIFIED-BEGIN by jian.xu, 2016-03-18, BUG-1697006 */
    //this is used a new thread pool to excute task, this can make the task run right now.
    private static final Executor EXECUTOR;
    static {
        int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        int CORE_POOL_SIZE = CPU_COUNT + 1;
        int MAXIMUM_POOL_SIZE = 128;
        ThreadFactory sThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "AsyncTask #" + this.mCount.getAndIncrement());
            }
        };
        sPoolWorkQueue = new LinkedBlockingQueue(128);
// MODIFIED by jian.xu, 2016-03-22,BUG-1845873
        EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 1L, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
    }

    public class CategoryCountTask extends AsyncTask<FileInfo, Integer, String> {
        private final Context mContext;
        private final int mPosition;
        CountTextCallback mCallback;
        public CategoryCountTask(final int postion, final Context context, final CategoryCountManager.CountTextCallback callback) {
            mContext = context;
            mPosition = postion;
            mCallback = callback;
        }

        @Override
        protected String doInBackground(FileInfo... fileInfos) {
            mCallback.countTextCallback(doInBackgroundTMP(mPosition, mContext));
            return "";
        }
    }

    /* MODIFIED-BEGIN by jian.xu, 2016-03-22,BUG-1845873 */
    public void clearTaskQueue() {
        sPoolWorkQueue.clear();
    }

/* MODIFIED-END by jian.xu,BUG-1845873 */
    public void loadCategoryCountText(final int postion, final Context context, final CountTextCallback callback) {
//		if (mExecutorService.isShutdown()) {
//			return;
//		}
//        mExecutorService.execute(new Runnable() {
//
//            @Override
//            public void run() {
//              callback.countTextCallback(doInBackground(postion,context));
//            }
//        });
        //this is used to confirm the task can run right now.

// MODIFIED by jian.xu, 2016-03-22,BUG-1845873
        CategoryCountTask task =  new CategoryCountTask(postion, context, callback);
        task.executeOnExecutor(EXECUTOR);
        /* MODIFIED-END by jian.xu,BUG-1697006 */
    }

//    public void  shutDown() {
//		if (mExecutorService != null && !mExecutorService.isShutdown()) {
//			mExecutorService.shutdownNow();
//		}
//    }

    public void putMap(String key, long value) {
        synchronized (mSizeMap) {
            mSizeMap.put(key, value);
        }
    }

    public void clearMap() {
        synchronized (mSizeMap) {
            mSizeMap.clear();
        }
    }

    public int doInBackgroundTMP(int category, Context context) { // MODIFIED by songlin.qi, 2016-05-26,BUG-2202760
        String sizeString = null;
        int count = 0;
        switch (category) {
            case CategoryManager.CATEGORY_RECENT:
            case CategoryManager.CATEGORY_PICTURES:
            case CategoryManager.CATEGORY_VEDIOS:
            case CategoryManager.CATEGORY_MUSIC:
            case CategoryManager.CATEGORY_DOCS:
            case CategoryManager.CATEGORY_APKS:
                count = getCountFromMedia(category, context);
                break;
            case CategoryManager.CATEGORY_DOWNLOAD:
            case CategoryManager.CATEGORY_BLUETOOTH:
                count = getCountFromFiles(context, category);
                break;
            case CategoryManager.CATEGORY_SAFE:
                File file = new File(SafeUtils.getEncryptRootPath(context));
                count = getEncryptFileCount(file);
                break;

            default:
                break;
        }

        return count;
    }

    private int getEncryptFileCount(File file) {
        int count = 0;
        if (null != file) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File tempFile : files) {
                    if (tempFile.isDirectory()) {
                        count += getEncryptFileCount(tempFile);
                    } else {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private int getCountFromMedia(int position, Context context) {
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = {
//                    MediaStore.Files.FileColumns.DATA,
                                          "count(*)"
        };
        int count = 0;

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
//            if (CategoryManager.CATEGORY_MUSIC == position) {
//                sb.append(" and ").append(MediaStore.Audio.AudioColumns.IS_MUSIC + " like ");
//                DatabaseUtils.appendEscapedSQLString(sb, "1");
//            }
        String selection0 = sb.toString();
        if (CategoryManager.CATEGORY_DOCS == position) {
            //avoid to get folders like xx.mp4,xx.mp3, xx.apk
            sb.append(" and ");
            sb.append(MediaStore.Files.FileColumns.FORMAT + "!=");
            DatabaseUtils.appendEscapedSQLString(sb, MtpConstants.FORMAT_ASSOCIATION + "");

            sb.append(" and (").append(MediaStore.Files.FileColumns.MIME_TYPE + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "text/%");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.doc");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xls");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.ppt");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.docx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsm");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pptx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pdf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcs");
            sb.append(")");
        } else if (CategoryManager.CATEGORY_APKS == position) {
                //avoid to get folders like xx.mp4,xx.mp3, xx.apk
                sb.append(" and ");
                sb.append(MediaStore.Files.FileColumns.FORMAT + "!=");
                DatabaseUtils.appendEscapedSQLString(sb, MtpConstants.FORMAT_ASSOCIATION + "");
            sb.append(" and (").append(MediaStore.Files.FileColumns.MIME_TYPE + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "application/vnd.android.package-archive");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.apk");
            sb.append(")");
        } else if (CategoryManager.CATEGORY_RECENT == position) {
                //avoid to get folders like xx.mp4,xx.mp3, xx.apk
                sb.append(" and ");
                sb.append(MediaStore.Files.FileColumns.FORMAT + "!=");
                DatabaseUtils.appendEscapedSQLString(sb, MtpConstants.FORMAT_ASSOCIATION + "");
            sb.append(" and (").append(MediaStore.Files.FileColumns.MIME_TYPE + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "text/%");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.doc");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xls");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.ppt");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.docx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsm");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pptx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pdf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.jpg");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.jpeg");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.png");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.bmp");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.mp3");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.wav");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.mp4");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.avi");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.mov");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.zip");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.rar");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.apk");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcs");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.m4a");
                sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.3gp");
            sb.append(") and ").append(MediaStore.Files.FileColumns.DATE_MODIFIED + " > " + CommonUtils.getYesterdayTime());
            sb.append(" and ").append(MediaStore.Files.FileColumns.DATE_MODIFIED + " < "+CommonUtils.getCurrentTime());
         // sb.append(")");
            // LogUtils.d("DAT","SQL data is "+sb.toString());
        }
//            else if (CategoryManager.CATEGORY_ARCHIVES == position) {
//                sb.append(" and (").append(MediaStore.Files.FileColumns.MIME_TYPE + " like ");
//                DatabaseUtils.appendEscapedSQLString(sb, "application/zip");
//                sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
//                DatabaseUtils.appendEscapedSQLString(sb, "%.rar");
//                sb.append(")");
//            }

        String selection = sb.toString();

        Cursor cursor = null;
        try {
            if (CategoryManager.CATEGORY_PICTURES == position) {
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
                cursor = context.getContentResolver().query(uri, projection, selection0, null, null);
            } else if (CategoryManager.CATEGORY_VEDIOS == position) {
                sb.append(" and (_data not in (select " + MediaStore.Files.FileColumns.DATA + " from files where "
                        + MediaStore.Files.FileColumns.FORMAT + "==" + MtpConstants.FORMAT_ASSOCIATION + "))");
                sb.append(" and " + MediaStore.Files.FileColumns.MIME_TYPE + " in('video/avi', 'video/mp2p', 'video/x-ms-wmv', 'video/mpeg', 'video/flv', 'video/x-matroska', 'video/3gpp', 'video/x-ms-asf', 'video/x-msvideo', 'video/mp4', 'video/vnd.rn-realvideo', 'video/rmvb')");
                sb.append(" or " + MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.rmvb");
                selection0 = sb.toString();
                cursor = context.getContentResolver().query(uri, projection, selection0, null, null);
            } else if (CategoryManager.CATEGORY_MUSIC == position) {
                //select * from video where _data not in (select _data from files where  format == 12289) ;
                //avoid to get folders like xx.mp4,xx.mp3, xx.apk
                sb.append(" and (_data not in (select " + MediaStore.Files.FileColumns.DATA + " from files where "
                        + MediaStore.Files.FileColumns.FORMAT + "==" + MtpConstants.FORMAT_ASSOCIATION + "))");
                sb.append(" and " + MediaStore.Files.FileColumns.SIZE + " >= " + FileInfo.MUSIC_MIN_SIZE);
                sb.append(" and " + MediaStore.Files.FileColumns.MIME_TYPE + " in( 'audio/mpeg','audio/wav','audio/x-ms-wma','audio/x-wav', 'audio/mp4a-latm', 'audio/flac', 'application/vnd.americandynamics.acc', 'audio/x-ape')");
                selection0 = sb.toString();
                cursor = context.getContentResolver().query(uri, projection, selection0, null, null);
            } else if (CategoryManager.CATEGORY_DOCS == position
                    || CategoryManager.CATEGORY_APKS == position
                    || CategoryManager.CATEGORY_RECENT == position
                    ) {
                cursor = context.getContentResolver().query(uri, projection, selection, null, null);
            }
            if(cursor != null){
//                    count = cursor.getCount();
                    if (cursor.moveToNext()){
//                        String name = (String) cursor.getString(cursor
//                                .getColumnIndex(MediaStore.Files.FileColumns.DATA));
//                        FileInfo mFileInfo = new FileInfo(context, name);
//                        if(!mFileInfo.getFile().exists() || mFileInfo.isDirectory()){
//                            count--;
//                        }
                        count =  cursor.getInt(0);
                        /* MODIFIED-END by jian.xu,BUG-1697006 */
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
//                	while(cursor.moveToNext()){
//                    	LogUtils.d("DAT","Modify data is "+cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)));
//                    }
                //count = cursor.getCount();
                cursor.close();
            }
        }

        return count;
    }

    public static int getCountFromPath(Context context, String path, int category) {
        File dir = new File(path);
        File[] files = null;
        int count = 0;

        if (dir.exists()) {
            files = dir.listFiles();
            if (files != null) {
                int len = files.length;

                boolean isShowHidden = SharedPreferenceUtils.isShowHidden(context);
                for (int i = 0; i < len; i++) {
                    if (!files[i].isDirectory() && (isShowHidden || !files[i].getName().startsWith("."))) {
                        count++;
                            /* MODIFIED-END by jian.xu,BUG-1697006 */
                    }
                }
            }
        }

        return count;
    }

    private int getRecorderCount(Context context, int position) {
        String[] rootPaths = new String[2];
        rootPaths[0] = CategoryManager.getPhoneRootPath();
        rootPaths[1] = CategoryManager.getSDRootPath();
        int count = 0;
        for (String rootPath : rootPaths) {
            if (!TextUtils.isEmpty(rootPath)) {
                String[] paths = CategoryManager.getRecorderPaths(rootPath);
                int len = paths.length;
                for (int i = 0; i < len; i++) {
                    count += getCountFromPath(context, paths[i], position);
                }
            }
        }
        return count;
    }

    public static int getCountFromFiles(Context context, int category) {
        String[] rootPaths = new String[2];
        rootPaths[0] = CategoryManager.getPhoneRootPath();
        rootPaths[1] = CategoryManager.getSDRootPath();
        int count = 0;
        for (String rootPath : rootPaths) {
            if (!TextUtils.isEmpty(rootPath)) {
                count += getCountFromPath(context, CategoryManager.getCategoryPath(rootPath, category), category);
            }
        }
        return count;
    }

    public interface CountTextCallback {
        public void countTextCallback(int countText);
    }
}
/* MODIFIED-END by songlin.qi,BUG-2202760*/
