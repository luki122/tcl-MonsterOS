/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.gallery3d.app;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.TwoLineListItem;

import com.android.gallery3d.R;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.ui.TypefaceManager;
import com.android.gallery3d.util.ColorUtil;
import com.android.gallery3d.util.LogUtil;

import java.util.ArrayList;

import mst.widget.toolbar.Toolbar;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;

public class GalleryActionBar implements OnNavigationListener {
    @SuppressWarnings("unused")
    private static final String TAG = "GalleryActionBar";

    private ClusterRunner mClusterRunner;
    private CharSequence[] mTitles;
    private ArrayList<Integer> mActions;
    private Context mContext;
    private LayoutInflater mInflater;
    private AbstractGalleryActivity mActivity;
    
    // TCL ShenQianfeng Begin on 2016.08.11
    // Original:
    //private ActionBar mActionBar;
    // Modify To:
    private Toolbar mToolbar;
    // TCL ShenQianfeng End on 2016.08.11
    
    private int mCurrentIndex;
    private ClusterAdapter mAdapter = new ClusterAdapter();
    
    // TCL ShenQianfeng Begin on 2016.09.18
    private float mChildViewAlpha;
    // TCL ShenQianfeng End on 2016.09.18

    private AlbumModeAdapter mAlbumModeAdapter;
    private OnAlbumModeSelectedListener mAlbumModeListener;
    private int mLastAlbumModeSelected;
    private CharSequence [] mAlbumModes;
    public static final int ALBUM_FILMSTRIP_MODE_SELECTED = 0;
    public static final int ALBUM_GRID_MODE_SELECTED = 1;

    public interface ClusterRunner {
        public void doCluster(int id);
    }

    public interface OnAlbumModeSelectedListener {
        public void onAlbumModeSelected(int mode);
    }

    private static class ActionItem {
        public int action;
        public boolean enabled;
        public boolean visible;
        public int spinnerTitle;
        public int dialogTitle;
        public int clusterBy;

        public ActionItem(int action, boolean applied, boolean enabled, int title,
                int clusterBy) {
            this(action, applied, enabled, title, title, clusterBy);
        }

        public ActionItem(int action, boolean applied, boolean enabled, int spinnerTitle,
                int dialogTitle, int clusterBy) {
            this.action = action;
            this.enabled = enabled;
            this.spinnerTitle = spinnerTitle;
            this.dialogTitle = dialogTitle;
            this.clusterBy = clusterBy;
            this.visible = true;
        }
    }

    private static final ActionItem[] sClusterItems = new ActionItem[] {
        new ActionItem(FilterUtils.CLUSTER_BY_ALBUM, true, false, R.string.albums,
                R.string.group_by_album),
        new ActionItem(FilterUtils.CLUSTER_BY_LOCATION, true, false,
                R.string.locations, R.string.location, R.string.group_by_location),
        new ActionItem(FilterUtils.CLUSTER_BY_TIME, true, false, R.string.times,
                R.string.time, R.string.group_by_time),
        new ActionItem(FilterUtils.CLUSTER_BY_FACE, true, false, R.string.people,
                R.string.group_by_faces),
        new ActionItem(FilterUtils.CLUSTER_BY_TAG, true, false, R.string.tags,
                R.string.group_by_tags)
    };

