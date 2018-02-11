package cn.tcl.music.adapter.live;

import android.app.Activity;
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
import com.xiami.sdk.utils.ImageUtil;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.view.FooterView;
import cn.tcl.music.view.OnDetailItemClickListener;

/**
 * 热门音乐推荐详情页Adapter
 */

public class RankDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "RankDetailAdapter";
    private Context context;
    private List<SongDetailBean> dataList;
    private OnDetailItemClickListener onDetailItemClickListener;
    private MyBroadcastReceive myBroadcast = new MyBroadcastReceive();
    private boolean mPlayState = false;

    public RankDetailAdapter(Context context) {
        this.context = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("cn.tcl.music.sendstate");
        context.registerReceiver(myBroadcast, intentFilter);
    }

    private boolean isMore = true;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    public void setIsMore(boolean isMore) {
        this.isMore = isMore;
    }

    public void addDataList(List<SongDetailBean> dataList) {
        this.dataList = dataList;
    }

    public List<SongDetailBean> getDataList() {
        return dataList;
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
            View rootView = LayoutInflater.from(context)
                    .inflate(R.layout.item_live_rank_detail, parent, false);
            RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.layout_content_live_rank);
            relativeLayout.getLayoutParams().width = SystemUtility.getScreenWidth();
            MusicViewHolder musicViewHolder = new MusicViewHolder(rootView);
            return musicViewHolder;
        } else if (viewType == TYPE_FOOTER) {
            View rootView = LayoutInflater.from(context)
                    .inflate(R.layout.footer_view, parent, false);
            FooterViewHolder footerViewHolder = new FooterViewHolder(rootView);
            footerViewHolder.footerView.hide();
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
                @Override
                public void onClick(View v) {
                    if (onDetailItemClickListener != null) {
                        onDetailItemClickListener.onClick(v, songDetailBean, position);
                    }
                }
            };
            holder.itemView.setOnClickListener(onClickListener);
            holder.contextMenuImageButton.setOnClickListener(onClickListener);
            if (!MusicApplication.getApp().isDataSaver()) {
                Glide.with(context)
                        .load(songDetailBean.album_logo)
                        .placeholder(R.drawable.default_cover_list)
                        .into(holder.mediaArtworkImageView);
            } else {
                Glide.with(context).load("").placeholder(R.drawable.default_cover_ranking).into(holder.mediaArtworkImageView);
            }
            holder.mediaTitleTextView.setText(songDetailBean.song_name);
            StringBuilder stringBuilder = new StringBuilder();
            if (TextUtils.isEmpty(songDetailBean.singers)) {
                if (!TextUtils.isEmpty(songDetailBean.artist_name)) {
                    stringBuilder.append(songDetailBean.artist_name);
                } else {
                    stringBuilder.append(context.getString(R.string.unknown));
                }
            } else {
                stringBuilder.append(songDetailBean.singers);
            }
            stringBuilder.append(" / ");
            if (TextUtils.isEmpty(songDetailBean.album_name)) {
                stringBuilder.append(context.getString(R.string.unknown));
            } else {
                stringBuilder.append(songDetailBean.album_name);
            }
            holder.mediaSubtitleTextView.setText(stringBuilder.toString());
            holder.rankNumTV.setText(String.valueOf(position + 1));
            if (songDetailBean.change > 1000) {//new

                holder.rankTrendIV.setImageResource(R.drawable.picto_new);
            } else if (songDetailBean.change > 0) {//上升
                holder.rankTrendIV.setImageResource(R.drawable.picto_rise);
            } else if (songDetailBean.change == 0) {//没升没降
                holder.rankTrendIV.setImageResource(R.drawable.picto_smooth);
            } else {//下降
                holder.rankTrendIV.setImageResource(R.drawable.picto_drop);
            }

            holder.rankTrendNumTV.setText(String.valueOf(songDetailBean.change > 0 ? songDetailBean.change : -songDetailBean.change));

            MediaInfo currentMedia = null;
//            if (MediaQueue.mCurrentMedia != null) {
//                currentMedia = MediaQueue.mCurrentMedia.getCurrentMedia();
//            }
            boolean isTrackPlaying = (currentMedia != null && currentMedia.songRemoteId != null && songDetailBean != null && songDetailBean.song_id != null
                    && (currentMedia.songRemoteId.trim()).equals(songDetailBean.song_id.trim()));
            holder.mediaTitleTextView.setTextColor(isTrackPlaying ? context.getResources().getColor(R.color.green_4d) : context.getResources().getColor(R.color.black_87));
            holder.mediaSubtitleTextView.setTextColor(isTrackPlaying ? context.getResources().getColor(R.color.green_4d) : context.getResources().getColor(R.color.black_87));

            if (isTrackPlaying) {
                holder.setPlayingIcon(mPlayState);
                holder.mediaPlayView.setVisibility(View.VISIBLE);
            } else {
                holder.setPlayingIcon(false);
                holder.mediaPlayView.setVisibility(View.INVISIBLE);
            }

        } else if (viewHolder instanceof FooterViewHolder) {
            FooterViewHolder holder = (FooterViewHolder) viewHolder;
            if (!isMore) {
                holder.itemView.setVisibility(View.GONE);
            } else {
                //FIXME BY KUANGZENGTAO
                if (getItemCount() == 1) {

                }
            }
        }
    }

    private class MyBroadcastReceive extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("cn.tcl.music.sendstate".equals(action)) {
                int state = intent.getIntExtra("state", 0);
                mPlayState = state == 1 ? true : false;
                notifyDataSetChanged();
            }
        }
    }

    public void unRegisterListener() {
        context.unregisterReceiver(myBroadcast);
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {

        public TextView mediaTitleTextView;
        public TextView mediaSubtitleTextView;
        public ImageView mediaArtworkImageView;
        public ImageView contextMenuImageButton;
        public TextView rankNumTV;
        public ImageView rankTrendIV;
        public TextView rankTrendNumTV;

        public MusicViewHolder(View itemView) {
            super(itemView);
            ViewGroup parent = (ViewGroup) itemView;
            mediaTitleTextView = (TextView) parent.findViewById(R.id.title_text_view);
            mediaSubtitleTextView = (TextView) parent.findViewById(R.id.subtitle_text_view);
            mediaArtworkImageView = (ImageView) parent.findViewById(R.id.artwork_image_view);
            contextMenuImageButton = (ImageView) parent.findViewById(R.id.item_menu_image_button);
            rankNumTV = (TextView) parent.findViewById(R.id.rank_num);
            rankTrendIV = (ImageView) parent.findViewById(R.id.rank_trend);
            mediaPlayView = (ImageView) parent.findViewById(R.id.play_view);
            rankTrendNumTV = (TextView) parent.findViewById(R.id.rank_trend_num);
            mMediaAnimation = (AnimationDrawable) parent.getContext().getResources().getDrawable(R.anim.music_play);
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
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterView footerView;

        public FooterViewHolder(View view) {
            super(view);
            footerView = (FooterView) view;
        }
    }

}
