package cn.tcl.music.adapter;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.tcl.music.R;
import cn.tcl.music.activities.FolderDetailActivity;
import cn.tcl.music.activities.LocalMusicActivity;
import cn.tcl.music.adapter.holders.ClickableViewHolder;
import cn.tcl.music.adapter.holders.MediaViewHolder;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.fragments.LocalArtistsFragment;
import cn.tcl.music.util.ViewHolderBindingUtil;
import cn.tcl.music.view.ColoredRelativeLayout;
import cn.tcl.music.view.image.ImageFetcher;
import mst.app.MstActivity;

public class LocalArtistAdapter extends AbstractMediaAdapter implements ColoredRelativeLayout.IonSlidingButtonListener {
    private static final String TAG = LocalArtistAdapter.class.getSimpleName();

    private final static int ARTIST_KEY_Z = 25;
    private final static int ARTIST_KEY_OTHER = 26;
    private final static int SECTIONS_COUNT = 27;
    public List<Integer> mIndexList = new ArrayList<>();
    public List<Integer> mHeaderPositionList = new ArrayList<>();
    public int mEmptyNumber = 0;
    private int songNum = -1;
    private String mHeadLetter = null;
    private IonSlidingViewClickListener mIDeleteBtnClickListener;
    private ColoredRelativeLayout mColoredRelativeLayout = null;

    private boolean mIsMultiMode = false;
    private boolean mHideShuffleButton = false;
    private boolean mIsSelectAll = false;
    private Map<Integer, Boolean> mCheckedMap = new HashMap<Integer, Boolean>();
    int[] mItemViewTypes;

    public LocalArtistAdapter(Context context, Cursor c, ImageFetcher imageFetcher) {
        super(context, c, imageFetcher);
        setHasStableIds(true);
        buildIndexList(c);
        mHeaderPositionList = getHeaderPositionList();
        mIDeleteBtnClickListener = (IonSlidingViewClickListener) context;
    }

    public interface IonSlidingViewClickListener {
        void onItemClick(View view, int position);
        void onDeleteBtnClick(View view, int position);
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        mIndexList.clear();
        mHeaderPositionList.clear();
        mEmptyNumber = 0;
        buildIndexList(cursor);
        mHeaderPositionList = getHeaderPositionList();
    }

    private void buildIndexList(Cursor cursor) {
        Cursor cur = cursor;
        int currentKey = 0;
        int currentCount = 0;
        int otherCount = 0;
        if (cur != null && cur.getCount() > 0) {
            cur.moveToFirst();
            do {
                String artistKey = cur.getString(cur.getColumnIndex(MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST_KEY));
                if (artistKey.charAt(2) == CommonConstants.LETTER_STRING.charAt(ARTIST_KEY_Z)) {
                    otherCount++;
                }
                if (artistKey.charAt(0) == CommonConstants.LETTER_STRING.charAt(currentKey)) {
                    currentCount++;
                } else if (currentKey < ARTIST_KEY_Z) {
                    mIndexList.add(currentKey, currentCount);
                    currentKey++;
                    while (currentKey < ARTIST_KEY_OTHER && artistKey.charAt(0) != CommonConstants.LETTER_STRING.charAt(currentKey)) {
                        currentCount = 0;
                        mIndexList.add(currentKey, currentCount);
                        currentKey++;
                    }
                    currentCount = 1;
                }
            } while (cur.moveToNext());
            if (currentKey < ARTIST_KEY_Z) {
                mIndexList.add(currentKey, currentCount);
                while (currentKey < ARTIST_KEY_OTHER) {
                    mIndexList.add(++currentKey, 0);
                }
            } else {
                mIndexList.add(ARTIST_KEY_Z, currentCount - otherCount);
                mIndexList.add(ARTIST_KEY_OTHER, otherCount);
            }
        }
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType, RecyclerView.LayoutManager currentLayoutManager) {
        if (viewType == R.id.media_header) {
            ViewGroup rowContainer = (ViewGroup) mInflater.inflate(R.layout.row_header_item,
                    parent, false);

            MediaViewHolder mvh = new MediaViewHolder(rowContainer, null);
            return mvh;
        } else {
            int layoutId = currentLayoutManager instanceof GridLayoutManager ? R.layout.case_media_item
                    : R.layout.row_artists_item;

            ViewGroup rowContainer = (ViewGroup) mInflater.inflate(layoutId,
                    parent, false);
            ((ColoredRelativeLayout) rowContainer).setSlidingButtonListener(LocalArtistAdapter.this);
            final MediaViewHolder mvh = new MediaViewHolder(rowContainer, this);
            mvh.layout_content.getLayoutParams().width = getScreenWidth(mContext);
            mvh.mTextView_Delete.setText(mContext.getText(R.string.delete));
            mvh.mTextView_Delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = mvh.getLayoutPosition();
                    int n = layoutPosition - countHeaderSize(layoutPosition);
                    mIDeleteBtnClickListener.onDeleteBtnClick(v, n);
                    closeMenu();
                }
            });
            mvh.defaultTextColor = currentLayoutManager instanceof GridLayoutManager ? 0xFFFFFFFF : 0xFF333333;
            mvh.mediaArtworkImageView.setVisibility(View.INVISIBLE);
            mvh.layout_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (menuIsOpen()) {
                        closeMenu();
                        return;
                    }
                    if (mIsMultiMode) {
                        mvh.selectedLayout.setChecked(!mvh.selectedLayout.isChecked());
                        ((LocalMusicActivity) mContext).setSelectedNumber(getmSelectedArtistIds().size());
                        if (isSelectAll()) {
                            ((LocalMusicActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.cancel_select_all));
                        } else {
                            ((LocalMusicActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.select_all));
                        }
                        return;
                    }
                    int layoutPosition = mvh.getLayoutPosition();
                    int position = layoutPosition - countHeaderSize(layoutPosition);
                    mIDeleteBtnClickListener.onItemClick(v,position);
                }
            });
            mvh.layout_content.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mContext instanceof FolderDetailActivity || mIsMultiMode) {
                        return true;
                    }
                    mvh.selectedLayout.setVisibility(View.VISIBLE);
                    MstActivity activity = null;
                    if (mContext instanceof LocalMusicActivity){
                        activity = (LocalMusicActivity) mContext;
                        Fragment localMusicActivityCurrentFragment = ((LocalMusicActivity)activity).getCurrentFragment();
                        if (localMusicActivityCurrentFragment != null && localMusicActivityCurrentFragment instanceof LocalArtistsFragment) {
                            ((LocalArtistsFragment) localMusicActivityCurrentFragment).goToMultiChoose();
                            ((LocalMusicActivity)activity).setSelectedNumber(getmSelectedSongIds().size());
                        }
                    }
                    setMultiMode(true, -1);
                    return true;
                }
            });
            mvh.selectedLayout.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int layoutPosition = mvh.getLayoutPosition();
                    int n = layoutPosition - countHeaderSize(layoutPosition);
                    Cursor cursor = getCursorAtAdapterPosition(n);
                    int id = cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Artists.ArtistsColumns._ID));
                    if (isChecked) {
                        addSelectedArtistIds(id);
                    } else {
                        removeSelectedArtistIds(id);
                    }
