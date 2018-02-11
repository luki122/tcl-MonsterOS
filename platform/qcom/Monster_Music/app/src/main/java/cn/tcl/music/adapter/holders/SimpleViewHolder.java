package cn.tcl.music.adapter.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.tcl.music.R;

public class SimpleViewHolder extends ClickableViewHolder {

    public SimpleViewHolder(View itemView, OnViewHolderClickListener onViewHolderClickListener) {
        super(itemView, onViewHolderClickListener);
        parent = itemView;
        mediaTitleTextView = (TextView) parent.findViewById(R.id.title_text_view);
//        mediaAllDivider = (TextView) parent.findViewById(R.id.all_playlsit_divider);
        parent.setOnClickListener(this);
        mProgressLoad = (LinearLayout) parent.findViewById(R.id.progress_container);
        mArtworkImageView = (ImageView) parent.findViewById(R.id.create_new_playlist_image_view);
        mNumTextView = (TextView) parent.findViewById(R.id.number_textview);
    }

    public TextView mediaTitleTextView;
    public TextView mediaAllDivider;
    public LinearLayout mProgressLoad;
    public View parent;
    public ImageView mArtworkImageView;
    public TextView mNumTextView;
}
