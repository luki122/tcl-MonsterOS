package cn.tcl.music.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.widget.TextView;


import java.lang.reflect.Field;
import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.model.MediaInfo;

public class MixUtil {
    private static final String TAG = MixUtil.class.getSimpleName();


    /**
     * get song's name with no suffix
     *
     * @param songName
     * @return
     */
    public static String getSongNameWithNoSuffix(String songName) {
        if (!TextUtils.isEmpty(songName)) {
            int dotIndex = songName.lastIndexOf(".");//[BUGFIX]-add by Peng.Tian-nb,2016-06-29,defect 2397974
            if (dotIndex > 0) {
                songName = songName.substring(0, dotIndex).trim();
            }
        }
        return songName;
    }


    /**
     * get artist name by audioId
     *
     * @param context
     * @param audioId audioId
     * @return
     */
    public static String getArtistName(Context context, String audioId, String artist) {
        if (context == null) { // add for 2574715
            return null;
        }
        String strArtist = context.getString(R.string.unknown);
        Cursor cursor = null;
        try {
            ContentResolver cr = context.getContentResolver();
            cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                    MediaStore.Audio.Media._ID + " = ?", new String[]{audioId}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToNext();
                strArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            } else {
                if (!TextUtils.isEmpty(artist)) {
                    strArtist = artist;
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return strArtist;
    }

    public static String generateLyricFileName(String songName, String artist) {
        StringBuilder builder = new StringBuilder();
        return builder.append(getSongNameWithNoSuffix(songName) + "_" + artist).toString();
    }
}
