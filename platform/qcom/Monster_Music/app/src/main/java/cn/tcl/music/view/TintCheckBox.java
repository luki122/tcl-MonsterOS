package cn.tcl.music.view;

import android.content.Context;
import android.support.v7.internal.widget.TintManager;
import android.support.v7.internal.widget.TintTypedArray;
import android.util.AttributeSet;
import android.widget.CheckBox;
/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/11/2 15:50
 * @copyright TCL-MIE
 */
public class TintCheckBox extends CheckBox {

    private static final int[] TINT_ATTRS = {
            android.R.attr.button
    };

    private final TintManager mTintManager;

    public TintCheckBox(Context context) {
        this(context, null);
    }

    public TintCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkboxStyle);
    }

    public TintCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, TINT_ATTRS,
                defStyleAttr, 0);
        setButtonDrawable(a.getDrawable(0));
        a.recycle();

        mTintManager = a.getTintManager();
    }

    @Override
    public void setButtonDrawable(int resid) {
        setButtonDrawable(mTintManager.getDrawable(resid));
    }
}
