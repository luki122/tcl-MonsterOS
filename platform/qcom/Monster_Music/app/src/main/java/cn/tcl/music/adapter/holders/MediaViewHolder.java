package cn.tcl.music.adapter.holders;

import android.content.AsyncQueryHandler;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import cn.tcl.music.R;

public class MediaViewHolder extends ClickableViewHolder {

	public MediaViewHolder(View itemView, OnViewHolderClickListener onViewHolderClickListener) {
		super(itemView, onViewHolderClickListener);

        itemView.setId(R.id.item_recycler_parent_view);
        if(onViewHolderClickListener != null){
            itemView.setOnClickListener(this);
        }

		ViewGroup parent = (ViewGroup) itemView;

		mSongCountsAlbumTextView = (TextView) parent.findViewById(R.id.song_counts_textview);
		mTitleAlbumTextView = (TextView) parent.findViewById(R.id.title_album_text_view);
		mRecoverScanButton = (Button) parent.findViewById(R.id.recover_scan_button);
		mediaTitleTextView = (TextView) parent.findViewById(R.id.title_text_view);
		mediaSubtitleTextView = (TextView) parent.findViewById(R.id.subtitle_text_view);
		mediaSubtitleBisTextView = (TextView) parent.findViewById(R.id.subtitle_bis_text_view);
		mediaTrackNumberTextView = (TextView) parent.findViewById(R.id.track_number_text_view);
		mediaTrackDurationTextView = (TextView) parent.findViewById(R.id.track_duration_text_view);
		mediaArtworkImageView = (ImageView) parent.findViewById(R.id.artwork_image_view);
		contextMenuImageButton = (ImageButton) parent.findViewById(R.id.item_menu_image_button);
		mediaFavoriteImageView = (ImageView) parent.findViewById(R.id.favorite_tag_item_view);
		mediaLocalStorageImageView = (ImageView) parent.findViewById(R.id.local_storage_tag_item_view);
		indicatorPlaytItemView = parent.findViewById(R.id.indicator_current_item_view);
		mediaInfoTextBlock = (ViewGroup) parent.findViewById(R.id.item_info_text_block);
		mediaPlayView = (ImageView) parent.findViewById(R.id.play_view);
        mSearchHeaderTextView = (TextView) parent.findViewById(R.id.search_header_tv);
        mNoContentTextView = (TextView) parent.findViewById(R.id.no_result_tv);
//		mMediaAnimation = (AnimationDrawable) parent.getContext().getResources().getDrawable(R.anim.music_play);

        layout_content = (View) parent.findViewById(R.id.layout_content);
        mTextView_Delete = (TextView) parent.findViewById(R.id.tv_delete);
		mTextView_Head = (TextView) parent.findViewById(R.id.head_letter);
        selectedLayout = (CheckBox) parent.findViewById(R.id.selected_layout);//[BUGFIX]-Add by TCTNJ,huiyuan.wang, 2015-07-14,PR996622
		if (contextMenuImageButton != null) {
			contextMenuImageButton.setOnClickListener(this);
		}
		if (null != mediaPlayView) {
			mediaPlayView.setOnClickListener(this);
		}
	}

	private AsyncQueryHandler mAsyncQueryArtwork;
	public void setQueryArtwork(AsyncQueryHandler asyncQueryHandler)
	{
		if (mAsyncQueryArtwork != null)
			mAsyncQueryArtwork.cancelOperation(0);
		
		mAsyncQueryArtwork = asyncQueryHandler;
	}
	
	public TextView mediaTitleTextView;
	public TextView mediaSubtitleTextView;
	public TextView mediaSubtitleBisTextView;
	public TextView mediaTrackNumberTextView;
	public TextView mediaTrackDurationTextView;
	public ImageView mediaArtworkImageView;
	public ImageView mediaFavoriteImageView;
	public ImageView mediaLocalStorageImageView;
	public ImageButton contextMenuImageButton;
	public ViewGroup mediaInfoTextBlock;
    public CheckBox selectedLayout;
	public View indicatorPlaytItemView;
	public long currentItemId = -1;
	public int defaultTextColor = 0xFF333333;
	public int defaultSubTextColor = 0xFF666666;
	public ImageView mediaPlayView;
	public AnimationDrawable mMediaAnimation;
    public View layout_content;
    public TextView mTextView_Delete;
	public TextView mTextView_Head;
	public Handler mHandler = new Handler();
	public Button mRecoverScanButton;
	public TextView mTitleAlbumTextView;
	public TextView mSongCountsAlbumTextView;
    public TextView mSearchHeaderTextView;
    public TextView mNoContentTextView;

	public void setPlayingIcon(boolean isPlaying) {
		if (isPlaying) {
			mHandler.removeCallbacks(start);
			mHandler.removeCallbacks(stop);
			mHandler.postDelayed(start, 0);
		} else {
			mHandler.removeCallbacks(start);
			mHandler.removeCallbacks(stop);
			mHandler.postDelayed(stop, 0);
		}
	}

	Runnable start = new Runnable() {

		@Override
		public void run() {
			try{
				mediaPlayView.setImageDrawable(mMediaAnimation);
				mMediaAnimation.stop();
				mMediaAnimation.start();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	Runnable stop = new Runnable() {
		@Override
		public void run() {
			try{
				mMediaAnimation.stop();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
}
