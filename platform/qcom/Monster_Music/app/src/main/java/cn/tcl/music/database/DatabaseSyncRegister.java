package cn.tcl.music.database;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LongSparseArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;

public final class DatabaseSyncRegister extends ContentObserver {
    private final static String TAG = DatabaseSyncRegister.class.getSimpleName();
    /**
     * filter: the minimum size of file
     **/
    private final static String LOCAL_MEDIA_MIN_SIZE = String.valueOf(500 * 1024);
    // We prefer to limit row query, since it can take a lot into memory.
    protected static int ROW_LIMIT = 1000;
    private static final String DATABASE_SYNC_THREAD_NAME = "DatabaseSyncThread";

    private final static String[] sOwnMusicMediaColumnNames = new String[]{
            MusicMediaDatabaseHelper.Media.MediaColumns.PATH,
            MusicMediaDatabaseHelper.Media.MediaColumns.TITLE,
            MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_KEY,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_TRACK,
            MusicMediaDatabaseHelper.Media.MediaColumns.DURATION,
            MusicMediaDatabaseHelper.Media.MediaColumns.DATE_ADD,
            MusicMediaDatabaseHelper.Media.MediaColumns.SIZE,
            MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID,
            MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID
    };

    private final static String[] sAndroidMediaColumnNames = new String[]{
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.TITLE_KEY,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.ARTIST_ID,
            MediaStore.Audio.AudioColumns.ARTIST_KEY,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.ALBUM_KEY,
            MediaStore.Audio.AudioColumns.TRACK,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.DATE_ADDED,
            MediaStore.Audio.AudioColumns.SIZE,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns._ID
    };

    protected volatile Looper mDBSyncLooper;
    protected Handler mSyncHandler;
    protected Object mSyncLock = new Object();
    private Context mContext;
    private static DatabaseSyncRegister mDatabaseSyncRegister;

    public static DatabaseSyncRegister getInstance(Context context) {
        if (mDatabaseSyncRegister == null) {
            mDatabaseSyncRegister = new DatabaseSyncRegister(context);
        }
        return mDatabaseSyncRegister;
    }

