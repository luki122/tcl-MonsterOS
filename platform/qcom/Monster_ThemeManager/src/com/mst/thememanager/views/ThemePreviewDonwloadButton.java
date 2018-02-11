package com.mst.thememanager.views;

import mst.app.dialog.ProgressDialog;
import mst.widget.FoldProgressBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.mst.thememanager.R;
import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.listener.OnThemeApplyListener;
import com.mst.thememanager.state.DonwloadOption;
import com.mst.thememanager.state.DownLoadStateManager;
import com.mst.thememanager.state.DownloadButtonNormalState;
import com.mst.thememanager.state.DownloadState;
import com.mst.thememanager.utils.Config;
import com.mst.thememanager.utils.DialogUtils;
public class ThemePreviewDonwloadButton extends DownloadButton implements OnThemeApplyListener{
	
	private DownloadState NORMAL;
	private DownloadState DOWNLOADING;
	private DownloadState PAUSE;
	private DownloadState RESUME;
	private DownloadState STOP;
	private DownloadState SUCCESS;
	private DownLoadStateManager mStateManager;
	
	public ThemePreviewDonwloadButton(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
		init();
	}

	public ThemePreviewDonwloadButton(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		init();
	}

	public ThemePreviewDonwloadButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public ThemePreviewDonwloadButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}
	
	private void init(){
		mStateManager = DownLoadStateManager.getInstance(getContext());
		NORMAL = new DownloadButtonNormalState(this);
		mStateManager.setState(NORMAL);
		mStateManager.setOption(this);
		mStateManager.handleState();
		mStateManager.setApplyThemeListener(this);
		
	}

	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		mStateManager.start();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		mStateManager.pause();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		mStateManager.stop();
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		mStateManager.resume();
	}

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		mStateManager.apply();
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		mStateManager.update();
	}

	@Override
	public void setTheme(Theme theme) {
		// TODO Auto-generated method stub
		mStateManager.setTheme(theme);
	}


	@Override
	public void showDialog(Context context,int dialogId) {
		// TODO Auto-generated method stub
		mStateManager.showDialog(getContext(),dialogId);
	}
	@Override
	public void dismissDialog() {
		// TODO Auto-generated method stub
		mStateManager.dismissDialog();
	}
	


	@Override
	public void onApply(int applyStatus) {
		// TODO Auto-generated method stub
		if(applyStatus != Config.ThemeApplyStatus.STATUS_APPLING){
			dismissDialog();
		}
		if(applyStatus == Config.ThemeApplyStatus.STATUS_FAILED){
			Toast.makeText(getContext(), getResources().getString(R.string.statu_apply_theme_failed), Toast.LENGTH_SHORT).show();
		}
		
		
		if(getContext() instanceof Activity){
			((Activity)getContext()).finish();
		}
	}
	
	
}
