package cn.tcl.music.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;

public class MusicMediaDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = MusicMediaDatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "MusicMedia.db";
    private SQLiteDatabase mDefaultWritableDatabse = null;

    private static final int DATABASE_VERSION = 1;
    private Context mContext;

    public MusicMediaDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        this.mDefaultWritableDatabse = sqLiteDatabase;
        createMediaTable(sqLiteDatabase);
        createFoldersTable(sqLiteDatabase);
        createArtistsView(sqLiteDatabase);
        createAlbumsView(sqLiteDatabase);
        createGenresView(sqLiteDatabase);
        createPlaylistsTable(sqLiteDatabase);
        createPlaylistSongsTable(sqLiteDatabase);
        createQueueTable(sqLiteDatabase);
        createHistoryTable(sqLiteDatabase);
        createTriggers(sqLiteDatabase);
        createScenesTable(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        final SQLiteDatabase db;
        if (mDefaultWritableDatabse != null) {
            db = mDefaultWritableDatabse;
        } else {
            db = super.getWritableDatabase();
        }
        return db;
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        final SQLiteDatabase db;
        if (mDefaultWritableDatabse != null) {
            db = mDefaultWritableDatabse;
        } else {
            db = super.getReadableDatabase();
        }
        return db;
    }

    private void createScenesTable(SQLiteDatabase db) {
        //create scenes table
        String sqlRequest = "CREATE TABLE IF NOT EXISTS " + Scenes.TABLE_NAME + " (" + Scenes.ScenesColumns._ID + " INTEGER PRIMARY KEY NOT NULL, ";
        sqlRequest += Scenes.ScenesColumns.SCENES_TITLE + " TEXT NOT NULL, ";
        sqlRequest += Scenes.ScenesColumns.SCENES_ICON + " TEXT NOT NULL, ";
        sqlRequest += Scenes.ScenesColumns.EXT_0 + " TEXT,";
        sqlRequest += Scenes.ScenesColumns.EXT_1 + " TEXT,";
        sqlRequest += Scenes.ScenesColumns.EXT_2 + " TEXT,";
        sqlRequest += Scenes.ScenesColumns.EXT_3 + " TEXT,";
        sqlRequest += Scenes.ScenesColumns.EXT_4 + " TEXT";
        sqlRequest += ");";

        db.execSQL(sqlRequest);

        //insert default data into scenes table  CommonConstants.SCENES_1
        db.execSQL("INSERT INTO " + Scenes.TABLE_NAME + " VALUES " + "(" + CommonConstants.SCENES_RUN_ID + ",'" + mContext.getResources().getString(R.string.scenes_1) + "','scenes1.png','','','','','');");
        db.execSQL("INSERT INTO " + Scenes.TABLE_NAME + " VALUES " + "(" + CommonConstants.SCENES_STUDY_ID + ",'" + mContext.getResources().getString(R.string.scenes_2) + "','scenes2.png','','','','','');");
        db.execSQL("INSERT INTO " + Scenes.TABLE_NAME + " VALUES " + "(" + CommonConstants.SCENES_WORK_ID + ",'" + mContext.getResources().getString(R.string.scenes_3) + "','scenes3.png','','','','','');");
        db.execSQL("INSERT INTO " + Scenes.TABLE_NAME + " VALUES " + "(" + CommonConstants.SCENES_DRIVE_ID + ",'" + mContext.getResources().getString(R.string.scenes_4) + "','scenes4.png','','','','','');");
        db.execSQL("INSERT INTO " + Scenes.TABLE_NAME + " VALUES " + "(" + CommonConstants.SCENES_TRAVEL_ID + ",'" + mContext.getResources().getString(R.string.scenes_5) + "','scenes5.png','','','','','');");
        db.execSQL("INSERT INTO " + Scenes.TABLE_NAME + " VALUES " + "(" + CommonConstants.SCENES_SLEEP_ID + ",'" + mContext.getResources().getString(R.string.scenes_6) + "','scenes6.png','','','','','');");
        db.execSQL("INSERT INTO " + Scenes.TABLE_NAME + " VALUES " + "(" + CommonConstants.SCENES_GATHER_ID + ",'" + mContext.getResources().getString(R.string.scenes_7) + "','scenes7.png','','','','','');");
        db.execSQL("INSERT INTO " + Scenes.TABLE_NAME + " VALUES " + "(" + CommonConstants.SCENES_TEA_ID + ",'" + mContext.getResources().getString(R.string.scenes_8) + "','scenes8.png','','','','','');");
        db.execSQL("INSERT INTO " + Scenes.TABLE_NAME + " VALUES " + "(" + CommonConstants.SCENES_CLUB_ID + ",'" + mContext.getResources().getString(R.string.scenes_9) + "','scenes9.png','','','','','');");
        db.execSQL("INSERT INTO " + Scenes.TABLE_NAME + " VALUES " + "(" + CommonConstants.SCENES_OTHER_ID + ",'" + mContext.getResources().getString(R.string.scenes_10) + "','scenes10.png','','','','','');");
    }


    private void createHistoryTable(SQLiteDatabase db) {

        String sqlRequest = "CREATE TABLE IF NOT EXISTS " + History.TABLE_NAME +
                " (" + History.HistoryColumns.SEARCHSTRING + " STRING NOT NULL,";
        sqlRequest += History.HistoryColumns.TIMESEARCHED + " LONG NOT NULL, ";
        sqlRequest += History.HistoryColumns.EXT_0 + " TEXT,";
        sqlRequest += History.HistoryColumns.EXT_1 + " TEXT,";
        sqlRequest += History.HistoryColumns.EXT_2 + " TEXT,";
        sqlRequest += History.HistoryColumns.EXT_3 + " TEXT,";
        sqlRequest += History.HistoryColumns.EXT_4 + " TEXT";
        sqlRequest += ");";

        db.execSQL(sqlRequest);
    }

    private void createMediaTable(SQLiteDatabase db) {
        String sqlRequest = "CREATE TABLE IF NOT EXISTS " + Media.TABLE_NAME + " (" + Media.MediaColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ";
        sqlRequest += Media.MediaColumns.AUDIO_ID + " TEXT NOT NULL, ";
        sqlRequest += Media.MediaColumns.ANDROID_AUDIO_ID + " INTEGER DEFAULT(-1), ";
        sqlRequest += Media.MediaColumns.SOURCE_TYPE + " INTEGER NOT NULL,";
        sqlRequest += Media.MediaColumns.TITLE + " TEXT,";
        sqlRequest += Media.MediaColumns.TITLE_KEY + " TEXT,";
        sqlRequest += Media.MediaColumns.ALBUM_ID + " INTEGER,";
        sqlRequest += Media.MediaColumns.ALBUM + " TEXT,";
        sqlRequest += Media.MediaColumns.ALBUM_KEY + " TEXT,";
        sqlRequest += Media.MediaColumns.ALBUM_TRACK + " INTEGER,";
        sqlRequest += Media.MediaColumns.ARTIST_ID + " INTEGER,";
        sqlRequest += Media.MediaColumns.ARTIST + " TEXT,";
        sqlRequest += Media.MediaColumns.FOLDER_ID + " TEXT,";
        sqlRequest += Media.MediaColumns.FOLDER_NAME + " TEXT,";
        sqlRequest += Media.MediaColumns.FOLDER_PATH + " TEXT,";
        sqlRequest += Media.MediaColumns.ARTIST_KEY + " TEXT,";
        sqlRequest += Media.MediaColumns.ARTIST_PORTRAIT + " TEXT, ";
        sqlRequest += Media.MediaColumns.GENRE_ID + " INTEGER,";
        sqlRequest += Media.MediaColumns.GENRE + " TEXT,";
        sqlRequest += Media.MediaColumns.GENRE_KEY + " TEXT,";
        sqlRequest += Media.MediaColumns.FAVORITE + " INTEGER DEFAULT(0),";
        sqlRequest += Media.MediaColumns.ARTWORK + " TEXT,";
        sqlRequest += Media.MediaColumns.COMMENTS + " TEXT,";
        sqlRequest += Media.MediaColumns.REMOTE_IMPORT_PATH + " TEXT,";
        sqlRequest += Media.MediaColumns.PATH + " TEXT,";
        sqlRequest += Media.MediaColumns.HARMONIC_KEY + " INTEGER DEFAULT(-1),";
        sqlRequest += Media.MediaColumns.BPM + " INTEGER,";
        sqlRequest += Media.MediaColumns.DURATION + " INTEGER DEFAULT(0), ";
        sqlRequest += Media.MediaColumns.SIZE + " INTEGER DEFAULT(0), ";
        sqlRequest += Media.MediaColumns.TRANSITION + " INTEGER DEFAULT(0), ";
        sqlRequest += Media.MediaColumns.DATE_ADD + " INTEGER, ";
        sqlRequest += Media.MediaColumns.SONG_REMOTE_ID + " TEXT, ";
        sqlRequest += Media.MediaColumns.DOWNLOADED + " TEXT, ";
        sqlRequest += Media.MediaColumns.SUFFIX + " TEXT, ";
        sqlRequest += Media.MediaColumns.URL + " TEXT, ";
        sqlRequest += Media.MediaColumns.LYRIC_PATH + " TEXT, ";
        sqlRequest += Media.MediaColumns.SCENE_ID + " TEXT, ";
        sqlRequest += Media.MediaColumns.FAVORITE_DATE + " TEXT, ";
        sqlRequest += Media.MediaColumns.EXT_0 + " TEXT, ";
        sqlRequest += Media.MediaColumns.EXT_1 + " TEXT, ";
        sqlRequest += Media.MediaColumns.EXT_2 + " TEXT, ";
        sqlRequest += Media.MediaColumns.EXT_3 + " TEXT, ";
        sqlRequest += Media.MediaColumns.EXT_4 + " TEXT, ";
        sqlRequest += "UNIQUE (" + Media.MediaColumns.AUDIO_ID + ", " + Media.MediaColumns.SOURCE_TYPE + ")";
        sqlRequest += ");";
        db.execSQL(sqlRequest);

        sqlRequest = "CREATE INDEX " + Media.MediaColumns.ANDROID_AUDIO_ID + "_idx ON " + Media.TABLE_NAME + "(" + Media.MediaColumns.ANDROID_AUDIO_ID + ");";
        db.execSQL(sqlRequest);

        sqlRequest = "CREATE INDEX " + Media.MediaColumns.AUDIO_ID + "_idx ON " + Media.TABLE_NAME + "(" + Media.MediaColumns.AUDIO_ID + ");";
        db.execSQL(sqlRequest);

        sqlRequest = "CREATE INDEX " + Media.MediaColumns.ARTIST_ID + "_idx ON " + Media.TABLE_NAME + "(" + Media.MediaColumns.ARTIST_ID + ");";
        db.execSQL(sqlRequest);

        sqlRequest = "CREATE INDEX " + Media.MediaColumns.ALBUM_ID + "_idx ON " + Media.TABLE_NAME + "(" + Media.MediaColumns.ALBUM_ID + ");";
        db.execSQL(sqlRequest);

        sqlRequest = "CREATE INDEX " + Media.MediaColumns.GENRE_ID + "_idx ON " + Media.TABLE_NAME + "(" + Media.MediaColumns.GENRE_ID + ");";
        db.execSQL(sqlRequest);
    }

    private void createArtistsView(SQLiteDatabase db) {
        String sqlRequest = "CREATE VIEW IF NOT EXISTS " + Artists.TABLE_NAME + " AS ";
        sqlRequest += "SELECT " + Media.MediaColumns.ARTIST_ID + " AS " + Artists.ArtistsColumns._ID + ", ";
        sqlRequest += Media.MediaColumns.ARTIST + ", ";
        sqlRequest += Media.MediaColumns.ARTIST_KEY + ", ";
        sqlRequest += Media.MediaColumns.ARTIST_PORTRAIT + ", ";
        sqlRequest += "COUNT(DISTINCT " + Media.MediaColumns.ALBUM_ID + ") AS " + Artists.ArtistsColumns.NUMBER_OF_ALBUMS + ", ";
        sqlRequest += "COUNT(*) AS " + Artists.ArtistsColumns.NUMBER_OF_TRACKS;
        sqlRequest += " FROM (select * from " + Media.TABLE_NAME + " left join " + Folders.TABLE_NAME + " on " +
                Media.TABLE_NAME + "." + Media.MediaColumns.FOLDER_ID + " = " + Folders.TABLE_NAME + "." + Folders.FoldersColumns._ID +
                " where " + Folders.FoldersColumns.IS_SCAN + " = 1" + ") temp";
        sqlRequest += " WHERE temp." + Media.MediaColumns.SOURCE_TYPE + " = " + CommonConstants.SRC_TYPE_LOCAL;
        sqlRequest += " OR temp." + Media.MediaColumns.SOURCE_TYPE + " = " + CommonConstants.SRC_TYPE_MYMIX;
        sqlRequest += " GROUP BY temp." + Media.MediaColumns.ARTIST_KEY;
        Log.d(TAG,"createArtistsView and sqlRequest is " + sqlRequest);
        db.execSQL(sqlRequest);
    }

    private void createFoldersTable(SQLiteDatabase db) {
        String sqlRequest = "CREATE TABLE IF NOT EXISTS " + Folders.TABLE_NAME +
                " (" + Folders.FoldersColumns._ID + " INTEGER PRIMARY KEY NOT NULL, ";
        sqlRequest += Folders.FoldersColumns.FOLDER_NAME + " TEXT NOT NULL, ";
        sqlRequest += Folders.FoldersColumns.FOLDER_PATH + " TEXT NOT NULL,";
        sqlRequest += Folders.FoldersColumns.FOLDER_SONGS_NUM  + " INTEGER DEFAULT(0),";
        sqlRequest += Folders.FoldersColumns.IS_SCAN  + " INTEGER DEFAULT(1) NOT NULL ,";
        sqlRequest += Folders.FoldersColumns.EXT_0 + " TEXT,";
        sqlRequest += Folders.FoldersColumns.EXT_1 + " TEXT,";
        sqlRequest += Folders.FoldersColumns.EXT_2 + " TEXT,";
        sqlRequest += Folders.FoldersColumns.EXT_3 + " TEXT,";
        sqlRequest += Folders.FoldersColumns.EXT_4 + " TEXT";
        sqlRequest += ");";
        db.execSQL(sqlRequest);
    }

    private void createAlbumsView(SQLiteDatabase db) {
        String sqlRequest = "CREATE VIEW IF NOT EXISTS " + Albums.TABLE_NAME + " AS ";
        sqlRequest += "SELECT " + Media.MediaColumns.ALBUM_ID + " AS " + Albums.AlbumColumns._ID + ", ";
        sqlRequest += Media.MediaColumns.ALBUM + ", ";
        sqlRequest += Media.MediaColumns.ALBUM_KEY + ", ";
        sqlRequest += Media.MediaColumns.ARTWORK + ", ";
        sqlRequest += Media.MediaColumns.ARTIST + ", ";
        sqlRequest += Media.MediaColumns.ARTIST_ID + ", ";
        sqlRequest += Media.MediaColumns.ARTIST_PORTRAIT + ", ";
        sqlRequest += Media.MediaColumns.ARTIST_KEY + ", ";
        sqlRequest += "COUNT(*) AS " + Albums.AlbumColumns.NUMBER_OF_TRACKS;
        sqlRequest += " FROM (select * from " + Media.TABLE_NAME + " left join " + Folders.TABLE_NAME + " on " +
                Media.TABLE_NAME + "." + Media.MediaColumns.FOLDER_ID + " = " + Folders.TABLE_NAME + "." + Folders.FoldersColumns._ID +
                " where " + Folders.FoldersColumns.IS_SCAN + " = 1" + ") temp";
        sqlRequest += " WHERE temp." + Media.MediaColumns.SOURCE_TYPE + " = " + CommonConstants.SRC_TYPE_LOCAL;
        sqlRequest += " OR temp." + Media.MediaColumns.SOURCE_TYPE + " = " + CommonConstants.SRC_TYPE_MYMIX;
        sqlRequest += " GROUP BY temp." + Media.MediaColumns.ALBUM_ID + ", temp." + Media.MediaColumns.ARTIST_ID;

        Log.d(TAG,"createAlbumsView and sqlRequest is " + sqlRequest);

        db.execSQL(sqlRequest);
    }


    private void createGenresView(SQLiteDatabase db) {
        String sqlRequest = "CREATE VIEW IF NOT EXISTS " + Genres.TABLE_NAME + " AS ";
        sqlRequest += "SELECT " + Media.MediaColumns.GENRE_ID + " AS " + Albums.AlbumColumns._ID + ", ";
        sqlRequest += Media.MediaColumns.GENRE + ", ";
        sqlRequest += Media.MediaColumns.GENRE_KEY + ", ";
        sqlRequest += "COUNT(*) AS " + Genres.GenresColumns.NUMBER_OF_TRACKS;
        sqlRequest += " FROM " + Media.TABLE_NAME + " WHERE " + Media.MediaColumns.GENRE_ID + " IS NOT NULL ";
        sqlRequest += " AND " + Media.MediaColumns.SOURCE_TYPE + " = " + CommonConstants.SRC_TYPE_LOCAL;
        sqlRequest += " GROUP BY " + Media.MediaColumns.GENRE_KEY;
        db.execSQL(sqlRequest);
    }

    private void createPlaylistsTable(SQLiteDatabase db) {
        String sqlRequest = "CREATE TABLE IF NOT EXISTS " + Playlists.TABLE_NAME + " (" + Playlists.PlaylistsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ";
        sqlRequest += Playlists.PlaylistsColumns.NAME + " TEXT,";
        sqlRequest += Playlists.PlaylistsColumns.SOURCE + " TEXT,";
        sqlRequest += Playlists.PlaylistsColumns.ARTWORK + " TEXT,";
        sqlRequest += Playlists.PlaylistsColumns.DESCRIPTION + " TEXT,";
        sqlRequest += Playlists.PlaylistsColumns.PATH + " TEXT,";
        sqlRequest += Playlists.PlaylistsColumns.TYPE + " INTEGER DEFAULT(0),";
        sqlRequest += Playlists.PlaylistsColumns.EXT_0 + " TEXT, ";
        sqlRequest += Playlists.PlaylistsColumns.EXT_1 + " TEXT, ";
        sqlRequest += Playlists.PlaylistsColumns.EXT_2 + " TEXT, ";
        sqlRequest += Playlists.PlaylistsColumns.EXT_3 + " TEXT, ";
        sqlRequest += Playlists.PlaylistsColumns.EXT_4 + " TEXT";
        sqlRequest += ");";
        db.execSQL(sqlRequest);

        // Recently Played Playlist
        ContentValues recentlyPlayedValues = new ContentValues();
        recentlyPlayedValues.put(BaseColumns._ID, CommonConstants.RECENTLY_PLAYED_PLAYLIST_ID);
        recentlyPlayedValues.put(Playlists.PlaylistsColumns.NAME, "Recently Played");
        recentlyPlayedValues.put(Playlists.PlaylistsColumns.TYPE, Playlists.AUTOMATIC_TYPE);
        db.insert(Playlists.TABLE_NAME, null, recentlyPlayedValues);

        //删除歌单后，立马删除PlaylistSongs表相关的数据
        String createDeleteOfPlaylistTrigger = "create trigger delete_on_playlist after delete on playlists";
        createDeleteOfPlaylistTrigger += " Begin";
        createDeleteOfPlaylistTrigger += " delete from playlistsongs where playlist_id = old._id;";
        createDeleteOfPlaylistTrigger += " End;";
        db.execSQL(createDeleteOfPlaylistTrigger);
    }

    private void createPlaylistSongsTable(SQLiteDatabase db) {
        String sqlRequest = "CREATE TABLE IF NOT EXISTS " + PlaylistSongs.TABLE_NAME +
                " (" + PlaylistSongs.PlaylistSongsColumns._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ";
        sqlRequest += PlaylistSongs.PlaylistSongsColumns.MEDIA_ID + " INTEGER, ";
        sqlRequest += PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + " INTEGER, ";
        sqlRequest += PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER + " INTEGER DEFAULT(0),";
        sqlRequest += PlaylistSongs.PlaylistSongsColumns.TRANSITION + " INTEGER DEFAULT(0), ";
        sqlRequest += PlaylistSongs.PlaylistSongsColumns.EXT_0 + " TEXT, ";
        sqlRequest += PlaylistSongs.PlaylistSongsColumns.EXT_1 + " TEXT, ";
        sqlRequest += PlaylistSongs.PlaylistSongsColumns.EXT_2 + " TEXT, ";
        sqlRequest += PlaylistSongs.PlaylistSongsColumns.EXT_3 + " TEXT, ";
        sqlRequest += PlaylistSongs.PlaylistSongsColumns.EXT_4 + " TEXT, ";
        sqlRequest += "CONSTRAINT " + PlaylistSongs.UNIQUE_KEY + " UNIQUE (" + PlaylistSongs.PlaylistSongsColumns.MEDIA_ID + ", " + PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + "), ";
        sqlRequest += "FOREIGN KEY (" + PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + ") REFERENCES " + Playlists.TABLE_NAME + "(" + BaseColumns._ID + ") ON DELETE CASCADE ";
        sqlRequest += ");";
        db.execSQL(sqlRequest);

        sqlRequest = "CREATE INDEX " + PlaylistSongs.TABLE_NAME + "_" + PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER + "_idx ON " + PlaylistSongs.TABLE_NAME + "(" + PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER + ");";
        db.execSQL(sqlRequest);

        sqlRequest = "CREATE UNIQUE INDEX " + PlaylistSongs.UNIQUE_KEY + " on " + PlaylistSongs.TABLE_NAME + "(" + PlaylistSongs.PlaylistSongsColumns.MEDIA_ID + " , " + PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + ");";
        db.execSQL(sqlRequest);

        createIncreaseTrackNumberTrigger(db, "increment_playlist_songs", PlaylistSongs.TABLE_NAME, PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER, true);
        createDecreaseTrackNumberTrigger(db, "decrement_playlist_songs", PlaylistSongs.TABLE_NAME, PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER, true);
    }

    private void createQueueTable(SQLiteDatabase db) {
        String sqlRequest = "CREATE TABLE IF NOT EXISTS " + Queue.TABLE_NAME + " (" + Queue.QueueColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ";
        sqlRequest += Queue.QueueColumns.MEDIA_ID + " INTEGER,";
        sqlRequest += Queue.QueueColumns.PLAY_ORDER + " INTEGER DEFAULT(0),";
        sqlRequest += Queue.QueueColumns.RANDOM_PLAY_ORDER + " INTEGER DEFAULT(0),";
        sqlRequest += Queue.QueueColumns.IS_EFFECTIVE + " INTEGER DEFAULT(1),";
        sqlRequest += Queue.QueueColumns.TRANSITION + " INTEGER DEFAULT(0),";
        sqlRequest += Queue.QueueColumns.AUTO_ADDED + " INTEGER DEFAULT(0),";
        sqlRequest += Queue.QueueColumns.EXT_0 + " TEXT, ";
        sqlRequest += Queue.QueueColumns.EXT_1 + " TEXT, ";
        sqlRequest += Queue.QueueColumns.EXT_2 + " TEXT, ";
        sqlRequest += Queue.QueueColumns.EXT_3 + " TEXT, ";
        sqlRequest += Queue.QueueColumns.EXT_4 + " TEXT";
        sqlRequest += ");";

        db.execSQL(sqlRequest);
        sqlRequest = "CREATE INDEX " + Queue.TABLE_NAME + "_" + Queue.QueueColumns.PLAY_ORDER + "_idx ON " + Queue.TABLE_NAME + "(" + Queue.QueueColumns.PLAY_ORDER + ");";

        db.execSQL(sqlRequest);
        sqlRequest = "CREATE INDEX " + Queue.TABLE_NAME + "_" + Queue.QueueColumns.RANDOM_PLAY_ORDER + "_idx ON " + Queue.TABLE_NAME + "(" + Queue.QueueColumns.RANDOM_PLAY_ORDER + ");";
        db.execSQL(sqlRequest);
        sqlRequest = "CREATE INDEX " + Queue.TABLE_NAME + "_" + Queue.QueueColumns.AUTO_ADDED + "_idx ON " + Queue.TABLE_NAME + "(" + Queue.QueueColumns.AUTO_ADDED + ");";
        db.execSQL(sqlRequest);

        createIncreaseTrackNumberTrigger(db, "increment_queue", Queue.TABLE_NAME, Queue.QueueColumns.PLAY_ORDER, false);
        //createDecreaseTrackNumberTrigger(db, "decrement_queue", Queue.TABLE_NAME, Queue.QueueColumns.PLAY_ORDER, false);
        createIncreaseTrackNumberTrigger(db, "increment_queue_random", Queue.TABLE_NAME, Queue.QueueColumns.RANDOM_PLAY_ORDER, false);
        //createDecreaseTrackNumberTrigger(db, "decrement_queue_random", Queue.TABLE_NAME, Queue.QueueColumns.RANDOM_PLAY_ORDER, false);

    }

    public void createIncreaseTrackNumberTrigger(SQLiteDatabase db, String triggerName, String tableName, String columnName, boolean playlistIdClause) {
        // Trigger which increases TrackNumber
        String sqlRequest = "create trigger if not exists " + triggerName + " after insert on " + tableName;
        sqlRequest += " Begin ";
        sqlRequest += " Update " + tableName + " SET " + columnName + " = ( 1 + ";
        sqlRequest += " (Select Max(" + columnName + ") from " + tableName;
        if (playlistIdClause) {
            sqlRequest += " Where " + PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + " = new." + PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID;
        }
        sqlRequest += " ) ) ";
        sqlRequest += " Where " + BaseColumns._ID + " = new." + BaseColumns._ID + " AND " + columnName + " <= 0;";
        sqlRequest += " End; ";
        Log.d(TAG,"createIncreaseTrackNumberTrigger and sqlRequest is " + sqlRequest);
        db.execSQL(sqlRequest);

    }

    public void createDecreaseTrackNumberTrigger(SQLiteDatabase db, String triggerName, String tableName, String columnName, boolean playlistIdClause) {
        String sqlRequest = "create trigger if not exists " + triggerName + " before delete on " + tableName;
        sqlRequest += " Begin ";
        sqlRequest += " Update " + tableName + " SET " + columnName + " = " + columnName + " - 1";
        sqlRequest += " Where " + columnName + "> old." + columnName;
        if (playlistIdClause) {
            sqlRequest += " AND " + PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + " =  old." + PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID;
        }
        sqlRequest += ";";
        sqlRequest += " End; ";
        db.execSQL(sqlRequest);
    }

    /**
     * 创建触发器
     */
    public void createTriggers(SQLiteDatabase db) {
        //新增歌曲的触发器
        String createInsertMediaTrigger = "create trigger if not exists increment_media_songs after insert on media for each row when new.folder_name is not null";
        createInsertMediaTrigger += " Begin";
        createInsertMediaTrigger += " replace into folders values (new.folder_id,new.folder_name,new.folder_path,(select count (*) from media where folder_id = new.folder_id),(select is_scan from folders where _id = new.folder_id),'','','','','');";
        createInsertMediaTrigger += " End;";
        db.execSQL(createInsertMediaTrigger);

        //删除歌曲的触发器
        String createDeleteMediaTrigger = "create trigger if not exists delete_media_songs after delete on media for each row ";
        createDeleteMediaTrigger += " Begin";
        createDeleteMediaTrigger += " update folders set folder_songs_num = (select count (*) from media where folder_id = old.folder_id) where _id = old.folder_id ;";
        createDeleteMediaTrigger += " delete from folders where folder_songs_num = 0;";
        createDeleteMediaTrigger += " delete from playlistsongs where media_id = old.audio_id;";
        createDeleteMediaTrigger += " End;";
        db.execSQL(createDeleteMediaTrigger);

        //更新歌曲文件夹名的触发器
        String createUpdateMediaTrigger = "create trigger if not exists update_media_folder_name after update of folder_name on media";
        createUpdateMediaTrigger += " Begin";
        createUpdateMediaTrigger += " update folders set folder_name = new.folder_name,folder_path = new.folder_path where _id = new.folder_id;";
        createUpdateMediaTrigger += " End;";
        db.execSQL(createUpdateMediaTrigger);


        //更新文件夹是否扫描触发器
        String createUpdateIsScanOfFolderTrigger = "create trigger update_is_scan_of_folder after update of is_scan on folders";
        createUpdateIsScanOfFolderTrigger += " Begin";
        createUpdateIsScanOfFolderTrigger += " update queue set is_effective = new.is_scan where media_id in (select audio_id from media where folder_id = new._id);";
        createUpdateIsScanOfFolderTrigger += " End;";
        db.execSQL(createUpdateIsScanOfFolderTrigger);
    }

    /**
     * media table
     */
    public final static class Media {
        public static final String TABLE_NAME = "Media";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mixvibes.medias";
        public static final Uri CONTENT_URI = Uri.parse("content://" + MediaContentProvider.AUTHORITY + "/" + TABLE_NAME);
        public static final Uri CONTENT_URI_NOT_IGNORED = CONTENT_URI.buildUpon().appendPath("NotIgnored").build();

        public interface MediaColumns extends android.provider.BaseColumns {
            String AUDIO_ID = "audio_id";                   //this id created by user:for localmusic it just like android_audio_id;for online music,it record the songid of xiami
            String ANDROID_AUDIO_ID = "android_audio_id";   //
            String SOURCE_TYPE = "source_type";             //0 local  1 : Our Records 2 online
            String TITLE = "title";
            String TITLE_KEY = "title_key";
            String ALBUM_ID = "album_id";
            String ALBUM = "album";
            String ALBUM_KEY = "album_key";
            String ALBUM_TRACK = "album_track";
            String ARTIST_ID = "artist_id";
            String ARTIST = "artist";
            String ARTIST_KEY = "artist_key";
            String ARTIST_PORTRAIT = "artist_portrait";
            String GENRE_ID = "genre_id";
            String FOLDER_ID = "folder_id";
            String FOLDER_PATH = "folder_path";
            String FOLDER_NAME = "folder_name";
            String GENRE = "genre";
            String GENRE_KEY = "genre_key";
            String FAVORITE = "favorite";
            String ARTWORK = "artwork";
            String COMMENTS = "comments";
            String PATH = "path";
            String REMOTE_IMPORT_PATH = "remote_import_path";
            String HARMONIC_KEY = "harmonic_key";
            String BPM = "bpm";
            String DURATION = "duration";
            String SIZE = "size";
            String TRANSITION = "transition";
            String DATE_ADD = "date_add";
            String SONG_REMOTE_ID = "song_remote_id";       //保存在线歌曲id
            String DOWNLOADED = "downloaded";               //在线歌曲是否已下载
            String SUFFIX = "suffix";                       //文件后缀名
            String URL = "url";                             //歌曲url
            String LYRIC_PATH = "lyric_path";               //歌词下载路径
            String SCENE_ID = "scene_id";                   //relate to the _id column of scenes table
            String FAVORITE_DATE = "favorite_date";         //record add to my favorite time

            String EXT_0 = "ext_0";             //extention column
            String EXT_1 = "ext_1";             //extention column
            String EXT_2 = "ext_2";             //extention column
            String EXT_3 = "ext_3";             //extention column
            String EXT_4 = "ext_4";             //extention column
        }
    }

    public final static class Artists {
        public static final String TABLE_NAME = "Artists";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mixvibes.medias";
        public static final Uri CONTENT_URI = Uri.parse("content://" + MediaContentProvider.AUTHORITY + "/" + TABLE_NAME);

        public interface ArtistsColumns extends android.provider.BaseColumns {
            String ARTIST = "artist";
            String ARTIST_KEY = "artist_key";
            String ARTIST_PORTRAIT = "artist_portrait";
            String NUMBER_OF_ALBUMS = "number_of_albums";
            String NUMBER_OF_TRACKS = "number_of_tracks";
        }
    }

    public final static class Folders {
        public static final String TABLE_NAME = "Folders";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mixvibes.medias";
        //public static final Uri CONTENT_URI = Uri.parse("content://" + "media/external"+ "/" + TABLE_NAME);
        public static final Uri CONTENT_URI = Uri.parse("content://" + MediaContentProvider.AUTHORITY + "/" + TABLE_NAME);

        public interface FoldersColumns extends android.provider.BaseColumns {
            String FOLDER_NAME = "folder_name";
            String FOLDER_PATH = "folder_path";
            String FOLDER_SONGS_NUM = "folder_songs_num";
            String IS_SCAN = "is_scan";

            String EXT_0 = "ext_0";             //extention column
            String EXT_1 = "ext_1";             //extention column
            String EXT_2 = "ext_2";             //extention column
            String EXT_3 = "ext_3";             //extention column
            String EXT_4 = "ext_4";             //extention column
        }
    }

    public final static class Albums {
        public static final String TABLE_NAME = "Albums";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mixvibes.medias";
        public static final Uri CONTENT_URI = Uri.parse("content://" + MediaContentProvider.AUTHORITY + "/" + TABLE_NAME);

        public interface AlbumColumns extends android.provider.BaseColumns {
            String ALBUM = "album";
            String ALBUM_KEY = "album_key";
            String ARTWORK = "artwork";
            String ARTIST = "artist";
            String ARTIST_ID = "artist_id";
            String ARTIST_KEY = "artist_key";
            String ARTIST_PORTRAIT = "artist_portrait";
            String NUMBER_OF_TRACKS = "number_of_tracks";
        }

        public static final String DEFAULT_SORT_ORDER = AlbumColumns.ALBUM_KEY;
    }

    public final static class Genres {
        public static final String TABLE_NAME = "Genres";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mixvibes.medias";
        public static final Uri CONTENT_URI = Uri.parse("content://" + MediaContentProvider.AUTHORITY + "/" + TABLE_NAME);

        public interface GenresColumns extends android.provider.BaseColumns {
            String GENRE = "genre";
            String GENRE_KEY = "genre_key";
            String NUMBER_OF_TRACKS = "number_of_tracks";
        }

        public static final String DEFAULT_SORT_ORDER = GenresColumns.GENRE_KEY;
    }

    public final static class Playlists {
        public static final String TABLE_NAME = "Playlists";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mixvibes.medias";
        public static final String RECENTLY_PLAYED_PLAYLIST = "Recently Played";
        public static final long LAST_ADDED_PLAYLIST_ID = 1;
        public static final Uri FAVORITE_URI = Media.CONTENT_URI.buildUpon().appendPath("Favorite").build();
        public static final Uri RECORDS_URI = Media.CONTENT_URI.buildUpon().appendPath("Records").build();
        public static final Uri LAST_ADD_URI = Media.CONTENT_URI.buildUpon().appendPath("LastAdd").build();
        public static final int LAST_ADDED_TRACKS_LIMIT = 100;
        public static final int FROM_USER_TYPE = 0;
        public static final int AUTOMATIC_TYPE = 1;
        public static final Uri CONTENT_URI = Uri.parse("content://" + MediaContentProvider.AUTHORITY + "/" + TABLE_NAME);

        public interface PlaylistsColumns extends android.provider.BaseColumns {
            String NAME = "name";
            String ARTWORK = "artwork";
            String SOURCE = "source";
            String DESCRIPTION = "description";
            String PATH = "path";
            String TYPE = "type";

            String EXT_0 = "ext_0";             //extention column
            String EXT_1 = "ext_1";             //extention column
            String EXT_2 = "ext_2";             //extention column
            String EXT_3 = "ext_3";             //extention column
            String EXT_4 = "ext_4";             //extention column
        }

        public static final String DEFAULT_SORT_ORDER = PlaylistsColumns.NAME;
    }

    public final static class PlaylistSongs {
        public static final String TABLE_NAME = "PlaylistSongs";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mixvibes.medias";
        public static final Uri CONTENT_URI = Uri.parse("content://" + MediaContentProvider.AUTHORITY + "/" + TABLE_NAME);
        public static final Uri CONTENT_URI_PLAYLISTSONGS_MEDIA = PlaylistSongs.CONTENT_URI.buildUpon().appendPath("Media").build();
        public static final String UNIQUE_KEY = "media_playlist";

        public interface PlaylistSongsColumns extends android.provider.BaseColumns {
            String MEDIA_ID = "media_id";
            String PLAYLIST_ID = "playlist_id";
            String PLAY_ORDER = "play_order";
            String TRANSITION = "transition";

            String EXT_0 = "ext_0";             //extention column
            String EXT_1 = "ext_1";             //extention column
            String EXT_2 = "ext_2";             //extention column
            String EXT_3 = "ext_3";             //extention column
            String EXT_4 = "ext_4";             //extention column
        }

        public static final String DEFAULT_SORT_ORDER = PlaylistSongsColumns.PLAY_ORDER;
    }

    public final static class Queue {
        public static final String TABLE_NAME = "Queue";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mixvibes.medias";
        public static final Uri CONTENT_URI = Uri.parse("content://" + MediaContentProvider.AUTHORITY + "/" + TABLE_NAME);
        public static final Uri CONTENT_URI_QUEUE_MEDIA = Queue.CONTENT_URI.buildUpon().appendPath("Media").build();

        public interface QueueColumns extends android.provider.BaseColumns {
            String MEDIA_ID = "media_id";
            String PLAY_ORDER = "play_order";
            String RANDOM_PLAY_ORDER = "random_play_order";
            String IS_EFFECTIVE = "is_effective";
            String TRANSITION = "transition";
            String AUTO_ADDED = "auto_added";

            String EXT_0 = "ext_0";             //extention column
            String EXT_1 = "ext_1";             //extention column
            String EXT_2 = "ext_2";             //extention column
            String EXT_3 = "ext_3";             //extention column
            String EXT_4 = "ext_4";             //extention column
        }
    }

    public final static class History {
        public static final String TABLE_NAME = "Searchhistory";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mixvibes.medias";
        public static final Uri CONTENT_URI = Uri.parse("content://" + MediaContentProvider.AUTHORITY + "/" + TABLE_NAME);

        public interface HistoryColumns {
            String SEARCHSTRING = "searchstring";
            String TIMESEARCHED = "timesearched";

            String EXT_0 = "ext_0";             //extention column
            String EXT_1 = "ext_1";             //extention column
            String EXT_2 = "ext_2";             //extention column
            String EXT_3 = "ext_3";             //extention column
            String EXT_4 = "ext_4";             //extention column
        }
    }

    public final static class Scenes {
        public static final String TABLE_NAME = "scenes";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mixvibes.medias";
        public static final Uri CONTENT_URI = Uri.parse("content://" + MediaContentProvider.AUTHORITY + "/" + TABLE_NAME);

        public interface ScenesColumns extends android.provider.BaseColumns {
            String SCENES_TITLE = "scenes_title";
            String SCENES_ICON = "scenes_icon";

            String EXT_0 = "ext_0";             //extention column
            String EXT_1 = "ext_1";             //extention column
            String EXT_2 = "ext_2";             //extention column
            String EXT_3 = "ext_3";             //extention column
            String EXT_4 = "ext_4";             //extention column
        }
    }
}
