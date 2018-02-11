package cn.tcl.music.view;

import android.support.v7.internal.widget.TintManager;
import android.support.v7.internal.widget.TintTypedArray;
import android.content.Context;
import android.util.AttributeSet;

/**
 * An tint aware {@link android.widget.CheckBox}.
 *
 * @hide
 */
public class TintMusic5CheckBox extends TintCheckBox {

    private static final int[] TINT_ATTRS = {
            android.R.attr.button,
            android.R.attr.drawableEnd
    };

    private final TintManager mTintManager;

    public TintMusic5CheckBox(Context context) {
        this(context, null);
    }

    public TintMusic5CheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkboxStyle);
    }

    public TintMusic5CheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, TINT_ATTRS,
                defStyleAttr, 0);
        setButtonDrawable(a.getDrawable(0));
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, a.getDrawable(1), null);
        a.recycle();

        mTintManager = a.getTintManager();
    }

    @Override
    public void setButtonDrawable(int resid) {
        setButtonDrawable(mTintManager.getDrawable(resid));
    }
}
