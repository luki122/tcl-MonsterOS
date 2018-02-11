package cn.tcl.music.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.model.MediaInfo;

public class SelectSongsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TITLE_KEY_Z = 25;
    private final static int TITLE_KEY_OTHER = 26;

    private Context mContext;
    private ArrayList<MediaInfo> mSongsList = new ArrayList<>();
    private ArrayList<Long> mSelectedSongIds = new ArrayList<Long>();
    private ArrayList<Long> mPlayListSelectedSongIds = new ArrayList<Long>();
    private Map<Integer, Boolean> mCheckedMap = new HashMap<Integer, Boolean>();
    public List<Integer> mIndexList = new ArrayList<>();
    private boolean mIsSelectAll = false;
    private OnSongsSelectedListner mOnSongsSelectedListner;

    public void setOnSelectSongsListner(OnSongsSelectedListner onSongsSelectedListner) {
        mOnSongsSelectedListner = onSongsSelectedListner;
    }

    public ArrayList<Long> getmSelectedSongIds() {
        return mSelectedSongIds;
    }

    public int getPositionForSection(int sectionIndex) {
        int pos = 0;
        if (mIndexList.size() == 0) {
            return -1;
        }
        if (sectionIndex == 0) {
            return 0;
        } else {
            for (int i = sectionIndex; i > 0; i--) {
                pos += mIndexList.get(i - 1);
            }
            if (pos >= getItemCount()) {
                return getItemCount() - 1;
            }
            return pos;
        }
    }

    //Callback for update UI
    public interface OnSongsSelectedListner {
        void onSongsSelected(ArrayList<Long> songIds);
        void onSongsSelectedCount(int count);
    }

    public SelectSongsAdapter(Context context) {
        mContext = context;
    }

    public void setData(ArrayList<MediaInfo> mediaInfos) {
        mSongsList = mediaInfos;
        buildIndexList(mSongsList);
    }

    public void setSelectedSongs(long[] ids) {
        if (ids != null && ids.length > 0) {
            for (int i = 0; i < ids.length; i++) {
                mPlayListSelectedSongIds.add(ids[i]);
            }
        }
    }

    private void buildIndexList(ArrayList<MediaInfo> songList) {
        int currentKey = 0;
        int currentTitleCount = 0;
        int otherTitleCount = 0;
        if (songList != null && songList.size() > 0) {
            for (MediaInfo info : songList) {
                String titleKey = info.Key;
                if (titleKey.charAt(2) == CommonConstants.LETTER_STRING.charAt(TITLE_KEY_Z)) {
                    otherTitleCount++;
                }
                if (titleKey.charAt(0) == CommonConstants.LETTER_STRING.charAt(currentKey)) {
                    currentTitleCount++;
                } else if (currentKey < TITLE_KEY_Z) {
                    mIndexList.add(currentKey, currentTitleCount);
                    currentKey++;
                    //set middle empty title count as 0
                    while (currentKey < TITLE_KEY_OTHER && titleKey.charAt(0) !=
                            CommonConstants.LETTER_STRING.charAt(currentKey)) {
                        currentTitleCount = 0;
                        mIndexList.add(currentKey, currentTitleCount);
                        currentKey++;
                    }
                    currentTitleCount = 1;
                }
            }
            //set end empty title count as 0
            if (currentKey < TITLE_KEY_Z) {
                mIndexList.add(currentKey, currentTitleCount);
                while (currentKey < TITLE_KEY_OTHER) {
                    mIndexList.add(++currentKey, 0);
                }
            } else {
                mIndexList.add(TITLE_KEY_Z, currentTitleCount - otherTitleCount);
                mIndexList.add(TITLE_KEY_OTHER, otherTitleCount);
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        List<MediaInfo> dataList = this.mSongsList;
        String artworkpath;
        if (getItemCount() == 0) {
            return;
        }
        if (holder instanceof MusicViewHolder) {
            MusicViewHolder viewHolder = (MusicViewHolder) holder;
            viewHolder.mediaTitleTextView.setText(dataList.get(position).title);
            viewHolder.mediaSubtitleTextView.setText(dataList.get(position).artist +
                    " " + dataList.get(position).album);
            artworkpath = dataList.get(position).artworkPath;
            Glide.with(mContext)
                    .load(artworkpath)
                    .placeholder(R.drawable.default_cover_list)
                    .into(viewHolder.mediaArtworkImageView);

            long id = mSongsList.get(position).audioId;
            if (mIsSelectAll) {
                viewHolder.selectCheckBox.setChecked(mCheckedMap.get(position));
                mIsSelectAll = false;
            } else if (mSelectedSongIds.contains(id)) {
                viewHolder.selectCheckBox.setChecked(true);
            } else {
                viewHolder.selectCheckBox.setChecked(false);
            }
            //Set the songs in play list disabled.
            if (mPlayListSelectedSongIds.contains(id)) {
                viewHolder.selectCheckBox.setChecked(true);
                viewHolder.mediaTitleTextView.setAlpha(CommonConstants.VIEW_LOCAL_NO_SELECTER_TITLE_ALPHA);
                viewHolder.mediaSubtitleTextView.setAlpha(CommonConstants.VIEW_LOCAL_NO_SELECTER_TITLE_ALPHA);
                viewHolder.selectSongsItemLayout.setClickable(false);
                viewHolder.selectCheckBox.setEnabled(false);
            } else {
                viewHolder.selectCheckBox.setEnabled(true);
                viewHolder.selectSongsItemLayout.setClickable(true);
                viewHolder.mediaTitleTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_ENABLE_ALPHA);
                viewHolder.mediaSubtitleTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_ENABLE_ALPHA);
            }
            mOnSongsSelectedListner.onSongsSelectedCount(mSelectedSongIds.size());
        }
    }

    @Override
    public int getItemCount() {
        return mSongsList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_select_songs, parent, false);
        final MusicViewHolder musicViewHolder = new MusicViewHolder(rootView);
        musicViewHolder.selectSongsItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicViewHolder.selectCheckBox.setChecked(!musicViewHolder.selectCheckBox.isChecked());
            }
        });

        musicViewHolder.selectCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int position = musicViewHolder.getLayoutPosition();
                long id = mSongsList.get(position).audioId;
                if (isChecked) {
                    if (!mSelectedSongIds.contains(id)) {
                        mSelectedSongIds.add(id);
                    }
                } else {
                    if (mSelectedSongIds.contains(id)) {
                        mSelectedSongIds.remove(id);
                    }
                }
                for (long playlistSongsId : mPlayListSelectedSongIds) {
                    mSelectedSongIds.remove(playlistSongsId);
                }
                mOnSongsSelectedListner.onSongsSelected(mSelectedSongIds);
                mOnSongsSelectedListner.onSongsSelectedCount(mSelectedSongIds.size());
            }
        });
        return musicViewHolder;
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {

        public TextView mediaTitleTextView;
        public TextView mediaSubtitleTextView;
        public ImageView mediaArtworkImageView;
        public RelativeLayout selectSongsItemLayout;
        public CheckBox selectCheckBox;

        public MusicViewHolder(View itemView) {
            super(itemView);
            ViewGroup parent = (ViewGroup) itemView;
            mediaTitleTextView = (TextView) parent.findViewById(R.id.title_text_view);
            mediaSubtitleTextView = (TextView) parent.findViewById(R.id.subtitle_text_view);
            mediaArtworkImageView = (ImageView) parent.findViewById(R.id.artwork_image_view);
            selectSongsItemLayout = (RelativeLayout) parent.findViewById(R.id.select_songs_item_view);
            selectCheckBox = (CheckBox) parent.findViewById(R.id.selected_checkbox);
        }
    }

    public void selectAll(boolean isSelectAll) {
        mIsSelectAll = true;
        for (int i = 0; i < getItemCount(); i++) {
            mCheckedMap.put(i, isSelectAll);
            long id = mSongsList.get(i).audioId;
            if (isSelectAll) {
                if (!mSelectedSongIds.contains(id)) {
                    mSelectedSongIds.add(id);
                }
            } else {
                if (mSelectedSongIds.contains(id)) {
                    mSelectedSongIds.remove(id);
                }
            }
        }
    }
}
