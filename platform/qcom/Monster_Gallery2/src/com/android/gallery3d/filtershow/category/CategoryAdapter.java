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

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.FilterShowActivity;
import com.android.gallery3d.filtershow.filters.FilterRepresentation;
import com.android.gallery3d.filtershow.filters.FilterTinyPlanetRepresentation;
import com.android.gallery3d.filtershow.pipeline.ImagePreset;

public class CategoryAdapter extends ArrayAdapter<Action> {

    private static final String LOGTAG = "CategoryAdapter";
    private View mContainer;//container is CategoryPanel

    // TCL ShenQianfeng Begin on 2016.09.01
    private Context mContext;
    // TCL ShenQianfeng End on 2016.09.01
    
    // TCL ShenQianfeng Begin on 2016.09.01
    // Annotated Below:
    /*
    private int mItemWidth = ListView.LayoutParams.MATCH_PARENT;
    private int mItemHeight;
    */
    // TCL ShenQianfeng End on 2016.09.01
    
    private int mSelectedPosition;
    private int mCategory;
    private int mOrientation;
    private boolean mShowAddButton = false;
    private String mAddButtonText;
    
    // TCL ShenQianfeng Begin on 2016.09.01
    private int mCategoryItemWidth = 0;
    private int mCategoryItemHeight = 0;
    // TCL ShenQianfeng End on 2016.09.01

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 begin
    private int mColorFilterCurrentPosition = -1;
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 end

