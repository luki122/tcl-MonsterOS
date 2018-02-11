package cn.tcl.music.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;

import java.util.HashMap;
import java.util.Map;

import cn.tcl.music.R;
import cn.tcl.music.activities.FolderDetailActivity;
import cn.tcl.music.activities.LocalAlbumListActivity;
import cn.tcl.music.adapter.holders.ClickableViewHolder;
import cn.tcl.music.adapter.holders.MediaViewHolder;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.ViewHolderBindingUtil;
import cn.tcl.music.view.ColoredRelativeLayout;
import cn.tcl.music.view.image.ImageFetcher;
import mst.app.MstActivity;

/* MODIFIED-BEGIN by Kang.Ren, 2016-11-07,BUG-3373604*/
/* MODIFIED-END by Kang.Ren,BUG-3373604*/

public class AlbumListAdapter extends AbstractMediaAdapter implements ColoredRelativeLayout.IonSlidingButtonListener {
    private static final String TAG = AlbumListAdapter.class.getSimpleName();
    private int songNum = -1;
    private IonSlidingViewClickListener mIDeleteBtnClickListener;

    private boolean mIsMultiMode = false;
    private boolean mIsSelectAll = false;
    private Map<Integer, Boolean> mCheckedMap = new HashMap<Integer, Boolean>();

    public AlbumListAdapter(Context context, Cursor c, ImageFetcher imageFetcher) {
        super(context, c, imageFetcher);
        mIDeleteBtnClickListener = (IonSlidingViewClickListener) context;
        setHasStableIds(true);
    }

    public interface IonSlidingViewClickListener {
        void onItemClick(View view, int position);

        void onDeleteBtnClick(View view, int position);
    }

    @Override
    public void onBindCursorToViewHolder(ClickableViewHolder clickableViewHolder, int position) {
        MediaViewHolder viewHolder = (MediaViewHolder) clickableViewHolder;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean mShowHiddenTracks = sharedPrefs.getBoolean("show_hidden_tracks", false);
        ViewHolderBindingUtil.bindAlbum(mContext, viewHolder, mCursor, mImageFetcher);
        viewHolder.selectedLayout.setVisibility(mIsMultiMode ? View.INVISIBLE : View.GONE);
        viewHolder.mTextView_Delete.setVisibility(mIsMultiMode ? View.GONE : View.VISIBLE);
        int id = mCursor.getInt(mCursor.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns._ID));
        if (mIsMultiMode) {
            viewHolder.selectedLayout.setVisibility(View.VISIBLE);
            if (mIsSelectAll) {
                viewHolder.selectedLayout.setChecked(mCheckedMap.get(position));
                mIsSelectAll = false;
            } else if (getmSelectedAlbumIds().contains(id)) {
                viewHolder.selectedLayout.setChecked(true);
            } else {
                viewHolder.selectedLayout.setChecked(false);
            }
        }
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType, RecyclerView.LayoutManager currentLayoutManager) {
        int layoutId = currentLayoutManager instanceof GridLayoutManager ? R.layout.case_media_item : R.layout.item_singer_album;
        ViewGroup rowContainer = (ViewGroup) mInflater.inflate(layoutId, parent, false);
        ((ColoredRelativeLayout) rowContainer).setSlidingButtonListener(AlbumListAdapter.this);
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
                LogUtil.d(TAG, "onClick and position is " + mvh.getLayoutPosition());
                if (mIsMultiMode) {
                    mvh.selectedLayout.setChecked(!mvh.selectedLayout.isChecked());
                    ((LocalAlbumListActivity) mContext).setSelectedNumber(getmSelectedAlbumIds().size());
                    if (isSelectAll()) {
                        ((LocalAlbumListActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.cancel_select_all));
                    } else {
                        ((LocalAlbumListActivity) mContext).getActionMode().setPositiveText(mContext.getResources().getString(R.string.select_all));
                    }
                    return;
                }
                if (menuIsOpen()) {
                    closeMenu();
                    return;
                }
                int position = mvh.getLayoutPosition();
                if (((RecyclerViewCursorAdapter<?>) AlbumListAdapter.this).getItemCount() > position) {
                    mIDeleteBtnClickListener.onItemClick(v,position);
                }
            }
        });
        /* MODIFIED-BEGIN by Kang.Ren, 2016-11-07,BUG-3373604*/
        mvh.layout_content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mContext instanceof FolderDetailActivity || mIsMultiMode){
                    return true;
                }
                mvh.selectedLayout.setVisibility(View.VISIBLE);
                MstActivity activity = null;
                if (mContext instanceof LocalAlbumListActivity) {
                    ((LocalAlbumListActivity) mContext).goToMultiChoose();
                    ((LocalAlbumListActivity) mContext).setSelectedNumber(getmSelectedAlbumIds().size());
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
                int id = cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns._ID));
                if (isChecked){
                    addSelectedAlbumIds(id);
                } else {
                    removeSelectedAlbumIds(id);
                }
//                mOnAlbumFragmentSelectedListener.onAudioSelectdNum(getmSelectedAlbumIds());
            }
        });
        /* MODIFIED-END by Kang.Ren,BUG-3373604*/
        return mvh;
    }


    private int getScreenWidth(Context mContext) {
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    private ColoredRelativeLayout mMenu = null;

    @Override
    public void onMenuIsOpen(View view) {
        mMenu = (ColoredRelativeLayout) view;
    }

    @Override
    public void onDownOrMove(ColoredRelativeLayout slidingButtonView) {
        if (menuIsOpen()) {
            if (mMenu != slidingButtonView) {
                closeMenu();
            }
        }
    }

    public boolean menuIsOpen() {
        if (mMenu != null) {
            return true;
        }
        return false;
    }

    public void closeMenu() {
        mMenu.closeMenu();
        mMenu = null;
    }

    public void setMultiMode(boolean isMultiMode, int firstSongId) {
        this.mIsMultiMode = isMultiMode;
        if (mIsMultiMode) {
//            hideShuffleButton = true;
            // -1:come from palylist add button
            //  0:come from normal case
            // >0:come from longClick
            if (firstSongId > 0) {
                addSelectedAlbumIds(firstSongId);
            }
        } else {
//            hideShuffleButton = false;
            clearSelectedAlbumIds();
        }
        notifyDataSetChanged();
    }

    public void setSelectAll(boolean isSelectAll) {
        mIsSelectAll = true;
        for (int i = 0; i < getItemCount(); i++) {
            mCheckedMap.put(i, isSelectAll);
            Cursor cursor = getCursorAtAdapterPosition(i);
            int id = cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Albums.AlbumColumns._ID));
            if (isSelectAll) {
                addSelectedAlbumIds(id);
            } else {
                clearSelectedAlbumIds();
            }
        }
        if (mContext instanceof LocalAlbumListActivity) {
            ((LocalAlbumListActivity) mContext).setSelectedNumber(getmSelectedAlbumIds().size());
        }
    }

    public boolean isSelectAll() {
        if (mCheckedMap != null && getmSelectedAlbumIds() != null) {
            return (mCheckedMap.size() != 0 || getmSelectedAlbumIds().size() != 0) && (mCheckedMap.size() == getmSelectedAlbumIds().size());
        }
        return false;
    }
}
