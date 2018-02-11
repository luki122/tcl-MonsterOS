package cn.tcl.music.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.activities.LocalAlbumListActivity;
import cn.tcl.music.adapter.holders.MediaViewHolder;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.model.ArtistInfo;
import cn.tcl.music.model.live.ArtistBean;
import cn.tcl.music.util.Connectivity;

public class SearchArtistResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static int ITEM_HEADER = 0;
    private static int ITEM_CONTENT = 1;

    private static int ONLINE_SEARCH_HEADER_COUNT = 1;
    private static int SEARCH_HEADER_COUNT = 2;
    private Context mContext;
    private ArrayList<Object> mLocalArtistList = new ArrayList<>();
    private ArrayList<Object> mOnlineArtistList = new ArrayList<>();

    public SearchArtistResultAdapter(Context context) {
        mContext = context;
    }

    public void setLocalArtistData(ArrayList<Object> localInfo) {
        mLocalArtistList.clear();
        mLocalArtistList.addAll(localInfo);
    }

    public void setOnlineArtistData(ArrayList<Object> onlineInfo) {
        mOnlineArtistList.clear();
        mOnlineArtistList.addAll(onlineInfo);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_HEADER) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_search_header, parent, false);
            MediaViewHolder headerViewHolder = new MediaViewHolder(rootView, null);
            return headerViewHolder;
        } else {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_search_artist, parent, false);
            final MediaViewHolder artistViewHolder = new MediaViewHolder(rootView, null);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Temporary shield online item click
                    if (artistViewHolder.getLayoutPosition() > mOnlineArtistList.size() + ITEM_CONTENT) {
                        int position = artistViewHolder.getLayoutPosition() - mOnlineArtistList.size() - SEARCH_HEADER_COUNT;
                        Bundle bundle = new Bundle();
                        bundle.putString(CommonConstants.BUNDLE_KEY_ARTIST_ID, ((ArtistInfo) mLocalArtistList.get(position)).id);
                        bundle.putString(CommonConstants.BUNDLE_KEY_ARTIST, ((ArtistInfo) mLocalArtistList.get(position)).artist);
                        Intent intent = new Intent(mContext, LocalAlbumListActivity.class);
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                    }
                }
            });
            return artistViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemType = getItemViewType(position);
        if (holder instanceof MediaViewHolder) {
            if (itemType == ITEM_HEADER) {
                if (position == 0) {
                    ((MediaViewHolder) holder).mSearchHeaderTextView.setText(R.string.online_search_result);
                    if (mOnlineArtistList.size() == 0) {
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
                    if (mLocalArtistList.size() == 0) {
                        ((MediaViewHolder) holder).mSearchHeaderTextView.setVisibility(View.GONE);
                    } else {
                        ((MediaViewHolder) holder).mSearchHeaderTextView.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (position > 0 && position < mOnlineArtistList.size() + 1) {
                    //TO DO online search
                    position = position - 1;
                    MediaViewHolder viewHolder = (MediaViewHolder) holder;
                    ArtistBean artistBean = (ArtistBean) mOnlineArtistList.get(position);
                    String artistName = artistBean.artist_name;
                    if (artistName != null && artistName.equals("<unknown>")) {
                        artistName = mContext.getResources().getString(R.string.unknown);
                    }
                    viewHolder.mediaTitleTextView.setText(artistName);
                    viewHolder.mediaSubtitleTextView.setText("");
                } else {
                    position = position - SEARCH_HEADER_COUNT - mOnlineArtistList.size();
                    MediaViewHolder viewHolder = (MediaViewHolder) holder;
                    ArtistInfo artistInfo = (ArtistInfo) mLocalArtistList.get(position);
                    String artistName = artistInfo.artist;
                    if (artistName != null && artistName.equals("<unknown>")) {
                        artistName = mContext.getResources().getString(R.string.unknown);
                    }
                    viewHolder.mediaTitleTextView.setText(artistName);
                    int numAlbums = artistInfo.numberOfAlbums;
                    int numSongs = artistInfo.numberOfTracks;
                    String artistDesc = mContext.getResources().getQuantityString(R.plurals.number_of_albums_2,
                            numAlbums, numAlbums);
                    artistDesc += " " + mContext.getResources().getQuantityString(R.plurals.number_of_songs_2,
                            numSongs, numSongs);
                    viewHolder.mediaSubtitleTextView.setText(artistDesc);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mLocalArtistList.size() == 0 && mOnlineArtistList.size() == 0) {
            return 0;
        }
        return mLocalArtistList.size() + mOnlineArtistList.size() + SEARCH_HEADER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == 1 + mOnlineArtistList.size()) {
            return ITEM_HEADER;
        } else {
            return ITEM_CONTENT;
        }
    }
}
