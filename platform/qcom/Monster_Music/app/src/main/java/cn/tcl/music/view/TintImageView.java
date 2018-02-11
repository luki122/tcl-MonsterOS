package cn.tcl.music.view;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.v7.internal.widget.TintManager;
import android.support.v7.internal.widget.TintTypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/11/2 17:45
 * @copyright TCL-MIE
 */
public class TintImageView extends ImageView {

    private static final int[] TINT_ATTRS = {
            android.R.attr.background,
            android.R.attr.src
    };

    private final TintManager mTintManager;

    public TintImageView(Context context) {
        this(context, null);
    }

    public TintImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, TINT_ATTRS,
                defStyleAttr, 0);
        if (a.length() > 0) {
            if (a.hasValue(0)) {
                setBackgroundDrawable(a.getDrawable(0));
            }
            if (a.hasValue(1)) {
                setImageDrawable(a.getDrawable(1));
            }
        }
        a.recycle();

        // Keep the TintManager in case we need it later
        mTintManager = a.getTintManager();
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        // Intercept this call and instead retrieve the Drawable via the tint manager
        setImageDrawable(mTintManager.getDrawable(resId));
    }
}
