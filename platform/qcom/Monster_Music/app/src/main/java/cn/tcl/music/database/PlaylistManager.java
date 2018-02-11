/* MODIFIED-BEGIN by beibei.yang, 2016-05-28, BUG-2209123*/
/* ----------|----------------------|---------------------|-------------------*/
/* 25/05/2015|zhongrui.guo1         |PR1002622            |When shuffle is enabled and tap on a song in library All Songs the Queue is not shuffled */
/* ----------|----------------------|---------------------|-------------------*/
package cn.tcl.music.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.model.MediaInfo;


public abstract class PlaylistManager {

    private static final String TAG = PlaylistManager.class.getSimpleName();
    public static String COLUMN_AUDIO_ID = null;
    public static String COLUMN_PLAYLIST_NAME = null;
    public static String COLUMN_SOURCE_TYPE = null;

    public enum AddTypes {
        ALL,
        ALL_LOCAL,
        ALL_ONLINE,
        MEDIA,
        FAVORITE,
        RECORDS,
        LASTADD,
        HIDDEN,
        PLAYLIST,
        ARTIST,
        ALBUM,
        GENRE,
        QUEUE,
        REMOTE_FOLDER,
        REMOTE_MEDIA,
        DEEZER,
        DEEZER_FAVORITE,
        DOWNLOADED,
        FOLDER
    }

    public static class AddPlaylistParameter
    {
        public PlaylistManager.AddTypes addType;
        public long id;
        public String path;
        public String folder_name;
        public int positionOffset = -1;
        public int limit = -1;
        public boolean isRandomlyAdded = false;
        public boolean descOrderBy = false;
        public boolean autoAdded = false;
        public boolean clearQueue = false;
        public int transitionId;

        @Override
        public String toString() {
            return "AddPlaylistParameter{" +
                    "addType=" + addType +
                    ", id=" + id +
                    ", path='" + path + '\'' +
                    ", positionOffset=" + positionOffset +
                    ", limit=" + limit +
                    ", isRandomlyAdded=" + isRandomlyAdded +
                    ", descOrderBy=" + descOrderBy +
                    ", autoAdded=" + autoAdded +
                    ", clearQueue=" + clearQueue +
                    ", transitionId=" + transitionId +
                    '}';
        }
    }

    public static int PLAYLIST_TYPE = 0;
    public static int QUEUE_TYPE = 1;
    protected Context mContext;
    private static PlaylistManager mInstance;
    private static PlaylistManagerFactory sFactory;

    public interface PlaylistManagerFactory {
        PlaylistManager create(Context context);
    }

    public static void setFactory(PlaylistManagerFactory factory)
    {
        sFactory = factory;
    }

    public static PlaylistManager getInstance(Context context)
    {
        if (mInstance != null) {
            return mInstance;
        }

        if (sFactory == null) {
            throw new IllegalArgumentException("No factory set for PlaylistManager ! ");
        }

        mInstance = sFactory.create(context);
        return mInstance;
    }

    protected PlaylistManager(Context context)
    {
        mContext = context.getApplicationContext();
    }

    public boolean addToPlaylist(long mediaId, int playOrder, Uri playlistUri)
    {
        ContentValues newValues = new ContentValues();
        newValues.put(COLUMN_AUDIO_ID, mediaId);
        if (playOrder >= 0) {
            newValues.put(MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER, playOrder);
        }
        Uri returnUri = mContext.getContentResolver().insert(playlistUri, newValues);
        return !playlistUri.equals(returnUri);
    }

    public int getNumTracks(Uri playlistUri)
    {
        return getNumTracks(playlistUri, -1);
    }

    public int getNumTracks(Uri playlistUri, int srcType)
    {
        String where = null;
        String[] whereArgs = null;
        /* MODIFIED-BEGIN by beibei.yang, 2016-05-28,BUG-2209123*/
        int numTracks = 0;
        if (srcType >= 0)
        {
            where = COLUMN_SOURCE_TYPE + " = ?";
            whereArgs = new String[]{String.valueOf(srcType)};
            if (whereArgs == null || String.valueOf(srcType) == null) return numTracks;
        }


/* MODIFIED-END by beibei.yang,BUG-2209123*/
        Cursor c = mContext.getContentResolver().query(playlistUri, new String[]{"Count(*)"},
                                                       where, whereArgs, null);
        if (c == null) {
            return numTracks;
        }
        if (c.moveToFirst()) {
            numTracks = c.getInt(0);
        }

        c.close();

        return numTracks;
    }

