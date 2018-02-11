package cn.tcl.music.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import cn.tcl.music.R;
import cn.tcl.music.adapter.holders.ClickableViewHolder;
import cn.tcl.music.adapter.holders.MediaViewHolder;
import cn.tcl.music.view.image.ImageFetcher;

public abstract class AbstractMediaAdapter extends RecyclerViewCursorAdapter<ClickableViewHolder> {

	boolean mShouldDisplayMenu = true;

	protected LayoutInflater mInflater;
	protected ImageFetcher mImageFetcher;

	public AbstractMediaAdapter(Context context, Cursor c,
								ImageFetcher imageFetcher) {
		this(context, c, imageFetcher, new int[] { R.id.media_content });
	}

	public AbstractMediaAdapter(Context context, Cursor c,
								ImageFetcher imageFetcher, int[] itemViewTypes) {
		super(context, c, itemViewTypes);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mImageFetcher = imageFetcher;
	}

	@Override
	public ClickableViewHolder onCreateViewHolder(ViewGroup parent,
												  int viewType, LayoutManager currentLayoutManager) {
		int layoutId = currentLayoutManager instanceof GridLayoutManager ? R.layout.case_media_item
				: R.layout.row_media_item;

		ViewGroup rowContainer = (ViewGroup) mInflater.inflate(layoutId,
				parent, false);

		MediaViewHolder mvh = new MediaViewHolder(rowContainer, this);
		mvh.layout_content.getLayoutParams().width = getScreenWidth(mContext);
		mvh.defaultTextColor = currentLayoutManager instanceof GridLayoutManager ? 0xFFFFFFFF : 0xFF333333;
		if (!mShouldDisplayMenu && mvh.contextMenuImageButton != null)
		{
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mvh.mediaTitleTextView.getLayoutParams();
			lp.addRule(RelativeLayout.START_OF, 0);
			lp.addRule(RelativeLayout.ALIGN_PARENT_END);

			lp = (RelativeLayout.LayoutParams) mvh.mediaInfoTextBlock.getLayoutParams();
			lp.addRule(RelativeLayout.START_OF, 0);
			lp.addRule(RelativeLayout.ALIGN_PARENT_END);

			mvh.contextMenuImageButton.setVisibility(View.GONE);
		}
		return mvh;
	}

	private int getScreenWidth(Context mContext) {
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.widthPixels;
	}

	public void setShouldDisplayMenu(boolean displayMenu)
	{
		mShouldDisplayMenu = displayMenu;
	}
}
