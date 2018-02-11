package cn.tcl.music.util;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.tcl.framework.db.EntityManager;
import com.tcl.framework.db.sqlite.Selector;
import com.tcl.framework.log.NLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.download.mie.downloader.DownloadTask;
import cn.download.mie.util.DBUtils;
import cn.tcl.music.R;
import cn.tcl.music.adapter.holders.MediaViewHolder;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.live.AlbumBean;
import cn.tcl.music.model.live.LiveMusicSearchMatchSongBean;
import cn.tcl.music.network.AlbumDetailTask;
import cn.tcl.music.network.DataRequest;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.view.image.ImageFetcher;

public final class ViewHolderBindingUtil {
    public final static String TAG = "ViewHolderBindingUtil";
    private static ILoadData mListener;
    private static ILoadData mListenerAlbum;
    private static AlbumDetailTask mAlbumDetailTask;

    public static void bindSong(Context context, final MediaViewHolder viewHolder, Cursor cursor, ImageFetcher imageFetcher) {
        long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        String trackTitle = cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.TITLE));
        String trackArtist = cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST));
        String trackAlbum = cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM));
        if (trackArtist != null && trackArtist.equals("<unknown>")) {
            trackArtist = context.getResources().getString(R.string.unknown);
        }
        if (trackAlbum == null) {
            trackAlbum = context.getResources().getString(R.string.unknown);
        }
        String artworkpath = cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.ARTWORK));

        NLog.d(TAG, "bindSong trackTitle = " + trackTitle + ", trackArtist = " + trackArtist + ", artworkpath = " + artworkpath);

        if (viewHolder.currentItemId != id) {
            viewHolder.mediaTitleTextView.setTag(id);

            if (artworkpath != null && !artworkpath.contains("://")) {
                StringBuilder stringBuilder = new StringBuilder("file://");
                stringBuilder.append(artworkpath);
                artworkpath = stringBuilder.toString();
            } else if (artworkpath == null) { // MODIFIED by beibei.yang, 2016-06-27,BUG-2193985,2194489
//                String s=trackTitle ;
//                if(s !=null && s.contains(".")){ // add for  2124837  protect
//                    s=s.substring(0,s.lastIndexOf('.'));
//                }
//                Selector selector = Selector.create().where("song_name", "=", s);
//                EntityManager<DownloadTask> dbManager = DBUtils.getDownloadTaskManager(MusicApplication.getApp(), null);
//                DownloadTask downloadTask = dbManager.findFirst(selector);

//                if (downloadTask != null && downloadTask.album_logo != null) {
//                    artworkpath = downloadTask.album_logo;
//                    updatePortrait(id, artworkpath);
//                }
//                if (downloadTask != null && downloadTask.artist_logo != null) {
//                    if(artworkpath==null){
//                        artworkpath = downloadTask.artist_logo;
//                    }
//                    updateArtRecord(id, artworkpath);
//                }
            }

            if (null == artworkpath) {
                getOnlineAlbum(context, cursor, viewHolder, id);
            }
//            viewHolder.mediaArtworkImageView.setImageResource(R.drawable.default_cover_list);
                Glide.with(context)
                        .load(artworkpath)
                        .placeholder(R.drawable.default_cover_list)
                        .into(viewHolder.mediaArtworkImageView);
        }
        viewHolder.currentItemId = id;

        // 歌曲的display_name字段有可能是带后缀的 显示的时候需要把后缀给去掉
        viewHolder.mediaTitleTextView.setText(MixUtil.getSongNameWithNoSuffix(trackTitle));

        viewHolder.mediaSubtitleTextView.setText(trackArtist + " " + trackAlbum);
        MediaInfo currentMedia = null;
//        if (MediaQueue.mCurrentMedia != null) {
//            currentMedia = MediaQueue.mCurrentMedia.getCurrentMedia();
//        }
        boolean isFavorite = cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE)) != 0;
        boolean isLocal = cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE)) <= CommonConstants.SRC_TYPE_MYMIX;
        if (isFavorite) {
            viewHolder.mediaFavoriteImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mediaFavoriteImageView.setVisibility(View.GONE);
        }
        //TODO
        //set the right view for test
        if (isLocal) {
            //viewHolder.mediaLocalStorageImageView.setVisibility(View.VISIBLE);
        } else {
            //viewHolder.mediaLocalStorageImageView.setVisibility(View.GONE);
        }

        long audioId = cursor.getLong(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID));
        boolean isTrackPlaying = (currentMedia != null && currentMedia.audioId == audioId);
        //TODO
        //Add Playing gif here.
        //viewHolder.indicatorPlaytItemView.setVisibility(isTrackPlaying ? View.VISIBLE : View.GONE);
