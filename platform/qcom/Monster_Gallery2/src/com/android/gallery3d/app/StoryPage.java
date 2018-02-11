package com.android.gallery3d.app;

import mst.widget.ActionMode;
import mst.widget.ActionMode.Item;
import mst.widget.ActionModeListener;
import android.graphics.Rect;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.gallery3d.data.DateGroupInfos;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.MyEnumerateListener;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.glrenderer.CustomStringTexture;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.MstToolBarListener;
import com.android.gallery3d.ui.PhotoView;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.StoryView;
import com.android.gallery3d.ui.SlotView.SelectionStatusGetter;

public class StoryPage extends ActivityState implements  GalleryActionBar.ClusterRunner, SelectionManager.SelectionListener, MediaSet.SyncListener, GalleryActionBar.OnAlbumModeSelectedListener,
        MyEnumerateListener, SelectionStatusGetter, ActionModeListener, MstToolBarListener, PhotoView.AlbumPageSlotPositionProvider {

    private String mParentMediaSetString;
    private StoryView mStoryView;
    public static final String KEY_PARENT_MEDIA_PATH = "parent-media-path";
    private GLView mRootView = new GLView(){
        @Override
        protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
//            int storyViewTop = mActivity.getGalleryActionBar().getHeight();
            int storyViewRight = right - left;
            int storyViewBottom = bottom - top;
            mStoryView.layout(0, 0, storyViewRight, storyViewBottom);
        }
        
    };
    
    @Override
    protected void onCreate(android.os.Bundle data, android.os.Bundle storedState) {
         super.onCreate(data, storedState);
        setContentPane(mRootView);
        mStoryView = new StoryView(mActivity);
        CustomStringTexture.initialize(mActivity);
        mRootView.addComponent(mStoryView);
        mParentMediaSetString = data.getString(KEY_PARENT_MEDIA_PATH);
    }
    
    @Override
    protected void onResume() {
//        GalleryActionBar actionBar = mActivity.getGalleryActionBar();
//        boolean enableHomeButton = (mActivity.getStateManager().getStateCount() > 1) | mParentMediaSetString != null;
//        actionBar.setDisplayOptions(enableHomeButton, true);
        super.onResume();
    }
    
    @Override
    protected boolean onCreateActionBar(Menu menu) {
//        GalleryActionBar actionBar = mActivity.getGalleryActionBar();
//        MenuInflater inflator = getSupportMenuInflater();
        return false;
    }
    
    @Override
    public Rect getSlotRect(int slotIndex) {
        return null;
    }

    @Override
    public void onMstToolbarNavigationClicked(View view) {
        
    }

    @Override
    public boolean onMstToolbarMenuItemClicked(MenuItem item) {
        return false;
    }

    @Override
    public void onActionItemClicked(Item arg0) {
        
    }

    @Override
    public void onActionModeDismiss(ActionMode arg0) {
        
    }

    @Override
    public void onActionModeShow(ActionMode arg0) {
        
    }

    @Override
    public boolean isInSelectionMode() {
        return false;
    }

    @Override
    public void select(int slotIndex) {
        
    }

    @Override
    public void unselect(int slotIndex) {
        
    }

    @Override
    public boolean isItemSelected(int slotIndex) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onEnumerate(int index, boolean finished, DateGroupInfos info) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onAlbumModeSelected(int mode) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onSyncDone(MediaSet mediaSet, int resultCode) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onSelectionModeChange(int mode) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onSelectionChange(Path path, boolean selected) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void doCluster(int id) {
        // TODO Auto-generated method stub
        
    }

}
