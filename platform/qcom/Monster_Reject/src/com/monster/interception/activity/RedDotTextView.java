/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monster.interception.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.monster.interception.R;

public class RedDotTextView extends TextView {

    public static final int RED_TIP_INVISIBLE = 0;  
    public static final int RED_TIP_VISIBLE = 1;  
    public static final int RED_TIP_GONE = 2;  
    private int tipVisibility = 0;  
      
    public RedDotTextView(Context context) {  
        super(context);  
        // TODO Auto-generated constructor stub  
        init(null);  
    }  
  
    public RedDotTextView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        // TODO Auto-generated constructor stub  
        init(attrs);  
    }  
  
    public RedDotTextView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
        // TODO Auto-generated constructor stub  
        init(attrs);  
    }  
      
    public void init(AttributeSet attrs) {  
        /*if(attrs != null) {  
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RedTipTextView);  
            tipVisibility = array.getInt(R.styleable.RedTipTextView_redTipsVisibility, 0);  
            array.recycle();  
        }  */
    }  
      
    @Override  
    protected void onDraw(Canvas canvas) {  
        // TODO Auto-generated method stub  
        super.onDraw(canvas);  
        if(tipVisibility == 1) {  
            int width = getWidth();  
            int paddingRight = getPaddingRight();  
            Paint paint = new Paint();  
            paint.setColor(Color.RED);  
            paint.setAntiAlias(true);  
            paint.setDither(true);  
            paint.setStyle(Style.FILL_AND_STROKE);  
            canvas.drawCircle(width - getPaddingRight() / 2, paddingRight / 2, paddingRight/2, paint);  
        }
    }  
      
    public void setVisibility(int visibility) {  
        tipVisibility = visibility;  
        invalidate();  
    }  
}
