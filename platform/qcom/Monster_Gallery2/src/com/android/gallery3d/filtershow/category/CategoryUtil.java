/* ----------|----------------------|----------------------|----------------- */
/* 06/12/2015| jian.pan1            | PR1003170            |[5.0][Gallery] Frame grids' position should retain the previous position after cancel/accept editing
/* ----------|----------------------|----------------------|----------------- */
package com.android.gallery3d.filtershow.category;

import android.util.SparseIntArray;

public class CategoryUtil {
    private static SparseIntArray mPosArray = new SparseIntArray();

    public static SparseIntArray getCategoryPosArray() {
        if (mPosArray == null) {
            mPosArray = new SparseIntArray();
        }
        return mPosArray;
    }

    public static void clearArray() {
        if (mPosArray != null) {
            mPosArray.clear();
            mPosArray = null;
        }
    }
}
