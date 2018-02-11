package cn.tcl.music.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import cn.tcl.music.R;
import cn.tcl.music.activities.IgnoreActivity;
import cn.tcl.music.adapter.holders.ClickableViewHolder;
import cn.tcl.music.adapter.holders.MediaViewHolder;
import cn.tcl.music.util.DialogMenuUtils;
import cn.tcl.music.util.ViewHolderBindingUtil;
import cn.tcl.music.view.ColoredRelativeLayout;
import cn.tcl.music.view.image.ImageFetcher;


public class LocalFolderAdapter extends AbstractMediaAdapter implements ColoredRelativeLayout.IonSlidingButtonListener {
    private static final String TAG = LocalFolderAdapter.class.getSimpleName();
    private int songNum = -1;
    private Boolean mIsIgnored;
   private IonSlidingViewClickListener mIDeleteBtnClickListener;

    public interface IonSlidingViewClickListener {
        void onItemClick(View view, int position);

        void onDeleteBtnClick(View view, int position);
    }

    public LocalFolderAdapter(Context context, Cursor c, ImageFetcher imageFetcher, Boolean isIgnored) {
        super(context, c, imageFetcher);
        setHasStableIds(true);
        mIDeleteBtnClickListener = (IonSlidingViewClickListener) context;
        mIsIgnored = isIgnored;
    }

    @Override
    public void onBindCursorToViewHolder(ClickableViewHolder clickableViewHolder, int position) {
        android.util.Log.v("FoldersAdapter", "FoldersAdapter--->onBindCursorToViewHolder");
        if (menuIsOpen()) {
            closeMenu();
        }
        MediaViewHolder viewHolder = (MediaViewHolder) clickableViewHolder;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean mShowHiddenTracks = sharedPrefs.getBoolean("show_hidden_tracks", false);
        ViewHolderBindingUtil.bindFolder(mContext, viewHolder, mCursor, mImageFetcher);
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType, RecyclerView.LayoutManager currentLayoutManager) {
        int layoutId = currentLayoutManager instanceof GridLayoutManager ? R.layout.case_media_item
                : R.layout.row_media_folder_item;
        android.util.Log.v("ysh", "layoutId: " + layoutId + " row_media_item: " + R.layout.row_media_item);
        ViewGroup rowContainer = (ViewGroup) mInflater.inflate(layoutId, parent, false);
        if ((Activity)mContext instanceof IgnoreActivity) {
            ((ColoredRelativeLayout) rowContainer).setSlidingButtonListener(null);
        } else {
            ((ColoredRelativeLayout) rowContainer).setSlidingButtonListener(LocalFolderAdapter.this);
        }
        final MediaViewHolder mvh = new MediaViewHolder(rowContainer, this);
        mvh.defaultTextColor = currentLayoutManager instanceof GridLayoutManager ? 0xFFFFFFFF : 0xFFC3CDD3;
        mvh.mTextView_Delete.setText(mContext.getText(R.string.ignore));
        mvh.layout_content.getLayoutParams().width = getScreenWidth(mContext);
        mvh.mTextView_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                            case mst.app.dialog.AlertDialog.BUTTON_POSITIVE:
                                int n = mvh.getLayoutPosition();
                                mIDeleteBtnClickListener.onDeleteBtnClick(v,n);
                                break;
                            case mst.app.dialog.AlertDialog.BUTTON_NEGATIVE:
                                break;
                        }
                        dialog.dismiss();
                    }
                };
                DialogMenuUtils.displayIgnoreConfirmDialog(mContext,mContext.getString(R.string.dialogtitle_ignore_scan),mContext.getString(R.string.dialogmessage_ignore_scan),onClick);
            }
        });
        mvh.layout_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuIsOpen()){
                    closeMenu();
                    return;
                } else {
                    int position = mvh.getLayoutPosition();
                    mIDeleteBtnClickListener.onItemClick(v,position);
                }
            }
        });
        if (!mShouldDisplayMenu && mvh.contextMenuImageButton != null) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mvh.mediaTitleTextView.getLayoutParams();
            lp.addRule(RelativeLayout.START_OF, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_END);

            lp = (RelativeLayout.LayoutParams) mvh.mediaSubtitleTextView.getLayoutParams();
            lp.addRule(RelativeLayout.START_OF, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_END);

            mvh.contextMenuImageButton.setVisibility(View.GONE);
        }
        if (mIsIgnored) {
            mvh.mRecoverScanButton.setVisibility(View.VISIBLE);
        } else {
            mvh.mRecoverScanButton.setVisibility(View.GONE);
        }
        return mvh;
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
        mColoredRelativeLayout.closeMenu();
        mColoredRelativeLayout = null;
    }

}
