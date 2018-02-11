package com.monster.launcher.dynamic;

import android.content.Context;

import com.monster.launcher.BubbleTextView;
import com.monster.launcher.ItemInfo;

/**
 * Created by antino on 16-7-15.
 */
public interface IDynamicIcon {
    boolean init(Context context, BubbleTextView bubbleTextView, ItemInfo info,boolean isAllapps);
    void onAttachedToWindow(boolean register);
    void onDetachedFromWindow();
    void onSizeChanged(int w, int h, int oldw, int oldh);
    boolean updateDynamicIcon();
    void cleanupdateDynamicIcon();
}