//        viewHolder.mediaTitleTextView.setTextColor(isTrackPlaying ? context.getResources().getColor(R.color.green_4d) : viewHolder.defaultTextColor);
//        viewHolder.mediaSubtitleTextView.setTextColor(isTrackPlaying ? context.getResources().getColor(R.color.green_4d) : viewHolder.defaultSubTextColor);
    }

    private static void updatePortrait(Long recNo, String name) {
        Uri uri = ContentUris.withAppendedId(MusicMediaDatabaseHelper.Media.CONTENT_URI, recNo);
        ContentValues values = new ContentValues();
        values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_PORTRAIT, name);
        MusicApplication.getApp().getContentResolver().update(uri, values, null, null);
    }

    private static void updateArtRecord(Long recNo, String name) {
        Uri uri = ContentUris.withAppendedId(MusicMediaDatabaseHelper.Media.CONTENT_URI, recNo);
        ContentValues values = new ContentValues();
        values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ARTWORK, name);
        MusicApplication.getApp().getContentResolver().update(uri, values, null, null);
    }

    private static synchronized void getOnlineAlbum(Context context, Cursor itemCursor, final MediaViewHolder viewHolder, final long id) {
        String song_name = itemCursor.getString(itemCursor.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Media.MediaColumns.TITLE));
        String album_name = itemCursor.getString(itemCursor.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM));
        String artist_name = itemCursor.getString(itemCursor.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST));
        if (null == song_name || album_name == null || artist_name == null) {
            return;
        }
        LogUtil.d(TAG, "song_name : " + song_name);
        LogUtil.d(TAG, "album_name : " + album_name);
        LogUtil.d(TAG, "artist_name : " + artist_name);
        if (song_name.contains(".")) {
            song_name = song_name.substring(0, song_name.lastIndexOf('.'));
        }
        if (album_name.contains(".")) {
            album_name = album_name.substring(0, album_name.lastIndexOf('.'));
        }
        if (artist_name.contains(".")) {
            artist_name = artist_name.substring(0, artist_name.lastIndexOf('.'));
        }

        JSONObject object = new JSONObject();
        try {
            object.put("song_name", song_name);
            object.put("album_name", album_name);
            object.put("artist_name", artist_name);
        } catch (JSONException e) {

        }

        List l1 = new ArrayList();
        l1.add(object);

        findOnlineAlbumID(context, l1.toString(), viewHolder, id);
    }


    private static synchronized void findOnlineAlbumID(final Context context, String searchkey, final MediaViewHolder viewHolder, final long id) {
        final String skey = searchkey;
        mListener = new ILoadData() {
            @Override
            public void onLoadSuccess(int dataType, List datas) {
                if (datas != null && datas.size() > 0) {
                    LiveMusicSearchMatchSongBean data = (LiveMusicSearchMatchSongBean) datas.get(0);
                    if (data != null) {
                        if (data.songs != null && data.songs.size() > 0) {
                            loadAlbumDetailData(context, data.songs.get(0).album_id, viewHolder, id);
                        }
                    }
                }
            }

            @Override
            public void onLoadFail(int dataType, String message) {
                NLog.d(TAG, "onLoadFail");
            }
        };

//        new LiveMusicMatchSongByKeywordTask(MusicApplication.getApp().getApplicationContext(), mListener, skey).executeMultiTask();
    }

    public static synchronized void loadAlbumDetailData(final Context context, String albumId, final MediaViewHolder viewHolder, final long id) {
        if (mAlbumDetailTask != null && (mAlbumDetailTask.getStatus() != AsyncTask.Status.FINISHED)) {
            mAlbumDetailTask = null;
            return;
        }

        mListenerAlbum = new ILoadData() {
            @Override
            public void onLoadSuccess(int dataType, List datas) {
                NLog.d(TAG, datas.toString());
                if (datas == null) {
                    return;
                }
                if (datas.size() == 0) {
                    return;
                }
                if (DataRequest.Type.TYPE_LIVE_ALBUM_DETAIL == dataType) {
                    AlbumBean collectionBean = (AlbumBean) datas.get(0);
                    if (null == collectionBean.songs) {
                        return;
                    }
                    if (id == (long) viewHolder.mediaTitleTextView.getTag()) {
                        updateArtRecord(id, collectionBean.album_logo);
                    }
                }
            }

            @Override
            public void onLoadFail(int dataType, String message) {
                NLog.i(TAG, "mListenerAlbum onLoadFail");
            }
        };
        mAlbumDetailTask = new AlbumDetailTask(context, mListenerAlbum, albumId);
        mAlbumDetailTask.executeMultiTask();
    }

    public static void bindArtist(final Context context, MediaViewHolder viewHolder, Cursor cursor, ImageFetcher imageFetcher) {
        final long itemId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        String artistName = cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST));
        if (artistName != null && artistName.equals("<unknown>")) {
            artistName = context.getResources().getString(R.string.unknown);
        }
        viewHolder.mediaTitleTextView.setText(artistName);

        int numAlbums = cursor.getInt(cursor.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Artists.ArtistsColumns.NUMBER_OF_ALBUMS));
        int numSongs = cursor.getInt(cursor.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Artists.ArtistsColumns.NUMBER_OF_TRACKS));
        String artistDesc = context.getResources().getQuantityString(R.plurals.number_of_albums_2, numAlbums, numAlbums);
        artistDesc += " " + context.getResources().getQuantityString(R.plurals.number_of_songs_2, numSongs, numSongs);
        viewHolder.mediaSubtitleTextView.setText(artistDesc);
        viewHolder.currentItemId = itemId;
    }

    public static void bindFolder(Context context, MediaViewHolder viewHolder, Cursor cursor, ImageFetcher imageFetcher) {
        long itemId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        String folderTitle = cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Folders.FoldersColumns.FOLDER_NAME));
        String folderPath = cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Folders.FoldersColumns.FOLDER_PATH));
        int numSongs = cursor.getInt(cursor.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Folders.FoldersColumns.FOLDER_SONGS_NUM));
        String folderDesc = context.getResources().getQuantityString(R.plurals.number_of_folders, numSongs, numSongs);
        folderDesc += folderPath;

        viewHolder.mediaTitleTextView.setText(folderTitle);
        viewHolder.mediaSubtitleTextView.setText(folderDesc);

        viewHolder.contextMenuImageButton.setImageResource(R.drawable.picto_right);
        viewHolder.currentItemId = itemId;
    }

    public static void bindAlbum(Context context, MediaViewHolder viewHolder, Cursor cursor, ImageFetcher imageFetcher) {
        long itemId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        String albumTitle = cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM));
        viewHolder.mTitleAlbumTextView.setText(albumTitle);
        int numSongs = cursor.getInt(cursor.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Albums.AlbumColumns.NUMBER_OF_TRACKS));
        String albumtDesc = context.getResources().getQuantityString(R.plurals.number_of_songs, numSongs, numSongs);
        viewHolder.mSongCountsAlbumTextView.setText(albumtDesc);
        String artwork = cursor.getString(cursor.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTWORK));
        if (viewHolder.currentItemId != itemId) {
            imageFetcher.loadImage(artwork, viewHolder.mediaArtworkImageView);
            if (!TextUtils.isEmpty(artwork)) {
                NLog.d(TAG, "artwork=" + artwork);

                Glide.with(context)
                        .load(artwork)
                        .placeholder(R.drawable.default_cover_list)
                        .into(viewHolder.mediaArtworkImageView);
            } else {
                Selector selector = Selector.create().where("album_name", "=", albumTitle);
                EntityManager<DownloadTask> dbManager = DBUtils.getDownloadTaskManager(MusicApplication.getApp(), null);
                DownloadTask downloadTask = dbManager.findFirst(selector);
                NLog.d(TAG, "albumTitle= " + albumTitle);
                if (downloadTask != null && downloadTask.album_logo != null) {
                    artwork = downloadTask.album_logo;
                    Glide.with(context)
                            .load(artwork)
                            .placeholder(R.drawable.default_cover_list)
                            .into(viewHolder.mediaArtworkImageView);
                } else {
                    viewHolder.mediaArtworkImageView.setImageResource(R.drawable.default_cover_list);
                }
            }
        }
        viewHolder.currentItemId = itemId;
    }
}
