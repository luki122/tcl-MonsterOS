package mst.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;

/**
 * Created by caizhongting on 16-8-25.
 */
public class RelativeLayout extends android.widget.RelativeLayout implements Checkable{
    private boolean mChecked = false;

    public RelativeLayout(Context context) {
        super(context);
    }

    public RelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;
        int N = getChildCount();
        for(int i=0;i<N;i++){
            View child = getChildAt(i);
            if(child instanceof Checkable){
                ((Checkable)child).setChecked(checked);
            }
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }
}
