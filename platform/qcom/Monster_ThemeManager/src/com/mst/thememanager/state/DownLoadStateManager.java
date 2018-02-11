package com.mst.thememanager.state;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.mst.thememanager.ThemeManager;
import com.mst.thememanager.ThemeManagerApplication;
import com.mst.thememanager.ThemeManagerImpl;
import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.listener.OnThemeApplyListener;
import com.mst.thememanager.utils.CommonUtil;
import com.mst.thememanager.utils.DialogUtils;
import com.mst.thememanager.R;
/**
 * Manage Theme download state,See{@link com.mst.thememanager.state.DownloadState}
 * @author alexluo
 *
 */
public class DownLoadStateManager implements DonwloadOption{

	private static DownLoadStateManager mInstance;
	
	private DownloadState mState;
	private ThemeManager mThemeManager;
	private WeakReference<ThemeManagerApplication> mContext;
	private Theme mTheme;
	private OnThemeApplyListener mThemeApplyListener;
	private DialogUtils mDialogUtils;
	private DonwloadOption mOptionButton;
	private DownLoadStateManager(Context context){
		mContext = new WeakReference<ThemeManagerApplication>((ThemeManagerApplication)context.getApplicationContext());
		mThemeManager = mContext.get().getThemeManager();
		mDialogUtils = new DialogUtils();
	}
	public static DownLoadStateManager getInstance(Context context){
		synchronized (DownloadButtonNormalState.class) {
			if(mInstance == null){
				mInstance = new DownLoadStateManager(context);
			}
			return mInstance;
		}
	}
	
	public void setOption(DonwloadOption option){
		mOptionButton = option;
	}
	
	
	public synchronized void setState(DownloadState state){
		mState = state;
	}
	
	public synchronized DownloadState getState(){
		return mState;
	}
	
	public void handleState(){
		if(mState != null){
			mState.handleDownloadState();
		}
	}



	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void apply() {
		// TODO Auto-generated method stub
		if(mThemeManager.themeApplied(mTheme)){
			final Context context = mContext.get();
			if(context != null){
				Toast.makeText(context, context.getString(R.string.msg_select_theme_applied), Toast.LENGTH_SHORT).show();
			}
			return;
		}
		mOptionButton.showDialog(null,DialogUtils.DIALOG_ID_APPLY_PROGRESS);
		mThemeManager.applyTheme(mTheme, mContext.get(),mThemeApplyListener);
	}



	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void setTheme(Theme theme) {
		// TODO Auto-generated method stub
		mTheme = theme;
	}
	
	public void setApplyThemeListener(OnThemeApplyListener listener) {
		// TODO Auto-generated method stub
		mThemeApplyListener = listener;
	}
	
	
	@Override
	public void showDialog(Context context,int dialogId) {
		// TODO Auto-generated method stub
		mDialogUtils.showDialog(context, dialogId);
	}
	
	
	@Override
	public void dismissDialog() {
		// TODO Auto-generated method stub
		mDialogUtils.dismissDialog();
		Context context = mContext.get();
		if(context != null){
			mContext.get().startActivity(CommonUtil.getHomeIntent());
		}
		
		
	}
	
	
	
	
}
