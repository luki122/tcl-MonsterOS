/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.drm.DrmManager;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.service.FileManagerService;

public final class FileUtils {

    private static final int UNIT_INTERVAL = 1024;
    private static final double ROUNDING_OFF = 0.005;
    private static final int DECIMAL_NUMBER = 100;

    private static final int BITMAP_SIZE = 500;

    private static final String TAG = "FileUtils";

    private static Map<String, String> mimeTypeMap;

    static {
        mimeTypeMap = new HashMap<String, String>();

        // for audio
        mimeTypeMap.put("mp3", "audio/mp3");
        mimeTypeMap.put("wav", "audio/*");
        mimeTypeMap.put("ogg", "audio/ogg");
        mimeTypeMap.put("mid", "audio/*");
        mimeTypeMap.put("spm", "audio/*");
        mimeTypeMap.put("wma", "audio/x-ms-wma");
        mimeTypeMap.put("amr", "audio/*");
        mimeTypeMap.put("aac", "audio/*");
        mimeTypeMap.put("m4a", "audio/*");
        mimeTypeMap.put("midi", "audio/*");
        mimeTypeMap.put("awb", "audio/amr-wb");
        mimeTypeMap.put("mpga", "audio/mpeg");
        mimeTypeMap.put("xmf", "audio/xmf");
        mimeTypeMap.put("flac", "audio/*");
        mimeTypeMap.put("imy", "audio/*");
        //add by yujie.zhao for PR1198966 at 20151222 begin
        mimeTypeMap.put("diff", "audio/*");
        mimeTypeMap.put("gsm", "audio/*");
        mimeTypeMap.put("ape", "audio/x-ape");
        //add by yujie.zhao for PR1198966 at 20151222 end

        // for video
        mimeTypeMap.put("avi", "video/*");
        mimeTypeMap.put("wmv", "video/*");
        mimeTypeMap.put("mov", "video/*");
        mimeTypeMap.put("rmvb", "video/*");
        mimeTypeMap.put("mp4", "video/*");
        mimeTypeMap.put("mpeg", "video/*");
        //[FEATURE]-Add-BEGIN by TSNJ,qinglian.zhang,09/15/2014,PR-787616,
        mimeTypeMap.put("3gp", "video/*");
        mimeTypeMap.put("3g2", "video/*");
        mimeTypeMap.put("flv", "video/*");
        mimeTypeMap.put("m4v", "video/*");
        mimeTypeMap.put("mkv", "video/*");
        mimeTypeMap.put("mpg", "video/*");
        //[FEATURE]-Add-END by TSNJ,qinglian.zhang
        mimeTypeMap.put("3gpp", "video/*");//add for PR842423 by yane.wang@jrdcom.com 20141117

        // for application
        mimeTypeMap.put("sdp", "application/sdp");
        mimeTypeMap.put("jar", "application/java-archive");
        mimeTypeMap.put("jad", "application/java-archive");
        mimeTypeMap.put("zip", "application/zip");
        mimeTypeMap.put("rar", "application/x-rar-compressed");
        mimeTypeMap.put("tar", "application/x-tar");
        mimeTypeMap.put("7z", "application/x-7z-compressed");
        mimeTypeMap.put("gz", "application/x-gzip");
        mimeTypeMap.put("apk", "application/vnd.android.package-archive");
        mimeTypeMap.put("pdf", "application/pdf");
        mimeTypeMap.put("doc", "application/msword");
        mimeTypeMap.put("xls", "application/vnd.ms-excel");
        mimeTypeMap.put("ppt", "application/vnd.ms-powerpoint");
        mimeTypeMap.put("docx", "application/msword");
        mimeTypeMap.put("xlsx", "application/vnd.ms-excel");
        mimeTypeMap.put("pptx", "application/vnd.ms-powerpoint");
        mimeTypeMap.put("eml", "application/eml");
        // ADD START FOR PR1047782 BY HONGBIN.CHEN 20150811
        mimeTypeMap.put("xlsm", "application/vnd.ms-excel");
        // ADD END FOR PR1047782 BY HONGBIN.CHEN 20150811
        //add by yujie.zhao for PR1198966 at 20151222 begin
        mimeTypeMap.put("rtf", "application/msword");
        mimeTypeMap.put("keynote", "application/vnd.ms-powerpoint");
        mimeTypeMap.put("numbers", "application/vnd.ms-powerpoint");
        //add by yujie.zhao for PR1198966 at 20151222 end

        // for webfile
        mimeTypeMap.put("htm", "text/html");
        mimeTypeMap.put("html", "text/html");
        mimeTypeMap.put("xml", "text/html");
        mimeTypeMap.put("php", "application/vnd.wap.xhtml+xml");
        mimeTypeMap.put("url", "text/html");//add by yujie.zhao for PR1198966 at 20151222
        // for image
        mimeTypeMap.put("png", "image/*");
        mimeTypeMap.put("jpg", "image/*");
        mimeTypeMap.put("gif", "image/*");
        mimeTypeMap.put("bmp", "image/*");
        mimeTypeMap.put("jpeg", "image/*");
        mimeTypeMap.put("dm", "image/*");
        mimeTypeMap.put("dcf", "image/*");
        mimeTypeMap.put("wbmp", "image/*");
        mimeTypeMap.put("webp", "image/*");

        // for text
        mimeTypeMap.put("rc", "text/plain");
        mimeTypeMap.put("txt", "text/plain");
        mimeTypeMap.put("sh", "text/plain");
        mimeTypeMap.put("vcf", "text/x-vcard");
        mimeTypeMap.put("vcs", "text/x-vcalendar");
        mimeTypeMap.put("ics", "text/calendar");//add zibin.wang for 1477385 2016.01.26
        mimeTypeMap.put("ICZ", "text/calendar");
    }

