package cn.tcl.music.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.adapter.holders.MediaViewHolder;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.fragments.SearchSongResultFragment;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.Connectivity;

public class SearchSongResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static int ITEM_HEADER = 0;
    private static int ITEM_CONTENT = 1;
    private static int SEARCH_HEADER_COUNT = 2;
    private static int ONLINE_SEARCH_HEADER_COUNT = 1;

    private Context mContext;
    private SearchSongResultFragment mFragment;

    private int mCurrentOperatePosition = -1;
    public ArrayList<Object> mLocalSongsList = new ArrayList<>();
    public ArrayList<Object> mOnlineSongsList = new ArrayList<>();

    public SearchSongResultAdapter(Context context) {
        mContext = context;
    }

    public SearchSongResultAdapter(SearchSongResultFragment fragment) {
        mFragment = fragment;
        mContext = fragment.getActivity();

    }

    public void setLocalSongsData(ArrayList<Object> localInfo) {
        mLocalSongsList.clear();
        mLocalSongsList.addAll(localInfo);
    }

    public void setOnlineSongsData(ArrayList<Object> onlineInfo) {
        mOnlineSongsList.clear();
        mOnlineSongsList.addAll(onlineInfo);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_HEADER) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_search_header, parent, false);
            MediaViewHolder headerViewHolder = new MediaViewHolder(rootView, null);
            return headerViewHolder;
        } else {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_search_media, parent, false);
            final MediaViewHolder songViewHolder = new MediaViewHolder(rootView, null);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = songViewHolder.getLayoutPosition();
                    if (position > mOnlineSongsList.size()) {
                        position = position - mOnlineSongsList.size() - SEARCH_HEADER_COUNT;
                        mFragment.onItemClick(position);
                    }
                }
            });
            songViewHolder.contextMenuImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = songViewHolder.getLayoutPosition();
                    if (position > mOnlineSongsList.size()) {
                        position = position - mOnlineSongsList.size() - SEARCH_HEADER_COUNT;
                        setCurrentOperatePosition(position);
                        mFragment.onPopulatePopupMenu(v, position, true, getInfoAtAdapterPosition(position));
                    } else {
                        position = position - ONLINE_SEARCH_HEADER_COUNT;
                        mFragment.onPopulatePopupMenu(v, position, false, getOnlineInfoAtAdapterPosition(position));
                    }
                }
            });
            return songViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemType = getItemViewType(position);
        if (holder instanceof MediaViewHolder) {
            if (itemType == ITEM_HEADER) {
                if (position == 0) {
                    if (((MediaViewHolder) holder).mSearchHeaderTextView.getVisibility() == View.GONE) {
                        ((MediaViewHolder) holder).mSearchHeaderTextView.setVisibility(View.VISIBLE);
                    }
                    ((MediaViewHolder) holder).mSearchHeaderTextView.setText(R.string.online_search_result);
                    if (mOnlineSongsList.size() == 0) {
                        ((MediaViewHolder) holder).mNoContentTextView.setVisibility(View.VISIBLE);
                        if (MusicApplication.isNetWorkCanUsed()) {
                            ((MediaViewHolder) holder).mNoContentTextView.setText(R.string.loading);
                        } else {
                            ((MediaViewHolder) holder).mNoContentTextView.setText(R.string.can_not_online_search);
                        }
                    } else {
                        ((MediaViewHolder) holder).mNoContentTextView.setVisibility(View.GONE);
                    }
                } else {
                    ((MediaViewHolder) holder).mSearchHeaderTextView.setText(R.string.local_search_result);
                    if (mLocalSongsList.size() == 0) {
                        ((MediaViewHolder) holder).mSearchHeaderTextView.setVisibility(View.GONE);
                    } else {
                        ((MediaViewHolder) holder).mSearchHeaderTextView.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                MediaInfo currentMedia = MusicPlayBackService.getCurrentMediaInfo();
                MediaViewHolder viewHolder = (MediaViewHolder) holder;
                boolean isTrackPlaying = false;
                if (position > 0 && position < mOnlineSongsList.size() + 1) {
                    //TO DO online search
                    position = position - 1;
                    SongDetailBean songDetailBean = (SongDetailBean) mOnlineSongsList.get(position);
                    viewHolder.mediaTitleTextView.setText(songDetailBean.song_name);
                    viewHolder.mediaSubtitleTextView.setText(songDetailBean.artist_name + " - " + songDetailBean.album_name);
                    viewHolder.mediaFavoriteImageView.setVisibility(View.GONE);
                    viewHolder.mediaLocalStorageImageView.setVisibility(View.VISIBLE);
                } else {
                    position = position - SEARCH_HEADER_COUNT - mOnlineSongsList.size();
                    MediaInfo mediaInfo = ((MediaInfo) mLocalSongsList.get(position));
                    viewHolder.mediaTitleTextView.setText(mediaInfo.title);
                    viewHolder.mediaSubtitleTextView.setText(mediaInfo.artist + " " + mediaInfo.album);
                    viewHolder.mediaFavoriteImageView.setVisibility(mediaInfo.Favorite ? View.VISIBLE : View.GONE);
                    viewHolder.mediaLocalStorageImageView.setVisibility(mediaInfo.isLocal() ? View.VISIBLE : View.GONE);
                    isTrackPlaying = (currentMedia != null && currentMedia.audioId == mediaInfo.audioId);
                }
                if (isTrackPlaying) {
                    viewHolder.mediaPlayView.setVisibility(View.VISIBLE);
                    viewHolder.contextMenuImageButton.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.mediaPlayView.setVisibility(View.INVISIBLE);
                    viewHolder.contextMenuImageButton.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mLocalSongsList.size() == 0 && mOnlineSongsList.size() == 0) {
            return 0;
        }
        return mLocalSongsList.size() + mOnlineSongsList.size() + SEARCH_HEADER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == 1 + mOnlineSongsList.size()) {
            return ITEM_HEADER;
        } else {
            return ITEM_CONTENT;
        }
    }

    public MediaInfo getInfoAtAdapterPosition(int position) {
        return (MediaInfo) mLocalSongsList.get(position);
    }

    public SongDetailBean getOnlineInfoAtAdapterPosition(int position) {
        return (SongDetailBean) mOnlineSongsList.get(position);
    }

    private void setCurrentOperatePosition(int position) {
        mCurrentOperatePosition = position;
    }

    public int getmCurrentOperatePosition() {
        return mCurrentOperatePosition;
    }
}