    public CategoryAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        // TCL ShenQianfeng Begin on 2016.09.01
        // Annotated Below:
        // mItemHeight = (int) (context.getResources().getDisplayMetrics().density * 100);
        // TCL ShenQianfeng End on 2016.09.01
    }

    public CategoryAdapter(Context context) {
        this(context, 0);
        // TCL ShenQianfeng Begin on 2016.09.01
        // Annotated Below:
        mContext = context;
        // TCL ShenQianfeng End on 2016.09.01
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 begin
    // TCL ShenQianfeng Begin on 2016.09.05
    // Annotated Below:
    /* 
    public void addNormalIcon(Bitmap bitmap) {
        filtersNormalIcon.add(bitmap);
    }

    public void addSelectedIcon(Bitmap bitmap) {
        filtersSelectedIcon.add(bitmap);
    }
    */
    // TCL ShenQianfeng End on 2016.09.05
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 end

    @Override
    public void clear() {
        for (int i = 0; i < getCount(); i++) {
            Action action = getItem(i);
            action.clearBitmap();
        }
        super.clear();
    }

    // TCL ShenQianfeng Begin on 2016.09.01
    // Annotated Below:
    /*
    public void setItemHeight(int height) {
        mItemHeight = height;
    }
    */
    // TCL ShenQianfeng End on 2016.09.01

    // TCL ShenQianfeng Begin on 2016.09.01
    // Annotated Below:
    /*
    public void setItemWidth(int width) {
        mItemWidth = width;
    }
    */
    // TCL ShenQianfeng End on 2016.09.01
    
    @Override
    public void add(Action action) {
        super.add(action);
        action.setAdapter(this);
    }
    
    // TCL ShenQianfeng Begin on 2016.09.01
    private void initItemWidthAndHeight() {
        Resources res = mContext.getResources();
        int widthResId = 0;
        int heightResId = 0;
        switch(mCategory) {
        case MainPanel.CROP:
            widthResId = R.dimen.mst_category_view_item_width_crop;
            heightResId =R.dimen.mst_category_view_item_height_crop;
            break;
        case MainPanel.ROTATE:
            widthResId = R.dimen.mst_category_view_item_width_rotate;
            heightResId =R.dimen.mst_category_view_item_height_rotate;
            break;
        case MainPanel.LOOKS:
            widthResId = R.dimen.mst_category_view_item_width_looks;
            heightResId =R.dimen.mst_category_view_item_height_looks;
            break;
        case MainPanel.FILTERS:
            widthResId = R.dimen.mst_category_view_item_width_colors;
            heightResId =R.dimen.mst_category_view_item_height_colors;
            break;
        }
        mCategoryItemWidth = res.getDimensionPixelSize(widthResId);
        mCategoryItemHeight = res.getDimensionPixelSize(heightResId);
    }
    // TCL ShenQianfeng End on 2016.09.01

    public void initializeSelection(int category) {
        mCategory = category;
        initItemWidthAndHeight();
        mSelectedPosition = -1;
        if (category == MainPanel.LOOKS) {
            mSelectedPosition = 0;
            //mAddButtonText = getContext().getString(R.string.filtershow_add_button_looks);
        }
        // TCL ShenQianfeng Begin on 2016.08.23
        // Annotated Below:
        /* 
        if (category == MainPanel.BORDERS) {
            mSelectedPosition = 0;
        }
        */
        // TCL ShenQianfeng End on 2016.08.23
        /*
        if (category == MainPanel.VERSIONS) {
            mAddButtonText = getContext().getString(R.string.filtershow_add_button_versions);
        }
        */
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 begin
        if (category == MainPanel.FILTERS) {
            mSelectedPosition = mColorFilterCurrentPosition;
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 end
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new CategoryView(getContext());
        }
        CategoryView view = (CategoryView) convertView;
        view.setOrientation(mOrientation);
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 begin
        

        // TCL ShenQianfeng Begin on 2016.09.05
        // Annotated Below:
        /* 
        if(mCategory == MainPanel.FILTERS) {
            view.setNormalIcon(filtersNormalIcon.get(position));
            view.setSelectedIcon(filtersSelectedIcon.get(position));
        }
        */
        // TCL ShenQianfeng End on 2016.09.05

        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 end

        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-19,PR904445 begin
        view.setPosition(position);
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-19,PR904445 end
        
        Action action = getItem(position);
        view.setAction(action, this);
        
        // TCL ShenQianfeng Begin on 2016.09.01
        // Original:
        /*
        int width = mItemWidth;
        int height = mItemHeight;
        */
        // Modify To:
        int width = mCategoryItemWidth;
        int height = mCategoryItemHeight;
        // TCL ShenQianfeng End on 2016.09.01
        if (action.getType() == Action.SPACER) {
            if (mOrientation == CategoryView.HORIZONTAL) {
                width = width / 2;
            } else {
                height = height / 2;
            }
        }
        if (action.getType() == Action.ADD_ACTION
                && mOrientation == CategoryView.VERTICAL) {
            height = height / 2;
        }
        view.setLayoutParams(new ListView.LayoutParams(width, height));
        view.setTag(position);
        view.invalidate();
        return view;
    }
    
    // TCL ShenQianfeng Begin on 2016.09.05
    public void setSelected(int position, boolean selected) {
        View child = null;
        if (mContainer instanceof ListView) {
            ListView lv = (ListView) mContainer;
            child = lv.getChildAt(position - lv.getFirstVisiblePosition());
        } else {
            CategoryTrack ct = (CategoryTrack) mContainer;
            child = ct.getChildAt(position);
        }
        if (child != null) {
            child.setSelected(selected);
            if(selected) {
                mSelectedPosition = position;
            } else {
                mSelectedPosition = -1;
            }
            invalidateView(mSelectedPosition);
        }
    }
    // TCL ShenQianfeng End on 2016.09.05
    

    public void setSelected(View v) {
        int old = mSelectedPosition;
        //ShenQianfeng Modify Begin on 2016.08.17
        //Original:
        //mSelectedPosition = (Integer) v.getTag();
        //Modify To:
        mValidSelectedPos = mSelectedPosition = (Integer) v.getTag(); // MODIFIED by Hongbin.Chen, 2016-06-29,BUG-2432573
        mColorFilterCurrentPosition = mSelectedPosition;//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210
        //ShenQianfeng Modify End
        if (old != -1) {
            invalidateView(old);
        }
        invalidateView(mSelectedPosition);
    }

    public boolean isSelected(View v) {
        return (Integer) v.getTag() == mSelectedPosition;
    }
    
    //ShenQianfeng Sync Begin on 2016.08.17
    private boolean mUserClicked;
    private int mValidSelectedPos;

    public void doUserClicked() {
        mUserClicked = true;
    }

    public int getUserSelectedPos() {
        return mUserClicked ? mValidSelectedPos : -1;
    }

    public void setDefaultUserSelectedPos(int pos) {
        if (pos >= 0) {
            mUserClicked = true;
            mValidSelectedPos = pos;
        }
    }
    //ShenQianfeng Sync End on 2016.08.17
    
    private void invalidateView(int position) {
        View child = null;
        if (mContainer instanceof ListView) {
            ListView lv = (ListView) mContainer;
            child = lv.getChildAt(position - lv.getFirstVisiblePosition());
        } else {
            CategoryTrack ct = (CategoryTrack) mContainer;
            child = ct.getChildAt(position);
        }
        if (child != null) {
            child.invalidate();
        }
    }

    public void setContainer(View container) {
        mContainer = container;
    }

    public void imageLoaded() {
        notifyDataSetChanged();
    }

    public FilterRepresentation getTinyPlanet() {
        for (int i = 0; i < getCount(); i++) {
            Action action = getItem(i);
            if (action.getRepresentation() != null
                    && action.getRepresentation()
                    instanceof FilterTinyPlanetRepresentation) {
                return action.getRepresentation();
            }
        }
        return null;
    }

    public void removeTinyPlanet() {
        for (int i = 0; i < getCount(); i++) {
            Action action = getItem(i);
            if (action.getRepresentation() != null
                    && action.getRepresentation()
                    instanceof FilterTinyPlanetRepresentation) {
                super.remove(action);
                return;
            }
        }
    }

    @Override
    public void remove(Action action) {
        if (!(mCategory == MainPanel.VERSIONS
                || mCategory == MainPanel.LOOKS)) {
            return;
        }
        super.remove(action);
        FilterShowActivity activity = (FilterShowActivity) getContext();
        if (mCategory == MainPanel.LOOKS) {
            activity.removeLook(action);
        } else if (mCategory == MainPanel.VERSIONS) {
            activity.removeVersion(action);
        }
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void reflectImagePreset(ImagePreset preset) {
        if (preset == null) {
            return;
        }
        int selected = 0; // if nothing found, select "none" (first element)
        FilterRepresentation rep = null;
        if (mCategory == MainPanel.LOOKS) {
            int pos = preset.getPositionForType(FilterRepresentation.TYPE_FX);
            if (pos != -1) {
                rep = preset.getFilterRepresentation(pos);
            }
        }
        // TCL ShenQianfeng Begin on 2016.08.23
        // Annotated Below:
        /* 
        else if (mCategory == MainPanel.BORDERS) {
            int pos = preset.getPositionForType(FilterRepresentation.TYPE_BORDER);
            if (pos != -1) {
                rep = preset.getFilterRepresentation(pos);
            }
        }
        */
        // TCL ShenQianfeng End on 2016.08.23
        if (rep != null) {
            for (int i = 0; i < getCount(); i++) {
                FilterRepresentation itemRep = getItem(i).getRepresentation();
                if (itemRep == null) {
                    continue;
                }
                if (rep.getName().equalsIgnoreCase(
                        itemRep.getName())) {
                    selected = i;
                    break;
                }
            }
        }
        if (mSelectedPosition != selected) {
            mSelectedPosition = selected;
            this.notifyDataSetChanged();
        }
    }

    public boolean showAddButton() {
        return mShowAddButton;
    }

    public void setShowAddButton(boolean showAddButton) {
        mShowAddButton = showAddButton;
    }

    public String getAddButtonText() {
        return mAddButtonText;
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-19,PR904445 begin
    public int getCategory() {
        return mCategory;
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-19,PR904445 end
}
