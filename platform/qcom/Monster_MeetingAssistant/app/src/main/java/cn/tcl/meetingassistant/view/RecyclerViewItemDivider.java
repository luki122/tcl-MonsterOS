/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import cn.tcl.meetingassistant.R;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-19
 * the recycler view's divider
 */
public class RecyclerViewItemDivider extends RecyclerView.ItemDecoration {
    public RecyclerViewItemDivider() {
        super();
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int heightDivider = parent.getContext().getResources().getDimensionPixelOffset(R.dimen.
                recycler_item_divider_height);

        outRect.set(0, 0, 0,heightDivider);
    }
}
