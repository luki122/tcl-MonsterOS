package com.monster.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

/**
 * 创建于 cailiuzuo on 16-9-18 上午10:40.
 * 作者
 */
public class DeskWidgetEditTextView extends EditText{
    private Paint linePaint;

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
    }

    private boolean isEdit;
    private float margin;

    private int mPaintColor;
    public DeskWidgetEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPaintColor();
        this.linePaint=new Paint();
        this.linePaint.setColor(mPaintColor);
    }

    protected void onDraw(Canvas paramCanvas) {
        //paramCanvas.drawColor(this.mPaintColor);
        if(isEdit()) {
            int i = getLineCount();

            int j = getHeight();

            int k = getLineHeight();

            int m = 1 + j / k;

            if (i < m)

                i = m;

            int n = getCompoundPaddingTop();

            //paramCanvas.drawLine(0.0F, n, getRight(), n, this.linePaint);

            for (int f = 0; ; f++) {

                if (f >= i) {

                   // setPadding(10 + (int) this.margin, 0, 0, 0);

                    super.onDraw(paramCanvas);

                    paramCanvas.restore();

                    return;

                }

                n += k;

                paramCanvas.drawLine(0.0F, n, getRight(), n, this.linePaint);


                paramCanvas.save();
            }

        }
        super.onDraw(paramCanvas);
    }
    public void setPaintColor(){
        //int color =LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor();
        boolean isBlackText = LauncherAppState.getInstance().getWindowGlobalVaule().isBlackText();
        if(isBlackText){
            // mDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            mPaintColor=getResources().getColor(R.color.desk_widget_edit_color_black);
        }else{
            mPaintColor=getResources().getColor(R.color.desk_widget_edit_color_white);
        }
        invalidate();
    }
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection inputConnection = super.onCreateInputConnection(outAttrs);
        if(inputConnection != null){
            outAttrs.imeOptions &= ~EditorInfo.IME_ACTION_SEARCH;
        }
        return inputConnection;
    }
}
