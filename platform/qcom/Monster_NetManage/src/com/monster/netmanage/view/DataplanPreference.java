package com.monster.netmanage.view;

import com.monster.netmanage.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import mst.preference.Preference;

/**
 * 
 * @author zhaolaichao
 *
 */
public class DataplanPreference extends Preference {

	private View mView;
	private TextView mTvTitle;
	private TextView mTvContent;
	private ImageView mImv;
	private String mTitle;
	private String mSubContent;
	
	public DataplanPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public DataplanPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DataplanPreference(Context context) {
		this(context, null);
	}

	public DataplanPreference(Context context, AttributeSet attrs, int defStyleAttr, int arg3) {
		super(context, attrs, defStyleAttr, arg3);
		setLayoutResource(R.layout.item_set);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mView = view;
		initView();
	}
	
	private void initView() {
		mTvTitle = (TextView) mView.findViewById(R.id.tv_content);
		mImv = (ImageView) mView.findViewById(R.id.imv_action);
		mTvContent = (TextView) mView.findViewById(R.id.tv_subcontent_1);
		if (null != mTvTitle) {
			mTvTitle.setText(mTitle);
		}
		if (null != mImv) {
			mImv.setImageResource(R.drawable.ic_spinner);
			mImv.setVisibility(View.VISIBLE);
		}
		if (null != mTvContent) {
			mTvContent.setText(mSubContent);
			mTvContent.setVisibility(View.VISIBLE);
		}
	}
	
	public void setItemTitle(String title) {
		mTitle = title;
		notifyChanged();
	}
	
	public void setSubContent(String subContent) {
		mSubContent = subContent;
		notifyChanged();
	}
	
	public String getSubContent() {
		return mSubContent;
	}
}
