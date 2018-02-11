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
import java.lang.reflect.Field; // MODIFIED by caihong.gu-nb, 2016-05-11,BUG-2125214

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.FilterShowActivity;
import com.android.gallery3d.filtershow.TopBarManager;
import com.android.gallery3d.filtershow.imageshow.MasterImage;
import com.android.gallery3d.filtershow.state.StatePanel;
import com.android.gallery3d.util.LogUtil;

public class MainPanel extends Fragment {

    private static final String TAG = "MainPanel";

    private LinearLayout mMainView;
    
    // TCL ShenQianfeng Begin on 2016.08.19
    private ImageButton cropButton;
    private ImageButton rotateButton;
    // TCL ShenQianfeng End on 2016.08.19
    private ImageButton looksButton;
    
    // TCL ShenQianfeng Begin on 2016.08.23
    // Annotated Below:
    // private ImageButton bordersButton;
    // private ImageButton geometryButton;
    // TCL ShenQianfeng End on 2016.08.23
    
    private ImageButton filtersButton;

    public static final String FRAGMENT_TAG = "MainPanel";
    

    

    
    // TCL ShenQianfeng Begin on 2016.08.19
    // Original:
    /*
    public static final int LOOKS = 0;
    public static final int BORDERS = 1;
    public static final int GEOMETRY = 2;
    public static final int FILTERS = 3;
    public static final int VERSIONS = 4;
    */
    // Modify To:
    public static final int CROP = 0;
    public static final int ROTATE = 1;
    public static final int LOOKS = 2;
    // TCL ShenQianfeng Begin on 2016.08.23
    // Annotated Below:
    // public static final int BORDERS = 2;
    // TCL ShenQianfeng End on 2016.08.23
    
    // TCL ShenQianfeng Begin on 2016.08.29
    // Annotated Below:
    // public static final int GEOMETRY = 3;
    // TCL ShenQianfeng End on 2016.08.29

    public static final int FILTERS = 3;
    public static final int VERSIONS = 4;
    // TCL ShenQianfeng End on 2016.08.19
    

    private int mCurrentSelected = -1;
    private int mPreviousToggleVersions = -1;

