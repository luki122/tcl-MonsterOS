package com.mst.thememanager.utils;

import mst.app.dialog.ProgressDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;

import com.mst.thememanager.R;
public class DialogUtils {
	public static final int DIALOG_ID_APPLY_PROGRESS = 0;
	private Dialog mDialog;
	
	public synchronized void showDialog(Context context,int dialogId){
		dismissDialog();
		switch (dialogId) {
		case DIALOG_ID_APPLY_PROGRESS:
			 ProgressDialog applyThemeProgressDialog = new ProgressDialog(context);
			 applyThemeProgressDialog.setMessage(context.getResources().getString(R.string.msg_apply_theme));
			 mDialog = applyThemeProgressDialog;
			break;

		default:
			break;
		}
		Log.d("dialog", "show-->");
		if(mDialog != null){
			if(mDialog.isShowing()){
				return;
			}
			mDialog.show();
			mDialog.setCancelable(false);
			mDialog.setCanceledOnTouchOutside(false);
		}
	}
	
	public synchronized void dismissDialog(){
		if(mDialog != null){
			if(mDialog.isShowing()){
				mDialog.dismiss();
			}
		}
		mDialog = null;
	}
	
}
