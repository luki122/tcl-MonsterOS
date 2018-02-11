package cn.tcl.music.util;


import android.os.Environment;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import cn.download.mie.base.DirType;
import cn.download.mie.base.util.ServiceContext;
import cn.tcl.music.app.Music5Context;

/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/11/27 18:30
 * @copyright TCL-MIE
 */
public class FileManager {
    private static final String TAG = FileManager.class.getCanonicalName();
    private FileManager(){

    }

    public static String getAppCachePath(DirType dirType){
        Music5Context music5Context = (Music5Context) ServiceContext.getInstance();
        return Music5Context.getDirectoryPath(dirType);
    }

    /**
     * 获取歌曲和歌词存放的目录Path
     * @return
     */
    public static String getSongPath(){
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(rootPath).append(File.separator);
        stringBuilder.append("Music5").append(File.separator);
        stringBuilder.append("song");

        return stringBuilder.toString();
    }

    /**
     * 获取歌曲和歌词存放的目录Path
     * @return
     */
    public static String getLiricPath(){
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(rootPath).append(File.separator);
        stringBuilder.append("Music5").append(File.separator);
        stringBuilder.append("lyric").append(File.separator);
        stringBuilder.append("null");

        return stringBuilder.toString();
    }


    public static boolean isLrcFileExisted(String songId){
        String lrcFilePath = FileManager.getLiricPath()+  File.separator  + songId+ MusicUtil.getLiricSuffix();
        File saveFile=new File(lrcFilePath);
        LogUtil.d(TAG, "lrcFilePath = " + lrcFilePath);
        return saveFile.exists();

    }

    public static boolean isLrcFileExisted2(String songName, String artist){
        String lrcFilePath = FileManager.getLiricPath()+  File.separator  + MixUtil.generateLyricFileName(songName, artist)+ MusicUtil.getLiricSuffix();
        File saveFile=new File(lrcFilePath);
        LogUtil.d(TAG, "lrcFilePath2 = " + lrcFilePath);
        return saveFile.exists();


    }
    public static String getLyricParhByTitleAndArtist(String songName, String artist){
        String lrcFilePath = FileManager.getLiricPath()+  File.separator  + MixUtil.generateLyricFileName(songName, artist)+ MusicUtil.getLiricSuffix();
//        File saveFile=new File(lrcFilePath);
        LogUtil.d(TAG, "lrcFilePath = " + lrcFilePath);
        return lrcFilePath;
    }


    public static boolean isLrcFileExistedv2(String songName, String artist ){
        String lrcFilePath = FileManager.getLiricPath()+  File.separator  + MixUtil.generateLyricFileName(songName, artist)+ MusicUtil.getLiricSuffix();
        File saveFile=new File(lrcFilePath);
        LogUtil.d(TAG, "lrcFilePath = " + lrcFilePath);
        return saveFile.exists();
    }


    public static String readLrcFileBySongId(String songId){
        String lrcFilePath = FileManager.getLiricPath()+  File.separator  + songId+ MusicUtil.getLiricSuffix();
        File saveFile=new File(lrcFilePath);
        LogUtil.d(TAG, "lrcFilePath existed = " + saveFile.exists());
        if (saveFile.exists()){
            String result = readFileToString(saveFile);
            return result;
        }
        return null;
    }


    public static String readLrcFileBySongTitleAndArtist(String songName, String artist ){
        String lrcFilePath = FileManager.getLiricPath()+  File.separator  + MixUtil.generateLyricFileName(songName, artist)+ MusicUtil.getLiricSuffix();
        File saveFile=new File(lrcFilePath);
        LogUtil.d(TAG, "lrcFilePath existed = " + saveFile.exists());
        if (saveFile.exists()){
            String result = readFileToString(saveFile);
            return result;
        }
        return null;
    }

    public static String readFileToString(File file) {
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String readLine = "";
            String result="";
            while ((readLine = reader.readLine()) != null) {
                if(readLine.trim().equals("")) {
                    continue;
                }
                result += readLine.trim() + "\r\n";
            }
            reader.close();
            return result;
        } catch (Exception e) {
            LogUtil.e(TAG, "Exception e: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
