/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.manager;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.LruCache;

import cn.tcl.filemanager.drm.DrmManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.R;

public final class IconManager {
    public static final String TAG = "IconManager";

    private static IconManager sInstance = new IconManager();
    public static int GRID_ITEM = 0;
    public static int LIST_ITEM = 1;

    private Resources mRes;
    protected Bitmap mIconsHead;

    private Map<String, Boolean> rightCaches;
    private LruCache<String, Drawable> mLrucache;
    private ExecutorService executorService = Executors.newFixedThreadPool(8);
    private final int maxMemory = (int) Runtime.getRuntime().maxMemory();

    private IconManager() {
        //iconCaches = new HashMap<String, Drawable>(1000);
        mLrucache = new LruCache<String, Drawable>(maxMemory / 8) {
            @Override
            protected int sizeOf(String key, Drawable bitmap) {
                int size = bitmap.getIntrinsicWidth() * bitmap.getIntrinsicHeight();
                return size;
            }
        };
        rightCaches = new HashMap<String, Boolean>(100);
        //pathCaches = new ArrayList<String>(100);
    }

    //add for PR959312 by long.tang@tcl.com on 2015.03.31 start
    public void clearAll() {
        rightCaches.clear();
        mLrucache.evictAll();
    }

//	public void clearCache() {
//		if (mLrucache != null) {
//			if (pathCaches != null) {
//				for (String path : pathCaches) {
//					Drawable mBitmap = mLrucache.get(path);
//					if (mBitmap != null) {
//						mBitmap.setCallback(null);
//						mBitmap = null;
//					}
//				}
//				if (mLrucache != null) {
//					mLrucache.evictAll();
//				}
//			}
//		}
//	}
    //add for PR959312 by long.tang@tcl.com on 2015.03.31 end

    // ADD START FOR PR58533 BY HONGBIN.CHEN 20150914
    public void removeCache(String path) {
        mLrucache.remove(path);
        rightCaches.remove(path);
    }
    // ADD END FOR PR58533 BY HONGBIN.CHEN 20150914

    /**
     * This method gets instance of IconManager
     *
     * @return instance of IconManager
     */
    public static IconManager getInstance() {
        return sInstance;
    }

