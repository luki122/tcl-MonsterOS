package cn.tcl.music.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.tcl.framework.log.NLog;

import java.util.List;

import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.util.LogUtil;

public class MediaContentProvider extends ContentProvider {
    public static final String TAG = MediaContentProvider.class.getSimpleName();
    public static final String AUTHORITY = "cn.tcl.music.providers.MediaContentProvider";

    private static final int MEDIA_ALL = 1;
    private static final int MEDIA_SCANNED = MEDIA_ALL + 1;
    private static final int MEDIA_ID = MEDIA_SCANNED + 1;
    private static final int FAVORITE = MEDIA_ID + 1;
    private static final int FAVORITE_ADD = FAVORITE + 1;
    private static final int RECORDS = FAVORITE_ADD + 1;
    private static final int ALBUMS = RECORDS + 1;
    private static final int ALBUMS_ID = ALBUMS + 1;
    private static final int ARTISTS = ALBUMS_ID + 1;
    private static final int ARTISTS_ID = ARTISTS + 1;
    private static final int GENRES = ARTISTS_ID + 1;
    private static final int GENRES_ID = GENRES + 1;
    private static final int PLAYLISTS = GENRES_ID + 1;
    private static final int PLAYLISTS_SONGS_FROM_PLAYLIST_ID = PLAYLISTS + 1;
    private static final int PLAYLISTS_SONGS_FROM_PLAYLIST_ID_WITH_ROW_ID = PLAYLISTS_SONGS_FROM_PLAYLIST_ID + 1;
    private static final int PLAYLISTS_ADD = PLAYLISTS_SONGS_FROM_PLAYLIST_ID_WITH_ROW_ID + 1;
    private static final int PLAYLISTS_SONGS = PLAYLISTS_ADD + 1;
    private static final int PLAYLISTS_SONGS_ID = PLAYLISTS_SONGS + 1;
    private static final int PLAYLISTSONGS_MEDIA = PLAYLISTS_SONGS_ID + 1;
    private static final int QUEUE = PLAYLISTSONGS_MEDIA + 1;
    private static final int QUEUE_ID = QUEUE + 1;
    private static final int QUEUE_ADD = QUEUE_ID + 1;
    private static final int QUEUE_MEDIA = QUEUE_ADD + 1;                           //Queue表与Media表连表查询歌曲信息
    private static final int LAST_ADD = QUEUE_MEDIA + 1;
    private static final int FOLDERS = LAST_ADD + 1;
    private static final int FOLDERS_ID = FOLDERS + 1;
    private static final int SCENES = FOLDERS_ID + 1;
    private static final int SCENES_ID = SCENES + 1;
    private static final int HISTORY = SCENES_ID + 1;