//                    mOnMediaFragmentSelectedListener.onAudioSelectdNum(getmSelectedArtistIds());
                }
            });
            return mvh;
        }
    }

    private int getScreenWidth(Context mContext) {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position, List<Object> payloads) {
        int currentPosition = getHeaderLetterPosition(position);
        if (currentPosition == -1) {
            return;
        }
        if (currentPosition == 26) {
            mHeadLetter = CommonConstants.SECTIONS_NAVIGATION.substring(currentPosition);
        } else {
            mHeadLetter = CommonConstants.SECTIONS_NAVIGATION.substring(currentPosition, currentPosition + 1);
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindCursorToViewHolder(ClickableViewHolder clickableViewHolder, int position) {
        MediaViewHolder viewHolder = (MediaViewHolder) clickableViewHolder;
        viewHolder.itemView.setTag(CommonConstants.TAG_CONTENT_VIEW);
        viewHolder.itemView.setContentDescription(mHeadLetter);
        ViewHolderBindingUtil.bindArtist(mContext, viewHolder, mCursor, mImageFetcher);
        viewHolder.selectedLayout.setVisibility(mIsMultiMode ? View.INVISIBLE : View.GONE);
        viewHolder.mTextView_Delete.setVisibility(mIsMultiMode ? View.GONE : View.VISIBLE);
        int id = mCursor.getInt(mCursor.getColumnIndex(MusicMediaDatabaseHelper.Artists.ArtistsColumns._ID));
        if (menuIsOpen()) {
            closeMenu();
        }
        if (mIsMultiMode) {
            viewHolder.selectedLayout.setVisibility(View.VISIBLE);
            if (mIsSelectAll) {
                viewHolder.selectedLayout.setChecked(mCheckedMap.get(position));
                mIsSelectAll = false;
            } else if (getmSelectedArtistIds().contains(id)) {
                viewHolder.selectedLayout.setChecked(true);
            } else {
                viewHolder.selectedLayout.setChecked(false);
            }
        }
    }

    @Override
    public void onBindViewTypeToViewHolder(ClickableViewHolder viewHolder, int position, int itemViewType) {
        if (itemViewType == R.id.media_header) {
            MediaViewHolder holder = (MediaViewHolder) viewHolder;
            holder.mTextView_Head.setText(mHeadLetter);
            viewHolder.itemView.setTag(CommonConstants.TAG_HEAD_VIEW);
            viewHolder.itemView.setContentDescription(mHeadLetter);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? R.id.media_header : R.id.media_content;
    }

    @Override
    public long getItemIdForViewType(int position, int itemViewType) {
        if (itemViewType == com.mixvibes.mvlib.R.id.media_header) {
            return position;
        } else {
            return super.getItemIdForViewType(position, itemViewType);
        }

    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + SECTIONS_COUNT - mEmptyNumber;
    }

    private List<Integer> getHeaderPositionList() {
        List<Integer> list = mIndexList;
        List<Integer> positionList = new ArrayList<>();
        if (mIndexList.size() == 0) {
            mEmptyNumber = SECTIONS_COUNT;
            return positionList;
        }
        int position = 0;
        positionList.add(0, position);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != 0) {
                position++;
                position = position + list.get(i);
            } else {
                mEmptyNumber++;
            }
            //不需要增加最后一个字母下的歌手数量
            if (i != list.size() - 1) {
                positionList.add(i + 1, position);
            }
        }
        return positionList;
    }

    @Override
    public int countHeaderSize(int position) {
        int emptyCount = 0;
        List<Integer> list = mHeaderPositionList;
        int i = 0;
        for (; i < list.size(); i++) {
            if (i >= 1 && mIndexList.get(i - 1) == 0) {
                emptyCount++;
            }
            if (position <= list.get(i)) {
                return i - emptyCount;
            }
        }
        return i - emptyCount;
    }

    private int getHeaderLetterPosition(int position) {
        List<Integer> list = mHeaderPositionList;
        int i = 0;
        for (; i < list.size(); i++) {
            if (position < list.get(i)) {
                return i - 1;
            }
        }
        return i - 1;
    }

    private boolean isHeader(int position) {
        List<Integer> list = mHeaderPositionList;
        for (int i : list) {
            if (position == i) {
                return true;
            }
        }
        return false;
    }

    public int getPositionForSection(int sectionIndex) {
        int pos = 0;
        Log.d(TAG, "getPositionForSection: sectionIndex = " + sectionIndex);
        if (mIndexList.size() == 0) {
            return -1;
        }
        if (sectionIndex == 0) {
            return 0;
        } else {
            for (int i = sectionIndex; i > 0; i--) {
                pos += mIndexList.get(i - 1);
                if (mIndexList.get(i - 1) != 0) {
                    pos++;
                }
                Log.d(TAG, "getPositionForSection: pos = " + pos);
            }
            if (pos >= getItemCount()) {
                return getItemCount() - 1;
            }
            return pos;
        }
    }

    @Override
    public void onMenuIsOpen(View view) {
        mColoredRelativeLayout = (ColoredRelativeLayout) view;
    }

    @Override
    public void onDownOrMove(ColoredRelativeLayout slidingButtonView) {
        if (menuIsOpen()) {
            if (mColoredRelativeLayout != slidingButtonView) {
                closeMenu();
            }
        }
    }

    public boolean menuIsOpen() {
        if (mColoredRelativeLayout != null) {
            return true;
        }
        return false;
    }

    public void closeMenu() {
        if (null != mColoredRelativeLayout) {
            mColoredRelativeLayout.closeMenu();
            mColoredRelativeLayout = null;
        }
    }

    public void setMultiMode(boolean isMultiMode, int firstSongId) {
        this.mIsMultiMode = isMultiMode;
        if (mIsMultiMode) {
            //        hideShuffleButton = true;
            // -1:come from palylist add button
            //  0:come from normal case
            // >0:come from longClick
            if (firstSongId > 0) {
                addSelectedArtistIds(firstSongId);
            }
        } else {
            //         hideShuffleButton = false;
            clearSelectedArtist();
        }
        notifyDataSetChanged();
    }

    public void setSelectAll(boolean isSelectAll) {
        mIsSelectAll = true;
        for (int i = 0; i < (getItemCount() - SECTIONS_COUNT + mEmptyNumber); i++) {
            mCheckedMap.put(i, isSelectAll);
            Cursor cursor = getCursorAtAdapterPosition(i);
            int id = cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Artists.ArtistsColumns._ID));
            if (isSelectAll) {
                addSelectedArtistIds(id);
            } else {
                clearSelectedArtist();
            }
        }
        if (mContext instanceof LocalMusicActivity) {
            ((LocalMusicActivity) mContext).setSelectedNumber(getmSelectedArtistIds().size());
        }
    }

    public boolean isSelectAll() {
        if (mCheckedMap != null && getmSelectedArtistIds() != null) {
            return (mCheckedMap.size() != 0 || getmSelectedArtistIds().size() != 0) && (mCheckedMap.size() == getmSelectedArtistIds().size());
        }
        return false;
    }
}
