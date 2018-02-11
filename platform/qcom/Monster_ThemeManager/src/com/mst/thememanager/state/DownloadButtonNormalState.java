package com.mst.thememanager.state;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.mst.thememanager.views.DownloadButton;
import com.mst.thememanager.R;
public class DownloadButtonNormalState implements DownloadState ,OnClickListener{


	private DownloadButton mButton;
	
	public DownloadButtonNormalState(DownloadButton button){
		mButton = button;
		mButton.setOnClickListener(this);
	}

	@Override
	public boolean handleDownloadState() {
		if(mButton == null){
			return false;
		}
		mButton.setText(R.string.download_state_apply);
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		mButton.apply();
	}

}
