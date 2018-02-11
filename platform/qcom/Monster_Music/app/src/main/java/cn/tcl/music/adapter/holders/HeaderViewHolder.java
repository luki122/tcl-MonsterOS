package cn.tcl.music.adapter.holders;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tcl.music.R;

public class HeaderViewHolder extends ClickableViewHolder {

	public HeaderViewHolder(View itemView, OnViewHolderClickListener onViewHolderClickListener) {
		super(itemView, onViewHolderClickListener);

		ViewGroup parent = (ViewGroup) itemView;
        mLlRadioMusicInformation = (LinearLayout) parent.findViewById(R.id.ll_radio_music_information);
        mRlNormalMusicInformation = (RelativeLayout) parent.findViewById(R.id.rl_normal_music_information);
		titleTextView = (TextView) parent.findViewById(R.id.title_text_view);
		subtitleTextView = (TextView) parent.findViewById(R.id.subtitle_text_view);
		subtitleTextView.setOnClickListener(this);
		addtoList = (ImageView) parent.findViewById(R.id.add_songs_to_playlist);
		addtoList.setOnClickListener(this);
		playAllBtn = (ImageButton) parent.findViewById(R.id.play_all_btn);
		playAllBtn.setOnClickListener(this);
		titleTextView.setOnClickListener(this);
	}
    public LinearLayout mLlRadioMusicInformation;
    public RelativeLayout mRlNormalMusicInformation;
	public TextView titleTextView;
	public TextView batch_operate;
	public TextView subtitleTextView;
	//public TextView subtitleBisTextView;
	//public ImageMenuButton itemBtn;
	public ImageView addtoList;
	public ImageButton playAllBtn;
}