    /**
     * This method check the file name is valid.
     *
     * @param fileName the input file name
     * @return valid or the invalid type
     */
    public static int checkFileName(String fileName) {
        if (TextUtils.isEmpty(fileName) || fileName.trim().length() == 0) {
            return FileManagerService.OperationEventListener.ERROR_CODE_NAME_EMPTY;
        } else {
            try {
                int length = fileName.getBytes("UTF-8").length;
                if (length > FileInfo.FILENAME_MAX_LENGTH) {
                    return FileManagerService.OperationEventListener.ERROR_CODE_NAME_TOO_LONG;
                } else {
                    return FileManagerService.OperationEventListener.ERROR_CODE_NAME_VALID;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return FileManagerService.OperationEventListener.ERROR_CODE_NAME_EMPTY;
            }
        }
    }

    /**
     * This method gets extension of certain file.
     *
     * @param fileName name of a file
     * @return Extension of the file's name
     */
    public static String getFileExtension(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        String extension = null;
        final int lastDot = fileName.lastIndexOf('.');
        if ((lastDot > 0)) {
            extension = fileName.substring(lastDot + 1).toLowerCase();
        }
        return extension;
    }

    /**
     * This method gets name of certain file from its path.
     *
     * @param absolutePath the file's absolute path
     * @return name of the file
     */
    public static String getFileName(String absolutePath) {
        int sepIndex = absolutePath.lastIndexOf(MountManager.SEPARATOR);
        if (sepIndex >= 0) {
            return absolutePath.substring(sepIndex + 1);
        }
        return absolutePath;

    }

    /**
     * This method gets path to directory of certain file(or folder).
     *
     * @param filePath path to certain file
     * @return path to directory of the file
     */
    public static String getFilePath(String filePath) {
        int sepIndex = filePath.lastIndexOf(MountManager.SEPARATOR);
        if (sepIndex >= 0) {
            return filePath.substring(0, sepIndex);
        }
        return "";

    }

    /**
     * This method generates a new suffix if a name conflict occurs, ex: paste a
     * file named "stars.txt", the target file name would be "stars(1).txt"
     *
     * @param file the conflict file
     * @return a new name for the conflict file
     */
    public static File genrateNextNewName(File file) {
        String parentDir = file.getParent();
        String fileName = file.getName();
        String ext = "";
        int newNumber = 0;
        if (file.isFile()) {
            int extIndex = fileName.lastIndexOf(".");
            if (extIndex != -1) {
                ext = fileName.substring(extIndex);
                fileName = fileName.substring(0, extIndex);
            }
        }

        int leftBracketIndex = fileName.lastIndexOf("_");
        if (leftBracketIndex != -1) {
            String numeric = fileName.substring(leftBracketIndex + 1, fileName.length());
            if (numeric.matches("[0-9]+")) {
                try {
                    newNumber = Integer.parseInt(numeric);
                    newNumber++;
                    fileName = fileName.substring(0, leftBracketIndex);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        StringBuffer sb = new StringBuffer();
        sb.append(fileName).append("_").append(newNumber).append(ext);
        if (FileUtils.checkFileName(sb.toString()) < 0) {
            return null;
        }
        return new File(parentDir, sb.toString());
    }


    /**
     * This method converts a size to a string
     *
     * @param size the size of a file
     * @return the string represents the size
     */
    public static String safeFileSizeToString(Context context, long progress, long max) {


        String maxInfo = sizeToString(context, max);


        String UNIT_B = context.getString(R.string.unit_B);
        String UNIT_KB = context.getString(R.string.unit_KB);
        String UNIT_MB = context.getString(R.string.unit_MB);
        String UNIT_GB = context.getString(R.string.unit_GB);
        String UNIT_TB = context.getString(R.string.unit_TB);
        String unit = UNIT_B;
        if (max < DECIMAL_NUMBER) {
            unit = UNIT_B;
        } else {
            unit = UNIT_KB;
            double sizeDouble = (double) max / (double) UNIT_INTERVAL;
            if (sizeDouble > UNIT_INTERVAL) {
                sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
                unit = UNIT_MB;
            }
            if (sizeDouble > UNIT_INTERVAL) {
                sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
                unit = UNIT_GB;
            }
            if (sizeDouble > UNIT_INTERVAL) {
                sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
                unit = UNIT_TB;
            }
        }

        if (unit.equals(UNIT_B)) {

            return Long.toString(progress) + UNIT_B + "/" + maxInfo;

        } else {
            double sizeDouble = 0d;
            if (unit.equals(UNIT_KB)) {
                unit = UNIT_KB;
                sizeDouble = (double) progress / (double) UNIT_INTERVAL;
            } else if (unit.equals(UNIT_MB)) {
                unit = UNIT_MB;
                sizeDouble = (double) progress / ((double) UNIT_INTERVAL * (double) UNIT_INTERVAL);
            } else if (unit.equals(UNIT_GB)) {
                unit = UNIT_GB;
                sizeDouble = (double) progress / ((double) UNIT_INTERVAL * (double) UNIT_INTERVAL * (double) UNIT_INTERVAL);
            } else if (unit.equals(UNIT_TB)) {
                unit = UNIT_TB;
                sizeDouble = (double) progress / ((double) UNIT_INTERVAL * (double) UNIT_INTERVAL * (double) UNIT_INTERVAL * (double) UNIT_INTERVAL);
            }
            long sizeInt = (long) ((sizeDouble + ROUNDING_OFF) * DECIMAL_NUMBER);

            double formatedSize = ((double) sizeInt) / DECIMAL_NUMBER;

            if (formatedSize == 0) {
                return "0" + " " + unit;
            } else {
                return Double.toString(formatedSize) + unit + "/" + maxInfo;
            }
        }


    }

    /**
     * This method converts a size to a string
     *
     * @param size the size of a file
     * @return the string represents the size
     */
    public static String sizeToString(Context context, long size) {
        if (context != null) {
            String UNIT_B = context.getString(R.string.unit_B);
            String UNIT_KB = context.getString(R.string.unit_KB);
            String UNIT_MB = context.getString(R.string.unit_MB);
            String UNIT_GB = context.getString(R.string.unit_GB);
            String UNIT_TB = context.getString(R.string.unit_TB);


            String unit = UNIT_B;
            if (size < DECIMAL_NUMBER) {
                return Long.toString(size) + " " + unit;
            }
            unit = UNIT_KB;
            double sizeDouble = (double) size / (double) UNIT_INTERVAL;
            if (sizeDouble > UNIT_INTERVAL) {
                sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
                unit = UNIT_MB;
            }
            if (sizeDouble > UNIT_INTERVAL) {
                sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
                unit = UNIT_GB;
            }
            if (sizeDouble > UNIT_INTERVAL) {
                sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
                unit = UNIT_TB;
            }

            // Add 0.005 for rounding-off.
            long sizeInt = (long) ((sizeDouble + ROUNDING_OFF) * DECIMAL_NUMBER);

            double formatedSize = ((double) sizeInt) / DECIMAL_NUMBER;

            if (formatedSize == 0) {
                return "0" + " " + unit;
            } else {
                return Double.toString(formatedSize) + " " + unit;
            }
        } else {
            return "0";
        }
    }

    /**
     * This method converts a size to a string, Be used for PieChart bottom storage details
     *
     * @param size the size of a file
     * @return the string represents the size
     */
    public static String sizeToStringPieChart(Context context, long size) {
        if (context != null) {
            String UNIT_B = context.getString(R.string.unit_B);
            String UNIT_KB = context.getString(R.string.unit_KB);
            String UNIT_MB = context.getString(R.string.unit_MB);
            String UNIT_GB = context.getString(R.string.unit_GB);
            String UNIT_TB = context.getString(R.string.unit_TB);


            String unit = UNIT_B;
            if (size < DECIMAL_NUMBER) {
                return Long.toString(size) + unit;
            }
            unit = UNIT_KB;
            double sizeDouble = (double) size / (double) UNIT_INTERVAL;
            if (sizeDouble > UNIT_INTERVAL) {
                sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
                unit = UNIT_MB;
            }
            if (sizeDouble > UNIT_INTERVAL) {
                sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
                unit = UNIT_GB;
            }
            if (sizeDouble > UNIT_INTERVAL) {
                sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
                unit = UNIT_TB;
            }

            long sizeInt = (long) ((sizeDouble + ROUNDING_OFF) * DECIMAL_NUMBER);

            double formatedSize = ((double) sizeInt) / DECIMAL_NUMBER;

            NumberFormat nf=new DecimalFormat( "0.0 ");
            formatedSize = Double.parseDouble(nf.format(formatedSize));

            if (formatedSize == 0) {
                return "0" + unit;
            } else {
                return Double.toString(formatedSize) + unit;
            }
        } else {
            return "0";
        }
    }

    public static String sizeToGBString(Context context, long size, boolean isUnits) {
        return sizeToGBString(context, size, isUnits, false);
    }

    /**
     * This method converts a size to GB a string
     *
     * @param size the size of a file
     * @return the string represents the size
     */
    public static String sizeToGBString(Context context, long size, boolean isUnits, boolean withoutDecimal) {
        String UNIT_B = context.getString(R.string.unit_B);
        String UNIT_GB = context.getString(R.string.unit_GB);
        String UNIT_TB = context.getString(R.string.unit_TB);

        String unit = UNIT_B;
        if (size < DECIMAL_NUMBER) {
            if (isUnits) {
                return Long.toString(size) + " " + unit;
            } else {
                return Long.toString(size) + " ";
            }
        }
        unit = UNIT_GB;
        double sizeDouble = (double) size / ((double) UNIT_INTERVAL * UNIT_INTERVAL * UNIT_INTERVAL);
        if (sizeDouble > UNIT_INTERVAL) {
            sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
            unit = UNIT_TB;
        }

        if (withoutDecimal) {
            // Add 0.005 for rounding-off.
            long formatedSize = (long) sizeDouble;

            if (isUnits) {
                return formatedSize + " " + unit;
            } else {
                return formatedSize + " ";
            }
        } else {
            // Add 0.005 for rounding-off.
            long sizeInt = (long) ((sizeDouble + ROUNDING_OFF) * DECIMAL_NUMBER);

            double formatedSize = ((double) sizeInt) / DECIMAL_NUMBER;

            if (formatedSize == 0) {
                if (isUnits) {
                    return "0.00" + " " + unit;
                } else {
                    return "0.00" + " ";
                } // PR-1208439 Nicky Ni -001 20151201
            } else {
                if (isUnits) {
                    return Double.toString(formatedSize) + " " + unit;
                } else {
                    return Double.toString(formatedSize) + " ";
                }
            }
        }
    }

    /**
     * This method gets the MIME type from multiple files (order to return:
     * image->video->other)
     *
     * @param service service of FileManager
     * @param currentDirPath the current directory
     * @param files a list of files
     * @return the MIME type of the multiple files
     */
//    public static String getMultipleMimeType(FileManagerService service, String currentDirPath,
//            List<FileInfo> files) {
//        String mimeType = null;
//
//        for (FileInfo info : files) {
//            mimeType = info.getMimeType();
//            if ((null != mimeType)
//                    && (mimeType.startsWith("image/") || mimeType.startsWith("video/"))) {
//                break;
//            }
//        }
//
//        if (TextUtils.isEmpty(mimeType) || mimeType.startsWith("unknown")) {
//            mimeType = FileInfo.MIMETYPE_UNRECOGNIZED;
//        }
//        return mimeType;
//    }

    /**
     * This method checks weather extension of certain file(not folder) is
     * changed.
     *
     * @param newFilePath path to file before modified.(Here modify means
     *                    rename).
     * @param oldFilePath path to file after modified.
     * @return true for extension changed, false for not changed.
     */
    public static boolean isExtensionChange(String newFilePath, String oldFilePath) {
        File oldFile = new File(oldFilePath);
        if (oldFile.isDirectory()) {
            return false;
        }
        String origFileExtension = FileUtils.getFileExtension(oldFilePath);
        String newFileExtension = FileUtils.getFileExtension(newFilePath);
        if (((origFileExtension != null) && (!origFileExtension.equals(newFileExtension)))
                || ((newFileExtension != null) && (!newFileExtension.equals(origFileExtension)))) {
            return true;
        }
        return false;
    }

    /**
     * This method checks weather file name start with ".".
     *
     * @param newFilePath path to file before modified.(Here modify means
     *                    rename).
     * @param oldFilePath path to file after modified.
     * @return true for extension changed, false for not changed.
     */
    public static boolean isStartWithDot(String newFilePath, String oldFilePath) {
        boolean oldStartWithDot = FileUtils.getFileName(oldFilePath).startsWith(".");
        boolean newStartWithDot = FileUtils.getFileName(newFilePath).startsWith(".");

        if (!oldStartWithDot && newStartWithDot) {
            return true;
        } else {
            return false;
        }
    }

    private static final String[] THUMB_PROJECTION = new String[]{
            Thumbnails._ID,
            Thumbnails.DATA,
    };

    private static final int ID_INDEX_COLUMN = 0;
    private static final int DATA_INDEX_COLUMN = 1;

    private static final int MINI_THUMB_TARGET_SIZE = 256;
    private static final int MINI_THUMB_MAX_NUM_PIXELS = 512 * 512;

    private static final int UNCONSTRAINED = -1;

    //add for PR889409 by yane.wang@jrdcom.com 20150109
    public static Drawable queryThumbnail(Context context, FileInfo fileInfo, String mimeType, boolean isDrm) {
        DrmManager drmManager = DrmManager.getInstance(context.getApplicationContext());
        if (fileInfo.getFile().isDirectory()) {
            return null;
        }
        Bitmap bitmap = null;
        if (isDrm) {
            //add for PR941855,PR944500 by yane.wang@jrdcom.com 20150312 begin
            String path = fileInfo.getFileAbsolutePath();
            String drmOriginalType = drmManager.getOriginalMimeType(path);
            if (drmOriginalType != null && !drmOriginalType.isEmpty()) {
                mimeType = drmOriginalType;
            }
            //add for PR941855,PR944500 by yane.wang@jrdcom.com 20150312 end
            if (mimeType.contains(FileInfo.MIME_HAED_IMAGE)) {
                Options op = new Options();
                bitmap = drmManager.getDrmRealThumbnail(path, op,
                        DrmManager.DRM_THUMBNAIL_WITH);
            } else if (mimeType.contains(FileInfo.MIME_HEAD_VIDEO)) {
                bitmap = drmManager.getDrmVideoThumbnail(FileUtils.getVideoThumbnail(context, fileInfo),
                        path, DrmManager.DRM_THUMBNAIL_WITH);
            } else if (mimeType.contains(FileInfo.MIME_HEAD_AUDIO)) {
                bitmap = drmManager.getDrmThumbnail(path,
                        DrmManager.DRM_THUMBNAIL_WITH);
            }
            //add for Drm icon by yane.wang@jrdcom.com 20150420 begin
            if (DrmManager.mCurrentDrm == 10) {
                bitmap = drawLockIcon(context, bitmap, drmManager.isRightsStatus(path), mimeType);
            }
            //add for Drm icon by yane.wang@jrdcom.com 20150420 end
        } else {
            if (mimeType == null) {
                return null;
            }

            if (CategoryManager.isSafeCategory) {

                int fileType = fileInfo.getFileType();
                if (fileType == CategoryManager.SAFE_CATEGORY_PICTURES || fileType == CategoryManager.SAFE_CATEGORY_VEDIO) {
                    bitmap = BitmapFactory.decodeFile(fileInfo.getFileAbsolutePath() + "_temp");
                /*MODIFIED-BEGIN by wenjing.ni, 2016-04-15,BUG-1950858*/
                } else if (mimeType.contains(FileInfo.MIMETYPE_APK)) {
                    bitmap = getAPKThumbnail(context, fileInfo);
                    /*MODIFIED-END by wenjing.ni,BUG-1950858*/
                }

            } else {
                if (mimeType.contains(FileInfo.MIME_HAED_IMAGE)) {
                    bitmap = getImageThumbnail(context, fileInfo);
                } else if (mimeType.contains(FileInfo.MIME_HEAD_VIDEO) ||
                        mimeType.contains(FileInfo.MIMETYPE_3GPP_VIDEO) ||
                        mimeType.contains(FileInfo.MIMETYPE_3GPP2_VIDEO) ||
                        mimeType.contains(FileInfo.MIMETYPE_3GPP_UNKONW)) {
                    bitmap = getVideoThumbnail(context, fileInfo);
                } else if (mimeType.contains(FileInfo.MIME_HEAD_AUDIO)) {
                    bitmap = getAudioThumbnail(context, fileInfo);
                } else if (mimeType.contains(FileInfo.MIMETYPE_APK)) {
                    bitmap = getAPKThumbnail(context, fileInfo);
                }
            }


        }
        //Drawable drawable = getDrawableForBitmap(context.getResources(), zoomImg(bitmap,bitmap.getWidth(),bitmap.getHeight()));
        Drawable drawable = centerSquareScaleBitmap(context.getResources(), bitmap, BITMAP_SIZE);
        return drawable;

    }

    private static Bitmap readBitmap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    private static Bitmap drawLockIcon(Context context, Bitmap sourceBitmap, boolean isRightValid, String mimeType) {
        Bitmap lockBitmap = null;
        if (sourceBitmap == null) {
            if (mimeType.contains(FileInfo.MIME_HAED_IMAGE)) {
                sourceBitmap = readBitmap(context, R.drawable.drm_image);
            } else if (mimeType.contains(FileInfo.MIME_HEAD_VIDEO)) {
                sourceBitmap = readBitmap(context, R.drawable.drm_video);
            } else if (mimeType.contains(FileInfo.MIME_HEAD_AUDIO)) {
                sourceBitmap = readBitmap(context, R.drawable.drm_music);
            } else {
                sourceBitmap = readBitmap(context, R.drawable.ic_list_unknown);
            }

        }
        int sourceW = sourceBitmap.getWidth();
        int sourceH = sourceBitmap.getHeight();
        if (isRightValid) {
            lockBitmap = readBitmap(context, R.drawable.drm_green_lock);
        } else {
            lockBitmap = readBitmap(context, R.drawable.drm_red_lock);
        }
        int lockW = lockBitmap.getWidth();
        int lockH = lockBitmap.getHeight();
        Bitmap newLock = Bitmap.createBitmap(sourceW, sourceH, Config.ARGB_8888);
        Canvas canvas = new Canvas(newLock);
        canvas.drawBitmap(sourceBitmap, 0, 0, null);
        canvas.drawBitmap(lockBitmap, sourceW - lockW, sourceH - lockH, null);
        return newLock;
    }

    /**
     * @param bitmap     bitmap
     * @param edgeLength szie
     * @return BitmapDrawable
     */
    /*PR 1246428 zibin.wang 2016.0120 add */
    public static BitmapDrawable centerSquareScaleBitmap(Resources resources, Bitmap bitmap, int edgeLength) {
        if (null == bitmap || edgeLength <= 0) {
            return null;
        }
        BitmapDrawable bitmapDrawable = new BitmapDrawable(resources, bitmap);
        Bitmap result = bitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();

        if (widthOrg > edgeLength && heightOrg > edgeLength) {
            int longerEdge = (int) (edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg));
            int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
            int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
            Bitmap scaledBitmap;
            try {
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
            } catch (Exception e) {
                return null;
            }
            int xTopLeft = (scaledWidth - edgeLength) / 2;
            int yTopLeft = (scaledHeight - edgeLength) / 2;
            try {
                result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
                bitmapDrawable = new BitmapDrawable(resources, result);
                scaledBitmap.recycle();
            } catch (Exception e) {
                if (result != null) result.recycle();
                return null;
            }
        }
        return bitmapDrawable;
    }

    //modify for PR959312 by long.tang@tcl.com on 2015.03.31 start
//    private static Drawable getDrawableForBitmap(Resources resources, Bitmap bitmap) {
//		if (bitmap == null) {
//			return null;
//		}
//        Bitmap bitmap1 = null;
//        /*PR 1246428 zibin.wang 2016.0120 add */
//        int picWidth = bitmap.getWidth();
//        int picHeight=bitmap.getHeight();
//        try {
//            bitmap1 = Bitmap.createScaledBitmap(bitmap, 128, 128, false);
////            final RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(resources, bitmap1);
//            final  BitmapDrawable bitmapDrawable=new BitmapDrawable(resources, bitmap1);
//            bitmap1=null;
////            drawable.setAntiAlias(true);
////            drawable.setCornerRadius(bitmap1.getWidth() / 2);
////            return drawable;
//            return bitmapDrawable;
//		} catch (OutOfMemoryError ex) {
//			if (bitmap1 != null) bitmap1.recycle();
//            return null;
//        }
//    }
    //add for PR889409 by yane.wang@jrdcom.com 20150109 end
//    public static Bitmap zoomImg(Bitmap bm, int newWidth ,int newHeight){ /*PR 1246428 zibin.wang 2016.0120 add */
//        int width = bm.getWidth();
//        int height = bm.getHeight();
//        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
//        Matrix matrix = new Matrix();
//        matrix.postScale(scaleWidth*0.5f, scaleHeight*0.5f);
//        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
//        return newbm;
//    }
    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-15,BUG-1944914*/
    public static Bitmap getImageThumbnail(Context context, FileInfo fileInfo) { //PR 1877818 zibin.wang add 2016/03/31
        ContentResolver contentResolver = context.getContentResolver();
        Bitmap bitmap = null;
        File file = fileInfo.getFile();
        if (file.exists()) {
            String filePath = file.getPath();
            LogUtils.i(TAG, "getImageThumbnail path:" + filePath);
            String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, };
            String whereClause = MediaStore.Images.Media.DATA + " = '" + filePath + "'";
            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, whereClause,
                    null, null);
            try {
                int _id = 0;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Config.RGB_565;
                options.inDither = false;
            /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-2073492*/
                if (cursor !=null &&  cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        int _idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                        _id = cursor.getInt(_idColumn);
                    }
                    options.inSampleSize = 1;
                    bitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, _id, Images.Thumbnails.MINI_KIND,
                            options);
//                bitmap = ThumbnailUtils.extractThumbnail(bitmap, BITMAP_SIZE, BITMAP_SIZE);
                } else {
                    LogUtils.i(TAG, "filePath:" + filePath);
                    if (fileInfo.getFile().length() < (UNIT_INTERVAL * DECIMAL_NUMBER)) {
                        options.inSampleSize = 1;
                    } else {
                        options.inSampleSize = 6;
                    }
                    bitmap = BitmapFactory.decodeFile(filePath, options);
                }
                try {
                    bitmap = ThumbnailUtils.extractThumbnail(bitmap, BITMAP_SIZE, BITMAP_SIZE, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                } catch (OutOfMemoryError ex) {
                    LogUtils.e(TAG, ex.toString());
                    bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_imagenew);
                }
            /* MODIFIED-END by haifeng.tang,BUG-2073492*/
            } catch (Exception e) {
                LogUtils.d("FilUtil", "Exception occured when getImageThumbnail():", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_imagenew);
        }

        return bitmap;
        /*MODIFIED-END by haifeng.tang,BUG-1944914*/
    }
    private static ParcelFileDescriptor makeInputStream(Uri uri, ContentResolver cr) {
        try {
            return cr.openFileDescriptor(uri, "r");
        } catch (IOException ex) {
            return null;
        }
    }

