package cn.tcl.music.adapter.holders;

import android.view.View;
import android.widget.CheckBox;

import cn.tcl.music.R;

public class PickerViewHolder extends MediaViewHolder{

	public PickerViewHolder(View itemView,
			ClickableViewHolder.OnViewHolderClickListener onViewHolderClickListener) {
		super(itemView, onViewHolderClickListener);

		/* MODIFIED-BEGIN by beibei.yang, 2016-05-10,BUG-2120374*/
		itemPickRadioBtn = (CheckBox) itemView.findViewById(R.id.item_pick_radio_btn);
		itemPickRadioBtn.setOnClickListener(this);
	}

	public CheckBox itemPickRadioBtn;
	/* MODIFIED-END by beibei.yang,BUG-2120374*/

}
