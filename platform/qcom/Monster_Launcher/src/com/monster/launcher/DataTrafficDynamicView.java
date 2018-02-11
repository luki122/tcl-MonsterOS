package com.monster.launcher;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by antino on 16-6-21.
 */
public class DataTrafficDynamicView extends BubbleTextView{
    public DataTrafficDynamicView(Context context) {
        this(context, null, 0);
    }

    public DataTrafficDynamicView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataTrafficDynamicView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
