/* Copyright (C) 2016 Tcl Corporation Limited */
package com.monster.appmanager;

import android.content.Context;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author heng.zhang1
 */
public class WidgetPreference extends Preference {
    private TextView mTextView;
    private String mDetail;
    private ImageView mImageView;
    private boolean mShow = false;
    // [BUGFIX]-ADD-BEGIN BY TCTNB.DINGYI,2016/05/10,defect-2106034
    private boolean needsetcolor = false;
    private Context mContext = null;
    // [BUGFIX]-ADD-END BY TCTNB.DINGYI,2016/05/10,defect-2106034

    public WidgetPreference(Context context) {
        this(context, null);
        // [BUGFIX]-ADD-BEGIN BY TCTNB.DINGYI,2016/05/10,defect-2106034
        mContext=context;
        // [BUGFIX]-ADD-END BY TCTNB.DINGYI,2016/05/10,defect-2106034
        setWidgetLayoutResource(R.layout.pref_widget);
    }

    public WidgetPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.preferenceStyle);
    }

    public WidgetPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public WidgetPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // [BUGFIX]-ADD-BEGIN BY TCTNB.DINGYI,2016/05/10,defect-2106034
        mContext=context;
        // [BUGFIX]-ADD-END BY TCTNB.DINGYI,2016/05/10,defect-2106034
        setWidgetLayoutResource(R.layout.pref_widget);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        View widgetFrame = view.findViewById(com.android.internal.R.id.widget_frame);

        if (widgetFrame != null) {
            mTextView = (TextView) widgetFrame.findViewById(R.id.pref_tv_detail);
            if (!TextUtils.isEmpty(mDetail)) {
                mTextView.setText(mDetail);
            } else {
                mTextView.setText("");
            }
            mImageView = (ImageView) widgetFrame.findViewById(R.id.pref_image_detail);
            if (mShow) {
                mImageView.setVisibility(View.GONE);
            }
            // [BUGFIX]-ADD-BEGIN BY TCTNB.DINGYI,2016/05/10,defect-2106034
            if(needsetcolor){
                mImageView.setColorFilter(mContext.getResources().getColor(R.color.text_disable));
            }
            // [BUGFIX]-ADD-END BY TCTNB.DINGYI,2016/05/10,defect-2106034
        }
    }

    public void setDetail(String detail) {
        this.mDetail = detail;
        notifyChanged();
    }

    public void setGone(boolean visible) {
        this.mShow = visible;
        notifyChanged();
    }

    public String getDetail() {
        return mDetail;
    }

    // [BUGFIX]-ADD-BEGIN BY TCTNB.DINGYI,2016/05/10,defect-2106034
    public void setDsiabledImageColorFilter(boolean needset) {
        this.needsetcolor=needset;
        notifyChanged();
    }
    // [BUGFIX]-ADD-END BY TCTNB.DINGYI,2016/05/10,defect-2106034
}
