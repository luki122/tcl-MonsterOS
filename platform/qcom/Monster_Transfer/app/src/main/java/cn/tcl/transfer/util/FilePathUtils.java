/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Downloads;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import org.w3c.dom.Text;

import cn.tcl.transfer.File_Exchange;
import cn.tcl.transfer.entity.AudioContent;
import cn.tcl.transfer.entity.DocumentContent;
import cn.tcl.transfer.entity.PictureContent;
import cn.tcl.transfer.entity.VideoContent;


public class FilePathUtils {

    private static final String LOG_TAG = FilePathUtils.class.getSimpleName();

    private static final String DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss";
    private static final Uri CONTACTS_RAW_URI = Uri
            .parse("content://com.android.contacts/raw_contacts");

    // public static final String DEVICES_PATH =
    // getStoragePath(HomeActivity.STORAGE_ID_DEVICE);

    public static final String PHONE_DIR_PATH = "OnetouchMigration";

    // public static String SDCARD_PATH =
    // getStoragePath(HomeActivity.STORAGE_ID_SDCARD);

    public static String DATA_DATA_PATH = Environment.getDataDirectory()
            .getPath() + "/data/com.tct.smartmove";

    public static final String[] CATAGORY_PATHS = new String[]{"message",
            "contacts", "calender", "Photo", "Video", "Music", "app",
            "calllog", "settings", "bookmark", "otherfiles", "data"};

    public static final String MESSAGE_PATH = "message";

    public static final String BOOKMATKS_PATH = "bookmarks";

    public static final String SETTINGS_PATH = "settings";

    private static String[] selectSqlItem;

    private static ArrayList<File_Exchange> pictureList;
    private static ArrayList<File_Exchange> videoList;
    private static ArrayList<File_Exchange> audioList;
    private static ArrayList<String> picturePathList;
    private static ArrayList<String> videoPathList;
    private static ArrayList<String> audioPathList;
    private static ArrayList<String> appPathList;
    private static ArrayList<String> appPackageList;
    private static ArrayList<String> systemList;
    private static ArrayList<String> messageList;
    private static ArrayList<String> calenderList;
    private static ArrayList<String> contactList;
    private static ArrayList<String> calllogList;
    private static ArrayList<String> settingList;

    private static int systemCount;