    public boolean removeFromPlaylist(long audioId, Uri playlistUri)
    {
        Uri uriToDelete = playlistUri.buildUpon().appendEncodedPath(String.valueOf(audioId)).build();
        return mContext.getContentResolver().delete(uriToDelete,
                                                    null, null) != 0;
    }

    public boolean removeFromPlaylist(Uri playlistUri, String selection, String[] selectionArgs)
    {
        return mContext.getContentResolver().delete(playlistUri,
                                                    selection, selectionArgs) != 0;
    }

    public boolean removeMediaIdFromPlaylist(long mediaId, Uri playlistUri)
    {
        return mContext.getContentResolver().delete(playlistUri,
                                                    COLUMN_AUDIO_ID + " = ?",
                                                    new String[]{String.valueOf(mediaId)}) != 0;
    }

    public boolean removeFromPlaylistAtPosition(int zeroBasedIndex, Uri playlistUri)
    {
        Uri uriToDelete = playlistUri;
        return mContext.getContentResolver().delete(uriToDelete,
                MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER + " = ?", new String[]{String.valueOf(zeroBasedIndex + 1)}) != 0;
    }

    public boolean removeAll(Uri playlistUri)
    {
        return mContext.getContentResolver().delete(playlistUri, null, null) != 0;
    }

    public boolean moveWithinPlaylist(Uri playlistUri, int fromPosition, int toPosition)
    {
        Uri moveUri = playlistUri.
                                  buildUpon().
                                  appendEncodedPath(String.valueOf(fromPosition)).
                                  appendQueryParameter("move", "true")
                                  .appendQueryParameter("column_name", MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER)
                                  .build();
        ContentValues values = new ContentValues();
        values.put(MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER, toPosition);
        return mContext.getContentResolver().update(moveUri, values, null, null) != 0;

    }

    public boolean swapWithinPlaylist(Uri playlistUri, int position1, int position2)
    {
        Uri swapUri = playlistUri.
                                  buildUpon().
                                  appendEncodedPath(String.valueOf(position1)).
                                  appendQueryParameter("swap", "true")
                                  .build();
        ContentValues values = new ContentValues();
        values.put(MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER, position2);
        return mContext.getContentResolver().update(swapUri, values, null, null) != 0;

    }

