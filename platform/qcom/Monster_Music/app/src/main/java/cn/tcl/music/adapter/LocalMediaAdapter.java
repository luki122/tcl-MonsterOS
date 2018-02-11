package cn.tcl.music.adapter;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;

import java.util.HashMap;
import java.util.Map;

import cn.tcl.music.R;
import cn.tcl.music.activities.FolderDetailActivity;
import cn.tcl.music.activities.LocalAlbumDetailActivity;
import cn.tcl.music.activities.LocalMusicActivity;
import cn.tcl.music.activities.MyFavouriteMusicActivity;
import cn.tcl.music.activities.PlaylistDetailActivity;
import cn.tcl.music.activities.RecentlyPlayActivity;
import cn.tcl.music.adapter.holders.ClickableViewHolder;
import cn.tcl.music.adapter.holders.MediaViewHolder;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.fragments.LocalMediaFragment;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.ViewHolderBindingUtil;
import cn.tcl.music.view.ColoredRelativeLayout;
import cn.tcl.music.view.image.ImageFetcher;
import mst.app.MstActivity;

public class LocalMediaAdapter extends AbstractMediaAdapter implements ColoredRelativeLayout.IonSlidingButtonListener{
    private final static String TAG = LocalMediaAdapter.class.getSimpleName();
    private boolean mIsMultiMode = false;
    private IonSlidingViewClickListener mIDeleteBtnClickListener;
    private boolean mIsSelectAll = false;
    private Map<Integer,Boolean> mCheckedMap = new HashMap<Integer, Boolean>();

    public boolean mIsShowMyfavorite;

    public boolean getmIsShowMyfavorite() {
        return mIsShowMyfavorite;
    }

    public void setmIsShowMyfavorite(boolean mIsShowMyfavorite) {
        this.mIsShowMyfavorite = mIsShowMyfavorite;
    }

    public interface IonSlidingViewClickListener {
        void onItemClick(View view, int position);
        void onDeleteBtnClick(View view, int position);
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
    }

