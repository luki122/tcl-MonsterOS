package cn.tcl.music.view.xlistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.tcl.music.R;


/**
 *
 * @author markmjw
 * @date 2013-10-08
 */
public class XHeaderView extends LinearLayout {
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_REFRESHING = 2;
	public final static int STATE_DONE = 3;

	private LinearLayout mContainer;

	private ProgressBar mProgressBar;

	private TextView mHintTextView;

	private int mState = STATE_NORMAL;

	private boolean mIsFirst;
	private int topMargin;
	private int mHeight;

	public XHeaderView(Context context) {
		super(context);
		initView(context);
	}

	public XHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	@SuppressLint("InflateParams")
	private void initView(Context context) {
		// Initial set header view height 0
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
		mContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.vw_header, null);
		addView(mContainer, lp);
		setGravity(Gravity.BOTTOM);

		mHintTextView = (TextView) findViewById(R.id.header_hint_text);
		mProgressBar = (ProgressBar) findViewById(R.id.header_progressbar);

	}

	public int getState() {
		return mState;
	}

	public void setState(int state) {
		if (state == mState && mIsFirst) {
			mIsFirst = true;
			return;
		}

		if (state == STATE_REFRESHING) {
			// show progress
			mProgressBar.setVisibility(View.VISIBLE);
		} else {
			// show arrow image
			mProgressBar.setVisibility(View.INVISIBLE);
		}

		switch (state) {
		case STATE_NORMAL:
			mHintTextView.setText(R.string.p2refresh_pull_to_refresh);
			break;

		case STATE_READY:
			if (mState != STATE_READY) {
				mHintTextView.setText(R.string.p2refresh_release_refresh);
			}
			break;

		case STATE_REFRESHING:
			mHintTextView.setText(R.string.p2refresh_doing_head_refresh);
			break;

		default:
			mHintTextView.setVisibility(View.VISIBLE);
			break;
		}

		mState = state;
	}

	/**
	 * Set the header view visible height.
	 *
	 * @param height
	 */
	public void setVisibleHeight(int height) {
		if (this.mHeight == height) {
			return;
		}

		this.mHeight = height;

		if (height < 0) {
			height = 0;
		}
		LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
		lp.height = height;
		mContainer.setLayoutParams(lp);
	}

	/**
	 * Get the header view visible height.
	 *
	 * @return
	 */
	public int getVisibleHeight() {
		return mContainer.getHeight();
	}

	/**
	 * Set the header view top height.
	 *
	 * @param topMargin
	 */
	public void setMarginTop(int topMargin) {
		if (topMargin <= 0 || this.topMargin == topMargin) {
			return;
		}

		this.topMargin = topMargin;

		LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
		lp.topMargin = topMargin;
		mContainer.setLayoutParams(lp);
	}

	public int getMarginTop() {
		LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
		return lp.topMargin;
	}
}
