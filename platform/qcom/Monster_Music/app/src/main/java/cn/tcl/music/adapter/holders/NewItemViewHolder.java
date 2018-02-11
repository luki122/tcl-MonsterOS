package cn.tcl.music.adapter.holders;

import android.view.View;
import android.widget.Button;

import cn.tcl.music.R;

public class NewItemViewHolder extends ClickableViewHolder {

	public NewItemViewHolder(View itemView,
			OnViewHolderClickListener onViewHolderClickListener) {
		super(itemView, onViewHolderClickListener);
		newItemBtn = (Button) itemView.findViewById(R.id.new_item_btn);
		newItemBtn.setOnClickListener(this);
	}
	
	public Button newItemBtn;

}
