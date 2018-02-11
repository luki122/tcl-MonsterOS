package com.monster.launcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

/**
 * 创建于 cailiuzuo on 16-10-25 上午10:36.
 * 作者
 */
public class WidgetTextView extends TextView {


    private boolean isCheckable;
    private boolean isChecked;
    private Drawable mSignDrawable;

    public WidgetTextView(Context context) {
        super(context);
    }


    public boolean isCheckable() {
        return isCheckable;
    }

    public void setCheckable(boolean checkable) {
        isCheckable = checkable;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        if (isCheckable) {
            isChecked = checked;
            invalidate();
            if(isChecked){
                setBackgroundAndTextColor(true);
            }else {
                setBackgroundAndTextColor(false);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSignDrawable(getSignDrawable(), canvas);
    }


    private Drawable getSignDrawable() {
        if (isChecked) {
            if (mSignDrawable == null) {
                final Resources res = getContext().getResources();
                int padding = res.getDimensionPixelSize(R.dimen.widget_checked_padding);
                int width = getWidth();
                int widthOfmCheckDrawable;
                mSignDrawable = res.getDrawable(R.drawable.check);
                float scale = res.getInteger(R.integer.config_workspaceNormalDragShrinkPercentage) / 100f;
                widthOfmCheckDrawable = res.getDimensionPixelSize(R.dimen.widget_check_size)/*mSignDrawable.getIntrinsicWidth()*/;
                int left = (int) (((getWidth() - width) / 2 + width) * scale) - widthOfmCheckDrawable / 2 - 1;
               /* int top = getPaddingTop() + (widthOfmCheckDrawable/2 - width) / 2 - widthOfmCheckDrawable / 2 + 1;*/
                float textSize = getTextSize();
                int top = (int) ((getHeight() - textSize - width) / 4.5 * scale)+padding;
                top = top <= 0 ? padding : top;
                int right = left + widthOfmCheckDrawable;
                int buttom = top + widthOfmCheckDrawable;
                if (right > getWidth()) {
                    left = getWidth() - widthOfmCheckDrawable;
                    right = getWidth();
                } else {

                }
                Log.d("liuzuo182","left="+left+"  right="+right+" padding="+padding);
                mSignDrawable.setBounds(left-padding, top, right-padding, buttom);


            }
            return mSignDrawable;
        } else {
            return null;
        }
    }

    private void setBackgroundAndTextColor(boolean b) {
        if(b){
            setBackground(getResources().getDrawable(R.drawable.desk_widget_text_view_stroke_check));
            //setTextColor(getResources().getColor(R.color.desk_widget_text_color_check_delete));
        }else {

            setBackground(getResources().getDrawable(R.drawable.desk_widget_text_view_stroke));
           // setTextColor(getResources().getColor(R.color.desk_widget_text_color_default));

        }
    }

    private void drawSignDrawable(Drawable mSignDrawable, Canvas canvas) {
        if (mSignDrawable != null) {

            final int scrollX = getScrollX();
            final int scrollY = getScrollY();

            if ((scrollX | scrollY) == 0) {
                mSignDrawable.draw(canvas);
            } else {
                canvas.save();
                canvas.translate(scrollX, scrollY);
                mSignDrawable.draw(canvas);
                canvas.restore();
            }

        }else {

        }
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        Log.d("liuzuo86", "get text="+getText().toString());
    }
}