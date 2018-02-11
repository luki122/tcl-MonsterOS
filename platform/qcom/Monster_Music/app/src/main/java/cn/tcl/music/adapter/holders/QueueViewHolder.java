package cn.tcl.music.adapter.holders;

import android.view.View;
import android.widget.ImageButton;

import cn.tcl.music.R;
import cn.tcl.music.view.ImageMenuButton;

public class QueueViewHolder extends MediaViewHolder {

	public QueueViewHolder(View itemView,
			ClickableViewHolder.OnViewHolderClickListener onViewHolderClickListener) {
		super(itemView, onViewHolderClickListener);
		
		moveItemImageButton = (ImageButton) itemView.findViewById(R.id.move_item_image_button);
		menuItemImageButton = (ImageMenuButton) itemView.findViewById(R.id.item_menu_image_button);
		itemDisablerView = itemView.findViewById(R.id.item_disabler_view);
		
		
	}
	
	public ImageButton moveItemImageButton;
	public ImageMenuButton menuItemImageButton;
	public View itemDisablerView;

}