    private void selection(int position, boolean value) {
        if (value) {
            FilterShowActivity activity = (FilterShowActivity) getActivity();
            activity.setCurrentPanel(position);
        }
        switch (position) {
            // TCL ShenQianfeng Begin on 2016.08.19
            case CROP: {
                cropButton.setSelected(value);
                break;
            }
            case ROTATE: {
                rotateButton.setSelected(value);
                break;
            }
            // TCL ShenQianfeng End on 2016.08.19
            case LOOKS: {
                looksButton.setSelected(value);
                break;
            }
            // TCL ShenQianfeng Begin on 2016.08.23
            // Annotated Below:
            /*
            case BORDERS: {
                bordersButton.setSelected(value);
                break;
            }
            case GEOMETRY: {
                geometryButton.setSelected(value);
                break;
            }
            */
            // TCL ShenQianfeng End on 2016.08.23
            
            case FILTERS: {
                filtersButton.setSelected(value);
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMainView != null) {
            if (mMainView.getParent() != null) {
                ViewGroup parent = (ViewGroup) mMainView.getParent();
                parent.removeView(mMainView);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = (LinearLayout) inflater.inflate(
                R.layout.filtershow_main_panel, null, false);
        // TCL ShenQianfeng Begin on 2016.08.19
        cropButton = (ImageButton) mMainView.findViewById(R.id.cropButton);
        rotateButton = (ImageButton) mMainView.findViewById(R.id.rotateButton); 
        // TCL ShenQianfeng End on 2016.08.19
        looksButton = (ImageButton) mMainView.findViewById(R.id.fxButton);
        // TCL ShenQianfeng Begin on 2016.08.23
        // Annotated Below:
        // bordersButton = (ImageButton) mMainView.findViewById(R.id.borderButton);
        // geometryButton = (ImageButton) mMainView.findViewById(R.id.geometryButton);
        // TCL ShenQianfeng End on 2016.08.23
        
        filtersButton = (ImageButton) mMainView.findViewById(R.id.colorsButton);
        
        // TCL ShenQianfeng Begin on 2016.08.22
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel(CROP);
				//modify begin by liaoanhua
				FilterShowActivity activity = (FilterShowActivity) getActivity();
				activity.setSelectWholeBitmap();
				//modify end
            }
        });
        
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel(ROTATE);
            }
        });
        // TCL ShenQianfeng End on 2016.08.22

        looksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel(LOOKS);
            }
        });
        // TCL ShenQianfeng Begin on 2016.08.23
        // Annotated Below:
        /*
        bordersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel(BORDERS);
            }
        });
        geometryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel(GEOMETRY);
            }
        });
        */
        // TCL ShenQianfeng End on 2016.08.23
        
        filtersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel(FILTERS);
            }
        });

        FilterShowActivity activity = (FilterShowActivity) getActivity();
      //[BUGFIX]-Add by TCTNJ,wencan.wu, 2016-08-11,Defect2269568 begin
        if (!activity.isFinishing()) {
            showImageStatePanel(activity.isShowingImageStatePanel());
            showPanel(activity.getCurrentPanel());
        }
      //[BUGFIX]-Add by TCTNJ,wencan.wu, 2016-08-11,Defect2269568 end
        return mMainView;
    }

    private boolean isRightAnimation(int newPos) {
        if (newPos < mCurrentSelected) {
            return false;
        }
        return true;
    }
    
    private void setCategoryFragment(CategoryPanel category, boolean fromRight) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        // TCL BaiYuan Begin on 2016.10.18
        // Original:
        /*
        if (fromRight) {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
        } else {
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
        }
         */
        // Modify To:
        transaction.setCustomAnimations(R.anim.float_up_in, R.anim.float_down_out);
        // TCL BaiYuan End on 2016.10.18
        transaction.replace(R.id.category_panel_container, category, CategoryPanel.FRAGMENT_TAG);
        transaction.commitAllowingStateLoss();
    }
    
    // TCL ShenQianfeng Begin on 2016.08.19
    public void loadCategoryCropPanel() {
        if (mCurrentSelected == CROP) {
            return;
        }
        boolean fromRight = isRightAnimation(CROP);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(CROP);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = CROP;
        selection(mCurrentSelected, true);
    }
    
    public void loadCategoryRotatePanel() {
        if (mCurrentSelected == ROTATE) {
            return;
        }
        boolean fromRight = isRightAnimation(ROTATE);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(ROTATE);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = ROTATE;
        selection(mCurrentSelected, true);
    }
    
    // TCL ShenQianfeng End on 2016.08.19

    public void loadCategoryLookPanel(boolean force) {
        if (!force && mCurrentSelected == LOOKS) {
            return;
        }
        boolean fromRight = isRightAnimation(LOOKS);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(LOOKS);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = LOOKS;
        selection(mCurrentSelected, true);
    }

    // TCL ShenQianfeng Begin on 2016.08.23
    // Annotated Below:
    /*
    public void loadCategoryBorderPanel() {
        if (mCurrentSelected == BORDERS) {
            return;
        }
        boolean fromRight = isRightAnimation(BORDERS);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(BORDERS);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = BORDERS;
        selection(mCurrentSelected, true);
    }
    
    public void loadCategoryGeometryPanel() {
        if (mCurrentSelected == GEOMETRY) {
            return;
        }
        if (MasterImage.getImage().hasTinyPlanet()) {
            return;
        }
        boolean fromRight = isRightAnimation(GEOMETRY);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(GEOMETRY);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = GEOMETRY;
        selection(mCurrentSelected, true);
    }
    */
    // TCL ShenQianfeng End on 2016.08.23



    public void loadCategoryFiltersPanel() {
        if (mCurrentSelected == FILTERS) {
            return;
        }
        boolean fromRight = isRightAnimation(FILTERS);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(FILTERS);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = FILTERS;
        selection(mCurrentSelected, true);
    }

    public void loadCategoryVersionsPanel() {
        if (mCurrentSelected == VERSIONS) {
            return;
        }
        FilterShowActivity activity = (FilterShowActivity) getActivity();
        activity.updateVersions();
        boolean fromRight = isRightAnimation(VERSIONS);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(VERSIONS);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = VERSIONS;
        selection(mCurrentSelected, true);
    }
    
    // TCL ShenQianfeng Begin on 2016.08.29
    private void resetTopBar(int prevPanel, int currentPanel) {
        if(prevPanel == currentPanel || currentPanel == -1) return;
        FilterShowActivity activity = ((FilterShowActivity)getActivity());
        if(prevPanel == MainPanel.CROP) {
            //cancelApplyCrop will reset top bar to cancel/save mode
            activity.cancelApplyCrop(false);
        }
        TopBarManager topBarManager = activity.getTopBarManager();
        topBarManager.switchMode(TopBarManager.MODE_CANCEL_SAVE);
        topBarManager.enableResetButton(false);
    }
    // TCL ShenQianfeng End on 2016.08.29

    public void showPanel(int currentPanel) {
        // TCL ShenQianfeng Begin on 2016.08.29
        if(currentPanel == -1) return;
        //LogUtil.i2(TAG, "currentPanel: " + currentPanel);
        int prevPanel = ((FilterShowActivity)getActivity()).getCurrentPanel();
        resetTopBar(prevPanel, currentPanel);
        // TCL ShenQianfeng End on 2016.08.29
        
        switch (currentPanel) {
        // TCL ShenQianfeng Begin on 2016.08.19
            case CROP: {
                loadCategoryCropPanel();
                break;
            }
            
            case ROTATE: {
                loadCategoryRotatePanel();
                break;
            }
        // TCL ShenQianfeng End on 2016.08.19
        
            case LOOKS: {
                loadCategoryLookPanel(false);
                break;
            }
            // TCL ShenQianfeng Begin on 2016.08.23
            // Annotated Below:
            /*
            case BORDERS: {
                loadCategoryBorderPanel();
                break;
            }
            case GEOMETRY: {
                loadCategoryGeometryPanel();
                break;
            }
            */
            // TCL ShenQianfeng End on 2016.08.23

            case FILTERS: {
                loadCategoryFiltersPanel();
                break;
            }
            case VERSIONS: {
                loadCategoryVersionsPanel();
                break;
            }
        }
    }

    public void setToggleVersionsPanelButton(ImageButton button) {
        if (button == null) {
            return;
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentSelected == VERSIONS) {
                    showPanel(mPreviousToggleVersions);
                } else {
                    mPreviousToggleVersions = mCurrentSelected;
                    showPanel(VERSIONS);
                }
            }
        });
    }

    public void showImageStatePanel(boolean show) {
        View container = mMainView.findViewById(R.id.state_panel_container);
        FragmentTransaction transaction = null;
        if (container == null) {
            FilterShowActivity activity = (FilterShowActivity) getActivity();
            container = activity.getMainStatePanelContainer(R.id.state_panel_container);
        } else {
            transaction = getChildFragmentManager().beginTransaction();
        }
        if (container == null) {
            return;
        } else {
            transaction = getFragmentManager().beginTransaction();
        }
        int currentPanel = mCurrentSelected;
        if (show) {
            container.setVisibility(View.VISIBLE);
            StatePanel statePanel = new StatePanel();
            statePanel.setMainPanel(this);
            FilterShowActivity activity = (FilterShowActivity) getActivity();
            activity.updateVersions();
            transaction.replace(R.id.state_panel_container, statePanel, StatePanel.FRAGMENT_TAG);
        } else {
            container.setVisibility(View.GONE);
            Fragment statePanel = getChildFragmentManager().findFragmentByTag(StatePanel.FRAGMENT_TAG);
            if (statePanel != null) {
                transaction.remove(statePanel);
            }
            if (currentPanel == VERSIONS) {
                currentPanel = LOOKS;
            }
        }
        mCurrentSelected = -1;
        showPanel(currentPanel);
      //[BUGFIX]-Add by TCTNJ,wencan.wu, 2016-08-11,Defect2269568 begin
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-27,PR950449 begin
        try {
            transaction.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-27,PR950449 end
      //[BUGFIX]-Add by TCTNJ,wencan.wu, 2016-08-11,Defect2269568 end
    }

    /* MODIFIED-BEGIN by caihong.gu-nb, 2016-05-11,BUG-2125214*/
    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class
                    .getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    /* MODIFIED-END by caihong.gu-nb,BUG-2125214*/
}
