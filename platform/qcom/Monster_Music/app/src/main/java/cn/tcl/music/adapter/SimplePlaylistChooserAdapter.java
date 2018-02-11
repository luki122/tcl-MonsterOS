package cn.tcl.music.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.tcl.music.R;
import cn.tcl.music.adapter.holders.ClickableViewHolder;
import cn.tcl.music.adapter.holders.SimpleViewHolder;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.util.DialogMenuUtils;

public class SimplePlaylistChooserAdapter extends RecyclerViewCursorAdapter<ClickableViewHolder> {
    private static final String TAG = SimplePlaylistChooserAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private final Dialog mDialog;
    private Handler mHandler;

    private boolean mIsUserHasCreatedPlaylist;

    public interface OnPlaylistChoiceListener {
        void onPlaylistChosen(Uri playlistUri, String playlistName);
    }

    private OnPlaylistChoiceListener mOnPlaylistChoiceListener;

    public SimplePlaylistChooserAdapter(Context context, Cursor c, OnPlaylistChoiceListener onPlaylistChoice, Dialog dialog) {
        super(context, c, new int[]{R.id.media_content, R.id.media_header});
        mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        mOnPlaylistChoiceListener = onPlaylistChoice;
        if (c != null && c.getCount() > 0) {
            mIsUserHasCreatedPlaylist = true;
        }
        mDialog = dialog;
        mHandler = new Handler();
    }

    @Override
    public void onBindCursorToViewHolder(ClickableViewHolder viewHolder, int position) {
        SimpleViewHolder svh = (SimpleViewHolder) viewHolder;
        svh.mediaTitleTextView.setText(mCursor.getString(1));
        svh.mediaTitleTextView.setVisibility(View.VISIBLE);
        svh.mNumTextView.setVisibility(View.VISIBLE);
        if (mCursor.moveToPosition(position)) {
            long playlistId = mCursor.getLong(mCursor.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns._ID));
            refreshPlaylistItem(mContext, (SimpleViewHolder) viewHolder, playlistId);
        }
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent,
                                                  int viewType,
                                                  RecyclerView.LayoutManager currentRecyclerLayoutManager) {
        switch (viewType) {
            case R.id.media_content: {
                ViewGroup rowContainer = (ViewGroup) mInflater.inflate(R.layout.simple_list_item, parent, false);
                SimpleViewHolder svh = new SimpleViewHolder(rowContainer, this);
                return svh;
            }
            case R.id.media_header: {
                ViewGroup rowContainer = (ViewGroup) mInflater.inflate(R.layout.simple_list_item, parent, false);
                SimpleViewHolder svh = new SimpleViewHolder(rowContainer, this);
                return svh;
            }
            default: {
                throw new IllegalArgumentException("View type is not recognized, cannot create a view Holder ");
            }
        }
    }

    @Override
    public void onBindViewTypeToViewHolder(ClickableViewHolder viewHolder, int position, int itemViewType) {
        if (itemViewType == R.id.media_header) {
            SimpleViewHolder svh = (SimpleViewHolder) viewHolder;
            if (position == 0) {
                svh.mArtworkImageView.setImageResource(R.drawable.create_new_playlist);
                svh.mediaTitleTextView.setText(R.string.new_song_list);
                svh.mediaTitleTextView.setVisibility(View.VISIBLE);
                svh.mNumTextView.setVisibility(View.GONE);
            } else if (position == 1) {
                svh.mArtworkImageView.setImageResource(R.drawable.i_like);
                svh.mediaTitleTextView.setText(R.string.my_favourite_music);
                svh.mediaTitleTextView.setVisibility(View.VISIBLE);
                svh.mNumTextView.setVisibility(View.VISIBLE);
                refreshFavoriteItem(mContext, (SimpleViewHolder) viewHolder);
            }
        } else {
            super.onBindViewTypeToViewHolder(viewHolder, position, itemViewType);
        }
    }

    @Override
    public int getItemCountFor(int itemViewType) {
        switch (itemViewType) {
            case R.id.media_content:
                return getCursor().getCount();
            case R.id.media_header:
                return 2;
            default:
                return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 2) {
            return R.id.media_header;
        } else {
            return R.id.media_content;
        }

    }

    @Override
    public int getPositionForContent(int position) {
        if (position < 2) {
            return position;
        } else if (position >= 2) {
            return position - 2;
        } else {
            return position;
        }

    }


    @Override
    public void onViewHolderClick(RecyclerView.ViewHolder vh, int position, View v) {
        Uri playlistUri = null;
        String playlistName = null;
        if (position == 0) {
            DialogMenuUtils.displayCreateNewPlaylistDialog(mContext, R.string.new_song_list, mOnPlaylistChoiceListener);
            mDialog.dismiss();
            return;
        } else if (position == 1) {
            playlistUri = MusicMediaDatabaseHelper.Playlists.FAVORITE_URI;
            playlistName = mContext.getString(R.string.my_favourite_music);
        } else {
            Cursor c = getCursorAtAdapterPosition(position);
            long playlistId = c.getLong(c.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns._ID));
            playlistName = c.getString(c.getColumnIndex(MusicMediaDatabaseHelper.Playlists.PlaylistsColumns.NAME));
            playlistUri = MusicMediaDatabaseHelper.Playlists.CONTENT_URI.buildUpon().appendPath(String.valueOf(playlistId)).build();
        }

        if (mOnPlaylistChoiceListener != null) {
            mOnPlaylistChoiceListener.onPlaylistChosen(playlistUri, playlistName);
        }

        mDialog.dismiss();
    }

    private synchronized void refreshPlaylistItem(final Context context, final SimpleViewHolder viewHolder, final long playlistId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String selection = MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAYLIST_ID + " = ? ";
                String[] selectionArgs = new String[]{String.valueOf(playlistId)};
                Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.PlaylistSongs.CONTENT_URI_PLAYLISTSONGS_MEDIA,
                        DBUtil.MEDIA_PLAYLIST_COLUMNS,
                        selection,
                        selectionArgs,
                        MusicMediaDatabaseHelper.PlaylistSongs.PlaylistSongsColumns.PLAY_ORDER + " DESC");
                cursor.moveToFirst();
                int playlistNumber = cursor.getCount();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.mNumTextView.setText(context.getResources().getQuantityString(R.plurals.number_of_songs, playlistNumber, playlistNumber));
                    }

                });
                cursor.close();
            }
        }).start();
    }

    private synchronized void refreshFavoriteItem(final Context context, final SimpleViewHolder viewHolder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String section = MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE + " = ? ";
                String[] selectionArgs = new String[]{String.valueOf(CommonConstants.VALUE_MEDIA_IS_FAVORITE)};
                Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED,
                        DBUtil.MEDIA_FOLDER_COLUMNS,
                        section,
                        selectionArgs,
                        MusicMediaDatabaseHelper.Media.MediaColumns.FAVORITE_DATE + " desc");
                int favoriteNumber = cursor.getCount();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.mNumTextView.setText(context.getResources().getQuantityString(R.plurals.number_of_songs, favoriteNumber, favoriteNumber));
                    }
                });
                cursor.close();
            }
        }).start();
    }
}