    public LocalMediaAdapter(Context context, Cursor c, ImageFetcher imageFetcher, boolean isMultiMode) {
        super(context, c, imageFetcher, new int[]{R.id.media_content});
        setHasStableIds(true);
        mIsMultiMode = isMultiMode;
        mIsShowMyfavorite = true;
        mIDeleteBtnClickListener = (IonSlidingViewClickListener) context;
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType, LayoutManager currentRecyclerLayoutManager) {
        switch (viewType) {
            case R.id.media_content: {
                int layoutId = currentRecyclerLayoutManager instanceof GridLayoutManager ? R.layout.case_media_item : R.layout.row_media_item;
                ViewGroup rowContainer = (ViewGroup) mInflater.inflate(layoutId, parent, false);
                ((ColoredRelativeLayout) rowContainer).setSlidingButtonListener(LocalMediaAdapter.this);
                final MediaViewHolder mvh = new MediaViewHolder(rowContainer, this);
                mvh.mTextView_Delete.setText(mContext.getText(R.string.delete));
                mvh.layout_content.getLayoutParams().width = getScreenWidth(mContext);
                mvh.mTextView_Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int n = mvh.getLayoutPosition();
                        mIDeleteBtnClickListener.onDeleteBtnClick(v, n);
                        closeMenu();
                    }
                });
                mvh.layout_content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (menuIsOpen()) {
                            closeMenu();
                            return;
                        } else {
                            int n = mvh.getLayoutPosition();
                            if (mIsMultiMode) {
                                mvh.selectedLayout.setChecked(!mvh.selectedLayout.isChecked());
                                if (mContext instanceof LocalMusicActivity) {
                                    ((LocalMusicActivity) mContext).setSelectedNumber(getmSelectedSongIds().size());
                                    if (isSelectAll()) {
                                        ((LocalMusicActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.cancel_select_all));
                                    } else {
                                        ((LocalMusicActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.select_all));
                                    }
                                } else if (mContext instanceof LocalAlbumDetailActivity) {
                                    ((LocalAlbumDetailActivity) mContext).setSelectedNumber(getmSelectedSongIds().size());
                                    if (isSelectAll()) {
                                        ((LocalAlbumDetailActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.cancel_select_all));
                                    } else {
                                        ((LocalAlbumDetailActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.select_all));
                                    }
                                } else if (mContext instanceof MyFavouriteMusicActivity) {
                                    ((MyFavouriteMusicActivity) mContext).setSelectedNumber(getmSelectedSongIds().size());
                                    if (isSelectAll()) {
                                        ((MyFavouriteMusicActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.cancel_select_all));
                                    } else {
                                        ((MyFavouriteMusicActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.select_all));
                                    }
                                } else if (mContext instanceof RecentlyPlayActivity) {
                                    ((RecentlyPlayActivity) mContext).setSelectedNumber(getmSelectedSongIds().size());
                                    if (isSelectAll()) {
                                        ((RecentlyPlayActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.cancel_select_all));
                                    } else {
                                        ((RecentlyPlayActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.select_all));
                                    }
                                } else if (mContext instanceof PlaylistDetailActivity) {
                                    ((PlaylistDetailActivity) mContext).setSelectedNumber(getmSelectedSongIds().size());
                                    if (isSelectAll()) {
                                        ((PlaylistDetailActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.cancel_select_all));
                                    } else {
                                        ((PlaylistDetailActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.select_all));
                                    }
                                }
                                return;
                            }
                            mIDeleteBtnClickListener.onItemClick(v, n);
                        }
                    }
                });
                mvh.layout_content.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (mContext instanceof FolderDetailActivity || mIsMultiMode){
                            return true;
                        }
                        mvh.selectedLayout.setVisibility(View.VISIBLE);
                        MstActivity activity = null;
                        if (mContext instanceof LocalMusicActivity){
                            activity = (LocalMusicActivity) mContext;
                            Fragment localMusicActivityCurrentFragment = ((LocalMusicActivity)activity).getCurrentFragment();
                            if (localMusicActivityCurrentFragment != null && localMusicActivityCurrentFragment instanceof LocalMediaFragment) {
                                ((LocalMediaFragment) localMusicActivityCurrentFragment).goToMultiChoose();
                                ((LocalMediaFragment) localMusicActivityCurrentFragment).hideDownloadManager();
                                ((LocalMediaFragment) localMusicActivityCurrentFragment).noclickableplayall();
                            }
                            ((LocalMusicActivity)activity).setSelectedNumber(getmSelectedSongIds().size());
                        } else if(mContext instanceof LocalAlbumDetailActivity) {
                            activity = (LocalAlbumDetailActivity) mContext;
                            ((LocalAlbumDetailActivity) activity).goToMultiChoose();
                            ((LocalAlbumDetailActivity) activity).setSelectedNumber(getmSelectedSongIds().size());
                        } else if(mContext instanceof MyFavouriteMusicActivity) {
                            activity = (MyFavouriteMusicActivity) mContext;
                            ((MyFavouriteMusicActivity) activity).goToMultiChoose();
                            ((MyFavouriteMusicActivity) activity).setSelectedNumber(getmSelectedSongIds().size());
                        } else if(mContext instanceof PlaylistDetailActivity) {
                            activity = (PlaylistDetailActivity) mContext;
                            ((PlaylistDetailActivity) activity).goToMultiChoose();
                            ((PlaylistDetailActivity) activity).setSelectedNumber(getmSelectedSongIds().size());
                        } else if(mContext instanceof RecentlyPlayActivity) {
                            activity = (RecentlyPlayActivity) mContext;
                            ((RecentlyPlayActivity) activity).goToMultiChoose();
                            ((RecentlyPlayActivity) activity).setSelectedNumber(getmSelectedSongIds().size());
                        }
                        setMultiMode(true, -1);
                        return true;
                    }
                });
                mvh.selectedLayout.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int n = mvh.getLayoutPosition();
                        Cursor cursor = getCursorAtAdapterPosition(n);
                        int id = cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID));
                        if (isChecked){
                            addSelectedSongId(id);
                        } else {
                            removeSelectedSongId(id);
                        }
