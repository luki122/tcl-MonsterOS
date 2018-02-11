package cn.tcl.music.media;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.database.PlaylistManager;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.util.LogUtil;

public class MediaPlaylist {

    private static final String TAG = PlaylistManager.class.getSimpleName();
    protected Uri mPlaylistUri;
    protected Context mContext;
    protected int mNumTracks;

    public MediaPlaylist(Context context, Uri uri) {
        mContext = context.getApplicationContext();
        mPlaylistUri = uri;
        updateNumTracksFromDB();
    }

    public void updateNumTracksFromDB() {
        mNumTracks = PlaylistManager.getInstance(mContext).getNumTracks(mPlaylistUri);
    }

    /**
     * 已下载界面，添加数据到播放队列
     */
    public static void insertDownloadedIntoQueue(ContentResolver resolver, MediaInfo mediaInfo) {
        if (resolver != null && mediaInfo != null) {
            Cursor c = null;
            try {
                if (resolver != null) {
                    c = resolver.query(MusicMediaDatabaseHelper.Queue.CONTENT_URI, new String[]{MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID},
                            MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID + " = ?",
                            new String[]{String.valueOf(mediaInfo.audioId)}, null);
                }
                if (c == null || c.getCount() <= 0) {
                    LogUtil.d("lyj", "insertDownloadedIntoQueue");
                    ContentValues values = new ContentValues();
                    values.put(MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID, mediaInfo.audioId);
                    values.put(MusicMediaDatabaseHelper.Queue.QueueColumns.TRANSITION, mediaInfo.transitionId);
                    values.put(MusicMediaDatabaseHelper.Queue.QueueColumns.AUTO_ADDED, 0);
                    resolver.insert(MusicMediaDatabaseHelper.Queue.CONTENT_URI, values);
                }
                if (null != c) {
                    c.close();
                }
            } catch (Exception e) {
                LogUtil.i(TAG, e.toString());
                if (null != c) {
                    c.close();
                }
            }
        }
    }

    /**
     * 仅当数据已存在于Media表中使用
     */
    public static int saveToFavouriteOnlyIfExist(Context context, MediaInfo media) {
        int result = 0;
        if (context == null) {
            return result;
        }
        try {
            ContentResolver resolver = context.getContentResolver();
            String where = "";
            String[] selectionArgs = null;
            ContentValues values = new ContentValues();
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE, media.Favorite ? "1" : "0");
            if (media.sourceType == CommonConstants.SRC_TYPE_DOWNLOADED) {
                where += MusicMediaDatabaseHelper.Media.MediaColumns.TITLE + " like ? " + " AND " + MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST + " = ? "
                        + " AND " + MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE + " < ? ";
                selectionArgs = new String[]{media.title.trim() + "%", media.artist, "2"};
            } else {
                where += MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID + " = ? ";
                selectionArgs = new String[]{String.valueOf(media.audioId)};
            }
            result = resolver.update(MusicMediaDatabaseHelper.Media.CONTENT_URI, values, where, selectionArgs);
            LogUtil.d(TAG, "saveToFavouriteOnlyIfExist and time2 is " + System.currentTimeMillis());
        } catch (Exception e) {
            LogUtil.i(TAG, e.toString());
        }
        return result;
    }

}
