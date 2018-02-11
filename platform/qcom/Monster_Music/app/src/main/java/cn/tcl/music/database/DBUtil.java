package cn.tcl.music.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;

import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;
import cn.tcl.music.util.ToastUtil;

public class DBUtil {
    private static String TAG = DBUtil.class.getSimpleName();

    /************************************************************* Media表操作模块 begin ***************************************************************************/
    /**
     * 根据mediaId获取MediaInfo
     *
     * @param mediaId
     * @return
     */
    public static MediaInfo getMediaInfoWithMediaId(Context context, long mediaId) {
        MediaInfo mediaInfo = null;
        String selection = MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(mediaId)};
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI, null, selection, selectionArgs, null);
        if (null != cursor) {
            cursor.moveToFirst();
            mediaInfo = MusicUtil.getMediaInfoFromCursor(cursor);
            cursor.close();
        }
        return mediaInfo;
    }

    /**
     * 删除media表中的歌曲数据
     *
     * @param context
     * @param mediaId
     * @return 删除成功返回1
     */
    public static int deleteMediaInMediaTableWithMediaId(Context context, long mediaId) {
        String where = MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(mediaId)};
        return context.getContentResolver().delete(MusicMediaDatabaseHelper.Media.CONTENT_URI, where, selectionArgs);
    }

    /**
     * 通过歌曲的mediaId数组，批量的该表是否喜欢
     *
     * @param context
     * @param mediaIds
     * @param favorite
     * @return
     */
    public static int changeFavoriteWithIds(Context context, ArrayList<Object> mediaIds, int favorite) {
        String where = MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID + " in ";
        where += "(";
        where += MusicUtil.translateArrayToString(mediaIds, ",");
        where += ")";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE, favorite);
        contentValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE_DATE, System.currentTimeMillis());
        return context.getContentResolver().update(MusicMediaDatabaseHelper.Media.CONTENT_URI, contentValues, where, null);
    }

    /**
     * 批量添加歌手到我喜欢中（实际的操作是取出歌手的歌曲添加到我喜欢中，需要注意歌曲可能存在被忽略的情况）
     *
     * @param context
     * @param artistIds
     * @return
     */
    public static int addArtistsToFavorite(Context context, ArrayList<Object> artistIds) {
        int result = 0;
        String where = MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_ID + " in ";
        where += "(";
        where += MusicUtil.translateArrayToString(artistIds, ",");
        where += ")";
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED, null, where, null, null);
        ArrayList<Object> mediaIds = new ArrayList<Object>();
        if (null != cursor && cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                mediaIds.add(cursor.getLong(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID)));
            }
            cursor.close();
            LogUtil.d(TAG, "mediaIds size is " + mediaIds.size());
            result = changeFavoriteWithIds(context, mediaIds, CommonConstants.VALUE_MEDIA_IS_FAVORITE);
        }
        return result;
    }

    /**
     * 批量添加专辑到我喜欢中（实际的操作是取出歌手的歌曲添加到我喜欢中，需要注意歌曲可能存在被忽略的情况）
     *
     * @param context
     * @param albumIds
     * @return
     */
    public static int addAlbumsToFavorite(Context context, ArrayList<Object> albumIds) {
        int result = 0;
        String where = MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID + " in ";
        where += "(";
        where += MusicUtil.translateArrayToString(albumIds, ",");
        where += ")";
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED, null, where, null, null);
        ArrayList<Object> mediaIds = new ArrayList<Object>();
        if (null != cursor && cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                mediaIds.add(cursor.getLong(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID)));
            }
            cursor.close();
            LogUtil.d(TAG, "mediaIds size is " + mediaIds.size());
            result = changeFavoriteWithIds(context, mediaIds, CommonConstants.VALUE_MEDIA_IS_FAVORITE);
        }
        return result;
    }

    /**
     * 判断歌曲是否在制定的文件夹中
     *
     * @param context
     * @param mediaId  歌曲id
     * @param folderId 文件夹id
     * @return 文件是否存在
     */
    public static boolean isMediaInTheFolder(Context context, long mediaId, long folderId) {
        boolean result = false;
        String selection = MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID + " = ? and " + MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(mediaId), String.valueOf(folderId)};
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI, null, selection, selectionArgs, null);
        int count = cursor.getCount();
        cursor.close();
        if (count != 0) {
            result = true;
        }
        return result;
    }

    /************************************************************* Media表操作模块 end***************************************************************************/

    /************************************************************* Playlist表操作模块 begin***************************************************************************/
    /**
     * create playlist
     *
     * @param name the playlist's name
     * @return
     */
    public static Uri createPlayList(Context context, String name) {
        ContentValues values = new ContentValues();
        values.put(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.NAME, name);
        if (TextUtils.isEmpty(name)) {
            ToastUtil.showToast(context, com.mixvibes.mvlib.R.string.cannot_specify_an_empty_name_for_playlist);
            return null;
        }
        if (doesPlaylistExist(context, name, MusicMediaDatabaseHelper.Playlists.CONTENT_URI)) {
            ToastUtil.showToast(context, com.mixvibes.mvlib.R.string.playlist_name_already_exists_);
            return null;
        }
        Uri playListUri = context.getContentResolver().insert(MusicMediaDatabaseHelper.Playlists.CONTENT_URI, values);
        if (MusicMediaDatabaseHelper.Playlists.CONTENT_URI.equals(playListUri)) {
            ToastUtil.showToast(context, com.mixvibes.mvlib.R.string.there_was_an_issue_during_playlist_creation);
            return null;
        } else {
            return playListUri;
        }
    }

    /**
     * 根据歌单的id删除歌单,会触发触发器delete_on_playlist,删除playlistsongs对应数据
     *
     * @param context
     * @param playlistId 歌单id
     * @return 1代表删除成功
     */
    public static int deletePlaylistWithPlaylistId(Context context, long playlistId) {
        String where = MusicMediaDatabaseHelper.Playlists.PlaylistsColumns._ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(playlistId)};
        return context.getContentResolver().delete(MusicMediaDatabaseHelper.Playlists.CONTENT_URI, where, selectionArgs);
    }

    /**
     * judge playlist exists by name
     *
     * @param context
     * @param name
     * @param playlistContainerUri
     * @return
     */
    public static boolean doesPlaylistExist(Context context, String name, Uri playlistContainerUri) {
        String selection = MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.NAME + " = ?";
        String[] selectionArgs = new String[]{name};
        Cursor c = context.getContentResolver().query(playlistContainerUri, null, selection, selectionArgs, null);
        if (null == c) {
            return false;
        }
        boolean playlistExists = c.moveToFirst();
        c.close();
        return playlistExists;
    }

    /**
     * get the first playable media in local playlist
     *
     * @param context
     * @param playlistId
     * @return
     */
    public static MediaInfo getFirstPlayableMediaInPlaylist(Context context, long playlistId) {
        MediaInfo mediaInfo = null;
        String selection = MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(playlistId)};
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.PlaylistSongs.CONTENT_URI_PLAYLISTSONGS_MEDIA,
                DBUtil.MEDIA_PLAYLIST_COLUMNS,
                selection,
                selectionArgs,
                MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER + " DESC");
        cursor.moveToFirst();
        mediaInfo = MusicUtil.getMediaInfoFromCursor(cursor);
        cursor.close();
        return mediaInfo;
    }

    /**
     * 批量添加歌手到歌单中（实际的操作是取出歌手的歌曲添加到歌单中，需要注意歌曲可能存在被忽略的情况）
     *
     * @param context
     * @param playlistId
     * @param artistIds
     * @return
     */
    public static int addArtistsToPlaylist(Context context, long playlistId, ArrayList<Object> artistIds) {
        int result = 0;
        String where = MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_ID + " in ";
        where += "(";
        where += MusicUtil.translateArrayToString(artistIds, ",");
        where += ")";
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED, null, where, null, null);
        ArrayList<Object> mediaIds = new ArrayList<Object>();
        if (null != cursor && cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                mediaIds.add(cursor.getLong(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID)));
            }
            cursor.close();
            LogUtil.d(TAG, "mediaIds size is " + mediaIds.size());
            result = addSongsToPlaylist(context, playlistId, mediaIds);
        }
        return result;
    }

    /**
     * 批量添加专辑到歌单中（实际的操作是取出专辑的歌曲添加到歌单中，需要注意歌曲可能存在被忽略的情况）
     *
     * @param context
     * @param playlistId
     * @param albumIds
     * @return
     */
    public static int addAlbumsToPlaylist(Context context, long playlistId, ArrayList<Object> albumIds) {
        int result = 0;
        String where = MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID + " in ";
        where += "(";
        where += MusicUtil.translateArrayToString(albumIds, ",");
        where += ")";
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED, null, where, null, null);
        ArrayList<Object> mediaIds = new ArrayList<Object>();
        if (null != cursor && cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                mediaIds.add(cursor.getLong(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID)));
            }
            cursor.close();
            LogUtil.d(TAG, "mediaIds size is " + mediaIds.size());
            result = addSongsToPlaylist(context, playlistId, mediaIds);
        }
        return result;
    }

    /************************************************************* Playlist表操作模块 end***************************************************************************/


    /************************************************************* Playlistsongs表操作模块 begin***************************************************************************/
    /**
     * 批量删除歌单中的歌曲数据
     *
     * @param context
     * @param playlistId
     * @param mediaIds
     * @return 删除的数据数量
     */
    public static int deleteSongsFromPlaylist(Context context, long playlistId, ArrayList<Object> mediaIds) {
        String where = MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.MEDIA_ID + " in ";
        where += "(";
        where += MusicUtil.translateArrayToString(mediaIds, ",");
        where += ") and ";
        where += MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + "= ?";
        String[] selectionArgs = new String[]{String.valueOf(playlistId)};
        return context.getContentResolver().delete(MusicMediaDatabaseHelper.PlaylistSongs.CONTENT_URI, where, selectionArgs);
    }

    /**
     * 批量添加歌曲到某个歌单
     *
     * @param context
     * @param playlistId 歌单id
     * @param mediaIds   歌曲id array
     * @return 插入的数量
     */
    public static int addSongsToPlaylist(Context context, long playlistId, ArrayList<Object> mediaIds) {
        ContentValues[] arrayValues = new ContentValues[mediaIds.size()];
        for (int i = 0; i < mediaIds.size(); i++) {
            long mediaId = Long.valueOf(mediaIds.get(i).toString());
            ContentValues contentValues = new ContentValues();
            contentValues.put(MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.MEDIA_ID, mediaId);
            contentValues.put(MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID, playlistId);
            arrayValues[i] = contentValues;
        }
        return context.getContentResolver().bulkInsert(MusicMediaDatabaseHelper.PlaylistSongs.CONTENT_URI, arrayValues);
    }

    /**
     * 添加歌曲到最近播放:
     * 添加歌曲到最近播放，然后触发器将会将当前插入歌曲的playorder加到最大
     * 然后判断当前最近播放的数量是否已经达到限制，如果达到限制，则删除先插入的数据
     *
     * @param context
     * @param mediaId 歌曲id
     * @return
     */
    public static Uri addToRecentlyPlay(Context context, long mediaId) {
        String selection = MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + " = " + CommonConstants.RECENTLY_PLAYED_PLAYLIST_ID
                + " and " + MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.MEDIA_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(mediaId)};
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.PlaylistSongs.CONTENT_URI, null, selection, selectionArgs, null);
        if (null != cursor) {
            //如果数据已经存在，那么删除
            if (cursor.getCount() != 0) {
                int result = context.getContentResolver().delete(MusicMediaDatabaseHelper.PlaylistSongs.CONTENT_URI, selection, selectionArgs);
                LogUtil.d(TAG, "result is " + result);
            }
            cursor.close();
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.MEDIA_ID, mediaId);
        contentValues.put(MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID, CommonConstants.RECENTLY_PLAYED_PLAYLIST_ID);
        return context.getContentResolver().insert(MusicMediaDatabaseHelper.PlaylistSongs.CONTENT_URI, contentValues);
    }

    /************************************************************* Playlistsongs表操作模块 end***************************************************************************/

    /************************************************************* Folders表操作模块 begin***************************************************************************/
    /**
     * 改变文件夹是否扫描的状态
     *
     * @param folderId 文件夹id
     * @param isScan   是否扫描 0不扫描 1扫描
     * @return 更新成功返回1
     */
    public static int changeIsScanInFolderTableWithFolderId(Context context, long folderId, int isScan) {
        String where = MusicMediaDatabaseHelper.Folders.FoldersColumns._ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(folderId)};
        ContentValues contentValues = new ContentValues();
        contentValues.put(MusicMediaDatabaseHelper.Folders.FoldersColumns._ID, folderId);
        contentValues.put(MusicMediaDatabaseHelper.Folders.FoldersColumns.IS_SCAN, isScan);
        return context.getContentResolver().update(MusicMediaDatabaseHelper.Folders.CONTENT_URI, contentValues, where, selectionArgs);
    }

    /*************************************************************
     * Folders表操作模块 begin
     ***************************************************************************/

    //注意:作为数据库的查询条件，下面的projection是必须的，因为每个表都有_id字段，否则会出现问题重复字段的问题

    //media表和folder表联合
    public static final String[] MEDIA_FOLDER_COLUMNS = new String[]{
            //media表
            MusicMediaDatabaseHelper.Media.TABLE_NAME + "." + MusicMediaDatabaseHelper.Media.MediaColumns._ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE,
            MusicMediaDatabaseHelper.Media.MediaColumns.TITLE,
            MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_TRACK,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_PORTRAIT,
            MusicMediaDatabaseHelper.Media.MediaColumns.GENRE_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID,
            MusicMediaDatabaseHelper.Media.TABLE_NAME + "." + MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_PATH,
            MusicMediaDatabaseHelper.Media.TABLE_NAME + "." + MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_NAME,
            MusicMediaDatabaseHelper.Media.MediaColumns.GENRE,
            MusicMediaDatabaseHelper.Media.MediaColumns.GENRE_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTWORK,
            MusicMediaDatabaseHelper.Media.MediaColumns.COMMENTS,
            MusicMediaDatabaseHelper.Media.MediaColumns.PATH,
            MusicMediaDatabaseHelper.Media.MediaColumns.REMOTE_IMPORT_PATH,
            MusicMediaDatabaseHelper.Media.MediaColumns.HARMONIC_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.BPM,
            MusicMediaDatabaseHelper.Media.MediaColumns.DURATION,
            MusicMediaDatabaseHelper.Media.MediaColumns.SIZE,
            MusicMediaDatabaseHelper.Media.MediaColumns.TRANSITION,
            MusicMediaDatabaseHelper.Media.MediaColumns.DATE_ADD,
            MusicMediaDatabaseHelper.Media.MediaColumns.SONG_REMOTE_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.DOWNLOADED,
            MusicMediaDatabaseHelper.Media.MediaColumns.SUFFIX,
            MusicMediaDatabaseHelper.Media.MediaColumns.URL,
            MusicMediaDatabaseHelper.Media.MediaColumns.LYRIC_PATH,
            MusicMediaDatabaseHelper.Media.MediaColumns.SCENE_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE_DATE,

            //folder表
            MusicMediaDatabaseHelper.Folders.FoldersColumns.IS_SCAN
    };

    public static final String[] MEDIA_PLAYLIST_COLUMNS = new String[]{
            //media表
            MusicMediaDatabaseHelper.Media.MediaColumns._ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE,
            MusicMediaDatabaseHelper.Media.MediaColumns.TITLE,
            MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_TRACK,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_PORTRAIT,
            MusicMediaDatabaseHelper.Media.MediaColumns.GENRE_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_PATH,
            MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_NAME,
            MusicMediaDatabaseHelper.Media.MediaColumns.GENRE,
            MusicMediaDatabaseHelper.Media.MediaColumns.GENRE_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTWORK,
            MusicMediaDatabaseHelper.Media.MediaColumns.COMMENTS,
            MusicMediaDatabaseHelper.Media.MediaColumns.PATH,
            MusicMediaDatabaseHelper.Media.MediaColumns.REMOTE_IMPORT_PATH,
            MusicMediaDatabaseHelper.Media.MediaColumns.HARMONIC_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.BPM,
            MusicMediaDatabaseHelper.Media.MediaColumns.DURATION,
            MusicMediaDatabaseHelper.Media.MediaColumns.SIZE,
            MusicMediaDatabaseHelper.Media.MediaColumns.TRANSITION,
            MusicMediaDatabaseHelper.Media.MediaColumns.DATE_ADD,
            MusicMediaDatabaseHelper.Media.MediaColumns.SONG_REMOTE_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.DOWNLOADED,
            MusicMediaDatabaseHelper.Media.MediaColumns.SUFFIX,
            MusicMediaDatabaseHelper.Media.MediaColumns.URL,
            MusicMediaDatabaseHelper.Media.MediaColumns.LYRIC_PATH,
            MusicMediaDatabaseHelper.Media.MediaColumns.SCENE_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE_DATE,

            //folder表
            MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID,
            MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER
    };


    public static final String[] defaultPlaylistColumns = new String[]{
            MusicMediaDatabaseHelper.Playlists.PlaylistsColumns._ID,
            MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.NAME,
            MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.DESCRIPTION,
            MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.TYPE,
            MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.ARTWORK,
            MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.PATH
    };

    public static final String[] defaultAlbumColumns = new String[]{
            MusicMediaDatabaseHelper.Albums.AlbumColumns._ID,
            MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM,
            MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST,
            MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST_ID,
            MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTWORK,
            MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST_PORTRAIT,
            MusicMediaDatabaseHelper.Albums.AlbumColumns.NUMBER_OF_TRACKS
    };

    public static final String[] defaultArtistColumns = new String[]{
            MusicMediaDatabaseHelper.Artists.ArtistsColumns._ID,
            MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST,
            MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST_KEY,
            MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST_PORTRAIT,
            MusicMediaDatabaseHelper.Artists.ArtistsColumns.NUMBER_OF_ALBUMS,
            MusicMediaDatabaseHelper.Artists.ArtistsColumns.NUMBER_OF_TRACKS
    };

    public static final String[] defaultFolderColumns = new String[]{
            MusicMediaDatabaseHelper.Folders.FoldersColumns._ID,
            MusicMediaDatabaseHelper.Folders.FoldersColumns.FOLDER_NAME,
            MusicMediaDatabaseHelper.Folders.FoldersColumns.FOLDER_PATH,
            MusicMediaDatabaseHelper.Folders.FoldersColumns.FOLDER_SONGS_NUM,
            MusicMediaDatabaseHelper.Folders.FoldersColumns.IS_SCAN
    };

    public static final String[] defaultGenreColumns = new String[]{
            MusicMediaDatabaseHelper.Genres.GenresColumns._ID,
            MusicMediaDatabaseHelper.Genres.GenresColumns.GENRE,
            MusicMediaDatabaseHelper.Genres.GenresColumns.NUMBER_OF_TRACKS
    };

    public static final String[] defaultQueueColumns = new String[]{
            MusicMediaDatabaseHelper.Queue.QueueColumns._ID,
            MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE,
            MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID,
            MusicMediaDatabaseHelper.Queue.QueueColumns.PLAY_ORDER,
            MusicMediaDatabaseHelper.Queue.QueueColumns.RANDOM_PLAY_ORDER,
            MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.TITLE,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_PORTRAIT,
            MusicMediaDatabaseHelper.Media.MediaColumns.DURATION,
            MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE,
            MusicMediaDatabaseHelper.Media.MediaColumns.BPM,
            MusicMediaDatabaseHelper.Media.MediaColumns.HARMONIC_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE,
            MusicMediaDatabaseHelper.Media.MediaColumns.PATH,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTWORK,
            MusicMediaDatabaseHelper.Media.MediaColumns.SONG_REMOTE_ID,
    };

    public static final String[] defaultScenesColumns = new String[]{
            MusicMediaDatabaseHelper.Scenes.ScenesColumns._ID,
            MusicMediaDatabaseHelper.Scenes.ScenesColumns.SCENES_TITLE,
            MusicMediaDatabaseHelper.Scenes.ScenesColumns.SCENES_ICON
    };


}
