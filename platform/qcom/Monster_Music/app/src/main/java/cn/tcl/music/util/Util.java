package cn.tcl.music.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import com.xiami.sdk.utils.Encryptor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.download.mie.downloader.DownloadTask;
import cn.tcl.music.R;
import cn.tcl.music.app.MusicApplication;

public class Util {
    public static int dip2px(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * 时间戳转换
     */
    public static String timestamp2DateString(String timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(Long.parseLong(timestamp) * 1000));
    }

    /**
     * 替换xiami名称
     */
    public static String replaceStringTags(Context context, String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }

        String nameStr = name;
        try {
            nameStr = name.replace(context.getString(R.string.xiami), "").replace(context.getString(R.string.xiami2), "");
        } catch (Exception e) {
            nameStr = name;
        }
        return nameStr;
    }

    /**
     * 生成唯一music transion id
     */
    public static int getTransionId() {
        String timeStr = String.valueOf(System.currentTimeMillis());
        return timeStr.hashCode();
    }

    /**
     * 生成media url
     */
    public static String getMediaUrl(String str) {
        if (!TextUtils.isEmpty(str)) {
            return Encryptor.decryptUrl(str);
        }

        return "";
    }

    public static void addExtraMp3Info(DownloadTask task) {
        try {
            Mp3File mp3file = new Mp3File(task.getTempFilePath());
            ID3v2 id3v2Tag;

            if (mp3file.hasId3v2Tag()) {
                id3v2Tag = mp3file.getId3v2Tag();
            } else {
                id3v2Tag = new ID3v24Tag();
                mp3file.setId3v2Tag(id3v2Tag);
            }

            id3v2Tag.setArtist(task.artist_name);
            id3v2Tag.setAlbum(task.album_name);
            id3v2Tag.setArtistUrl(task.artist_logo);
            mp3file.save(task.getFinalFilePath());

            File file = new File(task.getTempFilePath());
            if (file.exists()) {
                file.delete();
                file = null;
            }
        } catch (Exception e) {
            LogUtil.i("--DownloadedManagerFragment--", e.toString());
        }

    }

    /**
     * 检测网络是否可用
     *
     * @return
     */
    public static boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) MusicApplication.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    /**
     * 获取当前网络类型
     *
     * @return 0：没有网络   1：WIFI网络   2：移动网络
     */

    public static final int NETTYPE_NONE = 0x00;
    public static final int NETTYPE_WIFI = 0x01;
    public static final int NETTYPE_MOBILE = 0x02;

    public static int getNetworkType() {
        int netType = NETTYPE_NONE;
        ConnectivityManager connectivityManager = (ConnectivityManager) MusicApplication.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!StringUtils.isEmpty(extraInfo)) {
                netType = NETTYPE_MOBILE;
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = NETTYPE_WIFI;
        }
        return netType;
    }

    /**
     * translate array to string with specific separator
     *
     * @param array     the source data
     * @param separator the specific separator
     * @return [a, b, c] with separator $ --> a$b$c
     */
    public static String translateArrayToString(ArrayList<Object> array, String separator) {
        String result = "";
        for (int i = 0; i < array.size(); i++) {
            if (i == array.size() - 1) {
                result += String.valueOf(array.get(i));
            } else {
                result += String.valueOf(array.get(i)) + separator;
            }
        }
        return result;
    }
}
