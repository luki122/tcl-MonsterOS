package cn.tcl.music.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

import cn.tcl.music.adapter.holders.ClickableViewHolder;
import cn.tcl.music.adapter.holders.MediaViewHolder;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.util.ViewHolderBindingUtil;
import cn.tcl.music.view.image.ImageFetcher;

public class IgnoredAdapter extends AbstractMediaAdapter {

    private final static String TAG = IgnoredAdapter.class.getSimpleName();

    public IgnoredAdapter(Context context, Cursor c, ImageFetcher imageFetcher) {
        super(context, c, imageFetcher);
    }

    @Override
    public void onBindCursorToViewHolder(ClickableViewHolder viewHolder, int position) {
        LogUtil.d(TAG, "onBindCursorToViewHolder");
        MediaViewHolder mvh = (MediaViewHolder) viewHolder;
        ViewHolderBindingUtil.bindSong(mContext, mvh, mCursor, mImageFetcher);
        mvh.selectedLayout.setVisibility(View.GONE);
        mvh.mTextView_Delete.setVisibility(View.GONE);
        mvh.contextMenuImageButton.setVisibility(View.GONE);
        mvh.layout_content.getLayoutParams().width = SystemUtility.getScreenWidth();
    }
}
