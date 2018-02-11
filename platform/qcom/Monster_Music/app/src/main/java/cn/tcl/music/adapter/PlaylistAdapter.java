package cn.tcl.music.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tcl.framework.log.NLog;

import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.adapter.holders.MediaViewHolder;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.PlaylistInfo;
import cn.tcl.music.model.live.LiveMusicSearchMatchSongBean;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;

public class PlaylistAdapter extends BaseAdapter {
    private static final String TAG = PlaylistAdapter.class.getSimpleName();
    private Context mContext;
    private List<PlaylistInfo> mPlayList;
    private Handler mHandler;

    public PlaylistAdapter(Context context, List<PlaylistInfo> playlist) {
        mContext = context;
        mPlayList = playlist;
        mHandler = new Handler();
    }

    @Override
    public int getCount() {
        return mPlayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mPlayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_playlist, parent, false);
            holder.playlistAlbumIv = (ImageView) convertView.findViewById(R.id.create_playlist_imageview);
            holder.playlistNameTv = (TextView) convertView.findViewById(R.id.playlist_title_textview);
            holder.playlistSongsNumTv = (TextView) convertView.findViewById(R.id.playlist_num_textview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        PlaylistInfo playlistInfo = mPlayList.get(position);
        holder.playlistNameTv.setText(playlistInfo.getName());
        refreshLocalPlaylistItem(mContext,holder,playlistInfo.getId());
        return convertView;
    }

    private final class ViewHolder {
        ImageView playlistAlbumIv;
        TextView playlistNameTv;
        TextView playlistSongsNumTv;
    }

    private synchronized void refreshLocalPlaylistItem(final Context context, final ViewHolder viewHolder, final long playlistId) {
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
                final MediaInfo mediaInfo = MusicUtil.getMediaInfoFromCursor(cursor);
                final int mediaNum = cursor.getCount();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.playlistSongsNumTv.setText(context.getResources().getQuantityString(R.plurals.number_of_songs, mediaNum, mediaNum));
                        if (null != mediaInfo) {
                            LogUtil.d(TAG,"playlistId is " + playlistId + " and artwork path is " + mediaInfo.artworkPath);
                            Glide.with(context)
                                    .load(mediaInfo.artworkPath)
                                    .placeholder(R.drawable.musical_disc)
                                    .into(viewHolder.playlistAlbumIv);
                        }
                    }

                });
                cursor.close();
            }
        }).start();
    }
}
