package cn.tcl.music.adapter.holders;

import android.view.View;
import android.widget.TextView;

import cn.tcl.music.R;

public class SortAllSongsHeaderViewHolder extends ClickableViewHolder {
	
	@Override
	public void onClick(View v) {
		
		sortAllTextView.setSelected(false);
		sortLocalTextView.setSelected(false);
		sortOnlineTextView.setSelected(false);
		v.setSelected(true);
		super.onClick(v);
	}

	public SortAllSongsHeaderViewHolder(View itemView,
			OnViewHolderClickListener onViewHolderClickListener) {
		super(itemView, onViewHolderClickListener);
		
		sortAllTextView = (TextView) itemView.findViewById(R.id.sort_all);
		sortLocalTextView = (TextView) itemView.findViewById(R.id.sort_local);
		sortOnlineTextView = (TextView) itemView.findViewById(R.id.sort_online);
		
		sortAllTextView.setOnClickListener(this);
		sortLocalTextView.setOnClickListener(this);
		sortOnlineTextView.setOnClickListener(this);
		
		sortAllTextView.setSelected(true);
	}
	
	public TextView sortAllTextView;
	public TextView sortLocalTextView;
	public TextView sortOnlineTextView;

}
