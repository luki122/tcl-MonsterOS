package com.monster.netmanage.view;

import com.monster.netmanage.R;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import mst.preference.Preference;

/**
 * 
 * @author zhaolaichao
 *
 */
public class WarnDataPreference extends Preference implements OnSeekBarChangeListener {
	private static final int MSG_BIND_PREFERENCES = 0;
	private View mView;
	private TextView mTvWarnValue;
	private SeekBar mSBar;

	private String mWarnValue;
	private ISeekBarChangeListener mBarChangeListener;
	private int mProgress;
	private int mMaxProgress;

	public WarnDataPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public WarnDataPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WarnDataPreference(Context context) {
		this(context, null);
	}

	public WarnDataPreference(Context arg0, AttributeSet arg1, int arg2, int arg3) {
		super(arg0, arg1, arg2, arg3);
		setLayoutResource(R.layout.lay_warn_data);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MSG_BIND_PREFERENCES:
				if (null != mTvWarnValue) {
					mTvWarnValue.setText(mWarnValue);
				}
				break;
			}
		}
	};

	@Override
	protected View onCreateView(ViewGroup parent) {
		mView = super.onCreateView(parent);
		initView();
		return mView;
	}

	@Override
	protected void onBindView(View view) {
		 super.onBindView(view);
	}

	private void initView() {
		mTvWarnValue = (TextView) mView.findViewById(R.id.tv_warn_rate);
		mSBar = (SeekBar) mView.findViewById(R.id.sbar_warn);
		if (null != mTvWarnValue) {
			mTvWarnValue.setText(mWarnValue);
		}
		if (null != mSBar) {
			Log.v(">>", "mMaxProgress>>" + mMaxProgress + "--mProgress>>" + mProgress);
			mSBar.setMax(mMaxProgress);
			mSBar.setProgress(mProgress);
			mSBar.setOnSeekBarChangeListener(this);
		}
	}

	public void setWarnValue(String warnValue) {
		mWarnValue = warnValue;
		callChangeListener(mWarnValue);
		postBindPreferences();
	}

	public String getWarnValue() {
		return mWarnValue;
	}

	public void setProgress(int progress) {
		mProgress = progress;
		notifyChanged();
	}

	public void setMaxProgress(int maxProgress) {
		mMaxProgress = maxProgress;
		notifyChanged();
	}

	public void setSeekBarChangeListener(ISeekBarChangeListener listener) {
		mBarChangeListener = listener;
	}

	public interface ISeekBarChangeListener {
		void onStopTrackingTouch(SeekBar seekBar);

		void onStartTrackingTouch(SeekBar seekBar);

		void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);
	}

	public void setCallChangeListener(int progress) {
		if (mProgress != progress) {
			callChangeListener(progress);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		mProgress = progress;
		if (callChangeListener(progress)) {
			mBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mBarChangeListener.onStartTrackingTouch(seekBar);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mBarChangeListener.onStopTrackingTouch(seekBar);
	}

	public void postBindPreferences() {
		if (mHandler.hasMessages(MSG_BIND_PREFERENCES)) {
			return;
		}
		mHandler.obtainMessage(MSG_BIND_PREFERENCES).sendToTarget();
	}

}
