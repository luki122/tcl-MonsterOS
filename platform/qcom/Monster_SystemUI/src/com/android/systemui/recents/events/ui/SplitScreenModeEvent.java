package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus;

/**
 * Created by liuzhicang on 16-11-21.
 */

public class SplitScreenModeEvent extends EventBus.Event {
    public final boolean showRecents;

    public SplitScreenModeEvent(boolean showRecents) {
        this.showRecents = showRecents;
    }
}
