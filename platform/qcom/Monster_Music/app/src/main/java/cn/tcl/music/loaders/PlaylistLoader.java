package cn.tcl.music.loaders;

import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

import com.tcl.framework.log.NLog;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.database.MusicMediaDatabaseHelper;

/**
 * Created by jiangyuanxi on 2/23/16.
 */
public class PlaylistLoader extends CursorLoader {
    private static final String TAG = PlaylistLoader.class.getSimpleName();

    private static List<Integer> mNumTracks = new ArrayList<Integer>();
    private boolean mShowHiddenTracks;
    final ForceLoadContentObserver mFavoriteObserver;
    final ForceLoadContentObserver mRecordingsObserver;
    final ForceLoadContentObserver mHiddenObserver;
    private Context mContext;
    private TracksChangeCallBack mCallBack;

    public PlaylistLoader(Context context, Uri uri, String[] projection,
                               String selection, String[] selectionArgs, String sortOrder,TracksChangeCallBack callBack) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);

        mContext = context;
        mFavoriteObserver = new ForceLoadContentObserver();
        mRecordingsObserver = new ForceLoadContentObserver();
        mHiddenObserver = new ForceLoadContentObserver();
        this.mCallBack = callBack;
    }

    @Override
    public Cursor loadInBackground() {
        Cursor c = super.loadInBackground();

        //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mShowHiddenTracks = false;

        unregisterContentObserverSafely(mFavoriteObserver);
        unregisterContentObserverSafely(mRecordingsObserver);
        unregisterContentObserverSafely(mHiddenObserver);

        getContext().getContentResolver().registerContentObserver(MusicMediaDatabaseHelper.Playlists.FAVORITE_URI, true, mFavoriteObserver);
        getContext().getContentResolver().registerContentObserver(MusicMediaDatabaseHelper.Playlists.RECORDS_URI, true, mRecordingsObserver);

        mNumTracks.clear();

        addNumTracksWithUri(MusicMediaDatabaseHelper.Media.CONTENT_URI,
                MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE + " = ?",
                new String[]{"1"});


        if (c == null) {
            return c;
        }

        int columnPlaylistId = c.getColumnIndex(BaseColumns._ID);
        while(c.moveToNext())
        {
            String playlistIdStr = c.getString(columnPlaylistId);
            addNumTracksWithUri(MusicMediaDatabaseHelper.PlaylistSongs.CONTENT_URI,
                    MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + " = ?",
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

    private void addNumTracksWithUri(Uri uri, String selection, String[] selectionArgs) {
        Cursor numTracksCursor = getContext().getContentResolver().query(uri,
                new String[]{BaseColumns._ID},
                selection,
                selectionArgs,
                null);
        NLog.d(TAG, "mNumTracks = " + numTracksCursor.getCount());
        if (numTracksCursor != null)
        {
            mNumTracks.add(numTracksCursor.getCount());
            numTracksCursor.close();
        }

        if (null != mCallBack) {
            mCallBack.tracksChange();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        mNumTracks.clear();
        NLog.d(TAG,"===onReset=====");
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

    /**
     * 歌曲数变化回调
     */
    public interface TracksChangeCallBack {
        /**
         * 歌曲数发生变化
         */
        public void tracksChange();
    }

}
