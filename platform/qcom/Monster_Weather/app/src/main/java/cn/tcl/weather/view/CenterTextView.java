/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created on 16-8-22.
 */
public class CenterTextView extends TextView {
    private StaticLayout myStaticLayout;
    private TextPaint tp;

    public CenterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initView();
    }

    private void initView() {
        tp = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        tp.setTextSize(getTextSize());
        tp.setColor(getCurrentTextColor());
        myStaticLayout = new StaticLayout(getText(), tp, getWidth(), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        myStaticLayout.draw(canvas);
    }
}
