package mst.widget.impl;

import android.graphics.drawable.Drawable;

public interface ShadowViewDelegate {
    float getRadius();
    void setShadowPadding(int left, int top, int right, int bottom);
    void setBackgroundDrawable(Drawable background);
    boolean isCompatPaddingEnabled();
}
