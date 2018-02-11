package com.monster.paymentsecurity.views;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import mst.widget.recycleview.LinearLayoutManager;
import mst.widget.recycleview.RecyclerView;

/**
 * Created by sandysheny on 16-11-29.
 */

public class SpaceDecoration extends RecyclerView.ItemDecoration {
    private int size;
    private int mOrientation;
    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;


    public SpaceDecoration(Context context, int orientation, int size) {
        this.size = size;
        setOrientation(orientation);
    }

    //设置屏幕的方向
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    //由于Divider也有长宽高，每一个Item需要向下或者向右偏移
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == HORIZONTAL_LIST) {
            //画竖线，就是往右偏移一个分割线的宽度
            outRect.set(0, 0, size, 0);
        } else {
            //画横线，就是往下偏移一个分割线的高度
            outRect.set(0, 0, 0, size);
        }
    }
}