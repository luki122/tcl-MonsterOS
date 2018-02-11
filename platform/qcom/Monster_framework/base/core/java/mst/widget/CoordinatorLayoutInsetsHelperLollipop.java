package mst.widget;

import mst.widget.impl.CoordinatorLayoutInsetsHelper;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;


public class CoordinatorLayoutInsetsHelperLollipop implements CoordinatorLayoutInsetsHelper {

    public void setupForWindowInsets(View view, OnApplyWindowInsetsListener insetsListener) {
        if (view.getFitsSystemWindows()) {
            // First apply the insets listener
        	view.setOnApplyWindowInsetsListener(insetsListener);
            // Now set the sys ui flags to enable us to lay out in the window insets
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

}