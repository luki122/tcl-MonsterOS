package com.monster.launcher;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by lijun on 16-6-24.
 */
public class PageIndicatorDiagitalImagview extends ImageView {
    Paint paint = new Paint();
    Rect rect = new Rect();
    private int indicatorIndex;
    private int textSize = 60;
    private String text ;
    public void setIndicatorIndex(int indicatorIndex) {
        this.indicatorIndex = indicatorIndex;
    }

    public PageIndicatorDiagitalImagview(Context context) {
        super(context);
    }

    public PageIndicatorDiagitalImagview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PageIndicatorDiagitalImagview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = getWidth();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setAlpha(100);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(size/2,size/2,size/2-1,paint);

        paint.setAlpha(60);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#55bfbfbf"));
        canvas.drawCircle(size/2,size/2,size/2,paint);

        paint.setColor(Color.BLACK);
        paint.setAlpha(150);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);

        text = ""+(indicatorIndex+1);
        Paint.FontMetrics fm = paint.getFontMetrics();
        int baseline = (int) ((getHeight() - fm.bottom - fm.top) / 2);

        canvas.drawText(text,getWidth()/2,baseline,paint);
    }
}
