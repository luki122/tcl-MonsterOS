package cn.tcl.music.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.activities.LocalAlbumDetailActivity;
import cn.tcl.music.adapter.holders.MediaViewHolder;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.model.AlbumInfo;
import cn.tcl.music.model.live.AlbumBean;
import cn.tcl.music.util.Connectivity;

public class SearchAlbumResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private static int ITEM_HEADER = 0;
    private static int ITEM_CONTENT = 1;
    private static int SEARCH_HEADER_COUNT = 2;
    private static int ONLINE_SEARCH_HEADER_COUNT = 1;
    private ArrayList<Object> mLocalAlbumList = new ArrayList<>();
    private ArrayList<Object> mOnlineAlbumList = new ArrayList<>();

    public SearchAlbumResultAdapter(Context context) {
        mContext = context;
    }

    public void setLocalAlbumData(ArrayList<Object> localInfo) {
        mLocalAlbumList.clear();
        mLocalAlbumList.addAll(localInfo);
    }

    public void setOnlineAlbumData(ArrayList<Object> onlineInfo) {
        mOnlineAlbumList.clear();
        mOnlineAlbumList.addAll(onlineInfo);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_HEADER) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_search_header, parent, false);
            MediaViewHolder headerViewHolder = new MediaViewHolder(rootView, null);
            return headerViewHolder;
        } else {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_search_album, parent, false);
            final MediaViewHolder albumViewHolder = new MediaViewHolder(rootView, null);

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Temporary shield online item click
                    if (albumViewHolder.getLayoutPosition() > mOnlineAlbumList.size() + ITEM_CONTENT) {
                        int position = albumViewHolder.getLayoutPosition() - mOnlineAlbumList.size() - SEARCH_HEADER_COUNT;
                        Bundle bundle = new Bundle();
                        bundle.putInt(CommonConstants.BUNDLE_KEY_DETAIL_TYPE, CommonConstants.DETAIL_TYPE_ALBUM);
                        bundle.putString(CommonConstants.BUNDLE_KEY_ALBUM_NAME, ((AlbumInfo) mLocalAlbumList.get(position)).album);
                        bundle.putString(CommonConstants.BUNDLE_KEY_ALBUM_ID, ((AlbumInfo) mLocalAlbumList.get(position)).albumId);
                        bundle.putString(CommonConstants.BUNDLE_KEY_ARTIST, ((AlbumInfo) mLocalAlbumList.get(position)).artist);
                        bundle.putString(CommonConstants.BUNDLE_KEY_ARTIST_ID, ((AlbumInfo) mLocalAlbumList.get(position)).artistId);
                        bundle.putString(CommonConstants.BUNDLE_KEY_ARTWORK, ((AlbumInfo) mLocalAlbumList.get(position)).artworkPath);

                        Intent intent = new Intent(mContext, LocalAlbumDetailActivity.class);
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                    }
                }
            });
            return albumViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemType = getItemViewType(position);
        if (holder instanceof MediaViewHolder) {
            if (itemType == ITEM_HEADER) {
                if (position == 0) {
                    ((MediaViewHolder) holder).mSearchHeaderTextView.setText(R.string.online_search_result);
                    if (mOnlineAlbumList.size() == 0) {
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
                    if (mLocalAlbumList.size() == 0) {
                        ((MediaViewHolder) holder).mSearchHeaderTextView.setVisibility(View.GONE);
                    } else {
                        ((MediaViewHolder) holder).mSearchHeaderTextView.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (position > 0 && position < mOnlineAlbumList.size() + 1) {
                    //TO DO online search
                    position = position - 1;
                    MediaViewHolder viewHolder = (MediaViewHolder) holder;
                    AlbumBean albumBean = (AlbumBean) mOnlineAlbumList.get(position);
                    String albumName = albumBean.album_name;
                    viewHolder.mTitleAlbumTextView.setText(albumName);
                    String artWorkPath = albumBean.album_logo;
                    Glide.with(mContext).load(artWorkPath).placeholder(R.drawable.default_cover_list)
                            .into(viewHolder.mediaArtworkImageView);
                    int numSongs = albumBean.song_count;
                    String albumDesc = mContext.getResources().getQuantityString(R.plurals.number_of_songs_2,
                            numSongs, numSongs);
                    viewHolder.mSongCountsAlbumTextView.setText(albumDesc);
                } else {
                    position = position - SEARCH_HEADER_COUNT - mOnlineAlbumList.size();
                    MediaViewHolder viewHolder = (MediaViewHolder) holder;
                    AlbumInfo albumInfo = (AlbumInfo) mLocalAlbumList.get(position);
                    String albumName = albumInfo.album;
                    if (albumName != null && albumName.equals("<unknown>")) {
                        albumName = mContext.getResources().getString(R.string.unknown);
                    }
                    viewHolder.mTitleAlbumTextView.setText(albumName);
                    String artWorkPath = albumInfo.artworkPath;
                    Glide.with(mContext).load(artWorkPath).placeholder(R.drawable.default_cover_list)
                            .into(viewHolder.mediaArtworkImageView);
                    int numSongs = albumInfo.numberOfTracks;
                    String albumDesc = mContext.getResources().getQuantityString(R.plurals.number_of_songs_2,
                            numSongs, numSongs);
                    viewHolder.mSongCountsAlbumTextView.setText(albumDesc);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mLocalAlbumList.size() == 0 && mOnlineAlbumList.size() == 0) {
            return 0;
        }
        return mLocalAlbumList.size() + mOnlineAlbumList.size() + SEARCH_HEADER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == 1 + mOnlineAlbumList.size()) {
            return ITEM_HEADER;
        } else {
            return ITEM_CONTENT;
        }
    }
}