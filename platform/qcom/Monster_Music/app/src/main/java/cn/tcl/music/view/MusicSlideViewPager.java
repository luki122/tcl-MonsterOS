package cn.tcl.music.view;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.model.MediaInfo;

import android.content.Context;

import cn.tcl.music.util.ToastUtil;
import mst.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

public class MusicSlideViewPager extends ViewPager {

    private int mMusicBarHeight;
    private int last_x;
    private int mDeezerType = -1;
    private boolean isMoveToPre = false;
    private int mCurrentMediaPosition = 0;

	public MusicSlideViewPager(Context context) {
		this(context, null);
	}

	public MusicSlideViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mMusicBarHeight = context.getResources().getDimensionPixelSize(R.dimen.play_bar_height);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		float y = ev.getY();
		if (y <= mMusicBarHeight) {
			return false;
		}
		return super.onInterceptTouchEvent(ev);
	}

	public void setDeezerType(int radioType){
	    mDeezerType = radioType;
	}

	public void setCurrentMediaPosition(int position){
	    mCurrentMediaPosition = position;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
	    if(CommonConstants.SRC_TYPE_DEEZERRADIO == mDeezerType){
	        switch (ev.getAction()) {
	        case MotionEvent.ACTION_DOWN:
	            last_x = (int) ev.getX();
	            break;
	        case MotionEvent.ACTION_MOVE:
	            int now_x = (int) ev.getX();
	            int delta_x = now_x - last_x;
	            if(delta_x > 0){//move to right
	                isMoveToPre = true;
	                return true;
	            }else if(delta_x < 0){
	                isMoveToPre = false;
	            }
	            last_x = now_x;
	            break;
	        case MotionEvent.ACTION_UP:
	        case MotionEvent.ACTION_CANCEL:
                if(isMoveToPre){
					ToastUtil.showToast(getContext(), R.string.can_not_go_pre_radio);
                }
	            break;
	        }
	    }
		float y = ev.getY();
		if (y <= mMusicBarHeight) {
			return false;
		}
		return super.onTouchEvent(ev);
	}
}