    protected Runnable mMediaSyncRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mSyncLock) {
                onSync();
            }
        }
    };

    public DatabaseSyncRegister(Context context) {
        super(new Handler(Looper.getMainLooper()));
        mContext = context;
    }

    public void startListeningDBSync() {
        HandlerThread thread = new HandlerThread(DATABASE_SYNC_THREAD_NAME);
        thread.start();
        mDBSyncLooper = thread.getLooper();

        mSyncHandler = new Handler(mDBSyncLooper);
        mContext.getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, false, this);
        mContext.getContentResolver().registerContentObserver(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, false, this);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.setPriority(1000);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        mContext.registerReceiver(broadcastReceiver, intentFilter);
        launchSync();
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d(TAG, "action is :" + action);
            if (action.equals(Intent.ACTION_MEDIA_REMOVED) || action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
                //TODO SD card removed
            }
        }
    };

    public void stopListeningDBSync() {
        mContext.getContentResolver().unregisterContentObserver(this);
        mDBSyncLooper.quit();
        try {
            mContext.unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            LogUtil.e(TAG, "Exception msg : " + e.getMessage());
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        LogUtil.d(TAG, "uri is " + uri.toString());
        if (uri == null) {
            return;
        }
        launchSync();
    }

    private void launchSync() {
        mSyncHandler.post(mMediaSyncRunnable);
    }


    protected void onSync() {
        LogUtil.d(TAG, "onSync()");
        long lastRowIdReached = -1;
        long oldLastRowIdReached = -1;

        long timeStart = SystemClock.currentThreadTimeMillis();
        long timeStartProcess = timeStart;
        ContentResolver contentResolver = mContext.getContentResolver();
        //if user set ringtone form Settings by music,here need to check the permission.
        int hasPermission = mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            Cursor androidNumMediaCursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{"Count(*)"},
                    "_data not in (select _data from audio where _data like ? )" + " AND " + MediaStore.Audio.Media._ID + " > ?" + " AND " + MediaStore.Audio.AudioColumns.SIZE + " > " + LOCAL_MEDIA_MIN_SIZE + "",
                    new String[]{"/storage/emulated/0/Android/%"},
                    null);
            if (!androidNumMediaCursor.moveToFirst()) {
                androidNumMediaCursor.close();
                return;
            }
            int numTracks = androidNumMediaCursor.getInt(0);
            androidNumMediaCursor.close();

            LongSparseArray<String> albumArtworks = new LongSparseArray<>();
            Cursor androidAlbumsArtworkMediaBase = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART}, null, null, MediaStore.Audio.Albums.ALBUM_KEY);
            if (androidAlbumsArtworkMediaBase != null) {
                while (androidAlbumsArtworkMediaBase.moveToNext()) {
                    String albumArtwork = androidAlbumsArtworkMediaBase.getString(androidAlbumsArtworkMediaBase.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                    albumArtworks.put(androidAlbumsArtworkMediaBase.getLong(androidAlbumsArtworkMediaBase.getColumnIndex(MediaStore.Audio.Albums._ID)), albumArtwork);
                }
                androidAlbumsArtworkMediaBase.close();
            }

            List<ContentValues> valuesToAdd = new ArrayList<ContentValues>();
            List<ContentValues> valuesToUpdate = new ArrayList<ContentValues>();
            ArrayList<String> idsToUpdate = new ArrayList<String>();
            int numTracksProcessed = 0;
            do {
                timeStart = SystemClock.currentThreadTimeMillis();
                valuesToAdd.clear();
                valuesToUpdate.clear();
                idsToUpdate.clear();
                StringBuilder idsToDelete = new StringBuilder();
                Cursor androidMediaBase = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        sAndroidMediaColumnNames,
                        "_data not in (select _data from audio where _data like ? )" +
                                " AND " + MediaStore.Audio.Media._ID + " > ?" +
                                " AND " + MediaStore.Audio.AudioColumns.SIZE + " > " + LOCAL_MEDIA_MIN_SIZE + "",
                        new String[]{"/storage/emulated/0/Android/%", String.valueOf(lastRowIdReached)},
                        MediaStore.Audio.Media._ID + " LIMIT " + ROW_LIMIT);

                if (androidMediaBase == null) {
                    LogUtil.d(TAG, "androidMediaBase is null");
                    return;
                }
                LogUtil.d(TAG, "androidMediaBase count is " + androidMediaBase.getCount());
                numTracksProcessed += androidMediaBase.getCount();

                if (!androidMediaBase.moveToLast()) {
                    if (androidMediaBase.getCount() == 0) { // It seems we have no tracks anymore, let's delete all from android !
                        contentResolver.delete(MusicMediaDatabaseHelper.Media.CONTENT_URI, MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE + " <= ?", new String[]{String.valueOf(CommonConstants.SRC_TYPE_MYMIX)});
                    }
                    androidMediaBase.close();
                    break;
                }
                oldLastRowIdReached = lastRowIdReached;
                lastRowIdReached = androidMediaBase.getLong(androidMediaBase.getColumnIndex(MediaStore.Audio.AudioColumns._ID));


                String[] projection = new String[]{MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID,
                        MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID,
                        MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_ID,
                        MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID,
                        MusicMediaDatabaseHelper.Media.MediaColumns.PATH,
                        MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE
                };
                Cursor ownMusicMediaBase = contentResolver.query(MusicMediaDatabaseHelper.Media.CONTENT_URI,
                        projection,
                        MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE + "<= ? AND " +
                                MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID + " > ? AND " +
                                MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID + "  <= ?",
                        new String[]{String.valueOf(CommonConstants.SRC_TYPE_MYMIX), String.valueOf(oldLastRowIdReached), String.valueOf(lastRowIdReached)},
                        MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID);
                doFullSync(valuesToAdd, valuesToUpdate, idsToUpdate, idsToDelete, albumArtworks, androidMediaBase, ownMusicMediaBase);
                androidMediaBase.close();
                ownMusicMediaBase.close();

                long timeFullSync = SystemClock.currentThreadTimeMillis() - timeStart;
                LogUtil.i(TAG, "time Full Sync  : " + timeFullSync);
                timeStart = SystemClock.currentThreadTimeMillis();

                if (idsToDelete.length() > 0) {
                    String whereClause = MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID + " IN ( " + idsToDelete.substring(0, idsToDelete.length() - 1) + " )";
                    contentResolver.delete(MusicMediaDatabaseHelper.Media.CONTENT_URI,
                            whereClause, null);
                }

                if (!valuesToUpdate.isEmpty()) {
                    Bundle updateCallBundle = new Bundle();
                    updateCallBundle.putString("Uri", MusicMediaDatabaseHelper.Media.CONTENT_URI.toString());
                    updateCallBundle.putStringArrayList("Ids", idsToUpdate);
                    updateCallBundle.putParcelableArray("ContentValues", valuesToUpdate.toArray(new ContentValues[valuesToUpdate.size()]));
                    contentResolver.call(MusicMediaDatabaseHelper.Media.CONTENT_URI, CommonConstants.BULK_UPDATE_WITH_ID, null, updateCallBundle);
                }

                if (!valuesToAdd.isEmpty()) {
                    Bundle addNewTracksCallBundle = new Bundle();
                    addNewTracksCallBundle.putParcelableArray("ContentValues", valuesToAdd.toArray(new ContentValues[valuesToAdd.size()]));
                    contentResolver.call(MusicMediaDatabaseHelper.Media.CONTENT_URI, CommonConstants.ADD_NEW_TRACKS, null, addNewTracksCallBundle);
                }

                valuesToUpdate.clear();
                idsToUpdate.clear();
                long timeUpdateGenre = SystemClock.currentThreadTimeMillis() - timeStart;
                LogUtil.i(TAG, "time Update genre  : " + timeUpdateGenre);

            } while (numTracksProcessed < numTracks);

            // We were not able to reach rows with Id > last rowId in AndroidMediaStore. So Delete them
            if (lastRowIdReached >= 0) {
                String whereClause = MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID + " > ? AND ";
                whereClause += MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE + " <= ?";
                contentResolver.delete(MusicMediaDatabaseHelper.Media.CONTENT_URI,
                        whereClause, new String[]{String.valueOf(lastRowIdReached), String.valueOf(CommonConstants.SRC_TYPE_MYMIX)});

            }
            long timeProcessed = SystemClock.currentThreadTimeMillis() - timeStartProcess;
            LogUtil.i(TAG, "time Total Process  : " + timeProcessed + " for " + numTracks + " tracks");
        }
    }

    private boolean hasFile(String filepath) {
        return new File(filepath).exists();
    }

    private void doFullSync(List<ContentValues> valuesToAdd,
                            List<ContentValues> valuesToUpdate,
                            List<String> idsToUpdate,
                            StringBuilder idsToDeleteStr,
                            LongSparseArray<String> albumArtworks,
                            Cursor androidMediaBase,
                            Cursor ownMediaBase) {
        CursorIdJoiner joiner = new CursorIdJoiner(androidMediaBase, ownMediaBase);
        for (CursorIdJoiner.Result joinerResult : joiner) {
            switch (joinerResult) {
                case LEFT: {
                    LogUtil.d(TAG, "onSync LEFT");
                    if (androidMediaBase.getString(0) == null || !hasFile(androidMediaBase.getString(0))) {
                        LogUtil.d(TAG, "Pass Add:" + androidMediaBase.getString(0));
                        break;
                    }
                    ContentValues values = new ContentValues();
                    for (int i = 0; i < sOwnMusicMediaColumnNames.length; ++i) {
                        Log.d(TAG, "doFullSync and i is " + i + " and key is " + sOwnMusicMediaColumnNames[i] + " and values is " + androidMediaBase.getString(i));

                        if (sOwnMusicMediaColumnNames[i].equals(MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY)) {
                            values.put(sOwnMusicMediaColumnNames[i], OrderUtils.keyFor(androidMediaBase.getString(androidMediaBase.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME))));
                        } else if (sOwnMusicMediaColumnNames[i].equals(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_KEY)) {
                            values.put(sOwnMusicMediaColumnNames[i], OrderUtils.keyForArtist(androidMediaBase.getString(androidMediaBase.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST))));
                        } else if (sOwnMusicMediaColumnNames[i].equals(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_KEY)) {
                            values.put(sOwnMusicMediaColumnNames[i], OrderUtils.keyForAlbum(androidMediaBase.getString(androidMediaBase.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM))));
                        } else {
                            values.put(sOwnMusicMediaColumnNames[i], androidMediaBase.getString(i));
                        }
                    }
                    long androidId = androidMediaBase.getLong(androidMediaBase.getColumnIndex(MediaStore.Audio.AudioColumns._ID));
                    values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID, androidId);
                    values.put(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID, String.valueOf(androidId));
                    long androidAlbumId = androidMediaBase.getLong(androidMediaBase.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
                    values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID, androidAlbumId);
                    values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ARTWORK, albumArtworks.get(androidAlbumId));
                    values.put(MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE, CommonConstants.SRC_TYPE_LOCAL);
                    values.put(MusicMediaDatabaseHelper.Media.MediaColumns.SCENE_ID, CommonConstants.SCENES_OTHER_ID);
                    //添加bucket_id
                    String mediaRootPath = androidMediaBase.getString(0);
                    try {
                        String folder_id = MusicUtil.getBucketID(mContext, String.valueOf(androidId)); // MODIFIED by beibei.yang, 2016-05-31,BUG-2223803
                        values.put(MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID, folder_id);
                        values.put(MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_NAME, PathUtils.replaceFolderName(mediaRootPath));
                        values.put(MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_PATH, PathUtils.replaceFolderPath(mediaRootPath));
                        //folder name
                        values.put(MusicMediaDatabaseHelper.Media.MediaColumns.SUFFIX, PathUtils.getSuffix(mediaRootPath));
                        valuesToAdd.add(values);
                    } catch (NoClassDefFoundError error) {
                        error.printStackTrace();
                    }
                    break;
                }
                case RIGHT: {
                    LogUtil.d(TAG, "onSync RIGHT");
                    long trackId = ownMediaBase.getLong(0);
                    String analysisDir = MusicUtil.getAnalysisDir(mContext);
                    String analysisfileStr = analysisDir + "/" + String.valueOf(trackId) + "-" + CommonConstants.SRC_TYPE_LOCAL + ".analysis";
                    new File(analysisfileStr).delete();

                    // This can happen if track is missing after a delete and
                    // we still have a reference in our DB
                    idsToDeleteStr.append(trackId).append(",");
                }
                break;
                case BOTH: {
                    LogUtil.d(TAG, "onSync BOTH");
                    if (androidMediaBase.getString(0) == null || !hasFile(androidMediaBase.getString(0))) {
                        long trackId = ownMediaBase.getLong(0);
                        idsToDeleteStr.append(trackId).append(",");
                        LogUtil.d(TAG, "Update->Delete:" + androidMediaBase.getString(0));
                    } else {
                        if (ownMediaBase.getLong(ownMediaBase.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID)) == 0
                                && ownMediaBase.getInt(ownMediaBase.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE)) == CommonConstants.SRC_TYPE_DEEZER) {
                            idsToUpdate.add(androidMediaBase.getString(androidMediaBase.getColumnIndex(MediaStore.Audio.AudioColumns._ID)));

                            ContentValues valueToUpdate = new ContentValues();
                            valueToUpdate.put(MusicMediaDatabaseHelper.Media.MediaColumns.ANDROID_AUDIO_ID,androidMediaBase.getString(androidMediaBase.getColumnIndex(MediaStore.Audio.Media._ID)));

                            String mediaRootPath = androidMediaBase.getString(androidMediaBase.getColumnIndex(MediaStore.Audio.Media.DATA));
                            try {
                                String folder_id = MusicUtil.getBucketID(mContext, androidMediaBase.getString(androidMediaBase.getColumnIndex(MediaStore.Audio.AudioColumns._ID)));
                                valueToUpdate.put(MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID, folder_id);
                                valueToUpdate.put(MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_NAME, PathUtils.replaceFolderName(mediaRootPath));
                                valueToUpdate.put(MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_PATH, PathUtils.replaceFolderPath(mediaRootPath));
                                valueToUpdate.put(MusicMediaDatabaseHelper.Media.MediaColumns.SUFFIX, PathUtils.getSuffix(mediaRootPath));
                            } catch (NoClassDefFoundError error) {
                                error.printStackTrace();
                            }
                            valuesToUpdate.add(valueToUpdate);
                        } else {
                            idsToUpdate.add(androidMediaBase.getString(androidMediaBase.getColumnIndex(MediaStore.Audio.Media._ID)));

                            ContentValues valueToUpdate = new ContentValues();
                            for (int i = 0; i < sOwnMusicMediaColumnNames.length; ++i) {
                                if (sOwnMusicMediaColumnNames[i].equals(MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY)) {
                                    valueToUpdate.put(sOwnMusicMediaColumnNames[i], OrderUtils.keyFor(androidMediaBase.getString(androidMediaBase.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME))));
                                } else if (sOwnMusicMediaColumnNames[i].equals(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_KEY)) {
                                    valueToUpdate.put(sOwnMusicMediaColumnNames[i], OrderUtils.keyForArtist(androidMediaBase.getString(androidMediaBase.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST))));
                                } else if (sOwnMusicMediaColumnNames[i].equals(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_KEY)) {
                                    valueToUpdate.put(sOwnMusicMediaColumnNames[i], OrderUtils.keyForAlbum(androidMediaBase.getString(androidMediaBase.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM))));
                                } else {
                                    valueToUpdate.put(sOwnMusicMediaColumnNames[i], androidMediaBase.getString(i));
                                }
                            }
                            long androidAlbumId = androidMediaBase.getLong(androidMediaBase.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
                            valueToUpdate.put(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM_ID, androidAlbumId);
                            valueToUpdate.put(MusicMediaDatabaseHelper.Media.MediaColumns.ARTWORK, albumArtworks.get(androidAlbumId));
                            LogUtil.e(TAG, "androidAlbumId = " + androidAlbumId + ",valueToUpdate = " + valueToUpdate + ", size = " + valueToUpdate.size());
                            String mediaRootPath = androidMediaBase.getString(0);
                            valueToUpdate.put(MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_PATH, PathUtils.replaceFolderPath(mediaRootPath));
                            valueToUpdate.put(MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_NAME, PathUtils.replaceFolderName(mediaRootPath));
                            valuesToUpdate.add(valueToUpdate);
                        }


                    }
                }
                break;
            }
        }
    }
}
