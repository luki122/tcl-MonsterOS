/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.filtershow.category;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import com.android.gallery3d.R;

public class CategoryTrack extends LinearLayout {

    private CategoryAdapter mAdapter;
    
    // TCL ShenQianfeng Begin on 2016.09.01
    // private int mElemSize;
    // TCL ShenQianfeng End on 2016.09.01

    private View mSelectedView;
    private float mStartTouchY;
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            if (getChildCount() != mAdapter.getCount()) {
                fillContent();
            } else {
                invalidate();
            }
        }
        @Override
        public void onInvalidated() {
            super.onInvalidated();
            fillContent();
        }
    };

    public CategoryTrack(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CategoryTrack);
        
        // mElemSize = a.getDimensionPixelSize(R.styleable.CategoryTrack_iconSize, 0);

        // TCL ShenQianfeng Begin on 2016.09.01
        // Annotated Below:
        // mElemSize = (int) context.getResources().getDimension(R.dimen.category_item_w);//Shenqianfeng Sync on on 2016.08.17. above line is original
        // TCL ShenQianfeng End on 2016.09.01
         
    }

    public void setAdapter(CategoryAdapter adapter) {
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataSetObserver);
        fillContent();
    }

    public void fillContent() {
        removeAllViews();
        // TCL ShenQianfeng Begin on 2016.09.01
        // Annotated Below:
        /*
        mAdapter.setItemWidth(mElemSize);
        mAdapter.setItemHeight(LayoutParams.MATCH_PARENT);
        */
        // TCL ShenQianfeng End on 2016.09.01
        int n = mAdapter.getCount();
        for (int i = 0; i < n; i++) {
            View view = mAdapter.getView(i, null, this);
            addView(view, i);
        }
        requestLayout();
    }

    @Override
    public void invalidate() {
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = getChildAt(i);
            child.invalidate();
        }
    }

}
