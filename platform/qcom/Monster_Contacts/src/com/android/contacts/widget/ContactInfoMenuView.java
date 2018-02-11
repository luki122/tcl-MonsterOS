package com.android.contacts.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.contacts.common.model.dataitem.DataKind;
import com.android.contacts.R;

public class ContactInfoMenuView extends LinearLayout implements OnClickListener {
	private LayoutInflater mInflater;
	OnMenuClickListener mListener;
	TextView title;
	public DataKind kind;

	public ContactInfoMenuView(Context context) {

		super(context, null);
	}

	public ContactInfoMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		mInflater = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		title = (TextView) findViewById(R.id.title);
	}

	public void setValue(DataKind kind, String title) {
		this.kind = kind;
		String titleStr = 
				getContext().getString(R.string.description_plus_button) + 
				getContext().getString(kind.titleRes);
		this.title.setText(titleStr);
	}

	public interface OnMenuClickListener {
		public void onMenuClick(DataKind kind, View v);
	}
	
	public void setOnMenuClickListener(OnMenuClickListener mListener) {
		this.mListener = mListener;
	}

	@Override
	public void onClick(View v) {
		mListener.onMenuClick(kind, v);
	}

}
