/*
 * Copyright (C) 2006 The Android Open Source Project
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

package mst.view.menu;



import com.mst.R;

import mst.view.menu.MstMenuBuilder.ItemInvoker;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * The expanded menu view is a list-like menu with all of the available menu items.  It is opened
 * by the user clicking no the 'More' button on the icon menu view.
 */
public final class MstExpandedMenuView extends ListView implements ItemInvoker, MstMenuView, OnItemClickListener {
    private MstMenuBuilder mMenu;

    /** Default animations for this menu */
    private int mAnimations;
    
    /**
     * Instantiates the ExpandedMenuView that is linked with the provided MenuBuilder.
     * @param menu The model for the menu which this MenuView will display
     */
    public MstExpandedMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MenuView, 0, 0);
        mAnimations = a.getResourceId(R.styleable.MenuView_android_windowAnimationStyle, 0);
        a.recycle();

        setOnItemClickListener(this);
    }

    public void initialize(MstMenuBuilder menu) {
        mMenu = menu;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        
        // Clear the cached bitmaps of children
        setChildrenDrawingCacheEnabled(false);
    }

    public boolean invokeItem(MstMenuItemImpl item) {
        return mMenu.performItemAction(item, 0);
    }

    public void onItemClick(AdapterView parent, View v, int position, long id) {
        invokeItem((MstMenuItemImpl) getAdapter().getItem(position));
    }

    public int getWindowAnimations() {
        return mAnimations;
    }
    
}
