/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import cn.tcl.note.R;
import cn.tcl.note.data.CommonData;
import cn.tcl.note.data.NoteAudioData;
import cn.tcl.note.data.NotePicData;
import cn.tcl.note.db.DBData;
import cn.tcl.note.ui.DialogHelper;

/**
 * the class provide many methods about file
 */
public class FileUtils {
    public final static String APP_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Note/";
    public final static String IMAGE_NAME = "Image/";
    private final static String IMAGE_FILE_BEFORE = "IMG";
    private final static String IMAGE_FILE_AFTER = ".jpg";
    public final static String AUDIO_NAME = "Audio/";
    public static String SAVE_NAME = "Save/";
    public final static String AUDIO_FILE_BEFORE = "Recording";
    public final static String AUDIO_FILE_AFTER = ".wav";
    private static String TAG = FileUtils.class.getSimpleName();
    public static LinkedList<String> mCopyingAudio = new LinkedList<>();
    //when edit activity restart,whether adatper need refresh data
    public static boolean iSRefresh = false;

    public static int KEY_50M = -1;
    public static int KEY_100M = -2;
    private static boolean IS_SHOW_50M = false;
    private static boolean IS_SHOW_100M = false;
    private static String mShareName = "share.jpg";
    private static Uri mShareUri;
    private static Semaphore mShareUriSemaphore = new Semaphore(0);

