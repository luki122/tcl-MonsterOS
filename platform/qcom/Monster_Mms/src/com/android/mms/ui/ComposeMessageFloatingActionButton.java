package com.android.mms.ui;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
//tangyisen
import mst.widget.FloatingActionButton;

public class ComposeMessageFloatingActionButton extends FloatingActionButton {
    
    
    public ComposeMessageFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    public ComposeMessageFloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ComposeMessageFloatingActionButton(Context context) {
        this(context, null);
    }
    
    private void init(Context context) {
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec );
    }
}
