package cn.tcl.music.adapter.holders;

import android.view.View;
import android.widget.ImageButton;

import cn.tcl.music.R;

public class PlayPreviousNextHolder extends ClickableViewHolder {

	public PlayPreviousNextHolder(View itemView,
			OnViewHolderClickListener onViewHolderClickListener) {
		super(itemView, onViewHolderClickListener);
		
		mPlayPrevious= (ImageButton) itemView.findViewById(R.id.track_prev_image_btn);
		mPlayNext = (ImageButton) itemView.findViewById(R.id.track_next_image_btn);
		
		mPlayPrevious.setOnClickListener(this);
		mPlayNext.setOnClickListener(this);
		
	}

	public ImageButton mPlayPrevious;
	public ImageButton mPlayNext;

}
