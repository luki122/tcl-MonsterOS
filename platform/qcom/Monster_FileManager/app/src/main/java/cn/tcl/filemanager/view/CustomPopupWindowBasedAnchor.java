/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.view;


import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import cn.tcl.filemanager.utils.DisplayUtil;
import cn.tcl.filemanager.R;


public class CustomPopupWindowBasedAnchor extends CustomPopupWindow{

    private PopupWindow mPopupWindow;
    private DisplayUtil mDisplayUtil;
    private int mWidth;
    private int mHeight;
    Resources mRes;

    public CustomPopupWindowBasedAnchor(View contentView, int width, int height,
            Activity activity) {
        super(contentView, width, height, activity);
        mRes = activity.getResources();
        mWidth = width;
        mHeight = height;
        mPopupWindow = new PopupWindow(contentView, width, height);
        Log.d("POP","CustomPopupWindowBasedAnchor(), mPopupWindow = " + mPopupWindow);
        mPopupWindow.setOutsideTouchable(false);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setBackgroundDrawable(mRes.getDrawable(R.drawable.pop_menu_bg));
        if(android.os.Build.VERSION.SDK_INT > 20) {
            mPopupWindow.setElevation(mRes.getDimension(R.dimen.floating_window_z));
        }
        mPopupWindow.update();
        mDisplayUtil = new DisplayUtil(activity);
        //Log.d("POP","CustomPopupWindowBasedAnchor() find ::::", new Exception());//PR-1001659 Nicky Ni -001 20151128
    }

    public void showForCustomedOptionsMenu(View anchor, int x, int y) {
        mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);
    }

	public void showAsDropDownBasedAnchor(View anchor, int offX, int offY) {
		if (mPopupWindow != null && !mPopupWindow.isShowing()) {
			int xOff = 0;
			int yOff = 0;
			int[] location = new int[2];
			anchor.getLocationOnScreen(location);
			int width = anchor.getWidth();
			int height = anchor.getHeight();
			int centerX = location[0] + width / 2;
			int centerY = location[1] + height / 2;
			int screenWidth = mDisplayUtil.getScreenWidth();
			int screenHeight = mDisplayUtil.getScreenHeight();
			int targetX = centerX - offX;
			int targetY = centerY - offY;
			int dx = screenWidth - targetX;
			int dy = screenHeight - targetY;
			if (mWidth > 0) {
				if (dx >= mWidth) {
					xOff = width / 2 - offX;
				} else {
					xOff = width / 2 - mWidth + (offX * 2);
				}
			} else {
				xOff = width / 2 - offX;
			}
			if (mHeight > 0) {
				if (dy >= mHeight) {
					yOff = -height / 2 - offY;
				} else {
					yOff = -height / 2 + (offY * 2);
				}
			} else {
				yOff = -height / 2 - offY;
			}
			mPopupWindow.showAsDropDown(anchor, xOff, yOff);
		}
	}

	public void showAtLocationBasedAnchor(View anchor, int xOff, int yOff) {
		if (mPopupWindow != null && !mPopupWindow.isShowing()) {
			int xOFF = 0;
			int yOFF = 0;
			int[] location = new int[2];
			anchor.getLocationOnScreen(location);
			int width = anchor.getWidth();
			int height = anchor.getHeight();
			int centerX = location[0] + width / 2;
			int centerY = location[1] + height / 2;
			int targetX = centerX - xOff;
			int targetY = centerY - yOff;
			int screenWidth = mDisplayUtil.getScreenWidth();
			int screenHeight = mDisplayUtil.getScreenHeight();
			int dx = screenWidth - targetX;
			int dy = screenHeight - targetY;
			if (mWidth > 0) {
				if (dx >= mWidth) {
					xOFF = targetX;
				} else {
					xOFF = targetX - mWidth + 2 * xOff;
				}
			} else {
				xOFF = targetX;
			}
			if (mHeight > 0) {
				if (dy >= mHeight) {
					yOFF = targetY;
				} else {
					yOFF = targetY - mHeight + 2 * yOff;
				}
			} else {
				yOFF = targetY;
			}
			mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xOFF, yOFF);
		}
	}
    //[BUG-FIX]-BEGIN by NJTS Junyong.Sun 2016/01/22 PR-1445808
    public void update(Activity activity){
        if(mPopupWindow != null){
            Log.d("POP","this is update");
            mDisplayUtil = new DisplayUtil(activity);
            mPopupWindow.update();
        }
    }

    public void update(View view,Activity activity){
        if(mPopupWindow != null){
            Log.d("POP","this is update");
            mPopupWindow.setContentView(view);
            update(activity);
        }
    }
    //[BUG-FIX]-END by NJTS Junyong.Sun 2016/01/22 PR-1445808
    @Override
    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
        	Log.d("POP","this is dismiss");
            mPopupWindow.dismiss();
        }
    }

    @Override
    public boolean isShowing() {
    	Log.d("POP","isShowing(), mPopupWindow = " + mPopupWindow);
        if (mPopupWindow != null) {
            return mPopupWindow.isShowing();
        }

        return false;
    }
}
