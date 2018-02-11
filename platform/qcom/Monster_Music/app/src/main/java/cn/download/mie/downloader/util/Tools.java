package cn.download.mie.downloader.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import cn.tcl.music.app.Music5Context;
import cn.tcl.music.util.FileManager;
import cn.download.mie.base.DirType;
import cn.download.mie.base.util.ServiceContext;
import cn.tcl.music.util.LogUtil;

import org.apache.http.HttpResponse;

import java.io.File;


public class Tools {

    public static boolean checkPermission(String permission, Context context) {

        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @TargetApi(11)
    public static boolean canWriteExtraStorage(Context context) {
        boolean hasPermission = false;
        if (Build.VERSION.SDK_INT < 19 && Build.VERSION.SDK_INT > 3) {
            hasPermission = checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", context);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //19以上可以直接调用
            hasPermission = true;
        }

        boolean sdcard = context.getExternalFilesDir(null) != null;
        return hasPermission && sdcard;
    }

    public static String getCommonDownloadPath1(Context context) {
        if (canWriteExtraStorage(context)) {
            return  FileManager.getSongPath();
        } else {
            return context.getFilesDir().getAbsolutePath();
        }
    }

    public static String getCommonDownloadPathSong(Context context) {
        if (canWriteExtraStorage(context)) {
            return FileManager.getSongPath();
        }else
            return null ;
    }

    public static String getCommonDownloadPath(Context context) {
        if( canWriteExtraStorage(context)) {
            File file = Environment.getExternalStorageDirectory();
            if( file != null) {
                LogUtil.d("FilesDir","file.getAbsolutePath()="+file.getAbsolutePath());

                return file.getAbsolutePath()+"/Music"+"/download";
            }
            else{
                LogUtil.d("FilesDir",context.getFilesDir().getPath());

                return context.getFilesDir().getPath();
            }
        }
        else {
            LogUtil.d("FilesDir",context.getFilesDir().getPath());
            return context.getFilesDir().getPath();
        }
    }

    public static String getCommonDownloadPathLyric(Context context) {
        if (canWriteExtraStorage(context)) {
            Music5Context music5Context = (Music5Context) ServiceContext.getInstance();
            String path = Music5Context.getDirectoryPath(DirType.lyric);
            return path;
        }else
            return null ;
    }

    public static void safeClose(HttpResponse response) {
        if (response != null) {
            try {
                response.getEntity().getContent().close();
            } catch (Exception e) {
            }
        }
    }
}
