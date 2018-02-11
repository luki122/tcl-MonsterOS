package com.monster.interception.adapter;

import java.util.HashSet;
import java.util.Set;

import com.monster.interception.R;
import com.monster.interception.activity.slideDeleteListener;
import com.monster.interception.util.YuloreUtil;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.AbsListView.RecyclerListener;
import android.widget.TextView;
import mst.widget.SliderView;
import com.monster.interception.util.InterceptionUtils;

public class BlackAdapter extends InterceptionAdapterBase {

	public BlackAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {

		TextView name = (TextView) arg0.findViewById(R.id.name);
		TextView title = (TextView) arg0.findViewById(R.id.title);
		String names = arg2.getString(arg2.getColumnIndex("black_name"));
		String lable = arg2.getString(arg2.getColumnIndex("lable"));
		String number = arg2.getString(arg2.getColumnIndex("number"));
		if (names == null || "".equals(names)) {
			name.setText(number);
			if (lable == null || "".equals(lable)) {
				String s = YuloreUtil.getArea(number);
				if (s == null || "".equals(s)) {
					title.setText(arg1.getResources().getString(R.string.mars));
				} else {
					title.setText(s);
				}
			} else {
				title.setText(lable);
			}
		} else {
			name.setText(names);
			title.setText(number);
		}

		super.bindView(arg0, arg1, arg2);
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		SliderView view = (SliderView)mInflater.inflate(R.layout.black_name_list_item, null);
		view.addTextButton(InterceptionUtils.SLIDER_BTN_POSITION_DELETE, arg0.getString(R.string.del));
		return view;
	}

}