    private MusicMediaDatabaseHelper mMusicDbHelper;
    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Media.TABLE_NAME, MEDIA_ALL);                                //查询全部Media表数据
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Media.TABLE_NAME + "/NotIgnored", MEDIA_SCANNED);            //查询所在文件夹is_scan为1的media数据
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Media.TABLE_NAME + "/#", MEDIA_ID);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Media.TABLE_NAME + "/Favorite", FAVORITE);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Media.TABLE_NAME + "/Favorite/Add", FAVORITE_ADD);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Media.TABLE_NAME + "/Records", RECORDS);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Albums.TABLE_NAME, ALBUMS);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Albums.TABLE_NAME + "/#", ALBUMS_ID);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Artists.TABLE_NAME, ARTISTS);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Artists.TABLE_NAME + "/#", ARTISTS_ID);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Folders.TABLE_NAME, FOLDERS);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Folders.TABLE_NAME + "/#", FOLDERS_ID);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Genres.TABLE_NAME, GENRES);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Genres.TABLE_NAME + "/#", GENRES_ID);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Playlists.TABLE_NAME, PLAYLISTS);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Playlists.TABLE_NAME + "/#", PLAYLISTS_SONGS_FROM_PLAYLIST_ID);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Playlists.TABLE_NAME + "/#/#", PLAYLISTS_SONGS_FROM_PLAYLIST_ID_WITH_ROW_ID);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Playlists.TABLE_NAME + "/#/Add", PLAYLISTS_ADD);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.PlaylistSongs.TABLE_NAME, PLAYLISTS_SONGS);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.PlaylistSongs.TABLE_NAME + "/#", PLAYLISTS_SONGS_ID);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.PlaylistSongs.TABLE_NAME + "/Media", PLAYLISTSONGS_MEDIA);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Queue.TABLE_NAME, QUEUE);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Queue.TABLE_NAME + "/#", QUEUE_ID);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Queue.TABLE_NAME + "/Add", QUEUE_ADD);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Queue.TABLE_NAME + "/Media", QUEUE_MEDIA);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Media.TABLE_NAME + "/LastAdd", LAST_ADD);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.History.TABLE_NAME, HISTORY);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Scenes.TABLE_NAME, SCENES);
        mUriMatcher.addURI(AUTHORITY, MusicMediaDatabaseHelper.Scenes.TABLE_NAME + "/#", SCENES_ID);
    }

    @Override
    public boolean onCreate() {
        mMusicDbHelper = new MusicMediaDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        LogUtil.d(TAG,"uri is " + uri.toString() + " and switch result is " + mUriMatcher.match(uri));
        SQLiteDatabase db = mMusicDbHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (mUriMatcher.match(uri)) {
            case MEDIA_ALL: {
                cursor = db.query(MusicMediaDatabaseHelper.Media.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            }
                break;
            case MEDIA_SCANNED: {
                String table = MusicMediaDatabaseHelper.Media.TABLE_NAME + " inner join " + MusicMediaDatabaseHelper.Folders.TABLE_NAME
                        + " on " + MusicMediaDatabaseHelper.Media.TABLE_NAME + "." + MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID + " = "
                        + MusicMediaDatabaseHelper.Folders.TABLE_NAME + "." + MusicMediaDatabaseHelper.Folders.FoldersColumns._ID
                        + " and " + MusicMediaDatabaseHelper.Folders.FoldersColumns.IS_SCAN + " = " + CommonConstants.VALUE_FOLDER_IS_SCAN;
                cursor = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
            }
                break;
            case ALBUMS:
            case ALBUMS_ID:
                cursor = db.query(MusicMediaDatabaseHelper.Albums.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ARTISTS:
            case ARTISTS_ID:
                cursor = db.query(MusicMediaDatabaseHelper.Artists.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FOLDERS:
            case FOLDERS_ID:
                cursor = db.query(MusicMediaDatabaseHelper.Folders.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PLAYLISTS:
            case PLAYLISTS_SONGS_FROM_PLAYLIST_ID:
                cursor = db.query(MusicMediaDatabaseHelper.Playlists.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PLAYLISTS_SONGS:
                cursor = db.query(MusicMediaDatabaseHelper.PlaylistSongs.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PLAYLISTSONGS_MEDIA: {
                String table = "(" + " select * from " + MusicMediaDatabaseHelper.PlaylistSongs.TABLE_NAME + "," + MusicMediaDatabaseHelper.Media.TABLE_NAME + "," + MusicMediaDatabaseHelper.Folders.TABLE_NAME
                        + " where " + MusicMediaDatabaseHelper.PlaylistSongs.TABLE_NAME + "." + MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.MEDIA_ID + " = "
                        + MusicMediaDatabaseHelper.Media.TABLE_NAME + "." + MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID + " and "
                        + MusicMediaDatabaseHelper.Media.TABLE_NAME + "." + MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID + " = "
                        + MusicMediaDatabaseHelper.Folders.TABLE_NAME + "." + MusicMediaDatabaseHelper.Folders.FoldersColumns._ID + " and "
                        + MusicMediaDatabaseHelper.Folders.TABLE_NAME + "." + MusicMediaDatabaseHelper.Folders.FoldersColumns.IS_SCAN + " = " + CommonConstants.VALUE_FOLDER_IS_SCAN + " )";
                cursor = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

            }
                break;
            case SCENES:
            case SCENES_ID:
                cursor = db.query(MusicMediaDatabaseHelper.Scenes.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case QUEUE:
            case QUEUE_ID:
            case QUEUE_ADD:
                cursor = db.query(MusicMediaDatabaseHelper.Queue.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case QUEUE_MEDIA: {
                String table = MusicMediaDatabaseHelper.Queue.TABLE_NAME + " inner join " + MusicMediaDatabaseHelper.Media.TABLE_NAME
                        + " on " + MusicMediaDatabaseHelper.Queue.TABLE_NAME + "." + MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID + " = "
                        + MusicMediaDatabaseHelper.Media.TABLE_NAME + "." + MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID;
                cursor = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
            }
                break;
            default:
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        LogUtil.d(TAG, "uri is " + uri + " and contentValues is " + contentValues.toString());
        SQLiteDatabase db = mMusicDbHelper.getWritableDatabase();
        String tableName = "";
        switch (mUriMatcher.match(uri)) {
            case MEDIA_ALL:
                tableName = MusicMediaDatabaseHelper.Media.TABLE_NAME;
                break;
            case FAVORITE:
                break;
            case FAVORITE_ADD:
                break;
            case PLAYLISTS:
                tableName = MusicMediaDatabaseHelper.Playlists.TABLE_NAME;
                break;
            case PLAYLISTS_SONGS:
                tableName = MusicMediaDatabaseHelper.PlaylistSongs.TABLE_NAME;
                break;
            case PLAYLISTS_SONGS_FROM_PLAYLIST_ID:
                tableName = MusicMediaDatabaseHelper.PlaylistSongs.TABLE_NAME;
                break;
            case PLAYLISTS_ADD:
                break;
            case QUEUE_ADD:
                break;
            case QUEUE:
                tableName = MusicMediaDatabaseHelper.Queue.TABLE_NAME;
                break;
            case HISTORY:
                tableName = MusicMediaDatabaseHelper.History.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        long rowId = db.insertOrThrow(tableName, null, contentValues);
        Log.d(TAG, "rowId is " + rowId);
        if (rowId > 0) {
            Uri mediaInsertedUri = ContentUris.withAppendedId(uri, rowId);
            return mediaInsertedUri;
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mMusicDbHelper.getWritableDatabase();
        String tableName = "";
        int result = 0;
        LogUtil.d(TAG,"match result is " + mUriMatcher.match(uri));
        switch (mUriMatcher.match(uri)) {
            case MEDIA_ALL:
            case MEDIA_SCANNED:
            case MEDIA_ID:
            case FAVORITE:
            case FAVORITE_ADD:
            case RECORDS:
            case LAST_ADD:
                tableName = MusicMediaDatabaseHelper.Media.TABLE_NAME;
                break;
            case ALBUMS:
            case ALBUMS_ID:
                tableName = MusicMediaDatabaseHelper.Albums.TABLE_NAME;
                break;
            case ARTISTS:
            case ARTISTS_ID:
                tableName = MusicMediaDatabaseHelper.Artists.TABLE_NAME;
                break;
            case FOLDERS:
            case FOLDERS_ID:
                tableName = MusicMediaDatabaseHelper.Folders.TABLE_NAME;
                break;
            case GENRES:
            case GENRES_ID:
                tableName = MusicMediaDatabaseHelper.Genres.TABLE_NAME;
                break;
            case PLAYLISTS:
            case PLAYLISTS_SONGS_FROM_PLAYLIST_ID:
            case PLAYLISTS_SONGS_FROM_PLAYLIST_ID_WITH_ROW_ID:
            case PLAYLISTS_ADD:
                tableName = MusicMediaDatabaseHelper.Playlists.TABLE_NAME;
                break;
            case PLAYLISTS_SONGS:
            case PLAYLISTS_SONGS_ID:
                tableName = MusicMediaDatabaseHelper.PlaylistSongs.TABLE_NAME;
                break;
            case QUEUE:
            case QUEUE_ID:
            case QUEUE_ADD:
                tableName = MusicMediaDatabaseHelper.Queue.TABLE_NAME;
                break;
            case HISTORY:
                tableName = MusicMediaDatabaseHelper.History.TABLE_NAME;
                break;
            case SCENES:
            case SCENES_ID:
                tableName = MusicMediaDatabaseHelper.Scenes.TABLE_NAME;
                break;
            default:
                break;
        }
        return db.delete(tableName, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mMusicDbHelper.getWritableDatabase();
        String tableName = "";
        int result = 0;
        switch (mUriMatcher.match(uri)) {
            case MEDIA_ALL:
            case MEDIA_SCANNED:
            case MEDIA_ID:
            case FAVORITE:
            case FAVORITE_ADD:
            case RECORDS:
            case LAST_ADD:
                tableName = MusicMediaDatabaseHelper.Media.TABLE_NAME;
                break;
            case ALBUMS:
            case ALBUMS_ID:
                tableName = MusicMediaDatabaseHelper.Albums.TABLE_NAME;
                break;
            case ARTISTS:
            case ARTISTS_ID:
                tableName = MusicMediaDatabaseHelper.Artists.TABLE_NAME;
                break;
            case FOLDERS:
            case FOLDERS_ID:
                tableName = MusicMediaDatabaseHelper.Folders.TABLE_NAME;
                break;
            case GENRES:
            case GENRES_ID:
                tableName = MusicMediaDatabaseHelper.Genres.TABLE_NAME;
                break;
            case PLAYLISTS:
            case PLAYLISTS_SONGS_FROM_PLAYLIST_ID:
            case PLAYLISTS_SONGS_FROM_PLAYLIST_ID_WITH_ROW_ID:
            case PLAYLISTS_ADD:
                tableName = MusicMediaDatabaseHelper.Playlists.TABLE_NAME;
                break;
            case PLAYLISTS_SONGS:
            case PLAYLISTS_SONGS_ID:
                tableName = MusicMediaDatabaseHelper.PlaylistSongs.TABLE_NAME;
                break;
            case QUEUE:
            case QUEUE_ID:
            case QUEUE_ADD:
                tableName = MusicMediaDatabaseHelper.Queue.TABLE_NAME;
                break;
            case HISTORY:
                tableName = MusicMediaDatabaseHelper.History.TABLE_NAME;
                break;
            case SCENES:
            case SCENES_ID:
                tableName = MusicMediaDatabaseHelper.Scenes.TABLE_NAME;
                break;
            default:
                break;
        }
        return db.update(tableName,contentValues,selection,selectionArgs);
    }

    @Nullable
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        LogUtil.d(TAG, "MediaContentProvider call method = " + method + ", arg = " + arg);
        if (CommonConstants.BULK_UPDATE_WITH_ID.equals(method)) {
            Uri updateUri = Uri.parse(extras.getString("Uri"));
            ContentValues[] values = (ContentValues[]) extras.getParcelableArray("ContentValues");
            List<String> idsToUpdate = extras.getStringArrayList("Ids");
            bulkUpdateWithId(updateUri, values, idsToUpdate);
            return null;
        } else if (CommonConstants.RANDOMIZE.equals(method)) {
            List<Integer> randomPlayOrder = extras.getIntegerArrayList("randomNumbers");
            bulkRandomize(randomPlayOrder);
        } else if (CommonConstants.ADD_NEW_TRACKS.equals(method)) {
            ContentValues[] values = (ContentValues[]) extras.getParcelableArray("ContentValues");
            addNewTracksToDB(values);
        }
        return super.call(method, arg, extras);
    }

    private void bulkUpdateWithId(Uri uri, ContentValues[] values, List<String> idsToUpdate) {
        String table = null;
        final int uriIndex = mUriMatcher.match(uri);
        final int valueSize = values.length;
        String whereClause = null;

        switch (uriIndex) {
            case MEDIA_ALL:
                table = MusicMediaDatabaseHelper.Media.TABLE_NAME;
                whereClause = MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID + " =?";
                break;
            case PLAYLISTS:
                table = MusicMediaDatabaseHelper.Playlists.TABLE_NAME;
                whereClause = MusicMediaDatabaseHelper.Playlists.PlaylistsColumns._ID + " =?";
                break;
            case PLAYLISTS_SONGS:
                table = MusicMediaDatabaseHelper.PlaylistSongs.TABLE_NAME;
                whereClause = MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.MEDIA_ID + " =?";
                break;
            case QUEUE:
                table = MusicMediaDatabaseHelper.Queue.TABLE_NAME;
                whereClause = MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID + " =?";
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        final String[] whereArgs = new String[1];
        SQLiteDatabase db = mMusicDbHelper.getWritableDatabase();
        db.beginTransactionNonExclusive();
        try {
            for (int i = 0; i < valueSize; i++) {
                whereArgs[0] = idsToUpdate.get(i);
                db.update(table, values[i], whereClause, whereArgs);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        LogUtil.d(TAG, "bulkInsert uri = " + uri);
        int numInserted = 0;
        String table = "";
        final int uriIndex = mUriMatcher.match(uri);
        final int valueSize = values.length;
        switch (uriIndex) {
            case MEDIA_ALL:
                table = MusicMediaDatabaseHelper.Media.TABLE_NAME;
                break;
            case PLAYLISTS:
                table = MusicMediaDatabaseHelper.Playlists.TABLE_NAME;
                break;
            case PLAYLISTS_SONGS:
                table = MusicMediaDatabaseHelper.PlaylistSongs.TABLE_NAME;
                break;
            case QUEUE:
                table = MusicMediaDatabaseHelper.Queue.TABLE_NAME;
                break;
            case HISTORY:
                table = MusicMediaDatabaseHelper.History.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        SQLiteDatabase db = mMusicDbHelper.getWritableDatabase();
        db.beginTransactionNonExclusive();
        try {
            for (int i = 0; i < valueSize; i++) {
                db.insertWithOnConflict(table, null, values[i], SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
            numInserted = valueSize;
        } finally {
            db.endTransaction();
        }
        LogUtil.d(TAG,"numInserted is " + numInserted);
        return numInserted;
    }

    private void addNewTracksToDB(ContentValues[] values) {
        SQLiteDatabase db = mMusicDbHelper.getWritableDatabase();
        final int valueSize = values.length;
        db.beginTransactionNonExclusive();
        try {
            final int numLastAddedRows = Math.min(valueSize, MusicMediaDatabaseHelper.Playlists.LAST_ADDED_TRACKS_LIMIT);
            int i = 0;
            for (i = 0; i < numLastAddedRows; i++) {
                long mediaId = db.insertWithOnConflict(MusicMediaDatabaseHelper.Media.TABLE_NAME, null, values[i], SQLiteDatabase.CONFLICT_REPLACE);
            }
            for (int j = i; j < valueSize; j++) {
                db.insertWithOnConflict(MusicMediaDatabaseHelper.Media.TABLE_NAME, null, values[j], SQLiteDatabase.CONFLICT_REPLACE);
            }
            Uri returnUri = MusicMediaDatabaseHelper.Media.CONTENT_URI.buildUpon().appendPath("Added").appendQueryParameter("numTracks", String.valueOf(valueSize)).build();
            getContext().getContentResolver().notifyChange(returnUri, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void bulkRandomize(List<Integer> randomPlayOrder) {
        SQLiteDatabase db = mMusicDbHelper.getWritableDatabase();
        db.beginTransactionNonExclusive();
        try {
            final int numTracks = randomPlayOrder.size();
            final String where = MusicMediaDatabaseHelper.Queue.QueueColumns.PLAY_ORDER + " = ?";
            final String[] whereArgs = new String[1];
            for (int i = 0; i < numTracks; i++) {
                ContentValues values = new ContentValues();
                values.put(MusicMediaDatabaseHelper.Queue.QueueColumns.RANDOM_PLAY_ORDER, randomPlayOrder.get(i));
                whereArgs[0] = String.valueOf(i + 1); // play Order starts as 1
                db.update(MusicMediaDatabaseHelper.Queue.TABLE_NAME, values, where, whereArgs);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
