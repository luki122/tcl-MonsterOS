package mst.widget.recycleview;

import android.graphics.Rect;
import android.view.View;

public class RecyclerViewGridItemSpace extends RecyclerView.ItemDecoration {
	private int mSpaceSize;
	private int mSpanCount;

    public RecyclerViewGridItemSpace(int spaceSize,int spanCount) {
        this.mSpaceSize = spaceSize;
        this.mSpanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        //不是第一个的格子都设一个左边和底部的间距
        outRect.left = mSpaceSize;
        outRect.bottom = mSpaceSize;
        //由于每行都只有mSpanCount个，所以第一个都是mSpanCount的倍数，把左边距设为0
        if (parent.getChildLayoutPosition(view) %mSpanCount==0) {
            outRect.left = 0;
        }
    }
}
