package com.android.gallery3d.ui;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.AlbumSetDataLoader;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.glrenderer.CustomStringTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.ui.AlbumSetSlotRenderer.LabelSpec;



public class MusicAlbumSlotRenderer  extends GLView {
	private GLRoot mRoot;
	private float [] mBackgroundColor;
	private ArrayList<GLView> mComponents;
	private MusicAlbumSlidingWindow mMusicDataWindow;
	
	public MusicAlbumSlotRenderer(AbstractGalleryActivity activity) {
		
	}
	
    private class MusicDataModelListener implements MusicAlbumSlidingWindow.Listener {
        @Override
        public void onContentChanged() {
           
        }
        
        @Override
        public void onSizeChanged(int size) {
           
        }
    }

	@Override
    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
		
	}
	
    public void setModel(AlbumSetDataLoader model) {
/*    	
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            mDataWindow = null;
            mSlotView.setSlotCount(0);
        }
        if (model != null) {
            mDataWindow = new AlbumSetSlidingWindow(
                    mActivity, model, mLabelSpec, CACHE_SIZE);
            mDataWindow.setListener(new MyCacheListener());
            mSlotView.setSlotCount(mDataWindow.size());
        }
 */       
    }
	
    public float [] getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(float [] color) {
        mBackgroundColor = color;
    }

    protected void renderBackground(GLCanvas view) {
 //       if (mBackgroundColor != null) {
        	view.fillRect(0, 0, 1080, 1000, Color.BLUE);
//        }
    }
	
	@Override
    protected void render(GLCanvas canvas) {
		renderBackground(canvas);
	}
	
    // This should only be called on the content pane (the topmost GLView).
    public void attachToRoot(GLRoot root) {
        Utils.assertTrue(mParent == null && mRoot == null);
        onAttachToRoot(root);
    }

    // This should only be called on the content pane (the topmost GLView).
    public void detachFromRoot() {
        Utils.assertTrue(mParent == null && mRoot != null);
        onDetachFromRoot();
    }
	
    // Adds a child to this GLView.
    public void addComponent(GLView component) {
        // Make sure the component doesn't have a parent currently.
        if (component.mParent != null) throw new IllegalStateException();

        // Build parent-child links
        if (mComponents == null) {
            mComponents = new ArrayList<GLView>();
        }
        mComponents.add(component);
        component.mParent = this;

        // If this is added after we have a root, tell the component.
        if (mRoot != null) {
            component.onAttachToRoot(mRoot);
        }
    }
	
	
}