/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class ExpandedGridView extends GridView {
	public ExpandedGridView(Context context) {
        super(context);
    }

	public ExpandedGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// If UNSPECIFIED is passed to GridView, it will show only one row.
		// Here GridView is put in a ScrollView, so pass it a very big size with
		// AT_MOST to show all the rows.
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(65536,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
