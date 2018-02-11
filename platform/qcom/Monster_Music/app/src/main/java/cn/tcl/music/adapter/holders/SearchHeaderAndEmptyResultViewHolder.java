package cn.tcl.music.adapter.holders;

import android.view.View;
import android.widget.TextView;

import cn.tcl.music.R;

public class SearchHeaderAndEmptyResultViewHolder extends SimpleViewHolder {

	public SearchHeaderAndEmptyResultViewHolder(View itemView,
			OnViewHolderClickListener onViewHolderClickListener) {
		super(itemView, onViewHolderClickListener);
		emptyResultTextView = (TextView) itemView.findViewById(R.id.empty_search_text_view);
		dividerHeader = itemView.findViewById(R.id.header_divider);
	}

	public TextView emptyResultTextView;
	public View dividerHeader;
}