    public boolean addTo(Uri playlistUri, AddPlaylistParameter parameter) {
        Uri addToUri = playlistUri
                .buildUpon()
                .appendEncodedPath("Add")
                .appendQueryParameter("AddType", parameter.addType.name())
                .appendQueryParameter("ItemId", String.valueOf(parameter.id))
                .appendQueryParameter("Path", parameter.path)
                .appendQueryParameter("folder_name", parameter.folder_name)
                .appendQueryParameter("Random", String.valueOf(parameter.isRandomlyAdded))
                .appendQueryParameter("Position", String.valueOf(parameter.positionOffset))
                .appendQueryParameter("Limit", String.valueOf(parameter.limit))
                .appendQueryParameter("Descendant", String.valueOf(parameter.descOrderBy))
                .appendQueryParameter("AutoAdded", String.valueOf(parameter.autoAdded))
                .appendQueryParameter("Transition", String.valueOf(parameter.transitionId))
                .build();
        Log.d(TAG,"addTo and addToUri is " + addToUri.toString());

        if (null != playlistUri && playlistUri.toString().startsWith(MusicMediaDatabaseHelper.Playlists.CONTENT_URI.toString())) {
            Cursor c = mContext.getContentResolver().query(playlistUri, new String[]{MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.MEDIA_ID},
                    MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.MEDIA_ID + " = ?",
                    new String[]{"" + parameter.id},
                    null);
            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return false;
                } else {
                    c.close();
                }
            }
        }

        Uri returnUri = mContext.getContentResolver().insert(addToUri, null);
        Log.d(TAG, "PlaylistManager--addToUri = " + addToUri.toString()+"\n"+",returnUri="+returnUri);

        return !playlistUri.equals(returnUri);
    }

    public boolean doesPlaylistExist(String name, Uri playlistContainerUri)
    {
        Cursor c = mContext.getContentResolver().query(playlistContainerUri, new String[]{COLUMN_PLAYLIST_NAME},
                                            COLUMN_PLAYLIST_NAME + " = ?",
                                            new String[]{name},
                                            null);
        if (c == null) {
            return false;
        }

        boolean playlistExists = c.moveToFirst();

        c.close();
        return playlistExists;
    }

    public Uri createNewPlaylist(ContentValues values, Uri playlistContainerUri) {
        Uri returnUri = mContext.getContentResolver().insert(playlistContainerUri, values);
        return returnUri;
    }

    public MediaInfo getTrackFromPlaylist(Uri playlistUri, int currentPlayOrder) {
        Log.d(TAG, "PlaylistManager getTrackFromPlaylist and playlistUri is " + playlistUri + " and currentPlayOrder is " + currentPlayOrder);
        MediaInfo infoNext = null;
//        final Cursor c = mContext.getContentResolver().query(playlistUri, CommonConstants.MEDIA_INFO_COLUMNS,
//                                                             MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER + " = ?",
//                                                             new String[]{String.valueOf(currentPlayOrder)},
//                                                             MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER);
//        if (c == null) {
//            return infoNext;
//        }
//        if (c.moveToFirst()) {
//            infoNext = LibraryNavigationUtil.getMediaInfoFromCursor(c);
//        }
//        if (null != infoNext) {
//            Log.d(TAG,"getTrackFromPlaylist and infoNext is " + infoNext.toString());
//        }
//        c.close();
        return infoNext;
    }

    public MediaInfo getTrackFromPlaylistByAudioID(Uri playlistUri, long audioId) {
        Log.d(TAG, "PlaylistManager getTrackFromPlaylist and playlistUri is " + playlistUri + " and audioId is " + audioId);
        MediaInfo mediaInfo = null;
//        if (audioId != -1) {
//            Cursor c = mContext.getContentResolver().query(playlistUri, CommonConstants.MEDIA_INFO_COLUMNS,
//                    MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.MEDIA_ID + " = ?",
//                    new String[]{String.valueOf(audioId)},
//                    MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER);
//            if (c != null) {
//                c.moveToFirst();
//                mediaInfo = LibraryNavigationUtil.getMediaInfoFromCursor(c);
//                c.close();
//            }
//        }
        return mediaInfo;
    }

    /**
     * Use to make specific calls on a playlist
     *
     * @param playlistUri : the playlistUri
     * @param actionId : the action identifier
     * @param actionOptions : options to manage the action.
     */
    public void callSpecificAction(Uri playlistUri, int actionId, Bundle actionOptions) {
        Log.d(TAG,"callSpecificAction and playlistUri is " + playlistUri + " and actionId is " + actionId);
//        if (QueueAndPlaylistManager.ACTION_REMOVE_AUTO_ADDED == actionId) {
//            String selection = MusicMediaDatabaseHelper.Queue.QueueColumns.AUTO_ADDED + " = ?";
//            String[] selectionArgs = new String[]{String.valueOf(CommonConstants.VALUE_QUEUE_IS_AUTO_ADDED)};
//            mContext.getContentResolver().delete(playlistUri,selection,selectionArgs);
//        } else if (QueueAndPlaylistManager.ACTION_REMOVE_ALL == actionId) {
//            mContext.getContentResolver().delete(playlistUri,null,null);
//        }
    }

    public int setRandomOrderAndGiveNewIndex(boolean isRandomizedOrder, int currentIndex) {
        return -1;
    }

    public int getIndexInQueuebyMediaId(long mediaId , boolean isRandom) {
        return -1;
    }



    public void initRandomized(boolean randomized)
    {
    }
    public boolean isRandomized()
    {
        return false;
    }

}
/* MODIFIED-END by beibei.yang,BUG-2209123*/
