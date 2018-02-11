/* ----------|----------------------|---------------------|-------------------*/
/* 23/06/2015|xiaolong.zhang        |PR1023228            |Radio screen miss all covers      */
/* ----------|----------------------|---------------------|-------------------*/
package cn.tcl.music.adapter.live;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.adapter.holders.FooterViewHolder;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.view.OnDetailItemClickListener;


/**
 * @author zengtao.kuang
 * @Description: 详情歌曲列表的Adapter
 * @date 2015/11/12 17:30
 * @copyright TCL-MIE
 */
public class DetailSongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "DetailSongAdapter";
    public static final int SINGER_CATEGORY = 1;//歌手
    public static final int HOT_CATEGORY = 2;//热门
    public static final int COLLECT_CATEGORY = 3;//
    public static final int ALBUM_CATEGORY = 4;
    private Context context;
    private List<SongDetailBean> dataList;
    private OnDetailItemClickListener onDetailItemClickListener;
    private MyBroadcastReceive myBroadcast = new MyBroadcastReceive();
    private boolean mPlayState = false;

    public DetailSongAdapter(Context context) {
        this.context = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("cn.tcl.music.sendstate");
        context.registerReceiver(myBroadcast, intentFilter);
    }

    private boolean isMore = true;
    private int state;
    private int category;
    private String albumName;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    public void setIsMore(boolean isMore) {
        this.isMore = isMore;
    }

    public void addDataList(List<SongDetailBean> dataList) {
        this.dataList = dataList;
    }

    public void addAlbumName(String mAlbumName) {
        this.albumName = mAlbumName;
    }

    public List<SongDetailBean> getDataList() {
        return dataList;
    }

    public void setState(int state) {
        this.state = state;
        notifyItemChanged(getItemCount() - 1);
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public void setOnDetailItemClickListener(OnDetailItemClickListener onDetailItemClickListener) {
        this.onDetailItemClickListener = onDetailItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        if (dataList == null) {
            return 1;
        }
        return dataList.size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (TYPE_ITEM == viewType) {
            if (category == ALBUM_CATEGORY) {
                View rootView = LayoutInflater.from(context)
                        .inflate(R.layout.item_album_song_row, parent, false);
                RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.layout_content_live_song);
                relativeLayout.getLayoutParams().width = SystemUtility.getScreenWidth();
                MusicViewHolder musicViewHolder = new MusicViewHolder(rootView);
                return musicViewHolder;
            } else {
                View rootView = LayoutInflater.from(context)
                        .inflate(R.layout.item_music_row, parent, false);
                RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.layout_content_live_singer);
                relativeLayout.getLayoutParams().width = SystemUtility.getScreenWidth();
                MusicViewHolder musicViewHolder = new MusicViewHolder(rootView);
                return musicViewHolder;
            }

        } else if (viewType == TYPE_FOOTER) {
            View rootView = LayoutInflater.from(context)
                    .inflate(R.layout.footer_view, parent, false);
            FooterViewHolder footerViewHolder = new FooterViewHolder(rootView);
            footerViewHolder.footerView.hide(); // MODIFIED by beibei.yang, 2016-06-01,BUG-2226088
            return footerViewHolder;
        }
        return null;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        List<SongDetailBean> dataList = this.dataList;
        if (getItemCount() == 0) {
            return;
        }
        if (viewHolder instanceof MusicViewHolder) {
            MusicViewHolder holder = (MusicViewHolder) viewHolder;
            final SongDetailBean songDetailBean = dataList.get(position);
            View.OnClickListener onClickListener = new View.OnClickListener() {
                public void onClick(View v) {
                    if (onDetailItemClickListener != null) {
                        onDetailItemClickListener.onClick(v, songDetailBean, position);
                    }
                }
            };
            holder.itemView.setOnClickListener(onClickListener);
            holder.contextMenuImageButton.setOnClickListener(onClickListener);
            holder.mediaTitleTextView.setText(songDetailBean.song_name);
            if (albumName != null) {
                holder.mediaSubtitleTextView.setText(songDetailBean.artist_name + " " + albumName);
            } else {
                if (songDetailBean.album_name != null) {
                    holder.mediaSubtitleTextView.setText(songDetailBean.artist_name + " " + songDetailBean.album_name);
                } else {
                    holder.mediaSubtitleTextView.setText(songDetailBean.artist_name);
                }
            }
            if (category == ALBUM_CATEGORY) {
                holder.numTextView.setText(String.valueOf(position + 1));
            } else {
                if (!MusicApplication.getApp().isDataSaver()) {
                    if (TextUtils.isEmpty(songDetailBean.album_logo)) {
                        Glide.with(context)
                                .load(songDetailBean.artist_logo)
                                .into(holder.mediaArtworkImageView);
                    } else {
                        Glide.with(context)
                                .load(songDetailBean.album_logo)
                                .into(holder.mediaArtworkImageView);
                    }
                } else {
                    Glide.with(context).load("").placeholder(R.drawable.default_cover_ranking).into(holder.mediaArtworkImageView);
                }
            }

            //[BUGFIX]-ADD by yanjia.li, 2016-06-18,BUG-2197064 begin
            MediaInfo currentMedia = new MediaInfo();
            boolean isTrackPlaying = (currentMedia != null && currentMedia.songRemoteId != null && songDetailBean != null && songDetailBean.song_id != null
                    && (currentMedia.songRemoteId.trim()).equals(songDetailBean.song_id.trim()));
            holder.mediaTitleTextView.setTextColor(isTrackPlaying ? context.getResources().getColor(R.color.green_4d) : context.getResources().getColor(R.color.black_87));
            holder.mediaSubtitleTextView.setTextColor(isTrackPlaying ? context.getResources().getColor(R.color.green_4d) : context.getResources().getColor(R.color.black_87));

            /* MODIFIED-BEGIN by yanjia.li, 2016-06-20,BUG-2197064*/
            if (isTrackPlaying) {
                holder.setPlayingIcon(mPlayState);
                holder.mediaPlayView.setVisibility(View.VISIBLE);
            } else {
                holder.setPlayingIcon(false);
                holder.mediaPlayView.setVisibility(View.INVISIBLE);
            }
            /* MODIFIED-END by yanjia.li,BUG-2197064*/
            //[BUGFIX]-ADD by yanjia.li, 2016-06-18,BUG-2197064 end

        } else if (viewHolder instanceof FooterViewHolder) {
            FooterViewHolder holder = (FooterViewHolder) viewHolder;
            if (!isMore) {
                holder.itemView.setVisibility(View.GONE);
            } else {
                holder.footerView.setState(state);
            }


        }


    }


    private class MyBroadcastReceive extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("cn.tcl.music.sendstate".equals(action)) {
                /* MODIFIED-BEGIN by yanjia.li, 2016-06-20,BUG-2197064*/
                int state = intent.getIntExtra("state", 0);
                mPlayState = state == 1 ? true : false;
                /* MODIFIED-END by yanjia.li,BUG-2197064*/
                notifyDataSetChanged();
            }

        }

    }

    public void unRegisterListener() {
        context.unregisterReceiver(myBroadcast);
    }
    //[BUGFIX]-ADD by yanjia.li, 2016-06-18,BUG-2197064 begin

    static class MusicViewHolder extends RecyclerView.ViewHolder {

        public TextView numTextView;
        public TextView mediaTitleTextView;
        public TextView mediaSubtitleTextView;
        public ImageView mediaArtworkImageView;
        public ImageView contextMenuImageButton;

        public MusicViewHolder(View itemView) {
            super(itemView);

            ViewGroup parent = (ViewGroup) itemView;
            numTextView = (TextView) parent.findViewById(R.id.num);
            mediaTitleTextView = (TextView) parent.findViewById(R.id.title_text_view);
            mediaSubtitleTextView = (TextView) parent.findViewById(R.id.subtitle_text_view);
            mediaArtworkImageView = (ImageView) parent.findViewById(R.id.artwork_image_view);
            contextMenuImageButton = (ImageView) parent.findViewById(R.id.item_menu_image_button);
            //[BUGFIX]-ADD by yanjia.li, 2016-06-20,BUG-2197064 begin
            mediaPlayView = (ImageView) parent.findViewById(R.id.play_view);
        }

        public ImageView mediaPlayView;
        public AnimationDrawable mMediaAnimation;
        public Handler mHandler = new Handler();

        public void setPlayingIcon(boolean isPlaying) {
            if (isPlaying) {
                mHandler.removeCallbacks(start);
                mHandler.removeCallbacks(stop);
                mHandler.postDelayed(start, 0);
            } else {
                mHandler.removeCallbacks(start);
                mHandler.removeCallbacks(stop);
                mHandler.postDelayed(stop, 0);
            }
        }

        Runnable start = new Runnable() {

            @Override
            public void run() {
                try {
                    mediaPlayView.setImageDrawable(mMediaAnimation);
                    mMediaAnimation.stop();
                    mMediaAnimation.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable stop = new Runnable() {
            @Override
            public void run() {
                try {
                    mMediaAnimation.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        //[BUGFIX]-ADD by yanjia.li, 2016-06-20,BUG-2197064 end
    }

}