//                        if (mContext instanceof LocalMusicActivity){
//                            mOnMediaFragmentSelectedListener.onAudioSelectdNum(getmSelectedSongIds());
//                        } else if(mContext instanceof LocalAlbumDetailActivity) {
//                            mOnDetailFragmentSelectedListener.onAudioSelectdNum(getmSelectedSongIds());
//                        } else if(mContext instanceof MyFavouriteMusicActivity) {
//                            mOnMyFavouriteFragmentSelectedListener.onAudioSelectdNum(getmSelectedSongIds());
//                        } else if(mContext instanceof PlaylistDetailActivity) {
//                            mOnPlayListFragmentSelectedListener.onAudioSelectdNum(getmSelectedSongIds());
//                        } else if(mContext instanceof RecentlyPlayActivity) {
//                            mOnRecentlyFragmentSelectedListener.onAudioSelectdNum(getmSelectedSongIds());
//                        }
                    }
                });
                mvh.defaultTextColor = currentRecyclerLayoutManager instanceof GridLayoutManager ? 0xFFFFFFFF : 0xFF333333;
                return mvh;
            }
            default: {
                throw new IllegalArgumentException("View type is not recognized, cannot create a view Holder ");
            }
        }
    }

    @Override
    public void onBindCursorToViewHolder(ClickableViewHolder viewHolder, int position) {
        MediaViewHolder mvh = (MediaViewHolder) viewHolder;
        LogUtil.d(TAG,"onBindCursorToViewHolder and position is " + position + " and title is " + mCursor.getString(mCursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.TITLE)));
        ViewHolderBindingUtil.bindSong(mContext, mvh, mCursor, mImageFetcher);
        mvh.selectedLayout.setVisibility(mIsMultiMode ? View.INVISIBLE : View.GONE);
        mvh.mTextView_Delete.setVisibility(mIsMultiMode ? View.GONE : View.VISIBLE);
        if (menuIsOpen()) {
            closeMenu();
        }

        MediaInfo currentMedia = MusicPlayBackService.getCurrentMediaInfo();
        long audioId = mCursor.getLong(mCursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID));
        boolean isTrackPlaying = (currentMedia != null && currentMedia.audioId == audioId);

        if (mIsMultiMode) {
            int id = mCursor.getInt(mCursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID));
            mvh.selectedLayout.setVisibility(View.VISIBLE);
            if (mIsSelectAll) {
                mvh.selectedLayout.setChecked(mCheckedMap.get(position));
                mIsSelectAll = false;
            } else if (getmSelectedSongIds().contains(id)) {
                mvh.selectedLayout.setChecked(true);
            } else {
                mvh.selectedLayout.setChecked(false);
            }
            mvh.mediaPlayView.setVisibility(View.INVISIBLE);
            mvh.contextMenuImageButton.setVisibility(View.INVISIBLE);
        } else {
            if(isTrackPlaying) {
                mvh.mediaPlayView.setImageResource(R.drawable.ic_isplaying);
                mvh.mediaPlayView.setVisibility(View.VISIBLE);
                mvh.contextMenuImageButton.setVisibility(View.GONE);
            } else {
                mvh.mediaPlayView.setVisibility(View.INVISIBLE);
                mvh.contextMenuImageButton.setVisibility(View.VISIBLE);
            }
        }

        if (mIsShowMyfavorite == false) {
            mvh.mediaFavoriteImageView.setVisibility(View.GONE);
        }
    }

    private int getScreenWidth(Context mContext) {
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    private ColoredRelativeLayout mColoredRelativeLayout = null;

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

    public void removeData(int position) {
//        mDatas.remove(position);
        notifyItemRemoved(position);

    }


    @Override
    public SpanSizeLookup createSpanSizeLookUp() {
        return new SpanSizeLookup() {

            @Override
            public int getSpanSize(int position) {
//                if (LocalMediaFragment.DISPLAY_SORT_OPTIONS && mIsLiteForDeezer)//[BUGFIX]-Mod by TCTNB,bo-hu, 2015-09-21,PR1090477
//                {
//                    if (position > 1)
//                        return 1;
//                    else
//                        return TabRecyclerViewFragment.SPAN_SIZE;//[BUGFIX]-Add by TCTNJ,qingjing.zhao, 2015-08-05,PR1057320 begin-end
//                } else {
//                    if (position > 0)
//                        return 1;
//                    else
//                        return TabRecyclerViewFragment.SPAN_SIZE;//[BUGFIX]-Add by TCTNJ,qingjing.zhao, 2015-08-05,PR1057320 begin-end
//                }
                return 1;
            }
        };
    }

    @Override
    public void onBindViewTypeToViewHolder(ClickableViewHolder viewHolder,
                                           int position, int itemViewType) {
//        if (itemViewType == R.id.media_shuffle_all && viewHolder instanceof ShuffleAllViewHolder) {
//            BatchOperateHolder savh = (BatchOperateHolder) viewHolder;
//            if (mCursor == null || mCursor.getCount() <= 0) {
//                savh.playAllBtn.setEnabled(false);
//                savh.batchPperate.setClickable(false);
//            } else {
//                savh.playAllBtn.setEnabled(true);
//                savh.batchPperate.setClickable(true);
//            }
//        }
    }

    @Override
    public int getItemCountFor(int itemViewType) {
        switch (itemViewType) {
            case R.id.media_content:
                return super.getItemCountFor(itemViewType);
//            case R.id.media_header:
//                //return LocalMediaFragment.DISPLAY_SORT_OPTIONS && mIsLiteForDeezer ? 1 : 0;//[BUGFIX]-Mod by TCTNB,bo-hu, 2015-09-21,PR1090477
//                return 1;
            default:
                return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return R.id.media_content;
//        if (position == 0) {
//            return R.id.media_header;
//        } else {
//            return R.id.media_content;
//        }

    }

    @Override
    public long getItemIdForViewType(int position, int itemViewType) {
        return super.getItemIdForViewType(position, itemViewType);
    }

    @Override
    public int getPositionForContent(int position) {
        return position;
//        if (position > 0)
//            return position - 1;
//        else
//            return position;

    }

    public void setMultiMode(boolean isMultiMode, int firstSongId) {
        this.mIsMultiMode = isMultiMode;
        if (mIsMultiMode) {
//            hideShuffleButton = true;
            // -1:come from palylist add button
            //  0:come from normal case
            // >0:come from longClick
            if (firstSongId > 0) {
                addSelectedSongId(firstSongId);
            }
        } else {
//            hideShuffleButton = false;
            clearSelectedSongs();
        }
        notifyDataSetChanged();
    }

    public void setCurrentVisibleMode(int currentVisibleMode) {
    }


//    public void registerListener() {
//
//        //register broadcast
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("cn.tcl.music.sendstate");
//        mContext.registerReceiver(myBroadcast, intentFilter);
//    }
//
//    public void unRegisterListener() {
//        mContext.unregisterReceiver(myBroadcast);
//    }
//
//    private class MyBroadcastReceive extends BroadcastReceiver {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if ("cn.tcl.music.sendstate".equals(action)) {
//                int state = intent.getIntExtra("state", 0);
//                mPlayState = state == 1 ? true : false;
//                notifyDataSetChanged();
//            }
//        }
//    }

    public void setSelectAll(boolean isSelectAll) {
        mIsSelectAll = true;
        for (int i = 0; i < getItemCount(); i++) {
            mCheckedMap.put(i, isSelectAll);
            Cursor cursor = getCursorAtAdapterPosition(i);
            int id = cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID));
            if (isSelectAll) {
                addSelectedSongId(id);
            } else {
                clearSelectedSongs();
            }
        }
        if (mContext instanceof LocalMusicActivity) {
            ((LocalMusicActivity) mContext).setSelectedNumber(getmSelectedSongIds().size());
        } else if (mContext instanceof LocalAlbumDetailActivity) {
            ((LocalAlbumDetailActivity) mContext).setSelectedNumber(getmSelectedSongIds().size());
        } else if (mContext instanceof MyFavouriteMusicActivity) {
            ((MyFavouriteMusicActivity) mContext).setSelectedNumber(getmSelectedSongIds().size());
        } else if (mContext instanceof RecentlyPlayActivity) {
            ((RecentlyPlayActivity) mContext).setSelectedNumber(getmSelectedSongIds().size());
        } else if (mContext instanceof PlaylistDetailActivity) {
            ((PlaylistDetailActivity) mContext).setSelectedNumber(getmSelectedSongIds().size());
        }
    }

    public boolean isSelectAll() {
        if (mCheckedMap != null && getmSelectedSongIds() != null) {
            return (mCheckedMap.size() != 0 || getmSelectedSongIds().size() != 0) && (mCheckedMap.size() == getmSelectedSongIds().size());
        }
        return false;
    }

}
