package com.monster.appmanager.virusscan;

import com.monster.appmanager.MainActivity;
import com.monster.appmanager.R;
import com.monster.appmanager.virusscan.MyQScanListener.OnAdScanListener;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

/**
 * 广告扫描页
 * 
 * @author liuqin
 *
 */
public class AdScanningActivity extends MainActivity{
	TextView mTvAllCount;
	TextView mTvAllCountBelow;
	TextView mTvVirusAppCount;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setAdScanning(true);
		super.onCreate(savedInstanceState);
		setTitle(R.string.virus_scan);
		showBackIcon(true);
		initView();
	}

	private void initView(){
		mTvAllCount = (TextView)findViewById(R.id.all_count);
		mTvAllCountBelow = (TextView)findViewById(R.id.all_count_below);
		mTvVirusAppCount=(TextView)findViewById(R.id.virus_app_count);
		mTvAllCountBelow.setVisibility(View.INVISIBLE);
		mTvAllCount.setVisibility(View.VISIBLE);
		mTvVirusAppCount.setVisibility(View.VISIBLE);
		mTvVirusAppCount.setText(getScanMsg(0));
		findViewById(R.id.function_area).setVisibility(View.INVISIBLE);
		getOneKeyScan().setVisibility(View.INVISIBLE);
		setOnAdScanListener(mOnAdScanListener);
	}
	
	@Override
	public void onLoadEntriesCompleted() {
		super.onLoadEntriesCompleted();
		
		int count = entries.size();
		mTvAllCount.setText("/ "+count);
		getOneKeyScan().performClick();
	}
	
	private Handler mHandler = new Handler();
	
	private OnAdScanListener mOnAdScanListener = new OnAdScanListener() {
		@Override
		public void onAdScan(final int count) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mTvVirusAppCount.setText(getScanMsg(count));
				}
			});
		}
	};
	
	private SpannableString getScanMsg(int count) {
		String txt = getResources().getString(R.string.virus_count_title, count);
		SpannableString ss = new SpannableString(txt);  
		ss.setSpan(new ForegroundColorSpan(0xFFB33C3C), 0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ss;
	}
}
