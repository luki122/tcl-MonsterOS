package cn.tcl.music.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.tcl.music.R;

public class FooterView extends LinearLayout {
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_LOADING = 2;
    public final static int STATE_FAILED = 3;
    public final static int STATE_NOMOREDATA = 4;
    public final static int STATE_OTHER = 5;
    private View mLayout;
    private View mProgressBar;
    private TextView mHintView;
    private int mState = STATE_NORMAL;

    public FooterView(Context context) {
        super(context);
        initView(context);
    }

    public FooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mLayout = LayoutInflater.from(context).inflate(R.layout.list_cell_footer, null);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        addView(mLayout,layoutParams);

        mProgressBar = mLayout.findViewById(R.id.progressbar);
        mHintView = (TextView) mLayout.findViewById(R.id.text);
    }

    /**
     * Set footer view state
     *
     * @param state
     * @see #STATE_LOADING
     * @see #STATE_NORMAL
     * @see #STATE_READY
     */
    public void setState(int state) {
        if (state == mState)
            return;

//      if (state == STATE_LOADING) {
//          mProgressBar.setVisibility(View.VISIBLE);
//          mHintView.setVisibility(View.VISIBLE);
//      } else {
//          mHintView.setVisibility(View.VISIBLE);
//          mProgressBar.setVisibility(View.GONE);
//      }

        switch (state) {
            case STATE_NORMAL:
                mHintView.setText(R.string.p2refresh_head_load_more);
                break;

            case STATE_READY:
                if (mState != STATE_READY) {
                    mHintView.setText(R.string.p2refresh_head_load_more);
                }
                break;
            case STATE_FAILED:
                mProgressBar.setVisibility(View.GONE);
                mHintView.setText(R.string.p2refresh_end_load_more_fail);
                break;
            case STATE_LOADING:
                mHintView.setText(R.string.p2refresh_doing_end_refresh);
                break;
            case STATE_NOMOREDATA:
                mHintView.setText(R.string.cannot_requestrefresh);
                mHintView.setTextSize(14);
                mHintView.setTextColor(getResources().getColor(R.color.p2refresh_hint));
                break;
            case STATE_OTHER:
                mHintView.setText(R.string.error_view_no_data);
                break;
        }
        mState = state;
    }

    public int getState() {
        return mState;
    }

    /**
     * Set footer view bottom margin.
     *
     * @param margin
     */
    public void setBottomMargin(int margin) {
        if (margin < 0) {
            return;
        }
        LayoutParams lp = (LayoutParams) mLayout.getLayoutParams();
        lp.bottomMargin = margin;
        mLayout.setLayoutParams(lp);
    }

    /**
     * Get footer view bottom margin.
     *
     * @return
     */
    public int getBottomMargin() {
        LayoutParams lp = (LayoutParams) mLayout.getLayoutParams();
        return lp.bottomMargin;
    }

    /**
     * normal status
     */
    public void normal() {
        mHintView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * loading status
     */
    public void loading() {
        mHintView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * hide footer when disable pull load more
     */
    public void hide() {
        mLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * show footer
     */
    public void show() {
        mLayout.setVisibility(View.VISIBLE);
    }

    public void showNoMoreData() {
        mHintView.setText("no more data");
    }

}
