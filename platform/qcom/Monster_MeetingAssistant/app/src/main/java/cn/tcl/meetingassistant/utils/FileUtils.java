/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-10.
 * File path config
 * |-MEETING_DIR
 * |    |-MEETING_FILE_PATH
 * |    |-IMAGE_FILE_PATH
 * |            |-TEMP_IMAGE_NAME
 * |    |-VOICE_FILE_PATH
 * |    |-PDF_FILE_PATH
 * |    |-SAVE_IMAGE_FILE_PATH
 *
 *
 */
public class FileUtils {

    public static int KEY_50M = -1;
    public static int KEY_100M = -2;

    private static boolean IS_SHOW_50M = false;
    private static boolean IS_SHOW_100M = false;

    public static final String TAG = FileUtils.class.getSimpleName();

    // the directory name of app
    public static final String MEETING_DIR = "MeetingAssistant";

    // the directory path of app
    public static final String MEETING_FILE_PATH = Environment.getExternalStorageDirectory().
            toString() + File.separator + MEETING_DIR + File.separator;

    // the directory path of image files in import point
    public static final String IMAGE_FILE_PATH = MEETING_FILE_PATH + "images" +
            File.separator;

    // the directory path of voice files
    public static final String VOICE_FILE_PATH = MEETING_FILE_PATH + "voices" +
            File.separator;

    // the directory path of pdf files
    public static final String PDF_FILE_PATH = MEETING_FILE_PATH + "pdf" +
            File.separator;

    // the directory path of saved meeting image
    public static final String SAVE_IMAGE_FILE_PATH = MEETING_FILE_PATH + "savePic" +
            File.separator;

    public static final String TEMP_IMAGE_NAME = ".temp.jpg";



    /**
     * judge if the SD card exist
     */
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }



    public static String getImageDirPath(){

        File dir = new File(IMAGE_FILE_PATH);
        if(!dir.exists()){
            dir.mkdirs();
            MeetingLog.i(TAG,"build "+dir.getPath());
        }

        return IMAGE_FILE_PATH;
    }


    public static File[] getImageFilesByTime(String dirPath){
        File fileDir = new File(dirPath);
        if (fileDir.exists() && fileDir.isDirectory()) {
            File[] mImagePaths;
            mImagePaths = fileDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    if (s.endsWith(".jpg")) {
                        return true;
                    }
                    return false;
                }
            });
            if(null != mImagePaths && mImagePaths.length >=1){
                Arrays.sort(mImagePaths,new ComparatorByLastModified());
                return mImagePaths;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }


    static class ComparatorByLastModified implements Comparator<File>
    {
        public int compare(File f1, File f2) {
            long diff = f1.lastModified() - f2.lastModified();
            if(diff > 0)
                return 1;
            else if(diff == 0)
                return 0;
            else
                return -1;
        }
        public boolean equals(Object obj){
            return true;
        }
    }


    public static String getTempImagePath(){
        return getImageDirPath() + TEMP_IMAGE_NAME;
    }


    public static String getVoiceFilePath(String fileName) {
        File meetingDir = new File(MEETING_FILE_PATH);
        if(!meetingDir.exists()){
            meetingDir.mkdir();
            MeetingLog.i(TAG,"build "+meetingDir.getPath());
        }

        File dir = new File(VOICE_FILE_PATH);
        if(!dir.exists()){
            dir.mkdir();
            MeetingLog.i(TAG,"build "+dir.getPath());
        }
        return VOICE_FILE_PATH + fileName;
    }



    public static String getUnusedFileName(String fileName, String fileSuffix, Context context){
        File dir = new File(FileUtils.MEETING_FILE_PATH +context.getResources().getString(R.string.export_meeting));
        if(!dir.exists()){
            dir.mkdirs();
        }
        File file = new File(FileUtils.MEETING_FILE_PATH +context.getResources().getString(R.string.export_meeting) + File.separator + fileName + "." + fileSuffix);
        if(!file.exists())
            return file.getAbsolutePath();
        // if had same name file,rename
        for (int i = 1; true; i++) {
            MeetingLog.d(TAG, "check rename");
            file = new File(FileUtils.MEETING_FILE_PATH +context.getResources().getString(R.string.export_meeting)+File.separator+ fileName + "(" + i + ")." + fileSuffix);
            if (file.exists()) {
                MeetingLog.d(TAG, file.getName() + "is exists");
            } else {
                break;
            }
        }
        return file.getAbsolutePath();
    }


    public static boolean deleteDir(File file){
        if(file.exists() && file.isDirectory()){
            File[] files = file.listFiles();
            for(File file1 : files){
                deleteDir(file1);
            }
            return file.delete();
        }else if(file.exists() && file.isFile()){
            return file.delete();
        }else {
            return true;
        }
    }



    /**
     * cope a uri file to a file
     *
     * @param inputStream uri inputstream
     * @param destFile    target file
     * @return -2 mean not available space;-1 mean fail;0 means success
     */
    public static int copyToFile(final InputStream inputStream, final File destFile) {
        MeetingLog.d(TAG, "start copy file to " + destFile.getAbsolutePath());
        try {
            if (!isHasSpace(inputStream)) {
                return -2;
            }
            //if copy file is a audio file,then put it to mCopyingAudio.
            //After copy,then remove it.So when prepare to play a audio,please judge.
            final FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
                MeetingLog.d(TAG, "copy successful");
            } catch (IOException e) {
                MeetingLog.e(TAG, "copy fail", e);
            } finally {
                try {
                    out.flush();
                    out.getFD().sync();
                    out.close();
                } catch (IOException e) {
                    MeetingLog.e(TAG, "copy fail", e);
                }
            }
            return 0;
        } catch (FileNotFoundException e) {
            MeetingLog.e(TAG, "file don't find", e);
            return -1;
        } catch (IOException e) {
            MeetingLog.e(TAG, "copy fail", e);
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

    /**
     * @return return KEY_50M means space is less 50M,KEY_100M means space is less 100M.
     */
    public static double getSdAvailableSize() {
        double allAvailableSize = Environment.getDataDirectory().getUsableSpace() * 1.0 / 1000 / 1000;
        return allAvailableSize;
    }

    public static void init50M100M() {
        IS_SHOW_100M = false;
        IS_SHOW_50M = false;
    }

    public static long getAudioDura(String file){
        MediaPlayer player = new MediaPlayer();
        try {
            player.reset();
            player.setDataSource(file);
            player.prepare();
            return player.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            player.release();
        }
        return -1;
    }

}