    private class ClusterAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return sClusterItems.length;
        }

        @Override
        public Object getItem(int position) {
            return sClusterItems[position];
        }

        @Override
        public long getItemId(int position) {
            return sClusterItems[position].action;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.action_bar_text,
                        parent, false);
            }
            TextView view = (TextView) convertView;
            view.setText(sClusterItems[position].spinnerTitle);
            return convertView;
        }
    }

    private class AlbumModeAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mAlbumModes.length;
        }

        @Override
        public Object getItem(int position) {
            return mAlbumModes[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.action_bar_two_line_text,
                        parent, false);
            }
            // TCL ShenQianfeng Begin on 2016.08.11
            // Annotated Below:
            /*
            TwoLineListItem view = (TwoLineListItem) convertView;
            view.getText1().setText(mActionBar.getTitle());
            view.getText2().setText((CharSequence) getItem(position));
            */
            // TCL ShenQianfeng End on 2016.08.11
           
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.action_bar_text,
                        parent, false);
            }
            TextView view = (TextView) convertView;
            view.setText((CharSequence) getItem(position));
            return convertView;
        }
    }

    public static String getClusterByTypeString(Context context, int type) {
        for (ActionItem item : sClusterItems) {
            if (item.action == type) {
                return context.getString(item.clusterBy);
            }
        }
        return null;
    }

    public GalleryActionBar(AbstractGalleryActivity activity) {

        // TCL ShenQianfeng Begin on 2016.08.11
        // Original:
        //mActionBar = activity.getActionBar();
        // Modify To:
        mToolbar = activity.getToolbar();
        int statusBarHeight = activity.getSystemUIManager().getStatusBarHeight();
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)mToolbar.getLayoutParams();
        lp.height = statusBarHeight + 144;
        mToolbar.setLayoutParams(lp);
        
        mToolbar.setPadding(mToolbar.getPaddingLeft(), 
                statusBarHeight, 
                mToolbar.getPaddingRight(), 
                mToolbar.getPaddingBottom());

        // TCL ShenQianfeng End on 2016.08.11
        mContext = activity.getAndroidContext();
        mActivity = activity;
        mInflater = ((Activity) mActivity).getLayoutInflater();
        mCurrentIndex = 0;
    }

    private void createDialogData() {
        ArrayList<CharSequence> titles = new ArrayList<CharSequence>();
        mActions = new ArrayList<Integer>();
        for (ActionItem item : sClusterItems) {
            if (item.enabled && item.visible) {
                titles.add(mContext.getString(item.dialogTitle));
                mActions.add(item.action);
            }
        }
        mTitles = new CharSequence[titles.size()];
        titles.toArray(mTitles);
    }

    public int getHeight() {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Original:
        //return mActionBar != null ? mActionBar.getHeight() : 0;
        // Modify To:
        return mToolbar != null ? mToolbar.getHeight() : 0;
        // TCL ShenQianfeng End on 2016.08.11
        
    }

    public void setClusterItemEnabled(int id, boolean enabled) {
        for (ActionItem item : sClusterItems) {
            if (item.action == id) {
                item.enabled = enabled;
                return;
            }
        }
    }

    public void setClusterItemVisibility(int id, boolean visible) {
        for (ActionItem item : sClusterItems) {
            if (item.action == id) {
                item.visible = visible;
                return;
            }
        }
    }

    public int getClusterTypeAction() {
        return sClusterItems[mCurrentIndex].action;
    }

    public void enableClusterMenu(int action, ClusterRunner runner) {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Annotated Below:
        /*
            if (mActionBar != null) {
            // Don't set cluster runner until action bar is ready.
            mClusterRunner = null;
            mActionBar.setListNavigationCallbacks(mAdapter, this);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            setSelectedAction(action);
            mClusterRunner = runner;
        }
         */
        // TCL ShenQianfeng End on 2016.08.11

    }

    // The only use case not to hideMenu in this method is to ensure
    // all elements disappear at the same time when exiting gallery.
    // hideMenu should always be true in all other cases.
    public void disableClusterMenu(boolean hideMenu) {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Annotated Below:
        /*
        if (mActionBar != null) {
            mClusterRunner = null;
            if (hideMenu) {
                mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            }
        }
        */
        // TCL ShenQianfeng End on 2016.08.11
        
    }

    public void onConfigurationChanged() {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Annotated Below:
        /*
         if (mActionBar != null && mAlbumModeListener != null) {
            OnAlbumModeSelectedListener listener = mAlbumModeListener;
            enableAlbumModeMenu(mLastAlbumModeSelected, listener);
        }
         */
        // TCL ShenQianfeng End on 2016.08.11
        
    }

    public void enableAlbumModeMenu(int selected, OnAlbumModeSelectedListener listener) {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Annotated Below:
        /*
         if (mActionBar != null) {
            if (mAlbumModeAdapter == null) {
                // Initialize the album mode options if they haven't been already
                Resources res = mActivity.getResources();
                mAlbumModes = new CharSequence[] {
                        res.getString(R.string.switch_photo_filmstrip),
                        res.getString(R.string.switch_photo_grid)};
                mAlbumModeAdapter = new AlbumModeAdapter();
            }
            mAlbumModeListener = null;
            mLastAlbumModeSelected = selected;
            mActionBar.setListNavigationCallbacks(mAlbumModeAdapter, this);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            mActionBar.setSelectedNavigationItem(selected);
            mAlbumModeListener = listener;
        }
         */
        // TCL ShenQianfeng End on 2016.08.11
        
    }

    public void disableAlbumModeMenu(boolean hideMenu) {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Annotated Below:
        /*
        if (mActionBar != null) {
            mAlbumModeListener = null;
            if (hideMenu) {
                mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            }
        }
        */
        // TCL ShenQianfeng End on 2016.08.11

    }

    public void showClusterDialog(final ClusterRunner clusterRunner) {
        createDialogData();
        final ArrayList<Integer> actions = mActions;
        new AlertDialog.Builder(mContext).setTitle(R.string.group_by).setItems(
                mTitles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Need to lock rendering when operations invoked by system UI (main thread) are
                // modifying slot data used in GL thread for rendering.
                mActivity.getGLRoot().lockRenderThread();
                try {
                    clusterRunner.doCluster(actions.get(which).intValue());
                } finally {
                    mActivity.getGLRoot().unlockRenderThread();
                }
            }
        }).create().show();
    }

    @TargetApi(ApiHelper.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setHomeButtonEnabled(boolean enabled) {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Annotated Below:
        //if (mActionBar != null) mActionBar.setHomeButtonEnabled(enabled);
        // TCL ShenQianfeng End on 2016.08.11
       
    }
    
    public void setDisplayOptions(boolean displayHomeAsUp, boolean showTitle) {

        // TCL ShenQianfeng Begin on 2016.08.11
        // Annotated Below:
        /*
                                        if (mActionBar == null) return;
                                        int options = 0;
                                        if (displayHomeAsUp) options |= ActionBar.DISPLAY_HOME_AS_UP;
                                        if (showTitle) options |= ActionBar.DISPLAY_SHOW_TITLE;
                                
                                        mActionBar.setDisplayOptions(options,
                                                ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
                                        mActionBar.setHomeButtonEnabled(displayHomeAsUp);
                                
                                        //TCL ShenQianfeng Begin on 2016.07.04
                                        // do not show the logo
                                        //mActionBar.setDisplayShowHomeEnabled(false);
                                        if(showTitle) {
                                            mActionBar.setDisplayShowCustomEnabled(false);
                                        }
                                        //TCL ShenQianfeng End on 2016.07.04
         */
        // TCL ShenQianfeng End on 2016.08.11
        
        
    }

    public void setTitle(String title) {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Original:
        //if (mActionBar != null) mActionBar.setTitle(title);
        // Modify To:
        if (mToolbar != null) {
            mToolbar.setTitle(title);
        }
        // TCL ShenQianfeng End on 2016.08.11
    }

    public void setTitle(int titleId) {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Original:
        // if (mActionBar != null) {
        //    mActionBar.setTitle(mContext.getString(titleId));
        // }
        // Modify To:
        if (mToolbar != null) {
            mToolbar.setTitle(mContext.getString(titleId));
        }
        // TCL ShenQianfeng End on 2016.08.11
    }

    public void setSubtitle(String title) {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Original:
        // if (mActionBar != null) mActionBar.setSubtitle(title);
        // Modify To:
        if (mToolbar != null) mToolbar.setSubtitle(title);
        // TCL ShenQianfeng End on 2016.08.11
    }

    public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Annotated Below:
        //if (mActionBar != null) mActionBar.addOnMenuVisibilityListener(listener);
        // TCL ShenQianfeng End on 2016.08.11
        
    }

    public void removeOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Annotated Below:
        //if (mActionBar != null) mActionBar.removeOnMenuVisibilityListener(listener);
        // TCL ShenQianfeng End on 2016.08.11
        
    }

    public boolean setSelectedAction(int type) {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Original:
        /*
        if (mActionBar == null) return false;

        for (int i = 0, n = sClusterItems.length; i < n; i++) {
            ActionItem item = sClusterItems[i];
            if (item.action == type) {
                mActionBar.setSelectedNavigationItem(i);
                mCurrentIndex = i;
                return true;
            }
        }
        return false;
         */
        // Modify To:
        return false;
        // TCL ShenQianfeng End on 2016.08.11
        
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (itemPosition != mCurrentIndex && mClusterRunner != null
                || mAlbumModeListener != null) {
            // Need to lock rendering when operations invoked by system UI (main thread) are
            // modifying slot data used in GL thread for rendering.
            mActivity.getGLRoot().lockRenderThread();
            try {
                if (mAlbumModeListener != null) {
                    mAlbumModeListener.onAlbumModeSelected(itemPosition);
                } else {
                    mClusterRunner.doCluster(sClusterItems[itemPosition].action);
                }
            } finally {
                mActivity.getGLRoot().unlockRenderThread();
            }
        }
        return false;
    }

    private Menu mActionBarMenu;
    private ShareActionProvider mSharePanoramaActionProvider;
    private ShareActionProvider mShareActionProvider;
    private Intent mSharePanoramaIntent;
    private Intent mShareIntent;

    public void createActionBarMenu(int menuRes, Menu menu) {
        mActivity.getMenuInflater().inflate(menuRes, menu);
        mActionBarMenu = menu;
        
        // TCL ShenQianfeng Begin on 2016.07.06
        // Annotated Below:
        /*
        MenuItem item = menu.findItem(R.id.action_share_panorama);
        if (item != null) {
            mSharePanoramaActionProvider = (ShareActionProvider)
                item.getActionProvider();
            mSharePanoramaActionProvider
                .setShareHistoryFileName("panorama_share_history.xml");
            mSharePanoramaActionProvider.setShareIntent(mSharePanoramaIntent);
        }

        item = menu.findItem(R.id.action_share);
        if (item != null) {
            mShareActionProvider = (ShareActionProvider)
                item.getActionProvider();
            mShareActionProvider
                .setShareHistoryFileName("share_history.xml");
            mShareActionProvider.setShareIntent(mShareIntent);
        }
        */
        // TCL ShenQianfeng End on 2016.07.06
    }

    public Menu getMenu() {
        return mActionBarMenu;
    }

    public void setShareIntents(Intent sharePanoramaIntent, Intent shareIntent,
        ShareActionProvider.OnShareTargetSelectedListener onShareListener) {
        mSharePanoramaIntent = sharePanoramaIntent;
        if (mSharePanoramaActionProvider != null) {
            mSharePanoramaActionProvider.setShareIntent(sharePanoramaIntent);
        }
        mShareIntent = shareIntent;
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
            mShareActionProvider.setOnShareTargetSelectedListener(
                onShareListener);
        }
    }
    
    //TCL ShenQianfeng Begin on 2016.07.05    
    
    public Toolbar getToolbar() {
        return mToolbar;
    }
    
    public void setToolbarChildViewsAlpha(float alpha) {
        Toolbar toolbar = getToolbar();
        for(int i=0; i<toolbar.getChildCount(); i++) {
            toolbar.getChildAt(i).setAlpha(alpha);
        }
        mChildViewAlpha = alpha;
    }
    
    public float getChildViewAlpha() {
        return mChildViewAlpha;
    }
    
    public void updatePhotoPageNumIndicator(String text,  View.OnClickListener listener) {
        /*
        View customView = mActionBar.getCustomView();
        if(null == customView) {
            //customView = new TextView(mActivity);
            //mActionBar.setCustomView(customView);
            customView = LayoutInflater.from(mActivity).inflate(R.layout.mst_photopage_customviews, null);
            mActionBar.setCustomView(customView);
        }
        
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        TextView textView = (TextView)customView.findViewById(R.id.photopage_indicator_textview);
        if(null == textView) return;
        textView.setTypeface(TypefaceManager.get(TypefaceManager.TFID_ROBOTO_LIGHT));
        textView.setText(text);
        
        ImageButton backButton = (ImageButton)customView.findViewById(R.id.photopage_back_button);
        backButton.setOnClickListener(listener);
        
        ImageButton shareButton = (ImageButton)customView.findViewById(R.id.photopage_share_button);
        shareButton.setOnClickListener(listener);
        */
    }
    //TCL ShenQianfeng End on 2016.07.05
}
