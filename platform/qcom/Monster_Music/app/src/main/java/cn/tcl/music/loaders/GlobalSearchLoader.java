package cn.tcl.music.loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.SparseArray;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;

public class GlobalSearchLoader extends AsyncTaskLoader<SparseArray<Cursor> > {
    final ForceLoadContentObserver mObserver;

	private String mSearchQuery;

	private SparseArray<Cursor> mCursors;

	public GlobalSearchLoader(Context context, String query) {
		super(context);
		mSearchQuery = query;
        mObserver = new ForceLoadContentObserver();
	}

	@Override
	public SparseArray<Cursor> loadInBackground() {

		if (TextUtils.isEmpty(mSearchQuery))
			return null;

		mSearchQuery = "%"+ mSearchQuery + "%";

		SparseArray<Cursor> searchCursors = new SparseArray<Cursor>();

		Cursor artistCursor = getContext().getContentResolver().query(MusicMediaDatabaseHelper.Artists.CONTENT_URI,
												DBUtil.defaultArtistColumns,
											    MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST + " LIKE ? ESCAPE '\\'",
											    new String[]{mSearchQuery}, MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST_KEY);

		artistCursor.registerContentObserver(mObserver);
		searchCursors.append(R.id.media_search_artists, artistCursor);

		Cursor albumCursor = getContext().getContentResolver().query(MusicMediaDatabaseHelper.Albums.CONTENT_URI,
				DBUtil.defaultAlbumColumns,
											    MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM + " LIKE ? ESCAPE '\\'",
											    new String[]{mSearchQuery}, MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM_KEY);

		albumCursor.registerContentObserver(mObserver);
		searchCursors.append(R.id.media_search_albums, albumCursor);

		Cursor playlistCursor = getContext().getContentResolver().query(MusicMediaDatabaseHelper.Playlists.CONTENT_URI,
				DBUtil.defaultPlaylistColumns,
																	    MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.NAME + " LIKE ? ESCAPE '\\'",
																	    new String[]{mSearchQuery}, MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.NAME);

		playlistCursor.registerContentObserver(mObserver);
		searchCursors.append(R.id.media_search_playlists, playlistCursor);

        Cursor songsCursor = getContext().getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED,
				DBUtil.MEDIA_FOLDER_COLUMNS,
               MusicMediaDatabaseHelper.Media.MediaColumns.TITLE + " LIKE ? ESCAPE '\\' AND  NOT ( "+ MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE +" > ? AND " + MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE + " = 0 )",
               new String[]{mSearchQuery,String.valueOf(CommonConstants.SRC_TYPE_MYMIX)}, MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY);

		songsCursor.registerContentObserver(mObserver);
		searchCursors.append(R.id.media_search_songs, songsCursor);

		return searchCursors;
	}

	@Override
	public void deliverResult(SparseArray<Cursor> data) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (data != null) {
            	closeCursors(data);
            }
            return;
        }
        SparseArray<Cursor> oldCursors = mCursors;
        mCursors = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldCursors != null && oldCursors != mCursors) {
           closeCursors(oldCursors);
        }
	}
	
	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		forceLoad();
	}
	
	@Override
	protected void onStopLoading() {
		cancelLoad();
	}
	
	private void closeCursors(SparseArray<Cursor> cursors)
	{
		final int size = cursors.size();
    	for (int i = 0; i < size; i++)
    	{
    		Cursor c = cursors.valueAt(i);
    		if (c != null && !c.isClosed()) {
				c.close();
			}
    	}
	}
	
	@Override
	public void onCanceled(SparseArray<Cursor> data) {
		super.onCanceled(data);
		if (data != null)
		{
			closeCursors(data);
		}
	}
	
	@Override
	protected void onReset() {
		super.onReset();
        // Ensure the loader is stopped
        onStopLoading();

        if (mCursors != null) {
            closeCursors(mCursors);
        }
        mCursors = null;
	}

}
