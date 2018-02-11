package mst.view.menu;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class BottomMenuListView extends ListView implements MstMenuView{

	public BottomMenuListView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public BottomMenuListView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public BottomMenuListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public BottomMenuListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(MstMenuBuilder menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getWindowAnimations() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	
}
