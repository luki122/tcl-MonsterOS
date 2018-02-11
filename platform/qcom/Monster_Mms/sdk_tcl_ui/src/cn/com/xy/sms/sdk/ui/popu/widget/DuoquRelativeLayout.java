package cn.com.xy.sms.sdk.ui.popu.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;

public class DuoquRelativeLayout extends RelativeLayout implements IViewAttr {

    public TypedArray mDuoquAttr = null;

    public DuoquRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDuoquAttr = context.obtainStyledAttributes(attrs,
                R.styleable.duoqu_attr);
    }

    @Override
    public Object obtainStyledAttributes(byte styleType, int styleId) {
        return ViewManger
                .obtainStyledAttributes(mDuoquAttr, styleType, styleId);
    }

}
