package com.monster.appmanager.utils.dialog;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SingleChoicePopWindow extends AbstractChoicePopWindow {

	private SingleChoicAdapter<String> mSingleChoicAdapter;

	public SingleChoicePopWindow(Context context, View parentView, List<String> list) {
		super(context, parentView, list);

		initData();
	}

	protected void initData() {
		mSingleChoicAdapter = new SingleChoicAdapter<String>(mContext, mList);

		mListView.setAdapter(mSingleChoicAdapter);
		mListView.setOnItemClickListener(mSingleChoicAdapter);

		setListViewHeightBasedOnChildren(mListView);

	}

	public int getSelectItem() {
		return mSingleChoicAdapter.getSelectItem();
	}
	
	public void setSelectItem(int position) {
		mSingleChoicAdapter.setSelectItem(position);
	}

	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		int tmp = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
			tmp = listItem.getMeasuredHeight();
		}
		totalHeight += 10;
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}
	
	public void setListner(OnItemClickListener mListner) {
		mSingleChoicAdapter.setListner(mListner);
	}
}