    /**
     * This method gets the drawable id based on the mimetype
     *
     * @param mimeType the mimeType of a file/folder
     * @return the drawable icon id based on the mimetype
     */
    public static int getDrawableId(String mimeType, int mode) {
        if (TextUtils.isEmpty(mimeType)) {
            //return R.drawable.ic_list_unknow_file_white_24dp;
            return R.drawable.ic_unknownnew;
        } else if (mimeType.startsWith("application/vnd.android.package-archive")) {
            //return R.drawable.ic_list_android_white_24dp;
            return R.drawable.ic_apknew;
        } else if (mimeType.startsWith("application/zip") || mimeType.startsWith("application/x-rar-compressed")
                || mimeType.startsWith("application/x-tar") || mimeType.startsWith("application/x-7z-compressed")) {
            //return R.drawable.ic_list_work_white_24dp;
            return R.drawable.ic_zipnew;
        } else if (mimeType.startsWith("application/ogg") || mimeType.startsWith("audio/ogg")) {
            //return R.drawable.ic_list_volume_up_white_24dp;
            return R.drawable.ic_audionew;
        } else if (mimeType.startsWith("audio/amr")) {
            //return R.drawable.ic_list_mic_white_24dp;
            return R.drawable.ic_audionew;
        } else if (mimeType.startsWith("audio/")) {
            //return R.drawable.ic_list_audio_white_24dp;
            return R.drawable.ic_audionew;
        } else if (mimeType.startsWith("image/")) {
            //return R.drawable.ic_list_photo_white_24dp;
            return R.drawable.ic_imagenew;
        } else if (mimeType.startsWith("text/html") || mimeType.startsWith("text/htm") || mimeType.startsWith("application/vnd.wap.xhtml+xml")) {
            return R.drawable.ic_html_word_excel;
        } else if (mimeType.startsWith("text/")) {
            //return R.drawable.ic_list_description_white_24dp;
            return R.drawable.ic_html_word_excel;
        } else if (mimeType.startsWith("video/") || mimeType.startsWith("application/sdp")) {
            //return R.drawable.ic_list_videocam_white_24dp;
            return R.drawable.ic_videonew;
        } else if (mimeType.startsWith("application/vnd.ms-powerpoint") || mimeType.startsWith("application/mspowerpoint")
                || mimeType.startsWith("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {//add for PR854606 by yane.wang@jrdcom.com 20141128
            //return R.drawable.ic_list_powerpoint_white_24dp;
            return R.drawable.ic_html_word_excel;// PR-1105564 Nicky Ni -001 20151209
        } else if (mimeType.startsWith("application/vnd.ms-excel") || mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {//add for PR854606 by yane.wang@jrdcom.com 20141128
            //return R.drawable.ic_list_excel_white_24dp;
            return R.drawable.ic_html_word_excel;// PR-1105564 Nicky Ni -001 20151209
        } else if (mimeType.startsWith("application/msword") || mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            //return R.drawable.ic_list_word_white_24dp;
            return R.drawable.ic_html_word_excel;
        } else if (mimeType.startsWith("application/pdf")) {
            //return R.drawable.ic_list_pdf_white_24dp;
            return R.drawable.ic_html_word_excel;
        } else {
            //return R.drawable.ic_list_unknow_file_white_24dp;
            return R.drawable.ic_unknownnew;
        }


    }

    /**
     * This method gets icon from resources according to file's information.
     *
     * @param res      Resources to use
     * @param fileInfo information of file
     * @param service  FileManagerService, which will provide function to get
     *                 file's Mimetype
     * @return bitmap(icon), which responds the file
     */
    public int getIcon(FileInfo fileInfo, int mode) {
        int iconId = -1;

        if (fileInfo.isDirectory()) {
            iconId = getFolderIcon(fileInfo, mode);
        } else {
            //String mimeType = fileInfo.getMimeType();
            String mimeType = fileInfo.getMIMEType();
            iconId = getDrawableId(mimeType, mode);
        }
        return iconId;
    }

    private int getFolderIcon(FileInfo fileInfo, int mode) {
//        String path = fileInfo.getFileAbsolutePath();
//        if (MountManager.getInstance().isInternalMountPath(path)) {
//            return R.drawable.ic_menu_smartphone_blue_24dp;
//        } else if (MountManager.getInstance().isExternalMountPath(path)) {
//            return R.drawable.ic_menu_sd_card_blue_24dp;
//        }
        //if (mode == LIST_ITEM) {
        //return R.drawable.ic_list_folder_white_24dp;
//        return CommonUtils.getRandomFolderIcon();

        int param = 7;
        // int id = (int) (Math.random() * param + 1);
        int id = 1;
        switch (id) {
            case 1:
                return R.drawable.ic_folder_new;
            case 2:
                return R.drawable.ic_folder_new;
            case 3:
                return R.drawable.ic_folder_new;
            case 4:
                return R.drawable.ic_folder_new;
            case 5:
                return R.drawable.ic_folder_new;
            case 6:
                return R.drawable.ic_folder_new;
            case 7:
                return R.drawable.ic_folder_new;
            default:
                return R.drawable.ic_folder_new;
        }
    }

    /**
     * This method initializes variable mExt of IIconExtension type, and create
     * system folder.
     *
     * @param context Context to use
     * @param path    create system folder under this path
     */
    public void init(Context context, String path) {
        mRes = context.getResources();
    }

    /**
     * Get the external icon . icon.
     *
     * @param path  for cache key
     * @param resId resource ID for external icon
     * @return external icon for certain item
     */
    public Bitmap getExternalIcon(String path, int resId) {
        Bitmap icon = getDefaultIcon(resId);
        return icon;
    }

    /**
     * Get the default bitmap and cache it in memory.
     *
     * @param resId resource ID for default icon
     * @return default icon
     */
    public Bitmap getDefaultIcon(int resId) {
        return readBitmap(resId);
    }

    public Bitmap readBitmap(int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = mRes.openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    //modify for PR959312 by long.tang@tcl.com on 2015.03.31 start
    public Drawable getImageCacheDrawable(String path) {
        return mLrucache.get(path);
    }

    //mofify for PR959312 by long.tang@tcl.com on 2015.03.31 end
    public Drawable loadImage(final Context context, FileInfo fileInfo, final IconCallback callback) {
        final FileInfo mFileInfo = fileInfo;
        final String filePath = fileInfo.getFileAbsolutePath();
        executorService.execute(new Runnable() {
            public void run() {
                final DrmManager mDrmManager = DrmManager.getInstance(context.getApplicationContext());
                boolean isDrm = mFileInfo.isDrmFile();
                if ((mLrucache.get(filePath) != null)) {
//modify for PR959312 by long.tang@tcl.com on 2015.03.31 start
                    Drawable icon = mLrucache.get(filePath);
                    if (icon != null) {
//add for PR959312 by long.tang@tcl.com on 2015.03.31 start
                        if (isDrm && (rightCaches.get(filePath) != null)) {
                            boolean oldRight = rightCaches.get(filePath);
                            boolean newRight = mDrmManager.isRightsStatus(filePath);
                            if (oldRight == newRight) {
                                callback.iconLoaded(icon);
                                return;
                            }
                        } else {
                            callback.iconLoaded(icon);
                            return;
                        }
                    }
                }
                try {
                    Drawable icon = FileUtils.queryThumbnail(context, mFileInfo, mFileInfo.getMimeType(), isDrm);

                    if (icon != null) {
                        //add for PR959312 by long.tang@tcl.com on 2015.03.31 start
                        mLrucache.put(filePath, icon);
                        if (isDrm) {
                            rightCaches.put(filePath, mDrmManager.isRightsStatus(filePath));
                        }
                        callback.iconLoaded(icon);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return null;
    }

    //add for PR959312 by long.tang@tcl.com on 2015.03.31 end
    public interface IconCallback {
        public void iconLoaded(Drawable iconDrawable);
    }
}
