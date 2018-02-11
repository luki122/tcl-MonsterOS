package cn.tcl.weather.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.leon.tools.view.AndroidUtils;

import cn.tcl.weather.utils.store.FontUtils;

/**
 * Created by pengsong on 16-10-17.
 */
public class TclCustomTextView extends TextView {

    private Context mContext;
    private Typeface mTypeface;

    public TclCustomTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public TclCustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public TclCustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TclCustomTextView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        if (null == mContext) {
            mContext = context;
        }
        float textSize = AndroidUtils.px2sp(context, getTextSize());
        if (textSize < 24) {
            mTypeface = FontUtils.TEXT_TYPEFACE_NORMAL;
        } else if (textSize >= 24 && textSize < 33) {
            mTypeface = FontUtils.TEXT_TYPEFACE_MEDIUM;
        } else {
            mTypeface = FontUtils.TEXT_TYPEFACE_THIN;
        }
        setTypeface(mTypeface);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled())
            return super.onTouchEvent(event);
        return false;
    }
}
