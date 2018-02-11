package cn.tcl.music.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import cn.tcl.music.R;
import cn.tcl.music.util.Utils;

import com.nineoldandroids.view.ViewHelper;

public class Slider extends CustomView {

    // Event when slider change value
    public interface OnValueChangedListener {
        void onValueChanged(int value);
    }

    int backgroundColor = Color.parseColor("#f77811");

    Ball ball;

    boolean press = false;

    int value = 0;
    int max = 100;
    int min = 0;

    OnValueChangedListener onValueChangedListener;

    public Slider(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes(attrs);
    }

    // Set atributtes of XML to View
    protected void setAttributes(AttributeSet attrs) {

        setBackgroundResource(R.drawable.background_transparent);

        // Set size of view
        setMinimumHeight(Utils.dpToPx(32, getResources()));
        setMinimumWidth(Utils.dpToPx(80, getResources()));

        // Set background Color
        // Color by resource
        int bacgroundColor = attrs.getAttributeResourceValue(ANDROIDXML,
                "background", -1);
        if (bacgroundColor != -1) {
            setBackgroundColor(getResources().getColor(bacgroundColor));
        } else {
            // Color by hexadecimal
            int background = attrs.getAttributeIntValue(ANDROIDXML, "background", -1);
            if (background != -1)
                setBackgroundColor(background);
        }

        ball = new Ball(getContext());
        LayoutParams params = new LayoutParams(Utils.dpToPx(15,
                getResources()), Utils.dpToPx(15, getResources()));
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        ball.setLayoutParams(params);
        addView(ball);
    }

    @Override
    public void invalidate() {
        ball.invalidate();
        super.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!placedBall || ball.xFin != getWidth() - getHeight() / 2 - ball.getWidth() / 2)//[BUGFIX]-Modify by TCTNJ,liang.guo, 2015-08-11,PR1065025
            placeBall();

        if (value == min) {
            // Crop line to transparent effect
            Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(),
                    canvas.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas temp = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#69757c"));
            paint.setStrokeWidth(Utils.dpToPx(4, getResources()));
            temp.drawLine(getHeight() / 2, getHeight() / 2, getWidth()
                    - getHeight() / 2, getHeight() / 2, paint);
            Paint transparentPaint = new Paint();
            transparentPaint.setColor(getResources().getColor(
                    android.R.color.transparent));
            transparentPaint.setXfermode(new PorterDuffXfermode(
                    PorterDuff.Mode.CLEAR));
            temp.drawCircle(ViewHelper.getX(ball) + ball.getWidth() / 2,
                    ViewHelper.getY(ball) + ball.getHeight() / 2,
                    ball.getWidth() / 2, transparentPaint);

            canvas.drawBitmap(bitmap, 0, 0, new Paint());
        } else {
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#69757c"));
            paint.setStrokeWidth(Utils.dpToPx(4, getResources()));
            canvas.drawLine(getHeight() / 2, getHeight() / 2, getWidth()
                    - getHeight() / 2, getHeight() / 2, paint);
            paint.setColor(backgroundColor);
            float division = (ball.xFin - ball.xIni) / (max-min);
            int value = this.value - min;
            canvas.drawLine(getHeight() / 2, getHeight() / 2, value * division
                    + getHeight() / 2, getHeight() / 2, paint);
            if(placedBall) {
                setValue(value);
            }
        }

        if (press) {
            Paint paint = new Paint();
            paint.setColor(backgroundColor);
            paint.setAntiAlias(true);
            canvas.drawCircle(ViewHelper.getX(ball) + ball.getWidth() / 2,
                    getHeight() / 2, getHeight() / 3, paint);
        }
        invalidate();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE) {
                if ((event.getX() <= getWidth() && event.getX() >= 0)) {
                    press = true;
                    // calculate value
                    int newValue = 0;
                    float division = (ball.xFin - ball.xIni) / (max-min);
                    if (event.getX() > ball.xFin) {
                        newValue = max;
                    } else if (event.getX() < ball.xIni) {
                        newValue = min;
                    } else {
                        newValue = min + (int) ((event.getX() - ball.xIni) / division);
                    }
                    if (value != newValue) {
                        value = newValue;
                        if (onValueChangedListener != null)
                            onValueChangedListener.onValueChanged(newValue);
                    }
                    // move ball indicator
                    float x = event.getX();
                    x = (x < ball.xIni) ? ball.xIni : x;
                    x = (x > ball.xFin) ? ball.xFin : x;
                    ViewHelper.setX(ball, x);
                } else {
                    press = false;
                }

            } else if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {
                press = false;
            }
        }
        return true;
    }

    /**
     * Make a dark color to press effect
     *
     * @return
     */
    protected int makePressColor() {
        int r = (this.backgroundColor >> 16) & 0xFF;
        int g = (this.backgroundColor >> 8) & 0xFF;
        int b = (this.backgroundColor >> 0) & 0xFF;
        r = (r - 30 < 0) ? 0 : r - 30;
        g = (g - 30 < 0) ? 0 : g - 30;
        b = (b - 30 < 0) ? 0 : b - 30;
        return Color.argb(70, r, g, b);
    }

    private void placeBall() {
        ViewHelper.setX(ball, getHeight() / 2 - ball.getWidth() / 2);
        ball.xIni = ViewHelper.getX(ball);
        ball.xFin = getWidth() - getHeight() / 2 - ball.getWidth() / 2;
        ball.xCen = getWidth() / 2 - ball.getWidth() / 2;
        placedBall = true;
    }

    // GETERS & SETTERS

    public OnValueChangedListener getOnValueChangedListener() {
        return onValueChangedListener;
    }

    public void setOnValueChangedListener(
            OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    public int getValue() {
        return value;
    }

    public void setValue(final int value) {
        if (placedBall == false) {
            post(new Runnable() {

                @Override
                public void run() {
                    setValue(value);
                }
            });
        } else {
            this.value = value;
            float division = (ball.xFin - ball.xIni) / max;
            ViewHelper.setX(ball,
                    value * division + getHeight() / 2 - ball.getWidth() / 2);
        }

    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    @Override
    public void setBackgroundColor(int color) {
        backgroundColor = color;
        if (isEnabled()) {
            beforeBackground = backgroundColor;
        }
    }

    boolean placedBall = false;

    class Ball extends View {

        float xIni, xFin, xCen;

        public Ball(Context context) {
            super(context);
            setBackgroundResource(R.drawable.background_checkbox);
            LayerDrawable layer = (LayerDrawable) getBackground();
            GradientDrawable shape = (GradientDrawable) layer
                    .findDrawableByLayerId(R.id.shape_bacground);
            shape.setColor(backgroundColor);
        }

    }

}
