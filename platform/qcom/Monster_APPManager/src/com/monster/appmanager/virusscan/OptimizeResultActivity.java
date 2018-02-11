package com.monster.appmanager.virusscan;

import com.monster.appmanager.FullActivityBase;
import com.monster.appmanager.R;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class OptimizeResultActivity extends FullActivityBase implements OnClickListener{
	public static final String TYPE = "type";
	public static final int TYPE_OPTIMIZE_COMPLETE = 1;
	public static final int TYPE_SEARCH_COMPLETE = 2;
	private Button btn;
	private TextView tvMsg;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.optimize_result);
		initView();
		updateMsg();
	}
	
	private void initView() {
		btn = (Button)findViewById(R.id.btn);
		tvMsg = (TextView)findViewById(R.id.msg);
		btn.setOnClickListener(this);
		btn.setText(R.string.optimize_btn_go_home);
	}
	
	private void updateMsg() {
		int type = getIntent().getIntExtra(TYPE, 0);
		switch (type) {
		case TYPE_OPTIMIZE_COMPLETE:
			tvMsg.setText(R.string.optimize_complete);
			delayToFinish();
			break;
		case TYPE_SEARCH_COMPLETE:
			tvMsg.setText(R.string.optimize_search_complete);
			delayToFinish();
			break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		finish();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent arg0) {
		if(arg0.getAction() != MotionEvent.ACTION_UP) {
			mHandler.removeCallbacks(mFinishRunnalbe);
		} else {
			delayToFinish();
		}
		return super.dispatchTouchEvent(arg0);
	}
	
	private Handler mHandler = new Handler();
	
	private void delayToFinish() {
		mHandler.postDelayed(mFinishRunnalbe, 2000);
	}
	
	private Runnable mFinishRunnalbe = new Runnable() {
		@Override
		public void run() {
			if(!isFinishing() && !isDestroyed()) {
				finish();
			}
		}
	};
}
