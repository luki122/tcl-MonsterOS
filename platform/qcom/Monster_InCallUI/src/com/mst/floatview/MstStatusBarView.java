package com.mst.floatview;

import android.content.Context;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.incallui.Call;
import com.android.incallui.CallList;
import com.android.incallui.CallTimer;
import com.android.incallui.InCallApp;
import com.android.incallui.InCallPresenter;
import com.android.incallui.InCallPresenter.InCallState;
import com.android.incallui.InCallPresenter.InCallStateListener;
import com.android.incallui.R;
import com.android.incallui.CallCardPresenterMst.CallCardUi;

public class MstStatusBarView extends LinearLayout implements
		View.OnClickListener, InCallStateListener {

	private static final String LOG_TAG = "MstStatusBarView";

	private static final boolean DBG = true;
	
	/**
	 * 记录小悬浮窗的宽度
	 */
	public static int viewWidth;

	/**
	 * 记录小悬浮窗的高度
	 */
	public static int viewHeight;

	private TextView mText, mTime;
	
    private CallTimer mCallTimer;
    
    private CallList mCallList;

	public MstStatusBarView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.statusbar_layout, this);
		View view = findViewById(R.id.parent_container);
		view.setOnClickListener(this);
		viewWidth = view.getLayoutParams().width;
		viewHeight = view.getLayoutParams().height;
		mText = (TextView) findViewById(R.id.text);
		mTime = (TextView) findViewById(R.id.time);
		this.setOnKeyListener(null);
		mCallList = CallList.getInstance();
        mCallTimer = new CallTimer(new Runnable() {
            @Override
            public void run() {
                updateCallTime();
            }
        });

	}

	public void onClick(View view) {
		int id = view.getId();
		log("onClick View ");

		switch (id) {
		case R.id.parent_container:
			InCallApp.getInstance().displayCallScreen();
			break;
		}
		FloatWindowManager.removeStatusBarWindow(InCallApp.getInstance());
	}
	
    private void updateCallTime() {
    	Call call = mCallList.getActiveCall();
       if (call == null) {
            mCallTimer.cancel();
    	    mTime.setVisibility(View.GONE);
        } else {
//            final long callStart = call.getConnectTimeMillis();
//            final long duration = System.currentTimeMillis() - callStart;
            final long duration = call.getDuration();
            String callTimeElapsed = DateUtils.formatElapsedTime(duration / 1000);
            mTime.setVisibility(View.VISIBLE);
            mTime.setText(callTimeElapsed);
        }
    }
    
    @Override  
    protected void onAttachedToWindow() {  
        super.onAttachedToWindow();  
        InCallPresenter.getInstance().addListener(this);
        startCallTimerOrNot();
    }
    
    @Override  
    protected void onDetachedFromWindow() {  
        super.onDetachedFromWindow();  
        InCallPresenter.getInstance().removeListener(this);
        mCallTimer.cancel();
    }

	private void log(String msg) {
		Log.d(LOG_TAG, msg);
	}

	@Override
	public void onStateChange(InCallState oldState, InCallState newState,
			CallList callList) {
		// TODO Auto-generated method stub
	      // Start/stop timers.
		startCallTimerOrNot();		
	}
	
	private void startCallTimerOrNot() {
    	Call call = mCallList.getActiveCall();
        if (call != null) {
            mCallTimer.start(1000);
        } else {
            mCallTimer.cancel();
    	    mTime.setVisibility(View.GONE);
        }
	}

}