    public static String getStoragePath(int index) {
        String strFatherPath = Environment.getExternalStorageDirectory()
                .getParent();
        String resultString = null;
        // PR872836-song.yang@tct.com-for The restore files does not refresh
        // after changing default storage-begin.
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            return getStoragePathForL(index);
        }
        // PR872836-song.yang@tct.com-for The restore files does not refresh
        // after changing default storage-end.
        if (strFatherPath.contains("mnt")) {
            if (index == 2) {
                if (Environment.MEDIA_MOUNTED.equals(Environment
                        .getExternalStorageState())) {
                    resultString = Environment.getExternalStorageDirectory()
                            .getAbsolutePath();
                }
            }
        } else {
            if (Environment.isExternalStorageRemovable()) {
                File externalFile = Environment.getExternalStorageDirectory();
                if (index == 2) {
                    if ((Environment.MEDIA_MOUNTED.equals(Environment
                            .getExternalStorageState()))) {
                        resultString = externalFile.getAbsolutePath();
                        /**
                         * File file = new File(externalFile,
                         * "oneTouchBackupText.txt"); boolean isExists = false;
                         * try { if (file.exists()) { if (file.delete()) {
                         * isExists = true; } } else if (file.createNewFile()) {
                         * isExists = true; } } catch (IOException e) {
                         * e.printStackTrace(); } if (isExists) { resultString =
                         * externalFile.getAbsolutePath(); }else{
                         *
                         * }
                         */
                    }
                } else {
                    File fileParent = Environment.getExternalStorageDirectory()
                            .getParentFile();
                    File[] files = fileParent.listFiles();
                    /* PR 883539- Neo Skunkworks - Wenjing.ni - 001 start */
                    if (files.length == 2 || files.length == 3
                            || files.length == 5) {/*
                                                     * PR 883539- Neo Skunkworks
                                                     * - Wenjing.ni - 001 end
                                                     */
                        resultString = files[0].getAbsolutePath().equals(
                                externalFile.getAbsolutePath()) ? files[1]
                                .getAbsolutePath() : files[0].getAbsolutePath();
                        /* PR 905704- Neo Skunkworks - Wenjing.ni - 001 start */
                        if (resultString.equals("/storage/usbotg")
                                && files.length == 5) {
                            resultString = files[0].getAbsolutePath().equals(
                                    externalFile.getAbsolutePath()) ? files[2]
                                    .getAbsolutePath() : files[3]
                                    .getAbsolutePath();
                        }
                        /* PR 905704- Neo Skunkworks - Wenjing.ni - 001 start */
                    }
                }
            } else {
                if (index == 1) {
                    resultString = Environment.getExternalStorageDirectory()
                            .getAbsolutePath();
                }
            }
        }
        Log.e(LOG_TAG, "--getStoragePath start--INDEX IS :" + index
                + "    storage path is:" + resultString);
        return resultString;
    }

    // PR872836-song.yang@tct.com-for The restore files does not refresh after
    // changing default storage-begin.
    public static String getStoragePathForL(int index) {
        LogUtils.d(LOG_TAG, "get storage for L");
        String resultString = null;
        File fileParent = Environment.getExternalStorageDirectory()
                .getParentFile();
        /* PR 905704- Neo Skunkworks - Wenjing.ni - 001 start */
        if (fileParent.getPath().contains("/storage/emulated")) {
            fileParent = new File(File.separator + "storage");
        }
        /* PR 905704- Neo Skunkworks - Wenjing.ni - 001 start */
        ArrayList<File> files = new ArrayList<File>();
        for (int i = 0; i < fileParent.listFiles().length; i++) {
            if (fileParent.listFiles()[i].getAbsolutePath().contains("sdcard")) {
                if (fileParent.listFiles()[i].listFiles() == null
                        || fileParent.listFiles()[i].length() == 0) {
                    continue;
                }
                files.add(fileParent.listFiles()[i]);
            }
        }
        /* PR 883539- Neo Skunkworks - Wenjing.ni - 001 start */
        if (files.size() == 2 || files.size() == 3) {/*
                                                     * PR 883539- Neo Skunkworks
                                                     * - Wenjing.ni - 001 end
                                                     */
            File externalFile = Environment.getExternalStorageDirectory();
            LogUtils.d("SNS",
                    "getStoragePathForL Environment.getExternalStorageDirectory()------22222222"
                            + externalFile.getAbsolutePath());
            File[] parentfiles = new File(File.separator + "storage")
                    .listFiles();
            if (index == 2) {
                // default storage is sd card
                if (Environment.isExternalStorageRemovable()) {
                    LogUtils.d("SNS",
                            "getStoragePathForL Environment.isExternalStorageRemovable() ------111111111"
                                    + externalFile.getAbsolutePath());
                    resultString = externalFile.getAbsolutePath();
                } else {
                    /* PR 905704- Neo Skunkworks - Wenjing.ni - 001 start */
                    if (externalFile.getParentFile().getAbsolutePath()
                            .contains("/storage/emulated")
                            && parentfiles.length == 6) {
                        resultString = parentfiles[4].getAbsolutePath();
                    } else {
                        resultString = externalFile.getAbsolutePath().replace(
                                "sdcard0", "sdcard1");
                    }
                    /* PR 905704- Neo Skunkworks - Wenjing.ni - 001 start */
                }
            } else {
                if (!Environment.isExternalStorageRemovable()) {
                    /* PR 905704- Neo Skunkworks - Wenjing.ni - 001 start */
                    if (externalFile.getParentFile().getAbsolutePath()
                            .contains("/storage/emulated")
                            && parentfiles.length == 6) {
                        resultString = parentfiles[0].getAbsolutePath();
                    } else {
                        resultString = externalFile.getAbsolutePath();
                    }
                    /* PR 905704- Neo Skunkworks - Wenjing.ni - 001 start */
                } else {
                    resultString = externalFile.getAbsolutePath().replace(
                            "sdcard0", "sdcard1");
                }
            }
        } else {
            if (index == 1) {
                resultString = Environment.getExternalStorageDirectory()
                        .getAbsolutePath();
            }
        }
        Log.e(LOG_TAG, "--getStoragePath start--INDEX IS :" + index
                + "    storage path is:" + resultString);
        return resultString;
    }

    // PR872836-song.yang@tct.com-for The restore files does not refresh after
    // changing default storage-end.
    public static String getStorageDir() {
        File extDir = Environment.getExternalStorageDirectory();
        String extPath = extDir.getParent();
        if (extPath.contains("mnt")) {
            extPath = extDir.getAbsolutePath();
        }
        return extPath;
    }


    public static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        Log.e(LOG_TAG, "date is " + str);
        return str;
    }

    public static String getCurrentDate(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        Log.e(LOG_TAG, "date is " + str);
        return str;
    }

    public static String getSDDirPath() {
        return getStoragePath(2) + "/" + PHONE_DIR_PATH + "/"
                + getCurrentDate();
    }

    public static String getDevicesDirPath() {
        return getStoragePath(1) + "/" + PHONE_DIR_PATH + "/"
                + getCurrentDate();
    }

    public static File createFileName(String dirPath, Uri uri,
                                      String fileExtensionName) {
        File fileDir = new File(dirPath);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        String uriFile = getUriString(uri) + fileExtensionName;
        File file = new File(dirPath, uriFile);
        LogUtils.d(LOG_TAG, "AbsolutePath-->" + file.getAbsolutePath());
        return file;
    }

    public static File createFileName(String dirPath, String fileName) {
        File fileDir = new File(dirPath);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        File file = new File(dirPath, fileName);
        LogUtils.d(LOG_TAG, "AbsolutePath-->" + file.getAbsolutePath());
        return file;
    }

    public static String getUriString(Uri uri) {
        String uriFile = uri.toString().replaceAll("//", "_")
                .replaceAll("/", "_").replaceAll(":", "");
        LogUtils.d(LOG_TAG, "AbsolutePath-->" + uriFile);
        return uriFile;
    }

    public static void closeStream(InputStream io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void closeStream(OutputStream io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void closeStream(Writer io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void closeStream(Reader io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void backupByCopyFile(String sourcePath, String targetPath) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(new File(sourcePath));
            fos = new FileOutputStream(new File(targetPath));
            int len;
            byte[] b = new byte[128];
            while ((len = fis.read(b)) > 0) {
                LogUtils.d(LOG_TAG, " len = " + len);
                fos.write(b, 0, len);
                fos.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(fis);
            closeStream(fos);
        }
    }

    public static int copyFile(String srcPath, String destPath) {
        try {
            InputStream in = new FileInputStream(srcPath);
            OutputStream out = new FileOutputStream(destPath);
            if (in == null || out == null)
                return -1;

            byte[] buffer = new byte[1024];
            int iRead = 0;
            while ((iRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, iRead);
            }

            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String convertTimeString(String time) {
        String timeR = null;
        String splitSubline[] = time.split("_");
        if (splitSubline != null) {
            if (splitSubline.length > 0) {
                String splitLine[] = splitSubline[0].split("-");
                if (splitLine != null && splitLine.length == 6) {
                    String year = splitLine[0];
                    String month = splitLine[1];
                    String day = splitLine[2];
                    String hour = splitLine[3];
                    String minute = splitLine[4];
                    String second = splitLine[5];
                    timeR = hour + ":" + minute + "  " + day + "/" + month
                            + "/" + year;
                }
            }
        }
        return timeR;
    }

    public static int caculateFolderSize(String path) {
        int size = 0;
        File dirFile = new File(path);
        if (!dirFile.isDirectory())
            return -1;
        for (File file : dirFile.listFiles()) {
            if (file.isDirectory())
                size += caculateFolderSize(file.getAbsolutePath());
            size += file.length();
        }
        return size;
    }

    public static String getOneTouchDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + PHONE_DIR_PATH;
    }

    public static String getSendFilePath() {
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + File.separator
                + PHONE_DIR_PATH
                + File.separator + "system" + File.separator);
        if (!file.exists()) {
            boolean f = file.mkdirs();
            LogUtils.d("SNS", "create dir flag" + f);
        }
        boolean can_write = file.canWrite();
        LogUtils.d("ZSZZ", "file is not write" + can_write);
        // try{
        // createFile(file);
        // }catch(Exception e){
        //
        // }
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + PHONE_DIR_PATH + File.separator + "system"
                + File.separator;
    }

    public static String getReceiverFilePath() {
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + File.separator
                + PHONE_DIR_PATH
                + File.separator + "system" + File.separator);
        if (!file.exists()) {
            boolean f = file.mkdirs();
            LogUtils.d("ZSZZ", "create dir flag" + f);
        }
        boolean can_write = file.canWrite();
        LogUtils.d("ZSZZ", "file is not write" + can_write);
        // try{
        // createFile(file);
        // }catch(Exception e){
        //
        // }

        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + PHONE_DIR_PATH + File.separator + "system"
                + File.separator;

    }

    public static String getReceiverPicturePath() {
        File file = new File("/storage/sdcard0" + File.separator
                + PHONE_DIR_PATH + File.separator + "photo" + File.separator);
        if (!file.exists()) {
            file.mkdirs();
        }
        return "/storage/sdcard0" + File.separator + PHONE_DIR_PATH
                + File.separator + "photo" + File.separator;
    }

    public static String getReceiverVideoPath() {
        File file = new File("/storage/sdcard0" + File.separator
                + PHONE_DIR_PATH + File.separator + "video" + File.separator);
        if (!file.exists()) {
            file.mkdirs();
        }
        return "/storage/sdcard0" + File.separator + PHONE_DIR_PATH
                + File.separator + "video" + File.separator;
    }

    public static String getReceiverAudioPath() {
        File file = new File("/storage/sdcard0" + File.separator
                + PHONE_DIR_PATH + File.separator + "audio" + File.separator);
        if (!file.exists()) {
            file.mkdirs();
        }
        return "/storage/sdcard0/" + File.separator + PHONE_DIR_PATH
                + File.separator + "audio" + File.separator;
    }

    public static String getReceiverAppPath() {
        File file = new File("/storage/sdcard0" + File.separator
                + PHONE_DIR_PATH + File.separator + "app" + File.separator);
        if (!file.exists()) {
            file.mkdirs();
        }
        return "/storage/sdcard0" + File.separator + PHONE_DIR_PATH
                + File.separator + "app" + File.separator;
    }

    public static String getReceiverCameraPath() {
        File file = new File("/storage/sdcard0" + File.separator + "DCIM"
                + File.separator + "Camera" + File.separator);
        if (!file.exists()) {
            file.mkdirs();
        }
        return "/storage/sdcard0/" + File.separator + "DCIM" + File.separator
                + "Camera" + File.separator;
    }

    public static File[] getDirFile() {
        try {
            File file = new File(getSendFilePath());
            if (!file.exists()) {
                return new File[0];
            }
            File[] array = file.listFiles();
            return array;
        } catch (Exception e) {

        }
        return new File[0];
    }

    public static String[] getDirFileName() {
        try {
            File[] files = getDirFile();
            selectSqlItem = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                selectSqlItem[i] = files[i].getName();
            }
        } catch (Exception e) {

        }
        return selectSqlItem;

    }

    public static ArrayList<String> getPictureFilePath(Context context) {
        pictureList = getFile(File_Exchange.TYPE_IMAGE, context);
        picturePathList = new ArrayList<String>();
        if (pictureList != null && pictureList.size() > 0) {
            for (File_Exchange image : pictureList) {
                LogUtils.d("ZZZ", "picture is path" + image.getFilePath());
                picturePathList.add(image.getFilePath());
            }
            return picturePathList;
        }
        return new ArrayList<String>();
    }

    public static long getPictureSize() {
        if (pictureList == null || pictureList.size() == 0) {
            return 0;
        }
        long size = 0;
        for (File_Exchange file : pictureList) {
            size += file.getSize();
        }
        return size;
    }

    public static long getVideoSize() {
        if (videoList == null || videoList.size() == 0) {
            return 0;
        }
        long size = 0;
        for (File_Exchange file : videoList) {
            size += file.getSize();
        }
        return size;
    }

    public static long getAudioSize() {
        if (audioList == null || audioList.size() == 0) {
            return 0;
        }
        long size = 0;
        for (File_Exchange file : audioList) {
            size += file.getSize();
        }
        return size;
    }

    public static ArrayList<String> getVideoFilePath(Context context) {
        videoList = getFile(File_Exchange.TYPE_VIDEO, context);
        videoPathList = new ArrayList<String>();
        if (videoList != null && videoList.size() > 0) {
            for (File_Exchange video : videoList) {
                LogUtils.d("ZZZ", "video is path" + video.getFilePath());
                videoPathList.add(video.getFilePath());
            }
            return videoPathList;
        }
        return new ArrayList<String>();
    }

    public static ArrayList<String> getAudioFilePath(Context context) {
        audioList = getFile(File_Exchange.TYPE_AUDIO, context);
        audioPathList = new ArrayList<String>();
        if (audioList != null && audioList.size() > 0) {
            for (File_Exchange audio : audioList) {
                LogUtils.d("ZZZ", "audio is path" + audio.getFilePath());
                audioPathList.add(audio.getFilePath());
            }
            return audioPathList;
        }
        return new ArrayList<String>();
    }

    public static ArrayList<String> getDocumentPath(Context context) {
        ArrayList<File_Exchange> docs = getFile(File_Exchange.TYPE_DOCUMENT, context);
        ArrayList<String> docuPathList = new ArrayList<String>();

        if (docs != null && docs.size() > 0) {
            for (File_Exchange doc : docs) {
                LogUtils.d("ZZZ", "audio is path" + doc.getFilePath());
                docuPathList.add(doc.getFilePath());
            }
            return docuPathList;
        }
        return new ArrayList<String>();
    }

    public static long getDocumentSize() {
        File doc = new File("/sdcard/wifi.zip");
        return doc.length();
    }

    public static ArrayList<String> getSystemPath(Context context) {
        systemList = new ArrayList<String>();
        LogUtils.d("ZZZ", "app is path" + systemList.size());
        try {
            File[] files = getDirFile();
            for (int i = 0; i < files.length; i++) {
                LogUtils.d("ZSZZ", "SysemList is path" + files[i].getAbsolutePath()
                        + "file exist" + files[i].exists());
                systemList.add(files[i].getAbsolutePath());
            }
            return systemList;
        } catch (Exception e) {

        }
        return new ArrayList<String>();
    }

    public static ArrayList<String> getMessagePath(Context context) {
        messageList = new ArrayList<String>();
        LogUtils.d("ZZZ", "messageList is path" + messageList.size());
        try {
            messageList.add(getReceiverFilePath() + "message.zip");
            return messageList;
        } catch (Exception e) {

        }
        return new ArrayList<String>();
    }

    public static ArrayList<String> getContactPath(Context context) {
        contactList = new ArrayList<String>();
        LogUtils.d("ZZZ", "contactList is path" + contactList.size());
        try {
            contactList.add(getReceiverFilePath() + "contacts.zip");
            return contactList;
        } catch (Exception e) {

        }
        return new ArrayList<String>();
    }

    public static ArrayList<String> getCalenderPath(Context context) {
        calenderList = new ArrayList<String>();
        LogUtils.d("ZZZ", "calenderList is path" + calenderList.size());
        try {
            calenderList.add(getReceiverFilePath() + "calender.zip");
            return calenderList;
        } catch (Exception e) {

        }
        return new ArrayList<String>();
    }

    public static ArrayList<String> getCalllogPath(Context context) {
        calllogList = new ArrayList<String>();
        LogUtils.d("ZZZ", "calllogList is path" + calllogList.size());
        try {
            calllogList.add(getReceiverFilePath() + "calllog.zip");
            return calllogList;
        } catch (Exception e) {

        }
        return new ArrayList<String>();
    }

    public static ArrayList<String> getSettingPath(Context context) {
        settingList = new ArrayList<String>();
        LogUtils.d("ZZZ", "settingList is path" + settingList.size());
        try {
            settingList.add(getReceiverFilePath() + "settings.zip");
            return settingList;
        } catch (Exception e) {

        }
        return new ArrayList<String>();
    }

    public static boolean isContactZipExist() {
        try {
            File file = new File(getReceiverFilePath() + "contacts.zip");
            return file.exists();
        } catch (Exception e) {

        }
        return false;
    }

    public static boolean isMessageZipExist() {
        try {
            File file = new File(getReceiverFilePath() + "message.zip");
            return file.exists();
        } catch (Exception e) {

        }
        return false;
    }

    public static boolean isCalendarZipExist() {
        try {
            File file = new File(getReceiverFilePath() + "calender.zip");
            return file.exists();
        } catch (Exception e) {

        }
        return false;
    }

    public static boolean isCalllogZipExist() {
        try {
            File file = new File(getReceiverFilePath() + "calllog.zip");
            return file.exists();
        } catch (Exception e) {

        }
        return false;
    }

    public static boolean isSettingZipExist() {
        try {
            File file = new File(getReceiverFilePath() + "settings.zip");
            return file.exists();
        } catch (Exception e) {

        }
        return false;
    }

    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);
                delFolder(path + "/" + tempList[i]);
                flag = true;
            }
        }
        return flag;
    }

    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath);
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delFile(String file) {
        try {

            File myFilePath = new File(file);
            if (!myFilePath.exists() || myFilePath.isDirectory()) {
                return;
            }
            myFilePath.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(String file) {
        try {
            if(TextUtils.isEmpty(file)) {
                return;
            }
            File myFilePath = new File(file);
            if (!myFilePath.exists()) {
                return;
            }
            if(!myFilePath.isDirectory()) {
                myFilePath.delete();
                return;
            }
            String[] tempList = myFilePath.list();
            if(tempList == null || tempList.length == 0) {
                myFilePath.delete();
            }
            for(String tmpPath : tempList) {
                deleteFile(tmpPath);
            }
            myFilePath.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> countMergeContacts(Context mContext) {
        Set<String> set = new HashSet<String>();
        // SQLiteDatabase
        // mSQLiteDatabase=mContext.openOrCreateDatabase("contacts2",0,null);
        // Cursor
        // cursor=mSQLiteDatabase.rawQuery("select account_name, account_type,display_name from view_raw_contacts group by display_name having count(display_name)>1",
        // null);
        ContentResolver mResolver = mContext.getContentResolver();
        String[] Item = new String[]{"display_name"};

        String[] Items = new String[]{"count(display_name)"};
        Cursor cursor = mResolver.query(ContactsContract.Contacts.CONTENT_URI,
                Item, null, null, null);
        Cursor resultCursor = null;
        // mResolver.q
        if (cursor != null) {
            LogUtils.d("DB", "cursor is sysDataSize" + cursor.getCount() * 1);
            while (cursor.moveToNext()) {
                String DISPLAY_NAME = cursor.getString(cursor
                        .getColumnIndexOrThrow(Contacts.Phones.DISPLAY_NAME));
                // LogUtils.d("DB", "cursor is sysDataSize Name" + DISPLAY_NAME);

                // resultCursor =
                // mResolver.query(ContactsContract.Contacts.CONTENT_URI, Item,
                // "(select count(*) from view_contacts where display_name like '"+DISPLAY_NAME+"')",
                // null, null);
                // LogUtils.d("DB", "cursor is display Name" +
                // DISPLAY_NAME.replaceAll("'", "''"));
                if (DISPLAY_NAME != null) {
                    resultCursor = mResolver.query(
                            ContactsContract.Contacts.CONTENT_URI, Item,
                            "display_name = ?", new String[]{DISPLAY_NAME},
                            null);
                    if (resultCursor != null) {
                        LogUtils.d("DB",
                                "cursor is sysDataSize count---111"
                                        + resultCursor.getCount());
                        if (resultCursor.getCount() > 1) {
                            set.add(DISPLAY_NAME);
                            LogUtils.d("DB", "cursor is sysDataSize ---22222"
                                    + DISPLAY_NAME);
                        }

                    }
                    if (resultCursor != null) {
                        resultCursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }

        }

        if (set == null) {
            return new ArrayList<String>();
        }
        LogUtils.d("DB", "set is sysDataSize " + set.size());
        return new ArrayList<String>(set);
    }


    public static void scanPhotos(String filePath, Context context) {
        try {
            MediaScannerConnection.scanFile(context, new String[]{FilePathUtils.getReceiverCameraPath() + filePath}, null, null);
            LogUtils.d("SCAN", FilePathUtils.getReceiverCameraPath() + filePath);
        } catch (Exception e) {

        }
    }

    public static long getFileSize(int fileType, Context mContext) {

        ArrayList<File_Exchange> fileList = new ArrayList<File_Exchange>();
        long sum = 0;
        Cursor c = null;
        try {
            if (fileType == File_Exchange.TYPE_IMAGE) {
                LogUtils.v(LOG_TAG, "getting image");
                c = mContext.getContentResolver().query(
                        PictureContent.CONTENT_URI,
                        new String[]{"sum(" + PictureContent.COLUMN_SIZE + ")"}, null, null,
                        PictureContent.COLUMN_ID + " ASC");
            } else if (fileType == File_Exchange.TYPE_VIDEO) {
                LogUtils.v(LOG_TAG, "getting video");
                c = mContext.getContentResolver().query(
                        VideoContent.CONTENT_URI,
                        new String[]{"sum(" + VideoContent.COLUMN_SIZE + ")"}, null, null,
                        VideoContent.COLUMN_ID + " ASC");
            } else if (fileType == File_Exchange.TYPE_AUDIO) {
                LogUtils.v(LOG_TAG, "getting audio");
                c = mContext.getContentResolver().query(
                        AudioContent.CONTENT_URI,
                        new String[]{"sum(" + AudioContent.COLUMN_SIZE + ")"},
                        /*AudioContent.COLUMN_IS_MUSIC + "=1"*/null, null,
                        AudioContent.COLUMN_ID + " ASC");
            } else if(fileType == File_Exchange.TYPE_DOCUMENT) {
                LogUtils.v(LOG_TAG, "getting document");
                c = mContext.getContentResolver().query(
                        DocumentContent.CONTENT_URI,
                        new String[]{"sum(" + DocumentContent.COLUMN_SIZE + ")"},
                        DocumentContent.WHERE, null,
                        DocumentContent.COLUMN_ID + " ASC");
            }
            if (c == null) {
                return 0;
            }
            if (c.moveToNext()) {
                sum = c.getLong(0);
            }
        } finally {
            if (null != c && !c.isClosed()) {
                c.close();
                c = null;
            }
        }
        return sum;
    }


    public static ArrayList<File_Exchange> getFile(int fileType, Context mContext) {

        ArrayList<File_Exchange> fileList = new ArrayList<File_Exchange>();
        Cursor c = null;

        try {

            if (fileType == File_Exchange.TYPE_IMAGE) {
                LogUtils.v(LOG_TAG, "getting image");
                c = mContext.getContentResolver().query(
                        PictureContent.CONTENT_URI,
                        new String[]{PictureContent.COLUMN_ID, PictureContent.COLUMN_PATH, PictureContent.COLUMN_SIZE}, null, null,
                        PictureContent.COLUMN_ID + " ASC");
            } else if (fileType == File_Exchange.TYPE_VIDEO) {
                LogUtils.v(LOG_TAG, "getting video");
                c = mContext.getContentResolver().query(
                        VideoContent.CONTENT_URI,
                        new String[]{VideoContent.COLUMN_ID, VideoContent.COLUMN_PATH, VideoContent.COLUMN_SIZE}, null, null,
                        VideoContent.COLUMN_ID + " ASC");
            } else if (fileType == File_Exchange.TYPE_AUDIO) {
                LogUtils.v(LOG_TAG, "getting audio");
                c = mContext.getContentResolver().query(
                        AudioContent.CONTENT_URI,
                        new String[]{AudioContent.COLUMN_ID, AudioContent.COLUMN_PATH, AudioContent.COLUMN_SIZE},
                        /*AudioContent.COLUMN_IS_MUSIC + "=1"*/null, null,
                        AudioContent.COLUMN_ID + " ASC");
            } else if(fileType == File_Exchange.TYPE_DOCUMENT) {
                LogUtils.v(LOG_TAG, "getting document");
                c = mContext.getContentResolver().query(
                        DocumentContent.CONTENT_URI,
                        new String[]{DocumentContent.COLUMN_ID, DocumentContent.COLUMN_PATH, DocumentContent.COLUMN_SIZE},
                        DocumentContent.WHERE, null,
                        DocumentContent.COLUMN_ID + " ASC");
            }

            if (c == null) {
                return fileList;
            }

            while (c.moveToNext()) {
                long id = c.getLong(0);
                String filePath = c.getString(1);
                long size = c.getLong(2);

                File_Exchange file = new File_Exchange();
                file.setId(id);
                file.setFilePath(filePath);
                file.setSize(size);
                file.setType(fileType);

                fileList.add(file);
            }


        } finally {
            if (null != c && !c.isClosed()) {
                c.close();
                c = null;
            }
        }

        return fileList;
    }

    public static int getFileCount(int fileType, Context mContext) {

        int count = 0;
        Cursor c = null;
        try {
            if (fileType == File_Exchange.TYPE_IMAGE) {
                LogUtils.v(LOG_TAG, "getting image");
                c = mContext.getContentResolver().query(
                        PictureContent.CONTENT_URI,
                        new String[]{"count(" + PictureContent.COLUMN_SIZE + ")"}, null, null,
                        PictureContent.COLUMN_ID + " ASC");
            } else if (fileType == File_Exchange.TYPE_VIDEO) {
                LogUtils.v(LOG_TAG, "getting video");
                c = mContext.getContentResolver().query(
                        VideoContent.CONTENT_URI,
                        new String[]{"count(" + VideoContent.COLUMN_SIZE + ")"}, null, null,
                        VideoContent.COLUMN_ID + " ASC");
            } else if (fileType == File_Exchange.TYPE_AUDIO) {
                LogUtils.v(LOG_TAG, "getting audio");
                c = mContext.getContentResolver().query(
                        AudioContent.CONTENT_URI,
                        new String[]{"count(" + AudioContent.COLUMN_SIZE + ")"},
                        /*AudioContent.COLUMN_IS_MUSIC + "=1"*/null, null,
                        AudioContent.COLUMN_ID + " ASC");
            } else if(fileType == File_Exchange.TYPE_DOCUMENT) {
                LogUtils.v(LOG_TAG, "getting document");
                c = mContext.getContentResolver().query(
                        DocumentContent.CONTENT_URI,
                        new String[]{"count(" + DocumentContent.COLUMN_SIZE + ")"},
                        DocumentContent.WHERE, null,
                        DocumentContent.COLUMN_ID + " ASC");
            }
            if (c == null) {
                return 0;
            }
            if (c.moveToNext()) {
                count = c.getInt(0);
            }
        } finally {
            if (null != c && !c.isClosed()) {
                c.close();
                c = null;
            }
        }
        return count;
    }

    public static ArrayList<String> getFilelistFromPath(String path) {
        ArrayList<String> pathList = new ArrayList<String>();
        File file = new File(path);
        if(!file.exists()) {
            return pathList;
        }
        if(!file.isDirectory()) {
            pathList.add(file.getAbsolutePath());
            return pathList;
        }
        File[] files = file.listFiles();
        for(File file1 : files) {
            if(file1.isDirectory()) {
                pathList.addAll(getFilelistFromPath(file1.getAbsolutePath()));
            } else {
                pathList.add(file1.getAbsolutePath());
            }
        }

        return pathList;
    }

    public static long getDirSizeFromPath(String path) {
        long size = 0;
        File file = new File(path);
        if(!file.exists()) {
            return 0;
        }
        if(!file.isDirectory()) {
            return file.length();
        }
        File[] files = file.listFiles();
        for(File file1 : files) {
            if(file1.isDirectory()) {
                size += getDirSizeFromPath(file1.getAbsolutePath());
            } else {
                size += file1.length();
            }
        }
        return size;
    }

    public void scanPathforMediaStore(List<String> scanPaths, Context context) {
        if (context != null && !scanPaths.isEmpty()) {
            String[] paths = new String[scanPaths.size()];
            scanPaths.toArray(paths);
            MediaScannerConnection.scanFile(context, paths, null, null);
        }
    }

    /**
     * delete contentprovider data by file path
     * @param path file path
     */
    private void deleContentProviderData(String path, Context context){
        int flagByCategory = -1;
        Uri uri = MediaStore.Files.getContentUri("external");
        int flagByFiles = context.getContentResolver().delete(uri, MediaStore.Files.FileColumns.DATA + " = \"" + path + "\"", null);
//        if (CategoryManager.CATEGORY_PICTURES == CategoryManager.mCurrentCagegory) {
//            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//            flagByCategory = context.getContentResolver().delete(uri, MediaStore.Images.ImageColumns.DATA + " = \"" + path + "\"", null);
//        } else if (CategoryManager.CATEGORY_VEDIOS == CategoryManager.mCurrentCagegory) {
//            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//            flagByCategory = context.getContentResolver().delete(uri, MediaStore.Video.VideoColumns.DATA + " = \"" + path + "\"", null);
//        } else if (CategoryManager.CATEGORY_MUSIC == CategoryManager.mCurrentCagegory) {
//            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//            flagByCategory = context.getContentResolver().delete(uri, MediaStore.Audio.AudioColumns.DATA + " = \"" + path + "\"", null);
//        } else if (CategoryManager.CATEGORY_DOWNLOAD == CategoryManager.mCurrentCagegory) {
//            uri = Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI;
//            flagByCategory = context.getContentResolver().delete(uri, Downloads.Impl._DATA + " = \"" + path + "\"", null);
//        }
        LogUtils.i(this.getClass().getName(), "flagByCategory:" + flagByCategory + ",path:" + path + ",flagByFiles:" + flagByFiles);
    }

}
