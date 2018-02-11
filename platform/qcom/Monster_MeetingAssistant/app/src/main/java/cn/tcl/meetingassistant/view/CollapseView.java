/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.ScreenSizeUtil;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-19.
 * Circle image view that can display a circle image
 */
@SuppressLint("NewApi")
public class CollapseView extends LinearLayout {

    private final String TAG = CollapseView.class.getSimpleName();

    private Context mContext;
    private long mDuration = 200;
    private ImageView mArrowImageView;
    private int mContentMinHeight = 300;


    private boolean isExpandable = false;

    // content
    private RelativeLayout mContentRelativeLayout;

    public CollapseView(Context context) {
        this(context, null);
    }

    public CollapseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mContext=context;
        // get layout
        LayoutInflater.from(mContext).inflate(R.layout.view_collapse_layout, this);
        mContentRelativeLayout=(RelativeLayout)findViewById(R.id.contentRelativeLayout);
        mArrowImageView =(ImageView)findViewById(R.id.arrowImageView);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateArrow();
            }
        });

        collapse(mContentRelativeLayout);
    }

    /**
     * judge the content expandable
     */
    private void judgeExpanable(){
        int widthMeasureSpec=MeasureSpec.makeMeasureSpec(ScreenSizeUtil.getScreenWidth(mContext), MeasureSpec.EXACTLY);
        int heightMeasureSpec=MeasureSpec.makeMeasureSpec((1<<30)-1, MeasureSpec.AT_MOST);
        mContentRelativeLayout.measure(widthMeasureSpec, heightMeasureSpec);
        int measuredHeight = mContentRelativeLayout.getMeasuredHeight();
        if(mContentMinHeight <= measuredHeight){
            isExpandable = false;
        }else {
            isExpandable = true;
        }
    }

    /**
     * set the content min height
     * @param view the main text view
     * @param lines the lines you want to display
     */
    public void initContentMinHeight(TextView view,int lines){
        if(lines > 0){
            int widthMeasureSpec = MeasureSpec.makeMeasureSpec(ScreenSizeUtil.getScreenWidth(mContext), MeasureSpec.EXACTLY);
            int heightMeasureSpec = MeasureSpec.makeMeasureSpec(( 1 << 30 ) - 1, MeasureSpec.AT_MOST);
            mContentRelativeLayout.measure(widthMeasureSpec, heightMeasureSpec);
            int measuredHeight = mContentRelativeLayout.getMeasuredHeight();
            Rect rect = new Rect();
            view.getLineBounds(0,rect);
            Paint.FontMetrics textFontMetrics = view.getPaint().getFontMetrics();
            float textHeight = textFontMetrics.descent - textFontMetrics.top;
            float expectHeight = ( lines - 1) * view.getLineHeight() + textHeight;
            mContentMinHeight = expectHeight < measuredHeight ? (int) expectHeight : measuredHeight;
        }
    }

    // setContent
    // TODO this maybe arise a bug,if the content is not the first child of content layout
    public void setContent(int resID,int resTextID,int lines){
        View view=LayoutInflater.from(mContext).inflate(resID,null);
        RelativeLayout.LayoutParams layoutParams=
                new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);
        mContentRelativeLayout.addView(view);

        TextView textView = (TextView) mContentRelativeLayout.findViewById(resTextID);
        initContentMinHeight(textView,lines);
    }


    /**
     *  display or dismiss the all content,and rotate arrow
     */
    public void rotateArrow() {
        int degree = 0;
        // rotate arrow
        if (mArrowImageView.getTag() == null || mArrowImageView.getTag().equals(true)) {
            mArrowImageView.setTag(false);
            degree = -180;
            expand(mContentRelativeLayout);
        } else {
            degree = 0;
            mArrowImageView.setTag(true);
            collapse(mContentRelativeLayout);
        }
        mArrowImageView.animate().setDuration(mDuration).rotation(degree);
    }

    private void expand(final View view) {
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(ScreenSizeUtil.getScreenWidth(mContext), MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec((1<<30)-1, MeasureSpec.AT_MOST);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        final int measuredHeight = view.getMeasuredHeight();
        MeetingLog.i(TAG,"expand measuredHeight is " + measuredHeight);
        //view.setVisibility(View.VISIBLE);
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    view.getLayoutParams().height = measuredHeight;
                }else{
                    view.getLayoutParams().height = (int) (mContentMinHeight + (measuredHeight - mContentMinHeight) * interpolatedTime);
                }
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setDuration(mDuration);
        view.startAnimation(animation);
    }

    private void collapse(final View view) {
        final int measuredHeight = view.getMeasuredHeight();
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.getLayoutParams().height = mContentMinHeight;
                } else {
                    view.getLayoutParams().height = (int) (measuredHeight - (measuredHeight - mContentMinHeight)*interpolatedTime);
                }
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setDuration(mDuration);
        view.startAnimation(animation);
    }
}