    //create root dir,success will return true.
    private static boolean createFile(String patch) {

        File rootFile = new File(APP_NAME + patch);

        if (rootFile.exists()) {
            return true;
        } else {
            NoteLog.d(TAG, "crate file:" + rootFile.getAbsolutePath());
            String state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                NoteLog.e(TAG, "external Storage State is " + state + ",can't write data.");
                return false;
            }
            if (rootFile.mkdirs()) {
                NoteLog.d(TAG, "mkdir successful");
                return true;
            } else {
                NoteLog.e(TAG, "mkdir fail");
                return false;
            }
        }
    }

    /**
     * create a img name using date
     *
     * @return img name
     */
    public static String getPicName() {
        if (!createFile(IMAGE_NAME)) {
            return null;
        }
        String picName = IMAGE_FILE_BEFORE + getCurrDate();
        String tempFile = new String(picName);
        // if had same name file,rename
        for (int i = 1; true; i++) {
            File imgFile = new File(getPicWholePath(picName + IMAGE_FILE_AFTER));
            if (imgFile.exists()) {
                NoteLog.d(TAG, "img file " + imgFile + " had exist,rename");
                picName = tempFile + "(" + i + ")";
            } else {
                break;
            }
        }
        return picName + IMAGE_FILE_AFTER;
    }

    /**
     * return a img name whole path
     *
     * @param name img name
     * @return img whole path
     */
    public static String getPicWholePath(String name) {
        if (createFile(IMAGE_NAME)) {
            return APP_NAME + IMAGE_NAME + name;
        } else {
            return null;
        }
    }

    public static boolean delImgFile(String fileName) {
        return delFile(getPicWholePath(fileName));
    }

    public static String getAudioName() {
        return getAudioName(null);
    }

    public static String getAudioName(String audioName) {
        if (!createFile(AUDIO_NAME)) {
            return null;
        }

        if (audioName != null) {
            audioName = getFileNameNoSuffixes(audioName);
        } else {
            audioName = AUDIO_FILE_BEFORE + getCurrDate();
        }
        String tempFile = new String(audioName);
        // if had same name file,rename
        for (int i = 1; true; i++) {
            NoteLog.d(TAG, "check rename");
            File audioFile = new File(getAudioWholePath(audioName + AUDIO_FILE_AFTER));
            if (audioFile.exists()) {
                NoteLog.d(TAG, "img file " + audioName + " had exist,rename");
                audioName = tempFile + "(" + i + ")";
            } else {
                break;
            }
        }
        return audioName + AUDIO_FILE_AFTER;
    }

    public static String getFileNameNoSuffixes(String fileName) {
        return fileName.substring(0, fileName.length() - AUDIO_FILE_AFTER.length());
    }

    public static String getAudioWholePath(String name) {
        if (createFile(AUDIO_NAME)) {
            return APP_NAME + AUDIO_NAME + name;
        } else {
            return null;
        }
    }

    public static boolean delAudioFile(String fileName) {
        return delFile(getAudioWholePath(fileName));
    }

    private static boolean delFile(String wholePath) {
        File file = new File(wholePath);
        if (file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }

    /**
     * cope a uri file to a file
     *
     * @param inputStream uri inputstream
     * @param destFile    target file
     * @return -2 mean not available space;-1 mean fail;0 means success
     */
    public static int copyToFile(final InputStream inputStream, final File destFile) {
        NoteLog.d(TAG, "start copy file to " + destFile.getAbsolutePath());
        try {
            if (!isHasSpace(inputStream)) {
                return -2;
            }
            //if copy file is a audio file,then put it to mCopyingAudio.
            //After copy,then remove it.So when prepare to play a audio,please judge.
            final String audioNmae = destFile.getName();
            addAudioFile(audioNmae);
            final FileOutputStream out = new FileOutputStream(destFile);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) >= 0) {
                            out.write(buffer, 0, bytesRead);
                        }
                        removeAudioFile(audioNmae);
                        NoteLog.d(TAG, "copy successful");
                    } catch (IOException e) {
                        NoteLog.e(TAG, "copy fail", e);
                    } finally {
                        try {
                            out.flush();
                            out.getFD().sync();
                            out.close();
                        } catch (IOException e) {
                            NoteLog.e(TAG, "copy fail", e);
                        }
                    }

                }
            }).start();

            return 0;
        } catch (FileNotFoundException e) {
            NoteLog.e(TAG, "file don't find", e);
            return -1;
        } catch (IOException e) {
            NoteLog.e(TAG, "copy fail", e);
            return -1;
        }
    }

    //return true if phone have space,or return false
    private static boolean isHasSpace(InputStream inputStream) throws IOException {
        int fileSize = inputStream.available();
        long allAvailableSize = Environment.getDataDirectory().getUsableSpace();
        if (allAvailableSize > fileSize) {
            return true;
        }

        return false;
    }

    public static void init50M100M() {
        IS_SHOW_100M = false;
        IS_SHOW_50M = false;
    }

    /**
     * @return return KEY_50M means space is less 50M,KEY_100M means space is less 100M.
     */
    public static double getSdAvailableSize() {
        double allAvailableSize = Environment.getDataDirectory().getUsableSpace() * 1.0 / 1000 / 1000;
        return allAvailableSize;
    }

    private static String getCurrDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(new Date());
    }

    private static void addAudioFile(String name) {
        if (name.endsWith(AUDIO_FILE_AFTER)) {
            mCopyingAudio.add(name);
        }
    }

    private static void removeAudioFile(String name) {
        if (name.endsWith(AUDIO_FILE_AFTER)) {
            mCopyingAudio.remove(name);
        }
    }

    /**
     * judge a audio is in mCopyingAudio.
     *
     * @param name
     * @return if return true,then can play.if return false,then don not play,need wait
     */
    public static boolean isCanPlay(String name) {
        return !mCopyingAudio.contains(name);
    }

    public static boolean writeImgToName(Bitmap bitmap, String fileName) {
        iSRefresh = true;
        File imgFile = new File(getPicWholePath(fileName));
        return writeImgToWholeFile(bitmap, imgFile);
    }

    private static boolean writeImgToWholeFile(Bitmap bitmap, File imgFile) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            NoteLog.d(TAG, "write img to file successful");
            return true;
        } catch (FileNotFoundException e) {
            NoteLog.e(TAG, "file not found", e);
        } catch (IOException e) {
            NoteLog.e(TAG, "flush fail", e);
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                NoteLog.e(TAG, "close fail", e);
            }
        }
        return false;
    }

    public static void writeShareImg(final Bitmap bitmap, Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mShareUri != null) {
                    context.getContentResolver().delete(mShareUri, null, null);
                }
                writeImgToName(bitmap, mShareName);
                getShareUri(context);
            }
        }).start();

    }

    public static void getShareUri(Context context) {
        String path = getPicWholePath(mShareName);
        NoteLog.d(TAG, "share img path=" + path);

        MediaScannerConnection.scanFile(context, new String[]{path}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {

            }

            @Override
            public void onScanCompleted(String s, Uri uri) {
                DialogHelper.disProgressDialog();
                NoteLog.d(TAG, "scan completed uri=" + uri);
                mShareUri = uri;
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                context.startActivity(Intent.createChooser(intent, ""));
            }
        });
    }

    public static boolean isExits(String path) {
        File file = new File(path);
        return file.exists();
    }

    //delete more data
    public static void deleteMoreNote(Context context, ArrayList<Long> ids) {
        NoteLog.d(TAG, "start more delete");
        for (Long id : ids) {
            deleteNote(context, id);
        }
        NoteLog.d(TAG, "end more delete");
    }

    //delete one database and file
    public static void deleteNote(Context context, Long id) {
        NoteLog.d(TAG, "start delete " + id);
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(DBData.TABLE_URI, new String[]{DBData.COLUMN_XML}, DBData.COLUMN_ID + "=?", new String[]{"" + id}, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String xmlString = cursor.getString(cursor.getColumnIndex(DBData.COLUMN_XML));
            cursor.close();
            contentResolver.delete(DBData.TABLE_URI, DBData.COLUMN_ID + "=?", new String[]{"" + id});
            LinkedList<CommonData> result = XmlPrase.prase(xmlString);
            for (CommonData data : result) {
                String path;
                if (data instanceof NoteAudioData) {
                    path = getAudioWholePath(((NoteAudioData) data).getFileName());
                } else if (data instanceof NotePicData) {
                    path = getPicWholePath(((NotePicData) data).getFileName());
                } else {
                    continue;
                }
                new File(path).delete();
            }
        }
        NoteLog.d(TAG, "end delete " + id);
    }

    public static String getWholeSavePath(Context context, String name) {
        SAVE_NAME = context.getResources().getString(R.string.save_img_name) + "/";
        if (createFile(SAVE_NAME)) {
            return APP_NAME + SAVE_NAME + name;
        } else {
            return null;
        }
    }

    public static boolean saveShareImg(Context context, Bitmap bitmap) {
        String saveName = getSaveImgName(context);
        String savePath = getWholeSavePath(context, saveName);
        File saveFile = new File(savePath);
        if (writeImgToWholeFile(bitmap, saveFile)) {
            MediaScannerConnection.scanFile(context, new String[]{savePath}, null, null);
            return true;
        } else {
            return false;
        }
    }

    private static String getSaveImgName(Context context) {
        String imgName = context.getResources().getString(R.string.app_name) + getCurrDate() + IMAGE_FILE_AFTER;

        return imgName;
    }
}
