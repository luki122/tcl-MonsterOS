/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.setupwizard.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.tcl.setupwizard.R;

public class CircularTextView extends LinearLayout {

    TextView mSimCarrier;
    TextView mSimType;
    TextView mSimStatus;
    LinearLayout mCircularLayout;

    public CircularTextView(Context context, AttributeSet attrs) {
        super(context,attrs);
        LayoutInflater.from(context).inflate(R.layout.circular_text_view, this);
        mSimCarrier = (TextView) findViewById(R.id.carrier);
        mSimStatus = (TextView) findViewById(R.id.status);
        mCircularLayout = (LinearLayout) findViewById(R.id.circular_layout);
    }

    public void setSimCarrier(CharSequence carrier) {
        mSimCarrier.setText(carrier);
    }

    public void setSimStatus(CharSequence status) {
        mSimStatus.setText(status);
    }

    public void setSimBackgroud(Drawable drawable) {
        mCircularLayout.setBackground(drawable);
    }
}
