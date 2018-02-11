package cn.tcl.music.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.database.OrderUtils;
import cn.tcl.music.database.PlaylistManager;
import cn.tcl.music.media.MediaPlaylist;
import cn.tcl.music.model.AlbumInfo;
import cn.tcl.music.model.ArtistInfo;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.live.SongDetailBean;

public class MusicUtil {
    private static final String EXTERNAL_MEDIA = "external";
    private static final String TAG = MusicUtil.class.getSimpleName();


    /**
     * 根据歌曲的id找到歌曲所在文件夹的id
     *
     * @param context
     * @param songID  the id of song which record in MediaStore
     * @return
     */
    public static String getBucketID(Context context, String songID) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        if (contentResolver != null) {
            cursor = contentResolver.query(MediaStore.Files.getContentUri(EXTERNAL_MEDIA), new String[]{"bucket_id"}, "_id = ?", new String[]{songID}, null);
        }
        try {
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getString(0);
            }
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 获取全部本地的歌曲的数量
     *
     * @param context
     * @return
     */
    public static int getSongCount(Context context) {
        int songCount = 0;
        String selection = MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE + " <= ? ";
        String[] selectionArgs = new String[]{String.valueOf(CommonConstants.SRC_TYPE_MYMIX)};
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED,
                null, selection, selectionArgs, null);
        if (cursor != null) {
            songCount = cursor.getCount();
            cursor.close();
        }
        return songCount;
    }

    /**
     * get recent play songs count
     *
     * @param context
     * @return
     */
    public static int getRecentCount(Context context) {
        int recentCount = 0;
        String section = MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(CommonConstants.RECENTLY_PLAYED_PLAYLIST_ID)};
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.PlaylistSongs.CONTENT_URI_PLAYLISTSONGS_MEDIA
                , null, section, selectionArgs, MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER + " DESC limit 0," + CommonConstants.RECENT_PLAY_SONG_SIZE_LIMIT);
        recentCount = cursor.getCount();
        cursor.close();
        return recentCount;
    }

    /**
     * get favorite songs count
     *
     * @param context
     * @return
     */
    public static int getLikeCount(Context context) {
        int likeCount = 0;
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED
                , null,
                MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE + " = 1 ",
                null,
                null);
        if (cursor != null) {
            likeCount = cursor.getCount();
            cursor.close();
        }
        return likeCount;
    }

    public static String getAnalysisDir(Context context) {
        File extFileDir = FileUtils.getApplicationDataDir(context);
        if (extFileDir == null) {
            ToastUtil.showToast(context, R.string.the_external_memory_storage_is_not_mounted);
            return null;
        }
        File file = new File(extFileDir, "Analysis");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                LogUtil.e("CrossDJ Analysis Dir", "Directory not created");
                return "";
            }
        }
        return file.getAbsolutePath();
    }

    /**
     * 将Cursor转换成对应的MediaInfo
     *
     * @param c
     * @return
     */
    public static MediaInfo getMediaInfoFromCursor(Cursor c) {
        MediaInfo info = new MediaInfo();
        if (null != c && c.getCount() != 0) {
            for (String column : c.getColumnNames()) {
                if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns._ID)) {
                    info.Id = c.getLong(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns._ID));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID)) {
                    info.audioId = c.getLong(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.TITLE)) {
                    info.title = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.TITLE));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM)) {
                    info.album = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST)) {
                    info.artist = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.DURATION)) {
                    info.durationMs = c.getDouble(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.DURATION));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.PATH)) {
                    info.filePath = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.PATH));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE)) {
                    info.Favorite = c.getInt(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE)) == 0 ? false : true;
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.BPM)) {
                    info.Bpm = c.getLong(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.BPM));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.ARTWORK)) {
                    info.artworkPath = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.ARTWORK));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.TRANSITION)) {
                    info.transitionId = c.getInt(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.TRANSITION));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE)) {
                    info.sourceType = c.getInt(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.REMOTE_IMPORT_PATH)) {
                    info.remoteImportPath = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.REMOTE_IMPORT_PATH));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_ID)) {
                    info.artistId = c.getLong(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_ID));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID)) {
                    info.albumId = c.getLong(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.SONG_REMOTE_ID)) {
                    info.songRemoteId = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.SONG_REMOTE_ID));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_PORTRAIT)) {
                    info.artistPortraitPath = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_PORTRAIT));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID)) {
                    info.folderId = c.getLong(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID));
                } else if (column.equals(MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE)) {
                    info.isEffective = c.getInt(c.getColumnIndex(MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE));
                } else if (column.equals(MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY)) {
                    info.Key = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY));
                }
            }
        }
        return info;
    }

    public static ArtistInfo getArtistInfoFromCursor(Cursor c) {
        Log.d(TAG, "getArtistInfoFromCursor and cursor column count is " + c.getColumnCount());
        ArtistInfo info = new ArtistInfo();
        if (null != c) {
            for (String column : c.getColumnNames()) {
                if (column.equals(MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST)) {
                    info.artist = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST));
                } else if (column.equals(MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST_KEY)) {
                    info.artistkey = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST_KEY));
                } else if (column.equals(MusicMediaDatabaseHelper.Artists.ArtistsColumns._ID)) {
                    info.id = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Artists.ArtistsColumns._ID));
                } else if (column.equals(MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST_PORTRAIT)) {
                    info.artistPortrait = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST_PORTRAIT));
                } else if (column.equals(MusicMediaDatabaseHelper.Artists.ArtistsColumns.NUMBER_OF_ALBUMS)) {
                    info.numberOfAlbums = c.getInt(c.getColumnIndex(MusicMediaDatabaseHelper.Artists.ArtistsColumns.NUMBER_OF_ALBUMS));
                } else if (column.equals(MusicMediaDatabaseHelper.Artists.ArtistsColumns.NUMBER_OF_TRACKS)) {
                    info.numberOfTracks = c.getInt(c.getColumnIndex(MusicMediaDatabaseHelper.Artists.ArtistsColumns.NUMBER_OF_TRACKS));
                }
            }
        }
        return info;
    }

    public static AlbumInfo getAlbumInfoFromCursor(Cursor c) {
        Log.d(TAG, "getAlbumInfoFromCursor and cursor column count is " + c.getColumnCount());
        AlbumInfo info = new AlbumInfo();
        if (null != c) {
            for (String column : c.getColumnNames()) {
                if (column.equals(MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM)) {
                    info.album = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM));
                } else if (column.equals(MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM_KEY)) {
                    info.albumKey = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM_KEY));
                } else if (column.equals(MusicMediaDatabaseHelper.Albums.AlbumColumns._ID)) {
                    info.albumId = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns._ID));
                } else if (column.equals(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTWORK)) {
                    info.artworkPath = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTWORK));
                } else if (column.equals(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST)) {
                    info.artist = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST));
                } else if (column.equals(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST_ID)) {
                    info.artistId = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST_ID));
                } else if (column.equals(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST_KEY)) {
                    info.artistkey = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST_KEY));
                } else if (column.equals(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST_PORTRAIT)) {
                    info.artistPortrait = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST_PORTRAIT));
                } else if (column.equals(MusicMediaDatabaseHelper.Albums.AlbumColumns.NUMBER_OF_TRACKS)) {
                    info.numberOfTracks = c.getInt(c.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns.NUMBER_OF_TRACKS));
                }
            }
        }
        return info;
    }

    /**
     * 获取歌曲详情时
     *
     * @return 1为text, 2为lrc, 3为trc, 4为翻译歌词
     */
    public static String getLiricType() {
        return "2";
    }

    /**
     * 根据歌词类型获取歌词后缀名 1为text,2为lrc,3为trc,4为翻译歌词
     *
     * @param
     * @return
     */
    public static String getLiricSuffix() {
        String lyricType = getLiricType();
        String result = ".lrc";
        if ("1".equals(lyricType)) {
            result = ".text";
        } else if ("2".equals(lyricType)) {
            result = ".lrc";
        } else if ("3".equals(lyricType)) {
            result = ".trc";
        }
        return result;
    }

    /**
     * save media info to local database by the SongDetailBean
     *
     * @param transitionId       首次点击列表，会将transitionId相同的数据一起加到播放队列
     * @param updateTransitionId 是否需要更新本地transitionId
     * @return MediaInfo
     */
    public static MediaInfo getMediaInfoBySongDetail(Context context, SongDetailBean song, int positionAtList, int transitionId, int sourceType, boolean updateTransitionId) {
        if (song == null || context == null) {
            return null;
        }
        LogUtil.d(TAG, "song.name = " + song.song_name + ",song.id=" + song.song_id + ",song.track=" + song.track + ",song.artist=" + song.artist_name
                + ",song.album=" + song.album_name);
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.albumId = Long.valueOf(song.album_id);
        mediaInfo.album = song.album_name;
        mediaInfo.artistId = Long.valueOf(song.artist_id);
        mediaInfo.artist = song.artist_name;
        mediaInfo.title = song.song_name;
        mediaInfo.sourceType = sourceType;
        mediaInfo.artworkPath = song.album_logo;
        mediaInfo.artistPortraitPath = song.artist_logo;
        mediaInfo.filePath = song.listen_file;
        mediaInfo.durationMs = song.length * 1000;
        mediaInfo.audioId = Long.valueOf(song.song_id);

        //judge whether recorded in local database
        Cursor c = context.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI, new String[]{MusicMediaDatabaseHelper.Media.MediaColumns._ID, MusicMediaDatabaseHelper.Media.MediaColumns.TRANSITION},
                MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID + " = ?" + " AND " + MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE + " = ?",
                new String[]{String.valueOf(mediaInfo.audioId), String.valueOf(mediaInfo.sourceType)}, null);
        boolean isFound = false;
        if (c.moveToFirst()) {
            mediaInfo.Id = c.getLong(0);
            mediaInfo.transitionId = c.getInt(1);
            isFound = true;
        }
        c.close();
        LogUtil.d(TAG, "isFound = " + isFound);
        if (isFound) {
            //need to update local database
            ContentValues updateValues = new ContentValues();
            updateValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.PATH, mediaInfo.filePath);
            updateValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.REMOTE_IMPORT_PATH, mediaInfo.filePath);
            updateValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.DURATION, mediaInfo.durationMs);
            if (updateTransitionId) {
                updateValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.TRANSITION, transitionId);
                updateValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID, String.valueOf(positionAtList));
                mediaInfo.transitionId = transitionId;
            }
            context.getContentResolver().update(MusicMediaDatabaseHelper.Media.CONTENT_URI, updateValues, MusicMediaDatabaseHelper.Media.MediaColumns._ID + " = ?", new String[]{String.valueOf(mediaInfo.Id)});
        } else {
            ContentValues values = new ContentValues();
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.TITLE, mediaInfo.title.trim());
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY, OrderUtils.keyFor(mediaInfo.title));
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID, mediaInfo.albumId);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM, mediaInfo.album);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_KEY, OrderUtils.keyForAlbum(mediaInfo.album));
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_ID, mediaInfo.artistId);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST, mediaInfo.artist);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_KEY, OrderUtils.keyForArtist(mediaInfo.artist));
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_PORTRAIT, mediaInfo.artistPortraitPath);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ARTWORK, mediaInfo.artworkPath);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID, mediaInfo.audioId);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID, String.valueOf(positionAtList));
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE, mediaInfo.sourceType);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.PATH, mediaInfo.filePath);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.REMOTE_IMPORT_PATH, mediaInfo.filePath);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.DURATION, mediaInfo.durationMs);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.TRANSITION, transitionId);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.SONG_REMOTE_ID, song.song_id);
            //针对网络歌曲，生成虚拟的文件夹路径，作为数据库存储使用，否则可能引起数据库连表查询的问题
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID, CommonConstants.VIRTUAL_ONLINE_FOLDER_ID);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_NAME, CommonConstants.VIRTUAL_ONLINE_FOLDER_NAME);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_PATH, CommonConstants.VIRTUAL_ONLINE_FOLDER_PATH);

            Uri insertedUri = context.getContentResolver().insert(MusicMediaDatabaseHelper.Media.CONTENT_URI, values);
            LogUtil.d(TAG, "insertedUri is " + insertedUri);
            if (null == insertedUri) {
                mediaInfo = null;
            } else {
                String lastSegment = insertedUri.getLastPathSegment();
                if (null != lastSegment && TextUtils.isDigitsOnly(lastSegment)) {
                    mediaInfo.Id = Long.parseLong(lastSegment);
                    mediaInfo.transitionId = transitionId;
                } else {
                    mediaInfo = null;
                }
            }
        }
        return mediaInfo;
    }

    public static String getUniqueMediaId(Context context, SongDetailBean song) {
        if (null == song) {
            return null;
        }
        ContentResolver contentResolver = context.getContentResolver();
        Cursor c = null;
        if (contentResolver != null) {
            c = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media._ID},
                    MediaStore.Audio.Media.DATA + " = ?",
                    new String[]{song.listen_file}, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    return c.getString(0);
                }
                c.close();
            }
        }
        return null;
    }

    /**
     * 在线音乐是否已收藏
     **/
    public static boolean isLiveSongSaved(Context context, SongDetailBean song, int sourceType) {
        int transitionId = Util.getTransionId();
        MediaInfo mediaInfo = getMediaInfoBySongDetail(context, song, 0, transitionId, sourceType, false);
        if (mediaInfo != null) {
            Cursor c = context.getContentResolver().query(MusicMediaDatabaseHelper.Playlists.FAVORITE_URI, new String[]{MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST, MusicMediaDatabaseHelper.Media.MediaColumns.TITLE, MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM}, null, null, null);
            if (c.getCount() != 0) {
                while (c.moveToNext()) {
                    if ((song.song_name.trim()).equals(c.getString(1)) && song.artist_name.equals(c.getString(0))) {
                        return true;
                    }
                }
                c.close();
            }
        }
        return false;
    }

    /**
     * 在线音乐保存我喜欢
     **/
    public static void saveFavourite(Context context, MediaInfo info) {
        ToastUtil.showToast(context, R.string.cancel_favorite);
        final PlaylistManager.AddPlaylistParameter parameter = new PlaylistManager.AddPlaylistParameter();
        parameter.addType = PlaylistManager.AddTypes.MEDIA;
        parameter.id = info.audioId;

        MediaInfo media = new MediaInfo();
        media.Id = info.Id;
        media.audioId = info.audioId;
        media.title = info.title;
        media.artist = info.artist;
        media.Favorite = false;
        media.sourceType = info.sourceType;
        MediaPlaylist.saveToFavouriteOnlyIfExist(context, media);
    }

    /**
     * 将array，通过特定的分隔符转换成字符串
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

    /***
     * 保存到收藏
     */
    public static void save2Favourite(Context context, SongDetailBean song, int sourceType) {
        if (song == null) {
            LogUtil.d("LibraryNavigationUtil", "save2Favourite--lack of song inf");
            return;
        }
        int transitionId = Util.getTransionId();
        MediaInfo mediaInfo = getMediaInfoBySongDetail(context, song, 0, transitionId, sourceType, false);
        if (null != mediaInfo) {
            if (0 != mediaInfo.audioId) {
                ArrayList<Object> ids = new ArrayList<>();
                ids.add(mediaInfo.audioId);
                DBUtil.changeFavoriteWithIds(context, ids, CommonConstants.VALUE_MEDIA_IS_FAVORITE);
            } else {
                LogUtil.d(TAG, "mediaInfo auduoId is 0");
            }
        } else {
            LogUtil.d(TAG, "media info is null");
        }
    }
}
