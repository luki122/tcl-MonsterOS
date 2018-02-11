package com.android.contacts.common.mst;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class MstScrollListView extends ListView {

    public MstScrollListView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    public MstScrollListView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    public MstScrollListView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
    }  
  
    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,MeasureSpec.AT_MOST);  
        super.onMeasure(widthMeasureSpec, expandSpec);  
    }  
  
    @Override  
    public boolean dispatchTouchEvent(MotionEvent ev) {  
        if(ev.getAction() == MotionEvent.ACTION_MOVE){     
            return true;   
        }   
        return super.dispatchTouchEvent(ev);  
    }  
}