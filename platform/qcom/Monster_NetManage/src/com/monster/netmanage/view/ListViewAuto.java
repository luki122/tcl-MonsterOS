package com.monster.netmanage.view;

import android.content.Context;
import android.util.AttributeSet;
import mst.widget.MstListView;

/**
 * 自适应item高度
 * @author zhaolaichao
 *
 */
public class ListViewAuto extends MstListView{

	public ListViewAuto(Context context) {
		super(context);
	}
  
	public ListViewAuto(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	public ListViewAuto(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//根据模式计算每个child的高度和宽度
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
  
}
