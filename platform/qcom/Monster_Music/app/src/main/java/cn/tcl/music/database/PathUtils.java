package cn.tcl.music.database;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

import cn.tcl.music.R;
import cn.tcl.music.model.MediaInfo;

public class PathUtils {
    public static String replaceFolderPath ( String oldPath){
        if (oldPath != null && oldPath.lastIndexOf("/") != -1) {
            String newPath = oldPath.substring(0, oldPath.lastIndexOf("/"));
            return newPath;
        }
        return oldPath;
    }

    public static String replaceFolderName ( String oldPath){
        if (oldPath != null && oldPath.lastIndexOf("/") != -1) {
            String newPath = oldPath.substring(0, oldPath.lastIndexOf("/") );
            if (newPath != null && newPath.lastIndexOf("/") != -1) {
                String folderName = newPath.substring(newPath.lastIndexOf("/")+1);
                return folderName;
            }
        }
        return oldPath;

    }


    /**
     * get the extention name of the file with the file
     * @param path the file abstract path
     * @return extention name
     */
    public static String getSuffix(String path) {
        File f =new File(path);
        return f.getName().substring(f.getName().lastIndexOf(".") + 1);
    }

    /**
     * share local media
     *
     * @param mediaInfo
     * @param context
     */
    public static void shareLocalMedia(MediaInfo mediaInfo, Context context) {
        File file = new File(mediaInfo.filePath);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        intent.setType("*/*");
        context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.share_to)));
    }

    /**
     * online share
     * @param context
     * @param url the url wants to share
     */
    public static void shareOnline(Context context,String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.share_to)));
    }

}