    private static Bitmap makeBitmapByStream(int minSideLength, int maxNumOfPixels, File file) {
        if (file == null || !file.exists()) {
            Log.d("FileManger.FileUtils", "file is null or not exist when invoke makeBitmapByStream.");
            return null;
        }

        Bitmap bm = null;
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            Options options = new Options();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, null, options);
            if (options.mCancel || options.outWidth == -1
                    || options.outHeight == -1) {
                return null;
            }
            options.inSampleSize = computeSampleSize(
                    options, minSideLength, maxNumOfPixels);
            options.inJustDecodeBounds = false;

            options.inDither = false;
            options.inPreferredConfig = Config.ARGB_8888;
            stream = new FileInputStream(file); //MODIFIED by zibin.wang, 2016-04-16,BUG-1958127 // MODIFIED by haifeng.tang, 2016-05-05,BUG-2073492
            bm = BitmapFactory.decodeStream(stream, null, options);
        } catch (Exception e) {
            //do nothing.
            //If the exception happened on open, bm will be null.
            Log.e("FileManger.FileUtils", "Unable to decode stream: " + e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // do nothing here
                }
            }
        }
        return bm;
    }

    private static Bitmap makeBitmap(int minSideLength, int maxNumOfPixels, Uri uri, ContentResolver cr, ParcelFileDescriptor pfd, BitmapFactory.Options options) {
        Bitmap b = null;
        try {
            if (pfd == null)
                pfd = makeInputStream(uri, cr);
            if (pfd == null)
                return null;
            if (options == null)
                options = new BitmapFactory.Options();

            FileDescriptor fd = pfd.getFileDescriptor();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            if (options.mCancel || options.outWidth == -1
                    || options.outHeight == -1) {
                return null;
            }
            options.inSampleSize = computeSampleSize(options, minSideLength, maxNumOfPixels);
            options.inJustDecodeBounds = false;

            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            b = BitmapFactory.decodeFileDescriptor(fd, null, options);

        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
            return null;
        } finally {
            closeSilently(pfd);
        }
        return b;
    }

    private static void closeSilently(ParcelFileDescriptor c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }

    private static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == UNCONSTRAINED) &&
                (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static Bitmap getVideoThumbnail(Context context, FileInfo fileInfo) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(fileInfo.getFile().getPath());
            synchronized (MediaMetadataRetriever.class) {//add for PR997015 by yane.wang@jrdcom.com 20150508
                bitmap = retriever.getFrameAtTime();
                bitmap = Bitmap.createScaledBitmap(bitmap, BITMAP_SIZE, BITMAP_SIZE, false);//add for PR824285 by yane.wang@jrdcom.com 20141106
            }
        } catch (IllegalArgumentException ex) {
            LogUtils.e(TAG, ex.toString());
        } catch (RuntimeException ex) {
            LogUtils.e(TAG, ex.toString());
        } catch(Exception e){
            LogUtils.e(TAG, e.toString());
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        if (null == bitmap) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_videonew);
        }
        return bitmap;
    }


    private static Bitmap getAPKThumbnail(Context context, FileInfo fileInfo) {
        String apkPath = fileInfo.getFile().getPath();
        File file = fileInfo.getFile();
        if (!file.exists()) {
            return null;
        }
        Bitmap icon = null;
        final String PATH_PackageParser = "android.content.pm.PackageParser";
        try {
            Class pkgParserCls = Class.forName(PATH_PackageParser);
            Class[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Constructor pkgParserCt = null;
            Object pkgParser = null;
            Object[] valueArgs = new Object[1];
            try {
                pkgParserCt = pkgParserCls.getConstructor(null);
                pkgParser = pkgParserCt.newInstance(null);
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
                try {
                    pkgParserCt = pkgParserCls.getConstructor(typeArgs);
                    valueArgs[0] = apkPath;
                    pkgParser = pkgParserCt.newInstance(valueArgs);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            if (pkgParserCt == null || pkgParser == null) {
                return null;
            }
            Method pkgParser_parsePackageMtd = null;

            try {
                typeArgs = new Class[2];
                typeArgs[0] = File.class;
                typeArgs[1] = Integer.TYPE;
                pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
                valueArgs = new Object[2];
                valueArgs[0] = file;
                valueArgs[1] = 0;
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
                try {
                    typeArgs = new Class[4];
                    typeArgs[0] = File.class;
                    typeArgs[1] = String.class;
                    typeArgs[2] = DisplayMetrics.class;
                    typeArgs[3] = Integer.TYPE;
                    pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
                    DisplayMetrics metrics = new DisplayMetrics();
                    metrics.setToDefaults();
                    valueArgs = new Object[4];
                    valueArgs[0] = new File(apkPath);
                    valueArgs[1] = apkPath;
                    valueArgs[2] = metrics;
                    valueArgs[3] = 0;
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            Object pkgParserPkg = null;
            if (pkgParser_parsePackageMtd == null) {
                return null;
            }
            pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
            Class assetMagCls = Class.forName("android.content.res.AssetManager");
            Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
            Object assetMag = assetMagCt.newInstance((Object[]) null);
            typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
            valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
            Resources res = context.getResources();
            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor resCt = Resources.class.getConstructor(typeArgs);
            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();
            res = (Resources) resCt.newInstance(valueArgs);

            if (info.icon != 0) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                try {
                    icon = BitmapFactory.decodeResource(res, info.icon, opts);
                } catch (OutOfMemoryError e) {
                    opts.inSampleSize = 1;
                    icon = BitmapFactory.decodeResource(res, info.icon, opts);
                }
            }

            /** Lift the occupancy of APK files */
            Method assetMag_close = assetMagCls.getDeclaredMethod("close");
            assetMag_close.invoke(assetMag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return icon;
    }

    /**
     * Add an additional set of assets to the asset manager. This can be either
     * a directory or ZIP file. Not for use by applications. Returns the cookie
     * of the added asset, or 0 on failure. {@hide}
     */
    private final int addAssetPath(String path) {
        int res = addAssetPathNative(path);
        return res;
    }

    private native final int addAssetPathNative(String path);

    private static Bitmap getAudioThumbnail(Context context, FileInfo fileInfo) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(fileInfo.getFile().getPath());
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null && art.length > 0) {
                Options op = new Options();
                op.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(art, 0, art.length, op);
                float scale = calculateScale(BITMAP_SIZE, BITMAP_SIZE, op.outWidth, op.outHeight);//add for PR824285 by yane.wang@jrdcom.com 20141106
                int inSampleSize = Math.round((1 / scale));
                if (inSampleSize > scale) {
                    inSampleSize -= 1;
                }
                op.inSampleSize = inSampleSize;
                op.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeByteArray(art, 0, art.length, op);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            if (null == bitmap){
                bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_audionew);
            }
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    private static float calculateScale(int destWidth, int destHeight,
                                        int originWidth, int originHeight) {
        float scale = 1.0f;
        if (destHeight * originWidth <= originHeight * destWidth) {
            scale = (float) destHeight / originHeight;
        } else {
            scale = (float) destWidth / originWidth;
        }
        return scale;
    }

    public static String getMimeTypeByExt(String fileName) {
        return mimeTypeMap.get(getFileExtension(fileName));
    }

    public static String getAudioMimeType(String mime) {
        if (mime.equals("mp3")) {
            return "audio/mp3";
        } else if (mime.equals("ogg")) {
            return "audio/ogg";
        } else if (mime.equals("wma")) {
            return "audio/x-ms-wma";
        } else if (mime.equals("awb")) {
            return "audio/amr-wb";
        } else if (mime.equals("aac") || mime.equals("m4a")) {
            return "audio/*";
        } else {
            return mime;
        }
    }

    public static void getALLTypeSql(StringBuilder sb) {

        sb.append(" and ");
        sb.append(MediaStore.Files.FileColumns.FORMAT + "!=");
        DatabaseUtils.appendEscapedSQLString(sb, MtpConstants.FORMAT_ASSOCIATION + "");
        sb.append(" and (").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.jpg");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.jpeg");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.png");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.bmp");

        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.pdf");

        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.mp3");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.mpga");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.wmv");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.wav");
        sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.m4a");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.ape");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.flac");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.acc");

        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.mpeg");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.mpg");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.flv");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.mkv");
        sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.3gp");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.asf");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.avi");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.mp4");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.rmvb");

        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.zip");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.rar");
        sb.append(" or ").append(
                MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%.apk");
        sb.append(")");
    }

}
