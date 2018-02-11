/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.filemanager.R;

public class PieChartView extends View {

    private ArrayList<Integer> colors=null;

    private List<PieMember> data = null;

    private int centerX, centerY = 0;// Center position
    private float radius = 0;// The radius of the pie chart
    private float hollowRadius = 0;// Middle circle radius

    private int lableHeight = 0;//lable height

    private Paint bgPaint = null;
    private Paint arcPaint = null;

    public PieChartView(Context context) {
        super(context);
        this.init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }

    private void init() {
        this.bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        this.bgPaint.setColor(getResources().getColor(android.R.color.transparent));
        this.bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        this.arcPaint = new Paint();
        this.arcPaint.setAntiAlias(true);
        this.arcPaint.setStyle(Paint.Style.FILL_AND_STROKE);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(getResources().getColor(android.R.color.transparent));// Set background transparency


        if (radius == 0) {
            if ((getHeight() - lableHeight) < getWidth()) {
                radius = (getHeight() - lableHeight) / 2;
            } else {
                radius = getWidth() / 2;
            }
        } else if ((radius * 2) > getWidth()) {
            radius = getWidth() / 2;
        } else if ((radius * 2 + lableHeight) > getHeight()) {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = (int) (radius * 2d + lableHeight);
            setLayoutParams(params);
        }
        if (hollowRadius == 0) {
            hollowRadius = (int) (radius * 0.9091f);//draw 10dp ring 0.9091=100/110
        }
        if (this.centerX == 0) {
            this.centerX = getWidth() / 2;
        }
        if (this.centerY == 0) {
            this.centerY = (getHeight() - lableHeight) / 2;
        }

        // Draw the circle below
        this.bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(centerX, centerY, radius, bgPaint);
        // Draw percentage arc
        this.drawArc(canvas);
        // Draw the circle above
        this.bgPaint.setColor(getResources().getColor(R.color.white));
        this.bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(centerX, centerY, hollowRadius, bgPaint);

    }
    /**
     * Draw the curve, if no data is stored, the entire ring color painted gray
     */
    public void drawArc(Canvas canvas) {
        RectF rect = new RectF();
        rect.left = centerX - radius;
        rect.top = centerY - radius;
        rect.right = centerX + radius;
        rect.bottom = centerY + radius;

        if (this.data == null || this.data.isEmpty()) {
            arcPaint.setColor(getResources().getColor(R.color.textGrayColor));
            canvas.drawArc(rect, // The size of a rectangular area used in arcs.
                    0,  // Start angle
                    360,//sweep angle
                    true, // Whether to use the center
                    arcPaint);
            return;
        }
        long total = 0;
        for (PieMember member : data) {
            total += member.getNumber();
        }
        float angle = 0f;
        for (int pointer = 0; pointer < data.size(); pointer++) {
            PieMember member = data.get(pointer);
            float d = 360f * ((float) member.getNumber() / (float) total);

            if ((int)d == 0){
                continue;
            }
            arcPaint.setColor(colors.get(member.getIndex()));
            canvas.drawArc(rect, angle, d, true, arcPaint);  // Draw arc according to schedule
            angle=angle+d;

        }

    }

    public void setData(List<PieMember> data) {
        this.data = data;
        postInvalidate();
    }

    public void setColors(ArrayList<Integer> colors){
        this.colors=colors;
    }

    public static class PieMember {
        private String text =null;
        private long number = 0;
        private int color = 0;
        private int index = 0;

        public void augment(int n) {
            this.number += n;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }// Text on a ring

        public long getNumber() {
            return number;
        }

        public void setNumber(long number) {
            this.number = number;
        }

        public int getColor() {
            return color;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }
}

