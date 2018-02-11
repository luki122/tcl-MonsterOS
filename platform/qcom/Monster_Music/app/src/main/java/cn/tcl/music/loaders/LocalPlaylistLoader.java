package cn.tcl.music.loaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.content.CursorLoader;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.database.MusicMediaDatabaseHelper.Media;
import cn.tcl.music.database.MusicMediaDatabaseHelper.PlaylistSongs;
import cn.tcl.music.database.MusicMediaDatabaseHelper.Playlists;

public class LocalPlaylistLoader extends CursorLoader {
    private List<Integer> mNumTracks = new ArrayList<Integer>();
    private boolean mShowHiddenTracks;
    final ForceLoadContentObserver mFavoriteObserver;
    final ForceLoadContentObserver mRecordingsObserver;
    final ForceLoadContentObserver mHiddenObserver;
    private Context mContext;

    public LocalPlaylistLoader(Context context, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);

        mContext = context;
        mFavoriteObserver = new ForceLoadContentObserver();
        mRecordingsObserver = new ForceLoadContentObserver();
        mHiddenObserver = new ForceLoadContentObserver();
    }

    @Override
    public Cursor loadInBackground() {
        Cursor c = super.loadInBackground();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mShowHiddenTracks = sharedPrefs.getBoolean("show_hidden_tracks", false);

        unregisterContentObserverSafely(mFavoriteObserver);
        unregisterContentObserverSafely(mRecordingsObserver);
        unregisterContentObserverSafely(mHiddenObserver);

        getContext().getContentResolver().registerContentObserver(Playlists.FAVORITE_URI, true, mFavoriteObserver);
        getContext().getContentResolver().registerContentObserver(Playlists.RECORDS_URI, true, mRecordingsObserver);

        mNumTracks.clear();

        addNumTracksWithUri(Media.CONTENT_URI,
                            Media.MediaColumns.FAVORITE + " = ?",
                            new String[]{"1"});
        //[BUGFIX]-DEL-BEGIN by yuanxi.jiang for PR1986161 on 2016/4/26
//        addNumTracksWithUri(Media.CONTENT_URI,
//                            Media.MediaColumns.DATE_ADD + " > ?",
//                            new String[]{String.valueOf(System.currentTimeMillis() / 1000 - (3600 * 24 * getContext().getResources().getInteger(R.integer.numdays)))});
        //[BUGFIX]-DEL-END by yuanxi.jiang

        if (c == null) {
            return c;
        }

        int columnPlaylistId = c.getColumnIndex(BaseColumns._ID);
        while(c.moveToNext())
        {
            String playlistIdStr = c.getString(columnPlaylistId);
            addNumTracksWithUri(PlaylistSongs.CONTENT_URI,
                                PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + " = ?",
                                new String[]{playlistIdStr});
        }


        return c;
    }



    @Override
    protected void onAbandon() {
        super.onAbandon();
        getContext().getContentResolver().unregisterContentObserver(mFavoriteObserver);
        getContext().getContentResolver().unregisterContentObserver(mRecordingsObserver);
        unregisterContentObserverSafely(mHiddenObserver);
    }

    @Override
    public void onCanceled(Cursor cursor) {
        super.onCanceled(cursor);
        getContext().getContentResolver().unregisterContentObserver(mFavoriteObserver);
        getContext().getContentResolver().unregisterContentObserver(mRecordingsObserver);
        unregisterContentObserverSafely(mHiddenObserver);
    }

    private void addNumTracksWithUri(Uri uri, String selection, String[] selectionArgs)
    {

        Cursor numTracksCursor = getContext().getContentResolver().query(uri,
                                                                  new String[]{BaseColumns._ID},
                                                                  selection,
                                                                  selectionArgs,
                                                                  null);
        if (numTracksCursor != null)
        {
            mNumTracks.add(numTracksCursor.getCount());
            numTracksCursor.close();
        }

    }

    @Override
    protected void onReset() {
        super.onReset();
        mNumTracks.clear();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mShowHiddenTracks = sharedPrefs.getBoolean("show_hidden_tracks", false);
        getContext().getContentResolver().unregisterContentObserver(mFavoriteObserver);
        getContext().getContentResolver().unregisterContentObserver(mRecordingsObserver);
        unregisterContentObserverSafely(mHiddenObserver);
    }

    public List<Integer> getNumTracksList()
    {
        return mNumTracks;
    }

    private void unregisterContentObserverSafely(ContentObserver observer) {
        try {
            getContext().getContentResolver().unregisterContentObserver(observer);
        } catch (Exception e) {
            // ingore
        }
    }

}
